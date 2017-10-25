'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on May 21, 2014

@author: <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
'''
from xml.dom.minidom import Element


KEY_NOT_FOUND = '<KEY_NOT_FOUND>'  # KeyNotFound for dictDiff

class XMLtoDictParserTools():


    @staticmethod
    def parseDOM_ToDict( node, returnedDict = None, ignoreList = [] ):
        """
        Return Dictionary representation of the xml DOM Element.
        Repeated tags are put to the array sorted by key (id or order)
        otherwise is the value represented by tag key name.
        @param node: DOM Element 
        @param returnedDict : dictionary (default value None)
        @param ignereList : list of ignored tags for the xml DOM Element
                            (default value is empty list)
        @return: dict representation for the input DOM Element
        """
        returnedDict = {} if returnedDict is None else returnedDict
        if ( node.nodeType == Element.ELEMENT_NODE ) :
            nodeKey = ( node.localName ).encode( 'utf-8', 'ignore' )
            if nodeKey not in ignoreList :
                if node.childNodes is not None :
                    childDict = {}
                    for child in node.childNodes :
                        if child.nodeType == Element.TEXT_NODE :
                            nodeValue = ( child.nodeValue ).encode( 'utf-8', 'ignore' )
                            if ( len( nodeValue.strip( ' \t\n\r' ) ) ) > 0 :
                                XMLtoDictParserTools.addDictValue( returnedDict, nodeKey, nodeValue )
                                nodeKey = None
                                break
                        elif child.nodeType == Element.ELEMENT_NODE :
                            childDict = XMLtoDictParserTools.parseDOM_ToDict( child, childDict, ignoreList )

                    XMLtoDictParserTools.addDictValue( returnedDict, nodeKey, childDict )

        return returnedDict


    @staticmethod
    def addDictValue( m_dict, key, value ):
        '''
        Method add key and value to input dict. If the dict 
        contain the key, we are creating array for the values 
        and sort array by sort_key ('order' tag or '*-id' tag )
        @param m_dict: dictionary for key and value
        @param key: dict key
        @param value: dict value
        '''
        if key is not None :
            if ( isinstance( value, str ) ) :
                # we need to predict possible differences
                # for same value in upper or lower case
                value = value.lower()
            if key not in m_dict :
                m_dict[key] = value
            else :
                exist_value = m_dict.get( key )
                if ( type( exist_value ) is dict ) :
                    list_values = [exist_value, value]
                    key_for_sort = XMLtoDictParserTools.searchKey( exist_value )
                    if key_for_sort is not None :
                        list_values = sorted( list_values, key = lambda k: k[key_for_sort] )
                    m_dict[key] = list_values
                elif ( isinstance( exist_value, list ) ) :
                    exist_value.append( value )
                    list_values = exist_value
                    key_for_sort = XMLtoDictParserTools.searchKey( value )
                    if key_for_sort is not None :
                        list_values = sorted( list_values, key = lambda k: k[key_for_sort] )
                    m_dict[key] = list_values
                else :
                    m_dict[key] += value


    @staticmethod
    def searchKey( dictionary ):
        """
        Return an order key for the array ordering. OF_13
        allows only two possible kind of the order keys
        'order' or '*-id'
        @param dictionary: dictionary with data
        @return: the array order key 
        """
        subKeyStr = ['-id', 'order']
        for substr in subKeyStr :
            for key in dictionary:
                if key == substr :
                    return key
                elif key.endswith( substr ):
                    return key
        return None


    @staticmethod
    def getDifferenceDict( original_dict, responded_dict ):
        """ 
        Return a dict of keys that differ with another config object.  If a value is
        not found in one fo the configs, it will be represented by KEY_NOT_FOUND.
        @param original_dict:   Fist dictionary to diff.
        @param responded_dict:  Second dictionary to diff.
        @return diff:   Dict of Key => (original_dict.val, responded_dict.val)
                        Dict of Key => (original_key, KEY_NOT_FOUND)
                        Dict of Key => (KEY_NOT_FOUND, original_key)
        """
        diff = {}
        # Check all keys in original_dict dict
        for key in original_dict.keys():
            if ( not responded_dict.has_key( key ) ):
                # missing key in responded dict
                diff[key] = ( key, KEY_NOT_FOUND )
            # check values of the dictionaries
            elif ( original_dict[key] != responded_dict[key] ):
                # values are not the same #

                orig_dict_val = original_dict[key]
                resp_dict_val = responded_dict[key]

                # check value is instance of dictionary
                if isinstance( orig_dict_val, dict ) and isinstance( resp_dict_val, dict ):
                    sub_dif = XMLtoDictParserTools.getDifferenceDict( orig_dict_val, resp_dict_val )
                    if sub_dif :
                        diff[key] = sub_dif

                # check value is instance of list
                # TODO - > change a basic comparator to compare by id or order
                elif isinstance( orig_dict_val, list ) and isinstance( resp_dict_val, list ) :
                    sub_list_diff = {}
                    # the list lengths
                    orig_i, resp_i = len( orig_dict_val ), len( resp_dict_val )
                    # define a max iteration length (less from both)
                    min_index = orig_i if orig_i < resp_i else resp_i
                    for index in range ( 0, min_index, 1 ) :
                        if ( orig_dict_val[index] != resp_dict_val[index] ) :
                            if isinstance( orig_dict_val, dict ) and isinstance( resp_dict_val, dict ) :
                                sub_list_diff[index] = ( XMLtoDictParserTools.getDifferenceDict( orig_dict_val[index], resp_dict_val[index] ) )
                            else :
                                sub_list_diff[index] = ( orig_dict_val[index], resp_dict_val[index] )
                    if ( orig_i > min_index ) :
                        # original is longer as responded dict
                        for index in range ( min_index, orig_i, 1 ):
                            sub_list_diff[index] = ( orig_dict_val[index], None )
                    elif ( resp_i > min_index ) :
                        # responded dict is longer as original
                        for index in range ( min_index, resp_i, 1 ) :
                            sub_list_diff[index] = ( None, resp_dict_val[index] )
                    if sub_list_diff :
                        diff[key] = sub_list_diff

                else :
                    diff[key] = ( original_dict[key], responded_dict[key] )

        # Check all keys in responded_dict dict to find missing
        for key in responded_dict.keys():
            if ( not original_dict.has_key( key ) ):
                diff[key] = ( KEY_NOT_FOUND, key )
        return diff
