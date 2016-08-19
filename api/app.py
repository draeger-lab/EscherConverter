from flask import Flask
from flask import send_from_directory

from api import api
from config import config

app = Flask(__name__)
app.register_blueprint(api, url_prefix='/api')


@app.route('/')
def root():
    """
    Returns the root page.
    :return:
    """
    return app.send_static_file('index.html')


@app.route('/js/<path:path>')
def scripts(path):
    """
    JavaScript assets.
    :param path: Path of JS file.
    :return: Script.
    """
    return send_from_directory('static/js', path)


@app.route('/css/<path:path>')
def styles(path):
    """
    CSS assets.
    :param path: Path of CSS file.
    :return: Stylesheet.
    """
    return send_from_directory('static/css', path)


@app.route('/<filename>.png')
def images(filename):
    """
    Image assets.
    :param filename: Path to PNG file.
    :return: Image.
    """
    return send_from_directory('static', filename + '.png')


if __name__ == "__main__":
    app.run(host=config['HOST'], port=config['PORT'], debug=config['DEBUG'])
