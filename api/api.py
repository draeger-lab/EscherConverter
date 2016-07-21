import json

from flask import Blueprint, jsonify
from flask import request

from models import ConvertRequest, OutputFormat

api = Blueprint('api', __name__)


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
    # TODO: Add conversion request to database.
    # TODO: return job id.
    return jsonify({
        'status': 'errored',
        'message': 'not yet implemented'
    }), 501


@api.route('/convert', methods=['GET'])
def help():
    """

    :return: Returns the API docs.
    """
    return jsonify({
        "message": "this end-point provides info about other end-points. not yet implemented!"
    }), 501
