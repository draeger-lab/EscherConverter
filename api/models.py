# -*- coding: utf-8 -*-
# Models for receiving a request and sending a respponse.
from enum import Enum

OutputFormat = Enum('format', 'sbml sbgn escher')
LogLevel = Enum('log_level', 'severe warning info fine finer finest')


class ConvertRequest:

    def __init__(self):
        self.output_format = OutputFormat()
        self.input_filename = None
        self.compartment_options = ComponentOptions()
        self.layout_options = LayoutOptions()


class ComponentOptions(object):

    def __init__(self):
        self.layout_id = None
        self.layout_name = None
        self.compartment_id = None
        self.compartment_name = None
        self.infer_compartment_bounds = None


class LayoutOptions(object):

    def __init__(self):
        self.canvas_default_height = None
        self.canvas_default_width = None
        self.label_height = None
        self.label_width = None
        self.node_depth = None
        self.node_label_height = None
        self.primary_node_height = None
        self.primary_node_width = None
        self.reaction_label_height = None
        self.reaction_node_ratio = None
        self.secondary_node_ratio = None
        self.z = None
