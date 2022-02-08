import os

from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker

from models import ConvertRequest, Base


class Database(object):
    """
    Controls database operations, viz. add, delete, update, retrieve.
    """

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
        """
        Adds an object to database.
        :param cr: The object to store.
        :return: Database addition result.
        """
        try:
            self.session.add(cr)
            self.session.commit()
            return True
        except:
            return False

    def retrieve(self, id):
        """
        Retrieves a record from the database.
        :param id: The conversion job id to retrieve.
        :return:
        """
        req = self.session.query(ConvertRequest).filter_by(id=id).first()
        return req

    def update(self):
        """
        Updates a database record.
        :return:
        """
        self.session.commit()
        pass

    def renew(self):
        """
        Renews the session to database.
        :return:
        """
        self.session = Database.session_maker()

    def finalize(self):
        """
        Close session and commit changes to database.
        :return:
        """
        self.session.commit()
        self.session.close()

