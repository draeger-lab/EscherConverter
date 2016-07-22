from random import randint

from flask import Blueprint, jsonify
from flask import request

from config import config
from database import Database
from models import ConvertRequest, OutputFormat

api = Blueprint('api', __name__)

db = Database(config['SQLITE_FILE'], config['DEBUG'])


@api.route('/convert/<req_id>', methods=['GET'])
def get_conversion_status(req_id):
    """
    Returns the meta info about a conversion request.
    :param req_id: The request id issued before.
    :return: Info about the conversion.
    """
    return jsonify({
        # "status": "complete",
        # "result": "failure",
        "message": "not yet implemented!"
    }), 501


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
    cr_object.id = randint(0, 10000)
    db_add_result = db.add(cr=cr_object)
    if db_add_result:
        cr_object = db.retrieve(cr_object.id)
        return jsonify({
            'id': cr_object.id,
            'status': 'waiting',
            'message': 'add files to start the conversion'
        })
    else:
        return jsonify({
            'status': 'errored',
            'message': r'can"t submit new job'
        }), 500


@api.route('/convert', methods=['GET'])
def help():
    """

    :return: Returns the API docs.
    """
    return jsonify({
        "message": "this end-point provides info about other end-points. not yet implemented!"
    }), 501
