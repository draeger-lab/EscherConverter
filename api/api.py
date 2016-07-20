from flask import Blueprint, jsonify
from flask import request

api = Blueprint('api', __name__)


@api.route('/convert/<req_id>', methods=['GET'])
def home(req_id):
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
def hello():
    body = request.json
    return jsonify(body)


@api.route('/convert', methods=['GET'])
def help():
    """

    :return: Returns the API docs.
    """
    return jsonify({
        "message": "this end-point provides info about other end-points. not yet implemented!"
    }), 501
