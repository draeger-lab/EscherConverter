import os
import uuid
from datetime import datetime

import time
from glob import glob

from flask import Blueprint, jsonify
from flask import Response
from flask import request

from config import config
from database import Database
from escherconverter import EscherConverter
from models import ConvertRequest, OutputFormat, ConversionStatus
from utils import extension_from_content_type

api = Blueprint('api', __name__)

db = Database(config['SQLITE_FILE'], config['DEBUG'])


@api.before_request
def before():
    db.renew()


@api.after_request
def after(response):
    db.finalize()
    response.headers['Access-Control-Allow-Origin'] = '*'
    return response


@api.route('/convert/<req_id>', methods=['GET'])
def get_conversion_status(req_id):
    """
    Returns the meta info about a conversion request.
    :param req_id: The request id issued before.
    :return: Info about the conversion.
    """
    cr = db.retrieve(req_id)
    if cr is None:
        return jsonify({
            'status': 'errored',
            'message': 'no job found matching request id: ' + req_id
        }), 404
    resp = {
        'id': cr.id,
        'status': cr.status.value,
        'submission_date': datetime.fromtimestamp(cr.submission_date),
        'total_files': cr.file_count,
        'uploaded_files': cr.files_uploaded
    }
    if cr.status == ConversionStatus.completed or cr.status == ConversionStatus.failed or \
                    cr.status == ConversionStatus.errored:
        resp['completion_date'] = datetime.fromtimestamp(cr.completion_date)
    return jsonify(resp), 200


@api.route('/convert', methods=['POST'])
def conversion_request():
    if request.content_type != 'application/json':
        return jsonify({
            'status': 'errored',
            'message': 'Content-Type must be application/json'
        }), 415
    d = None
    cr_object = None
    try:
        cr_object = ConvertRequest(request.json)
    except:
        return jsonify({
            'status': 'errored',
            'message': 'Invalid request JSON!!!'
        }), 400
    cr_object.output_format = OutputFormat.escher
    cr_object.id = uuid.uuid1().int >> 112
    cr_object.submission_date = int(time.time())
    cr_object.status = ConversionStatus.started
    db_add_result = db.add(cr=cr_object)
    if db_add_result:
        cr_object = db.retrieve(cr_object.id)
        resp = jsonify({
            'id': cr_object.id,
            'status': cr_object.status.value,
            'submission_time': datetime.fromtimestamp(cr_object.submission_date),
            'number_of_files': cr_object.file_count,
            'message': 'job submitted successfully, add files to start the conversion'
        })
        cr_object.status = ConversionStatus.waiting
        db.add(cr_object)
        return resp, 200
    else:
        return jsonify({
            'status': 'errored',
            'message': r'can"t submit new job'
        }), 500


@api.route('/convert/<req_id>/input/<file_number>', methods=['PUT'])
def add_file(req_id, file_number):
    cr = db.retrieve(req_id)
    if cr is None:
        return jsonify({
            'status': 'errored',
            'message': 'no job found matching request id: ' + req_id
        }), 404
    if request.content_type not in ['application/json', 'application/xml', 'text/xml']:
        return jsonify({
            'status': 'errored',
            'message': 'Content-Type must be either of :' + "'application/xml', "
                                                            "'application/json', "
                                                            "'text/xml'"
        }), 415
    if int(file_number) not in range(cr.file_count):
        return jsonify({
            'status': 'errored',
            'message': 'file number must be in (0, ' + str(cr.file_count - 1) + ').'
        }), 404
    is_new_file = True
    if os.path.exists(config['FILE_STORE'] + str(req_id) + '/input/' + str(file_number)):
        is_new_file = False
    os.makedirs(config['FILE_STORE'] + req_id + '/input/', exist_ok=True)

    file_path = config['FILE_STORE'] + req_id + '/input/' + file_number
    file_path += extension_from_content_type()[request.content_type]
    file = open(file_path, 'wb')
    file.write(request.stream.read())
    if is_new_file:
        cr.files_uploaded += 1
    db.update()
    if cr.files_uploaded != cr.file_count:
        return jsonify({
            'status': 'waiting',
            'message': 'file successfully added, add all files to start the conversion.'
        }), 202
    converter = EscherConverter(cr)
    converter.convert()
    cr.status = ConversionStatus.running
    return jsonify({
        'status': cr.status.value,
        'message': 'conversion started.'
    }), 201


