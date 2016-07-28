from flask import Flask
from api import api
from config import config

app = Flask(__name__)
app.register_blueprint(api, url_prefix='/api')


if __name__ == "__main__":
    app.run(host=config['HOST'], port=config['PORT'],
            debug=config['DEBUG'])
