import json
import unittest
import jsonpickle

from models import LayoutOptions
from database import Database


class SerializationTest(unittest.TestCase):

    def test_request_deserialization(self):
        sample_object = LayoutOptions()
        sample_object.z = 0.10
        sample_json = jsonpickle.encode(sample_object)
        d_object = jsonpickle.decode(sample_json)
        self.assertTrue(d_object.z == 0.10)


class DatabaseTest(unittest.TestCase):

    def test_database_connection_failure_on_bad_path(self):
        with self.assertRaises(Exception):
            Database(path="il|_f0rmed_p@th", echo=True)

    def test_database_connection_success(self):
        Database(path="/database/escher.db", echo=True)

