import glob
import os
import shutil

import subprocess
from threading import Thread

import time

from database import Database
from models import ConvertRequest, ConversionStatus
from config import config
from models import OutputFormat
from models import ComponentOptions, LayoutOptions

FORMAT = {
    'escher': 'Escher',
    'sbml': 'SBML',
    'sbgn': 'SBGN'
}

EXTENSION = {
    'escher': '.escher.json',
    'sbml': '.sbml.xml',
    'sbgn': '.sbgn.xml'
}


class EscherConverter(object):
    jar_path = config['JAR_PATH']
    file_store = config['FILE_STORE']
    common_command = ['java', '-jar', jar_path, '--gui=false', '--log-level=FINEST']

    def __init__(self, options: ConvertRequest):
        self.command = EscherConverter.common_command
        self.db = Database(config['SQLITE_FILE'], config['DEBUG'])
        self.db.renew()
        self.options = self.db.retrieve(options.id)
        if not isinstance(options, ConvertRequest):
            raise TypeError("Options must be of type 'ConvertRequest'")
        if options.output_format is None:
            raise ValueError("output_format is required")
        else:
            self.command.append("--format=" + FORMAT[str(options.output_format.value)])
        # if (options.input_filename is None) and (options.id is None):
        #     raise ValueError("id is required for file names")
        # else:
        input_file = "--input=" + EscherConverter.file_store + str(options.id) + "/input/"
        self.command.append(input_file)
        os.makedirs(EscherConverter.file_store + str(options.id) + "/output/")
        self.command.append("--output=" + EscherConverter.file_store + str(options.id) + "/output/")
        self.command.append("--log-file=" + EscherConverter.file_store + str(options.id) +
                            "/conversion.log")
        self.add_compartment_options(options.component_options)
        self.add_layout_option(options.layout_options)

    def add_compartment_options(self, component_options: ComponentOptions):
        """
        Add component options to the command.
        :param component_options:
        :return:
        """
        pass

    def add_layout_option(self, layout_options: LayoutOptions):
        """
        Add layout options to the command.
        :param layout_options:
        :return:
        """
        pass

    def convert(self):
        thread = Thread(target=self._convert, args=[self.options.id])
        thread.start()

    def _convert(self, id):
        # TODO: Run the conversion and update database.
        db = Database(config['SQLITE_FILE'], config['DEBUG'])
        db.renew()
        options = db.retrieve(id)
        status_code = subprocess.call(self.command)
        req_id = options.id
        for file in glob.glob(config['FILE_STORE'] + str(req_id) + '/output/input/*'):
            shutil.move(file, config['FILE_STORE'] + str(req_id) + '/output/')
        os.rmdir(config['FILE_STORE'] + str(req_id) + '/output/input/')
        if status_code == 0:
            options.status = ConversionStatus.completed
        else:
            options.status = ConversionStatus.failed
        options.completion_date = int(time.time())
        db.update()
        db.finalize()
