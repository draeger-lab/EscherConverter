import unittest
import json
from random import randint
from unittest import skip
import jsonpickle

from models import ConvertRequest, OutputFormat, ComponentOptions, LayoutOptions
from database import Database
from config import config


class SerializationTest(unittest.TestCase):

    @skip("I can't even begin to explain")
    def test_request_deserialization(self):
        sample_req = ConvertRequest(None)
        # sample_req.output_format = OutputFormat.escher
        sample_req.id = 0x0123456789abcde
        sample_req.input_filename = "sample file name.xml"
        sample_req.component_options = ComponentOptions()
        sample_req.component_options.compartment_id = "pokemon"
        sample_req.component_options.compartment_name = "squirtle"
        sample_req.component_options.infer_compartment_bounds = False
        sample_req.component_options.layout_id = "pidgey"
        sample_req.component_options.layout_name = "ratatta"
        sample_req.layout_options = LayoutOptions()
        sample_req.layout_options.z = 100

        json_of_a_object = jsonpickle.dumps(sample_req, unpicklable=True)
        print(json_of_a_object)
        object_of_a_json = ConvertRequest(json.loads(json_of_a_object))
        self.assertTrue(sample_req == object_of_a_json)

    @skip("Same as above")
    def test_request_deserialization_from_json(self):
        test_json = r'{"component_options": {"compartment_id": "pokemon", "compartment_name": "squirtle",                                       "infer_compartment_bounds": false, "layout_id": "pidgey",                             "layout_name": "ratatta"}}, "id": 5124095576030430,"input_filename": "sample file name.xml",    "layout_options": {"canvas_default_height": null, "canvas_default_width": null,                                     "label_height": null, "label_width": null, "node_depth": null,                                     "node_label_height": null, "primary_node_height": null,                                     "primary_node_width": null, "reaction_label_height": null,                                     "reaction_node_ratio": null, "secondary_node_ratio": null,                                     "z": 100}}}'
        co = ConvertRequest(json.loads(test_json))
        self.assertTrue(co.layout_options.z == 100)


class DatabaseTest(unittest.TestCase):

    def test_database_connection_failure_on_bad_path(self):
        with self.assertRaises(Exception):
            Database(path="il|_f0rmed_p@th", echo=True)

    def test_database_connection_success(self):
        Database(path="/database/escher.db", echo=True)

    @skip("No component_options and layout_options are retrieved from the database and hence, "
          "can't be compared.")
    def test_add_entity(self):
        cr = ConvertRequest({
            "id": randint(0, 100000),
            "output_format": OutputFormat.sbml,
            "input_filename": "rhydon",
            "component_options": {
                "layout_id": "snorlax"
            },
            "layout_options": {
                "z": 100
            }
        })
        db = Database(config['TEST_CONFIG']['SQLITE_FILE'], True)
        db.add(cr)
        db_cr = db.retrieve(cr.id)
        self.assertTrue(cr == db_cr)