@api.route('/convert/<req_id>/input/<file_number>', methods=['GET'])
def get_input_file(req_id, file_number):
    cr = db.retrieve(req_id)
    if cr is None:
        return jsonify({
            'status': 'errored',
            'message': 'no job found matching request id: ' + req_id
        }), 404
    if int(file_number) not in range(cr.file_count):
        return jsonify({
            'status': 'errored',
            'message': 'file number must be in (0, ' + str(cr.file_count - 1) + ').'
        }), 404
    if len(glob(config['FILE_STORE'] + str(req_id) + '/input/' + str(file_number) + '.*')) is 0:
        return jsonify({
            'status': 'errored',
            'message': 'file number ' + str(file_number) + ' has not been uploaded yet.'
        }), 404

    input_file = open(glob(config['FILE_STORE'] + str(req_id) + '/input/' + str(file_number) +
                           '.*')[0], 'r')

    def generate(file):
        while True:
            l = file.readline()
            if l:
                yield l
            else:
                return
    return Response(generate(input_file), content_type='application/' + os.path.splitext(
        input_file.name)[1].strip('.')), 200


@api.route('/convert/<req_id>/output/<file_number>', methods=['GET'])
def get_output_files(req_id, file_number):
    cr = db.retrieve(req_id)
    if cr is None:
        return jsonify({
            'status': 'errored',
            'message': 'no job found matching request id: ' + req_id
        }), 404
    if int(file_number) not in range(cr.file_count):
        return jsonify({
            'status': 'errored',
            'message': 'file number must be in (0, ' + str(cr.file_count - 1) + ').'
        }), 404
    if cr.status in [ConversionStatus.waiting, ConversionStatus.running]:
        return jsonify({
            'status': 'not found',
            'message': 'conversion is not complete yet, please try again later.'
        }), 404
    if len(glob(config['FILE_STORE'] + str(req_id) + '/output/' + str(file_number) + '.*')) \
            is 0:
        return jsonify({
            'status': 'not found',
            'message': 'file number ' + str(file_number) + ' not found, please check the log fo '
                                                           'details.'
        }), 404
    else:
        output_file = open(glob(config['FILE_STORE'] + str(req_id) + '/output/' + str(file_number) +
                                '.*')[0], 'r')

        def generate(file):
            while True:
                l = file.readline()
                if l:
                    yield l
                else:
                    return
        return Response(generate(output_file), content_type='application/' + os.path.splitext(
            output_file.name)[1].strip('.')), 200


@api.route('/convert/<req_id>/log', methods=['GET'])
def conversion_log(req_id):
    cr = db.retrieve(req_id)
    if cr is None:
        return jsonify({
            'status': 'errored',
            'message': 'no job found matching request id: ' + req_id
        }), 404
    if cr.status in [ConversionStatus.started, ConversionStatus.waiting, ConversionStatus.running]:
        return jsonify({
            'status': 'running',
            'message': 'conversion is still running, please try again later.'
        }), 202
    if cr.status is ConversionStatus.errored:
        return jsonify({
            'status': 'errored',
            'message': 'there was a problem in the request, could not start conversion.'
        }), 500

    log_file = open(config['FILE_STORE'] + str(req_id) + '/conversion.log', 'r')

    def generate(file):
        while True:
            l = file.readline()
            if l:
                yield l
            else:
                return
    return Response(generate(log_file), content_type='text/plain'), 200


@api.route('/convert', methods=['GET'])
def help():
    """

    :return: Returns the API docs.
    """
    return jsonify({
        "message": "this end-point provides info about other end-points. not yet implemented!"
    }), 501
