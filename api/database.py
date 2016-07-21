import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from models import ConvertRequest, Base


class Database(object):

    db_connection_string = None
    engine = None
    Session = None

    def __init__(self, path, echo):
        if not os.path.exists(os.path.dirname(path)):
            raise Exception("Path doesn't exists.")
        Database.db_connection_string = "sqlite:///" + path
        Database.engine = create_engine(Database.db_connection_string, echo=echo)
        Database.Session = sessionmaker(bind=Database.engine)
        Base.metadata.create_all(Database.engine)

    def add(self, cr: ConvertRequest):
        session = Database.Session()
        session.add(cr)
        session.commit()
        return

    def retrieve(self, id):
        session = Database.Session()
        return session.query(ConvertRequest).filter_by(id=id).first()

    def update(self):
        # TODO: Update status and result of conversion jobs.
        pass



