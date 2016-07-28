# -*- coding: utf-8 -*-
# Models for receiving a request and sending a response. Database objects also.
import ejson
import jsonpickle

from enum import Enum

from sqlalchemy import Boolean
from sqlalchemy import Column
from sqlalchemy import Float
from sqlalchemy import ForeignKey
from sqlalchemy import Integer
from sqlalchemy import String
from sqlalchemy import types
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import relationship
from sqlalchemy_enum34 import EnumType


class OutputFormat(Enum):
    sbml = 'sbml'
    sbgn = 'sbgn'
    escher = 'escher'

    @classmethod
    def from_str(cls, s):
        if s is 'sbml':
            return cls.sbml
        if s is 'sbgn':
            return cls.sbgn
        if s is 'escher':
            return cls.escher
        return None


class ConversionStatus(Enum):
    started = 'started'
    waiting = 'waiting'
    running = 'running'
    completed = 'completed'
    failed = 'failed'
    errored = 'errored'

    @classmethod
    def from_str(cls, s):
        if s is 'started':
            return cls.started
        if s is 'waiting':
            return cls.waiting
        if s is 'running':
            return cls.running
        if s is 'completed':
            return cls.completed
        if s is 'failed':
            return cls.failed
        if s is 'errored':
            return cls.errored
        return None


LogLevel = Enum('log_level', 'severe warning info fine finer finest')

Base = declarative_base()


class ConvertRequest(Base):

    __tablename__ = 'conversions'

    id = Column(Integer, primary_key=True, autoincrement=False)
    output_format = Column(EnumType(OutputFormat), nullable=True)
    file_count = Column(Integer, nullable=False, default=1)
    files_uploaded = Column(Integer, nullable=False, default=0)
    status = Column(EnumType(ConversionStatus), nullable=True)
    submission_date = Column(Integer, nullable=False)
    completion_date = Column(Integer)
    component_options = relationship("ComponentOptions", uselist=False,
                                     back_populates="conversions")
    layout_options = relationship("LayoutOptions", uselist=False, back_populates="conversions")

    def __init__(self, d):
        if d is not None and 'component_options' in d:
            self.component_options = ComponentOptions(d['component_options'])
            self.component_options.id = self.id
        if d is not None and 'layout_options' in d:
            self.layout_options = LayoutOptions(d['layout_options'])
            self.layout_options.id = self.id
        if d is not None:
            self.__dict__.update(d)

    def __eq__(self, other):
        return self.__dict__ == other.__dict__


class ComponentOptions(Base):

    __tablename__ = 'component_options'

    id = Column(Integer, ForeignKey('conversions.id'), primary_key=True, autoincrement=False)
    layout_id = Column(String, nullable=True)
    layout_name = Column(String, nullable=True)
    compartment_id = Column(String, nullable=True)
    compartment_name = Column(String, nullable=True)
    infer_compartment_bounds = Column(Boolean, default=False)
    conversions = relationship("ConvertRequest", uselist=False, back_populates="component_options")

    def __init__(self, d):
        self.__dict__.update(d)

    def __eq__(self, other):
        return (
            self.layout_id == other.layout_id and
            self.layout_name == other.layout_name and
            self.compartment_id == other.compartment_id and
            self.compartment_name == other.compartment_name and
            self.infer_compartment_bounds == other.infer_compartment_bounds
        )


class LayoutOptions(Base):

    __tablename__ = 'layout_options'

    id = Column(Integer, ForeignKey('conversions.id'), primary_key=True, autoincrement=True)
    canvas_default_height = Column(Float, nullable=True)
    canvas_default_width = Column(Float, nullable=True)
    label_height = Column(Float, nullable=True)
    label_width = Column(Float, nullable=True)
    node_depth = Column(Float, nullable=True)
    node_label_height = Column(Float, nullable=True)
    primary_node_height = Column(Float, nullable=True)
    primary_node_width = Column(Float, nullable=True)
    reaction_label_height = Column(Float, nullable=True)
    reaction_node_ratio = Column(Float, nullable=True)
    secondary_node_ratio = Column(Float, nullable=True)
    z = Column(Float, nullable=True)
    conversions = relationship("ConvertRequest", uselist=False, back_populates="layout_options")

    def __init__(self, d):
        self.__dict__.update(d)

    def __eq__(self, other):
        return (
            self.canvas_default_height == other.canvas_default_height and
            self.canvas_default_width == other.canvas_default_width and
            self.label_height == other.label_height and
            self.label_width == other.label_width and
            self.node_depth == other.node_depth and
            self.node_label_height == other.node_label_height and
            self.primary_node_height == other.primary_node_height and
            self.primary_node_width == other.primary_node_width and
            self.reaction_label_height == other.reaction_label_height and
            self.reaction_node_ratio == other.reaction_node_ratio and
            self.secondary_node_ratio == other.secondary_node_ratio and
            self.z == other.z
        )


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
