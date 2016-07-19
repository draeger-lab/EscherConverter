from flask import Blueprint

api = Blueprint('api', __name__)


@api.route('/convert/<req_id>', methods=['GET'])
def home(req_id):
    return "Hello " + req_id + "!!!"


@api.route('/', methods=['GET'])
def hello():
    return "YO!"
