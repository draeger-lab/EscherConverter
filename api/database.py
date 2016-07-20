import os

from sqlalchemy import create_engine


class Database(object):

    def __init__(self, path, echo):
        if not os.path.exists(os.path.dirname(path)):
            raise Exception("Path doesn't exists.")
        db_connection_string = "sqlite:///" + path
        engine = create_engine(db_connection_string, echo=echo)




