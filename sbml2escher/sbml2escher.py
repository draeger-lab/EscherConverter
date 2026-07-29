"""
This module provides tools for converting SBML and CellDesigner XML files to Escher format.
"""
import json
import argparse
import sys
import time
import os

from xml.parsers.expat import ExpatError
import xmltodict
import requests

# define edges
edges = {}
# define nodes
nodes = {}


# identify the file type, whether it is CellDesigner XML or SBML XML
def identify_file_type(file_path):
    file_data = load_xml_data(file_path)
    if 'sbml' in file_data:
        if '@xmlns:celldesigner' in file_data['sbml']:
            return 'celldesigner', file_data
        return 'sbml', file_data
    return 'Unknown XML type', None


def celldesigner2sbml(input_file_path, output_file_path):
    start_at = time.time()
    with open(input_file_path, 'rb') as file:
        file_data = file.read()

    url = 'https://minerva-service.lcsb.uni.lu/minerva/api/convert/CellDesigner_SBML:SBML'
    headers = {
        'Content-Type': 'text/plain'
    }

    response = requests.post(url, data=file_data, headers=headers, timeout=600)

    if response.status_code == 200:
        with open(output_file_path, 'wb') as file:
            file.write(response.content)
        end_at = time.time()
        print(f"CellDesigner2SBML request completed in {end_at - start_at:.2f} seconds.")
        print(f"CellDesigner2SBML request successful, file saved as {output_file_path}")
    else:
        print(f"CellDesigner2SBML request failed with status code {response.status_code}, "
              f"error message: {response.text}")
        sys.exit(1)


