import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from models import ConvertRequest, Base


class Database(object):

    db_connection_string = None
    engine = None
    session_maker = None

    def __init__(self, path, echo):
        if not os.path.exists(os.path.dirname(path)):
            raise Exception("Path doesn't exists.")
        Database.db_connection_string = "sqlite:///" + path
        Database.engine = create_engine(Database.db_connection_string, echo=echo)
        Database.session_maker = sessionmaker(bind=Database.engine)
        Base.metadata.create_all(Database.engine)
        self.session = None

    def add(self, cr: ConvertRequest):
        try:
            self.session.add(cr)
            self.session.commit()
            return True
        except:
            return False

    def retrieve(self, id):
        req = self.session.query(ConvertRequest).filter_by(id=id).first()
        return req

    def update(self):
        # TODO: Update status and result of conversion jobs.
        self.session.commit()
        pass

    def renew(self):
        self.session = Database.session_maker()

    def finalize(self):
        self.session.commit()
        self.session.close()

