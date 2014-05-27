'''
Created on Jan 24, 2014

@author: vdemcak
'''

import re
from xml.etree import ElementTree as ET

from convertor_tools import ConvertorTools


class ParseTools(): 

    @staticmethod
    def get_element_alias_by_key(element,key_dict):
        return key_dict.get(element.tag) if (key_dict.get(element.tag, None) > None) else None

    @staticmethod
    def sort_ordered_dict_to_array(x_dict=None):
        if (x_dict > None):
            out_put = []
            for val in map(lambda val: x_dict.get(val), sorted(x_dict.keys())) : 
                out_put.append(val)
#                 if (out_put > None) :
#                     out_put += ', %s' %val
#                 else :
#                     out_put = val
            return ', '.join(out_put)
        return

    @staticmethod
    def get_element_value(element):
        return (re.sub('[\s]+', '', element.text, count=1)).lower() if element.text > None else ''

    @staticmethod
    def __parse_ordered_tags_from_xml(element, kwd, p_elm_n=None, ikwd=None, ord_value=None):
        a_dict = {}
        if (element > None) :
            elm_n = ParseTools.get_element_alias_by_key(element, kwd)
            if ((element.getchildren() > None) & (len(element.getchildren()) > 0)) :
                sub_dict ={}
                for child in element.getchildren() :
                    if (child.tag == 'order') :
                        ord_value = ParseTools.get_element_value(child)
                    else :
                        sub_dict.update(ParseTools.__parse_ordered_tags_from_xml(child, kwd, p_elm_n, ikwd))
                        
                a_value = ParseTools.sort_ordered_dict_to_array(sub_dict)
                if (ord_value > None) :
                    order = ord_value if (len(ord_value) > 0) else '0'
                else :
                    order = '0'
                a_dict[order]=a_value
                
            else :
                if (ord_value > None) :
                    order = ord_value if ((len(ord_value) > 0)) else '0'
                else :
                    order = '0'
                a_val = elm_n if elm_n > None else element.tag
                a_dict[order] = a_val
                
        return a_dict

    @staticmethod
    def __parse_tags_from_xml(element, flow_dict, kwd, p_elm_n=None, ikwd=None):
        if element > None :
            # find and translate element.tag in key_word_dictionary
            elm_n = ParseTools.get_element_alias_by_key(element, kwd)
            if ((element.getchildren() > None) & (len(element.getchildren()) > 0)) :
                for child in element.getchildren() :
                    new_p_elm_n = elm_n if elm_n > None else p_elm_n
                    ParseTools.__parse_tags_from_xml(child, flow_dict, kwd, new_p_elm_n, ikwd)
            else :
                # prefer parent element_name before elment_name and element_name before element.tag
                a_key = elm_n if elm_n > None else p_elm_n if (p_elm_n > None) else element.tag
                a_value = ParseTools.get_element_value(element)
                # Don't continue for ignore tags
                if (ikwd > None) :
                    if (ikwd.get(a_key, None) > None) :
                        # TODO add check for cookie_mask (mask has to have same or more length as cookie if is more as 0)
                        return
                flow_dict[a_key] = ConvertorTools.base_tag_values_conversion(a_key, a_value)

    @staticmethod
    def get_switchflow_from_xml(xml_string, key_dict=None, action_key_dict=None, match_key_dict=None, ignore_key_dict=None):
        if xml_string > None :
            # remove namespace
            xml_string = re.sub(' xmlns="[^"]+"', '', xml_string, count=1)
            tree = ET.fromstring(xml_string)
            
        flow_dict = {}
        
        if (tree > None) :
            if (tree.getchildren() > None) :
                for child in tree.getchildren() :
                    if (child.tag == 'match') :
                        ParseTools.__parse_tags_from_xml(child, flow_dict, match_key_dict, ikwd=ignore_key_dict)
                    elif (child.tag == 'instructions') : 
                        x_dict = ParseTools.__parse_ordered_tags_from_xml(child, action_key_dict, ikwd=ignore_key_dict)
                        flow_dict['actions'] = ParseTools.sort_ordered_dict_to_array(x_dict)
                    else :
                        ParseTools.__parse_tags_from_xml(child, flow_dict, key_dict, ikwd=ignore_key_dict) 

        return flow_dict
        
        # TODO VD remove this method
#     @staticmethod
#     def get_switchflow_dict(switch_dict, ignore_key_dict=None):
#         x_dict={}
#         for sw_key in switch_dict.keys() :
#             if (ignore_key_dict.get(sw_key,None) is None):
#                 x_dict[sw_key] = switch_dict.get(sw_key)
#             
#         return x_dict
    
    @staticmethod
    def all_nodes(xml_root):
        """
        Generates every non-text nodes.
        """
        current_nodes = [xml_root]
        next_nodes = []

        while len(current_nodes) > 0:
            for node in current_nodes:
                if node.nodeType != xml_root.TEXT_NODE:
                    yield node
                    next_nodes.extend(node.childNodes)

            current_nodes, next_nodes = next_nodes, []

    @staticmethod
    def get_values(node, *tags):
        result = dict((tag, None) for tag in tags)
#         result = {tag: None for tag in tags}
        for node in ParseTools.all_nodes(node):
            if node.nodeName in result and len(node.childNodes) > 0:
                result[node.nodeName] = node.childNodes[0].nodeValue
        return result

    @staticmethod
    def dump_string_to_dict(dump_string):
        dump_list = ParseTools.dump_string_to_list(dump_string)
        return ParseTools.dump_list_to_dict(dump_list)

    @staticmethod
    def dump_string_to_list(dump_string):
        out_list = []
        for item in dump_string.split():
            out_list.extend(item.rstrip(',').split(','))

        return out_list

    @staticmethod
    def dump_list_to_dict(dump_list):
        out_dict = {}
        for item in dump_list:
            parts = item.split('=')
            if len(parts) == 1:
                parts.append(None)
            out_dict[parts[0]] = parts[1]

        return out_dict