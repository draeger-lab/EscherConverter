from flask import Flask
from flask import send_from_directory

from api import api
from config import config

app = Flask(__name__)
app.register_blueprint(api, url_prefix='/api')


@app.route('/')
def root():
    return app.send_static_file('index.html')


@app.route('/js/<path:path>')
def scripts(path):
    return send_from_directory('static/js', path)


@app.route('/css/<path:path>')
def styles(path):
    return send_from_directory('static/css', path)


@app.route('/<filename>.png')
def images(filename):
    return send_from_directory('static', filename + '.png')


if __name__ == "__main__":
    app.run(host=config['HOST'], port=config['PORT'],
            debug=config['DEBUG'])
