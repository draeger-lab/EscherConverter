# -*- coding: utf-8 -*-
# Models for receiving a request and sending a respponse.
import ejson
import jsonpickle

from enum import Enum
from sqlalchemy import Column
from sqlalchemy import Integer
from sqlalchemy import String
from sqlalchemy import types
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy_enum34 import EnumType


class OutputFormat(Enum):
    sbml = 'sbml'
    sbgn = 'sbgn'
    escher = 'escher'


LogLevel = Enum('log_level', 'severe warning info fine finer finest')

Base = declarative_base()


class ConvertRequest(Base):

    __tablename__ = 'conversions'

    id = Column(Integer, primary_key=True, autoincrement=False)
    output_format = Column(EnumType(OutputFormat), nullable=False)
    input_filename = Column(String)

    def __init__(self, d):
        if d is not None and 'component_options' in d:
            self.component_options = ComponentOptions(d['component_options'])
        if d is not None and 'layout_options' in d:
            self.layout_options = LayoutOptions(d['layout_options'])
        if d is not None:
            self.__dict__.update(d)

    def __eq__(self, other):
        return self.__dict__ == other.__dict__


class ComponentOptions(object):

    layout_id = None
    layout_name = None
    compartment_id = None
    compartment_name = None
    infer_compartment_bounds = None

    def __init__(self, d):
        self.__dict__.update(d)

    def __eq__(self, other):
        return self.__dict__ == other.__dict__


class LayoutOptions(object):

    canvas_default_height = None
    canvas_default_width = None
    label_height = None
    label_width = None
    node_depth = None
    node_label_height = None
    primary_node_height = None
    primary_node_width = None
    reaction_label_height = None
    reaction_node_ratio = None
    secondary_node_ratio = None
    z = None

    def __init__(self, d):
        self.__dict__.update(d)

    def __eq__(self, other):
        return self.__dict__ == other.__dict__


@ejson.register_serializer(ConvertRequest)
def cr_serializer(instance):
    d = jsonpickle.dumps(instance.__dict__, unpicklable=True)
    return instance.__dict__


@ejson.register_serializer(ComponentOptions)
def co_serializer(instance):
    d = instance.__dict__
    return instance.__dict__


@ejson.register_serializer(LayoutOptions)
def lo_serializer(instance):
    d = instance.__dict__
    return instance.__dict__


@ejson.register_deserializer(ConvertRequest)
def cr_deserializer(data):
    co = ConvertRequest()
    co.__dict__ = data
    return co


@ejson.register_deserializer(ComponentOptions)
def co_deserializer(data):
    co = ComponentOptions()
    co.__dict__ = data
    return co


@ejson.register_deserializer(LayoutOptions)
def lo_deserializer(data):
    co = LayoutOptions()
    co.__dict__ = data
    return co