def load_xml_data(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return xmltodict.parse(file.read())
    except FileNotFoundError:
        print(f"Error: The file {file_path} was not found.")
        sys.exit(1)
    except IOError as e:
        print(f"Error: Could not read the XML file {file_path}. I/O error: {e}")
        sys.exit(1)
    except ExpatError as e:
        print(f"Error: Failed to parse XML file {file_path}. Parsing error: {e}")
        sys.exit(1)


def save_json_data(json_data, file_path):
    if not file_path.endswith('.json'):
        print(f"Warning: The output file {file_path} does not have a .json extension. "
              f"It might not be opened correctly by JSON readers.")
    try:
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(json_data, file, indent=4)
    except IOError as e:
        print(f"Error: Could not write the JSON data to the file {file_path}. I/O error: {e}")
        sys.exit(1)


def is_substrates_metabolite(role):
    return role in ('substrate', 'sidesubstrate')


def is_products_metabolite(role):
    return role in ('product', 'sideproduct')


def is_main_metabolite(role):
    return role in ('substrate', 'product')


def is_valid_metabolite(role):
    return role in ('substrate', 'sidesubstrate', 'product', 'sideproduct')


def get_metabolites_for_reaction(reaction, specie2bigg):
    reaction_metabolites = []
    list_of_reactants = reaction.get('listOfReactants', {}).get('speciesReference')
    if isinstance(list_of_reactants, dict):
        list_of_reactants = [list_of_reactants]
    for reactant in list_of_reactants:
        metabolite_id = reactant['@species']
        metabolite_name = specie2bigg[metabolite_id]
        reaction_metabolites.append({
            'bigg_id': metabolite_name,
            'coefficient': -1
        })

    list_of_products = reaction.get('listOfProducts', {}).get('speciesReference')
    if isinstance(list_of_products, dict):
        list_of_products = [list_of_products]
    for product in list_of_products:
        metabolite_id = product['@species']
        metabolite_name = specie2bigg[metabolite_id]
        reaction_metabolites.append({
            'bigg_id': metabolite_name,
            'coefficient': 1
        })

    return reaction_metabolites


def put_segment_to_segments(segments, segment_id, from_node_id, to_node_id, b1=None, b2=None):
    segments[segment_id] = {
        'from_node_id': from_node_id,
        'to_node_id': to_node_id,
        'b1': b1,
        'b2': b2,
    }


def process_metabolite(role, index, start_node_id, end_node_id, start_x, start_y, segments,
                       mato_species_glyph, length_of_metabolite_segments, metabolite_curve_id):
    metabolite_segment_id = f"{metabolite_curve_id}-{mato_species_glyph}"
    current_metabolite_segment_id = f"{metabolite_segment_id}-{index}"
    next_metabolite_segment_id = f"{metabolite_segment_id}-{index + 1}"

    def handle_cant_direct_connect_to_reaction(is_produce_node, node_in_reaction):
        only_one_segment = length_of_metabolite_segments == 1
        next_node = mato_species_glyph if only_one_segment else next_metabolite_segment_id

        if node_in_reaction and (
                start_x != nodes[node_in_reaction]['x'] or
                start_y != nodes[node_in_reaction]['y']
        ):
            extra_seg_id = f"{current_metabolite_segment_id}-extra"
            nodes[extra_seg_id] = {
                'node_type': 'multimarker',
                'x': start_x,
                'y': start_y,
            }

            update_segments_with_node(is_produce_node, segments, start_x, start_y,
                                      extra_seg_id, node_in_reaction, current_metabolite_segment_id)

            from_id, to_id = (extra_seg_id, next_node) if is_produce_node else (
                next_node, extra_seg_id)

            put_segment_to_segments(segments, current_metabolite_segment_id, from_id, to_id)
        else:
            from_id, to_id = (node_in_reaction, next_node) if is_produce_node else (
                next_node, node_in_reaction)

            put_segment_to_segments(segments, current_metabolite_segment_id, from_id, to_id)

    if is_valid_metabolite(role):
        if index == 0:
            _node_in_reaction = end_node_id if is_products_metabolite(role) else start_node_id
            handle_cant_direct_connect_to_reaction(is_products_metabolite(role),
                _node_in_reaction)

        elif index == length_of_metabolite_segments - 1:
            from_id, to_id = (
                current_metabolite_segment_id, mato_species_glyph) if is_products_metabolite(
                role) else (
                mato_species_glyph, current_metabolite_segment_id)
            put_segment_to_segments(segments, current_metabolite_segment_id, from_id, to_id)

        else:
            from_id, to_id = (
                current_metabolite_segment_id,
                next_metabolite_segment_id) if is_products_metabolite(
                role) else (
                next_metabolite_segment_id, current_metabolite_segment_id)
            put_segment_to_segments(segments, current_metabolite_segment_id, from_id, to_id)

        nodes[current_metabolite_segment_id] = {
            'node_type': 'multimarker',
            'x': start_x,
            'y': start_y,
        }
    else:
        print("Unknown role", role)


def set_reaction_label_position(start, end, reaction):
    mid_node = {
        'x': (float(start['@layout:x']) + float(end['@layout:x'])) / 2,
        'y': (float(start['@layout:y']) + float(end['@layout:y'])) / 2
    }
    reaction['label_x'] = mid_node['x']
    reaction['label_y'] = mid_node['y'] - 20


def is_point_on_segment(px, py, x1, y1, x2, y2):
    cross_product = (py - y1) * (x2 - x1) - (px - x1) * (y2 - y1)
    if abs(cross_product) > 1e-6:
        return False
    if px < min(x1, x2) - 1 or px > max(x1, x2) + 1:
        return False
    if py < min(y1, y2) - 1 or py > max(y1, y2) + 1:
        return False
    return True


def update_segments_with_node(is_produce_node, segments, start_x, start_y, extra_node_id,
                              node_in_reaction_curve,
                              seg_id_for_debug=None):
    segment_to_remove = None
    from_node_id = None
    to_node_id = None

    for seg_id, segment in segments.items():
        from_node = nodes[segment['from_node_id']]
        to_node = nodes[segment['to_node_id']]
        if is_point_on_segment(start_x, start_y, from_node['x'], from_node['y'], to_node['x'],
                               to_node['y']):
            segment_to_remove = seg_id
            from_node_id = segment['from_node_id']
            to_node_id = segment['to_node_id']
            break

    if segment_to_remove is None:
        print("No segment found containing the point.", seg_id_for_debug)
        _from_node_id, _to_node_id = (
            node_in_reaction_curve, extra_node_id) if is_produce_node else (
            extra_node_id, node_in_reaction_curve)
        put_segment_to_segments(segments, extra_node_id, _from_node_id, _to_node_id)
        return

    del segments[segment_to_remove]

    new_segment_1_id = f"{extra_node_id}-left"
    new_segment_2_id = f"{extra_node_id}-right"
    put_segment_to_segments(segments, new_segment_1_id, from_node_id, extra_node_id)
    put_segment_to_segments(segments, new_segment_2_id, extra_node_id, to_node_id)


def create_reaction_basic_info(model, specie2bigg, layout_width, layout_height):
    reactions = model['listOfReactions']['reaction']
    for reaction in reactions:
        reaction_id = reaction['@id']
        reaction_name = reaction['@name'] if '@name' in reaction else reaction_id
        reaction_reversible = reaction['@reversible'] == 'true'
        reaction_metabolites = get_metabolites_for_reaction(reaction, specie2bigg)
        edges[reaction_id] = {}
        edges[reaction_id]['name'] = reaction_name
        edges[reaction_id]['bigg_id'] = reaction_name
        edges[reaction_id]['reversibility'] = reaction_reversible
        edges[reaction_id]['metabolites'] = reaction_metabolites
        edges[reaction_id]['label_x'] = layout_width + 100
        edges[reaction_id]['label_y'] = layout_height + 100


def create_metabolite_nodes(specie2bigg, layout_root):
    list_of_species_glyphs = layout_root['layout:listOfSpeciesGlyphs']['layout:speciesGlyph']
    for species_glyph in list_of_species_glyphs:
        layout_id = species_glyph['@layout:id']
        species_id = species_glyph['@layout:species']
        position = species_glyph['layout:boundingBox']['layout:position']
        width = species_glyph['layout:boundingBox']['layout:dimensions']['@layout:width']
        height = species_glyph['layout:boundingBox']['layout:dimensions']['@layout:height']
        name = specie2bigg[species_id]
        nodes[layout_id] = {
            'bigg_id': name,
            'name': name,
            'node_type': 'metabolite',
            'x': float(position['@layout:x']) + float(width) / 2,
            'y': float(position['@layout:y']) + float(height) / 2,
            'label_x': float(position['@layout:x']) + float(width) / 2,
            'label_y': float(position['@layout:y']) + float(height) / 2,
            'node_is_primary': False
        }


def create_reaction_segments(reaction_glyph, reaction_layout_id, segments, reaction):
    list_of_reaction_segments = []
    reaction_seg_start_node_id = None
    reaction_seg_end_node_id = None
    if 'layout:curve' in reaction_glyph:
        layout_curve = reaction_glyph['layout:curve']
        if layout_curve is not None and 'layout:listOfCurveSegments' in layout_curve:
            list_of_reaction_curves = layout_curve['layout:listOfCurveSegments']
            curve_segment_in_list = 'layout:curveSegment' in list_of_reaction_curves
            if list_of_reaction_curves is not None and curve_segment_in_list:
                list_of_reaction_segments = list_of_reaction_curves['layout:curveSegment']

    if isinstance(list_of_reaction_segments, dict):
        list_of_reaction_segments = [list_of_reaction_segments]

    length_of_reaction_segments = len(list_of_reaction_segments)
    for index, reaction_segment in enumerate(list_of_reaction_segments):
        start = reaction_segment['layout:start']
        end = reaction_segment['layout:end']
        start_x = float(start['@layout:x'])
        start_y = float(start['@layout:y'])
        end_x = float(end['@layout:x'])
        end_y = float(end['@layout:y'])
        current_reaction_segment_id = f"{reaction_layout_id}-{index}"
        next_reaction_segment_id = f"{reaction_layout_id}-{index + 1}"

        start_id = f"{current_reaction_segment_id}"
        nodes[start_id] = {
            'node_type': 'multimarker',
            'x': start_x,
            'y': start_y,
        }
        end_id = f"{next_reaction_segment_id}-end"
        if index == length_of_reaction_segments - 1:
            reaction_seg_end_node_id = end_id
            nodes[end_id] = {
                'node_type': 'multimarker',
                'x': end_x,
                'y': end_y,
            }
        if index == 0:
            reaction_seg_start_node_id = start_id
            set_reaction_label_position(start, end, reaction)

        to_id = end_id if index == length_of_reaction_segments - 1 else next_reaction_segment_id
        put_segment_to_segments(segments, current_reaction_segment_id, start_id, to_id)

    return reaction_seg_start_node_id, reaction_seg_end_node_id


def create_metabolite_segments(reaction, reaction_glyph, reaction_layout_id, segments,
                               reaction_seg_start_node_id,
                               reaction_seg_end_node_id):
    list_of_metabolite_curves = reaction_glyph['layout:listOfSpeciesReferenceGlyphs'][
        'layout:speciesReferenceGlyph']
    for metabolite_curve in list_of_metabolite_curves:
        metabolite_curve_id = f"{reaction_layout_id}-{metabolite_curve['@layout:id']}"
        role = metabolite_curve['@layout:role']
        mato_species_glyph = metabolite_curve['@layout:speciesGlyph']
        start_node_id = reaction_seg_start_node_id
        end_node_id = reaction_seg_end_node_id

        # Some speciesReferenceGlyphs carry no curve geometry (MINERVA emits an
        # empty <layout:curve/> for arcs it cannot route). There is nothing to draw
        # for those, so skip them rather than crashing on the missing curve.
        curve = metabolite_curve.get('layout:curve')
        list_of_curve_segments = curve.get('layout:listOfCurveSegments') if curve else None
        list_of_metabolite_segments = (
            list_of_curve_segments.get('layout:curveSegment')
            if list_of_curve_segments else None)
        if list_of_metabolite_segments is None:
            continue
        if isinstance(list_of_metabolite_segments, dict):
            list_of_metabolite_segments = [list_of_metabolite_segments]

        length_of_metabolite_segments = len(list_of_metabolite_segments)
        for index, metabolite_segment in enumerate(list_of_metabolite_segments):
            start_x = float(metabolite_segment['layout:start']['@layout:x'])
            start_y = float(metabolite_segment['layout:start']['@layout:y'])
            if ((start_node_id is None) or (end_node_id is None) and index == 0):
                center_node_id = f"{metabolite_curve_id}-center"
                nodes[center_node_id] = {
                    'node_type': 'multimarker',
                    'x': start_x,
                    'y': start_y,
                }
                start_node_id = end_node_id = center_node_id
                reaction['label_x'] = start_x
                reaction['label_y'] = start_y - 20

            if is_main_metabolite(role):
                nodes[mato_species_glyph]['node_is_primary'] = True

            process_metabolite(role, index, start_node_id, end_node_id, start_x, start_y,
                               segments,
                               mato_species_glyph, length_of_metabolite_segments,
                               metabolite_curve_id)


def create_all_segments(layout_root):
    list_of_reaction_glyphs = layout_root['layout:listOfReactionGlyphs']['layout:reactionGlyph']
    for reaction_glyph in list_of_reaction_glyphs:
        reaction = edges[reaction_glyph['@layout:reaction']]
        segments = {}
        reaction_layout_id = reaction_glyph['@layout:id']

        reaction_seg_start_node_id, reaction_seg_end_node_id = create_reaction_segments(
            reaction_glyph,
            reaction_layout_id,
            segments, reaction)

        create_metabolite_segments(reaction, reaction_glyph, reaction_layout_id, segments,
                                   reaction_seg_start_node_id,
                                   reaction_seg_end_node_id)

        reaction['segments'] = segments
        edges[reaction_glyph['@layout:reaction']] = reaction


def sbml2escher(input_file_path, output_file_path, delete_temp_file=False):
    xml_data = load_xml_data(input_file_path)

    model = xml_data['sbml']['model']

    specie2bigg = {}
    for sp in model['listOfSpecies']['species']:
        specie2bigg[sp['@id']] = sp['@name']

    list_of_layouts = model['layout:listOfLayouts']
    if isinstance(list_of_layouts, dict):
        list_of_layouts = [list_of_layouts]

    layout_root = list_of_layouts[0]['layout:layout']
    # MINERVA may emit several <layout:layout> elements (a data layout that carries
    # the map geometry plus render-only overlay layers). Only the data layout has
    # dimensions and glyphs, so select the first layout that actually has dimensions.
    if isinstance(layout_root, list):
        layout_root = next(
            (l for l in layout_root if 'layout:dimensions' in l),
            layout_root[0],
        )
    layout_width = float(layout_root['layout:dimensions']['@layout:width'])
    layout_height = float(layout_root['layout:dimensions']['@layout:height'])

    create_reaction_basic_info(model, specie2bigg, layout_width, layout_height)

    create_metabolite_nodes(specie2bigg, layout_root)

    create_all_segments(layout_root)

    escher_maps = [{
        "map_name": model['@id'],
        "map_id": model['@id'],
        "map_description": "",
        "homepage": "https://escher.github.io",
        "schema": "https://escher.github.io/escher/jsonschema/1-0-0#"
    },
        {
            "reactions": edges,
            "nodes": nodes,
            "text_labels": {},
            "canvas": {
                "x": -layout_width / 20,
                "y": -layout_height / 20,
                "width": layout_width * 1.1,
                "height": layout_height * 1.1
            }
        }
    ]

    save_json_data(escher_maps, output_file_path)

    if delete_temp_file:
        try:
            os.remove(input_file_path)
            print(f"File {input_file_path} deleted successfully")
        except OSError as e:
            print(f"Error: {input_file_path} - {e.strerror}")

    print(f"convert success, and save to {output_file_path}")


if __name__ == "__main__":
    start_time = time.time()
    parser = argparse.ArgumentParser(description='Process some JSON files.')
    parser.add_argument('--input', default='sbml.xml', help='Path to the input XML file')
    parser.add_argument('--output', default='sbml2escher_output.json',
                        help='Path to the output JSON file')

    args = parser.parse_args()
    INPUT_PATH = args.input
    OUPUT_PATH = args.output

    input_format, data = identify_file_type(INPUT_PATH)
    if input_format in ('celldesigner', 'sbml'):
        if input_format == 'celldesigner':
            TEMP_OUTPUT_FILE_PATH = OUPUT_PATH + '.SBML_converted.xml'
            celldesigner2sbml(INPUT_PATH, TEMP_OUTPUT_FILE_PATH)
            INPUT_PATH = TEMP_OUTPUT_FILE_PATH

        sbml2escher(INPUT_PATH, OUPUT_PATH, input_format == 'celldesigner')
        end_time = time.time()
        print(f"Conversion completed in {end_time - start_time:.2f} seconds.")
    else:
        print(f"Error: The input file {INPUT_PATH} is not a valid CellDesigner or SBML XML file.")
        sys.exit(1)
