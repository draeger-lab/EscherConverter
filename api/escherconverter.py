import os

import subprocess
from models import ConvertRequest
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
        if not isinstance(options, ConvertRequest):
            raise TypeError("Options must be of type 'ConvertRequest'")
        if options.output_format is None:
            raise ValueError("output_format is required")
        else:
            self.command.append("--format=" + FORMAT[str(options.output_format)])
        if (options.input_filename is None) and (options.id is None):
            raise ValueError("id is required for file names")
        else:
            input_file = "--input=" + EscherConverter.file_store + options.id + "/input"
            if options.output_format is OutputFormat.escher:
                input_file += ".xml"
            else:
                input_file += ".json"
            self.command.append(input_file)
        self.command.append("--output=" + EscherConverter.file_store + options.id + "/output/" +
                            EXTENSION[
            options.output_format])
        self.command.append("--log-file=conversion.log")
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
        return subprocess.call(self.command)
