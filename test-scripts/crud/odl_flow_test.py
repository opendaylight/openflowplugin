'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on May 11, 2014

@author: <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
'''
import requests
from xml.dom.minidom import Element

from openvswitch.parser_tools import ParseTools
from tools.crud_test_with_param_superclass import OF_CRUD_Test_Base
from tools.xml_parser_tools import XMLtoDictParserTools
import xml.dom.minidom as md


TABLE_ID_TAG_NAME = 'table_id'
FLOW_ID_TAG_NAME = 'id'

IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON = ['id', 'flow-name', 'barrier', 'cookie_mask', 'installHw', 'flags', 'strict', 'byte-count', 'duration', 'packet-count', 'in-port']

FLOW_TAGS_FOR_UPDATE = ['action']

class OF_CRUD_Test_Flows( OF_CRUD_Test_Base ):


    def setUp( self ):
        super( OF_CRUD_Test_Flows, self ).setUp()
        ids = ParseTools.get_values( self.xml_input_DOM, TABLE_ID_TAG_NAME, FLOW_ID_TAG_NAME )
        data = ( self.host, self.port, ids[TABLE_ID_TAG_NAME], ids[FLOW_ID_TAG_NAME] )
        self.conf_url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s/flow/%s' % data
        self.oper_url = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s/flow/%s' % data
        data = ( self.host, self.port, ids[TABLE_ID_TAG_NAME] )
        self.conf_url_post = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s' % data
        # ---- operations ---
        self.oper_url_get = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s' % data
        data = ( self.host, self.port )
        self.oper_url_add = 'http://%s:%d/restconf/operations/sal-flow:add-flow' % data
        self.oper_url_upd = 'http://%s:%d/restconf/operations/sal-flow:update-flow' % data
        self.oper_url_del = 'http://%s:%d/restconf/operations/sal-flow:remove-flow' % data
        # Modify input operations data
        self.data_from_file_input = ''
        for node in self.xml_input_DOM.documentElement.childNodes:
            nodeKey = None if node.localName == None else ( node.localName ).encode( 'utf-8', 'ignore' )
            if ( nodeKey is None or nodeKey != 'id' ) :
                self.data_from_file_input += node.toxml( encoding = 'utf-8' )

        # The xml body without data - data come from file (all flow's subtags)
        self.oper_input_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                                    '<input xmlns="urn:opendaylight:flow:service">\n' \
                                        '%s' \
                                        '<node xmlns:inv="urn:opendaylight:inventory" xmlns:finv="urn:opendaylight:flow:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                                    '</input>' % self.data_from_file_input


    def tearDown( self ) :
        # cleaning configuration DataStore and device without a response validation
        self.log.info( self._paint_msg_cyan( 'Uncontrolled cleaning after flow test' ) )
        requests.delete( self.conf_url, auth = self._get_auth(),
                               headers = self._get_xml_result_header() )
        requests.post( self.oper_url_del, data = self.oper_input_stream, auth = self._get_auth(),
                              headers = self._get_xml_request_result_header() )
        super( OF_CRUD_Test_Flows, self ).tearDown()


    def test_conf_PUT( self ):
        self.log.info( "--- Flow conf. PUT test ---" )
        # -------------- CREATE -------------------
        self.log.info( self._paint_msg_yellow( " CREATE Flow by PUT REST" ) )
        # send request via RESTCONF
        self.put_REST_XML_conf_request( self.conf_url, self.xml_input_stream )
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response( self.conf_url )
        xml_resp_stream = ( response.text ).encode( 'utf-8', 'ignore' )
        xml_resp_DOM = md.parseString( xml_resp_stream )
        self.assertDataDOM( self.xml_input_DOM, xml_resp_DOM )
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, self.xml_input_DOM )

        # -------------- UPDATE -------------------
        self.log.info( self._paint_msg_yellow( " UPDATE Flow by PUT REST" ) )
        xml_updated_stream = self.__update_flow_input();
        self.put_REST_XML_conf_request( self.conf_url, xml_updated_stream )
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response( self.conf_url )
        xml_resp_stream = ( response.text ).encode( 'utf-8', 'ignore' )
        xml_resp_DOM = md.parseString( xml_resp_stream )
        xml_upd_DOM = md.parseString( xml_updated_stream )
        self.assertDataDOM( xml_upd_DOM, xml_resp_DOM )
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, xml_upd_DOM )

        # -------------- DELETE -------------------
        self.log.info( self._paint_msg_yellow( " DELETE Flow by DELETE REST" ) )
        # Delte data from config DataStore
        response = self.delete_REST_XML_response( self.conf_url )
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Flow data has specific format and it has to be compared in different way
        response = self.get_REST_XML_response( super.oper_url )
        # find a correct flow-statistic (expect -> flow-statistic not exist)
        self.__validate_contain_flow( response, self.xml_input_DOM, False )


    def test_conf_POST( self ):
        self.log.info( "--- Flow conf. POST test ---" )
        # -------------- CREATE -------------------
        self.log.info( self._paint_msg_yellow( " CREATE Flow by POST REST" ) )
        # send request via RESTCONF
        self.post_REST_XML_request( self.conf_url_post, self.xml_input_stream )
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response( self.conf_url )
        xml_resp_stream = ( response.text ).encode( 'utf-8', 'ignore' )
        xml_resp_DOM = md.parseString( xml_resp_stream )
        self.assertDataDOM( self.xml_input_DOM, xml_resp_DOM )
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, self.xml_input_DOM )
        # test error for double create (POST could create data only)
        self.log.info( self._paint_msg_yellow( " UPDATE Flow by POST REST" ) )
        response = self.post_REST_XML_repeat_request( self.conf_url_post, self.xml_input_stream )

        # -------------- DELETE -------------------
        self.log.info( self._paint_msg_yellow( " DELETE Flow by DELETE REST" ) )
        # Delte data from config DataStore
        response = self.delete_REST_XML_response( self.conf_url )
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Flow data has specific format and it has to be compared in different way
        response = self.get_REST_XML_response( self.oper_url )
        # find a correct flow-statistic (expect -> flow-statistic not exist)
        self.__validate_contain_flow( response, self.xml_input_DOM, False )


    def test_operations_POST( self ):
        self.log.info( "--- Flow operations sal-service test ---" )
        # -------------- CREATE -------------------
        self.log.info( self._paint_msg_yellow( " CREATE Flow by add-sal-service" ) )
        # send request via RESTCONF
        self.post_REST_XML_request( self.oper_url_add, self.oper_input_stream )
        # TODO : check no empty transaction_id from post add_service

        # check request content against restconf's config datastore
        # operation service don't change anything in a Config. Data Store
        # so we expect 404 response code (same as a check after delete
        self.get_REST_XML_deleted_response( self.conf_url )
        # check request content against restconf's operational datastore
        # operational Data Store has to present new flow, but with generated key
        response = self.get_REST_XML_response( self.oper_url_get )
        self.__validate_contain_flow( response, self.xml_input_DOM )

        # -------------- UPDATE -------------------
        self.log.info( self._paint_msg_yellow( " UPDATE Flow by update-sal-service" ) )
        xml_updated_stream = self.__update_flow_input();
        xml_updated_DOM = md.parseString( xml_updated_stream )
        data_from_updated_stream = ''
        for node in xml_updated_DOM.documentElement.childNodes:
            nodeKey = None if node.localName == None else ( node.localName ).encode( 'utf-8', 'ignore' )
            if ( nodeKey is None or nodeKey != 'id' ) :
                data_from_updated_stream += node.toxml( encoding = 'utf-8' )

        # The xml body without data - data come from file (all flow's subtags)
        oper_update_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                                '<input xmlns="urn:opendaylight:flow:service">\n' \
                                    '<original-flow>\n' \
                                        '%s' \
                                    '</original-flow>\n' \
                                    '<updated-flow>\n' \
                                        '%s' \
                                    '</updated-flow>\n' \
                                    '<node xmlns:inv="urn:opendaylight:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                                '</input>' % ( self.data_from_file_input, data_from_updated_stream )

        self.post_REST_XML_request( self.oper_url_upd, oper_update_stream )
        # TODO : check no empty transaction_id from post add_service

        # check request content against restconf's config datastore
        # operation service don't change anything in a Config. Data Store
        # so we expect 404 response code (same as a check after delete
        self.get_REST_XML_deleted_response( self.conf_url )
        # check request content against restconf's operational datastore
        # operational Data Store has to present updated flow
        response = self.get_REST_XML_response( self.oper_url_get )
        self.__validate_contain_flow( response, xml_updated_DOM )

        # -------------- DELETE -------------------
        self.log.info( self._paint_msg_yellow( " DELETE Flow by remove-sal-service" ) )
        # Delte data from config DataStore
        response = self.post_REST_XML_request( self.oper_url_del, self.oper_input_stream )
        # Data never been added, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Flow operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response( self.oper_url_get )
        self.__validate_contain_flow( response, self.xml_input_DOM, False )


# --------------- HELP METHODS ---------------


    def __validate_contain_flow( self, oper_resp, orig_DOM, exp_contain = True ):
        xml_resp_stream = ( oper_resp.text ).encode( 'utf-8', 'ignore' )
        xml_resp_DOM = md.parseString( xml_resp_stream )
        nodeListOperFlows = xml_resp_DOM.getElementsByTagName( 'flow-statistics' )
        origDict = XMLtoDictParserTools.parseDOM_ToDict( orig_DOM._get_documentElement(),
                                                        ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
        origDict['flow-statistics'] = origDict.pop( 'flow' )
        nodeDict = {}
        for node in nodeListOperFlows :
            if self.__is_wanted_flow( orig_DOM, node ) :
                nodeDict = XMLtoDictParserTools.parseDOM_ToDict( node,
                                                                ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
                break
        if exp_contain :
            if nodeDict != origDict :
                err_msg = '\n!!!!! Loaded operation statistics doesn\'t contain expected flow \n' \
                            ' expected:      %s\n found:         %s\n differences:   %s\n' \
                            '' % ( origDict, nodeDict, XMLtoDictParserTools.getDifferenceDict( origDict, nodeDict ) )
                self.log.error( self._paint_msg_red( err_msg ) )
                raise AssertionError( err_msg )
        else :
            if nodeDict == origDict :
                err_msg = '\n !!! Loaded operation statistics contains expected flow, delete fail \n' \
                            ' found:         %s\n' % ( nodeDict )
                self.log.error( self._paint_msg_red( err_msg ) )
                raise AssertionError( err_msg )


    # ID is not a response part from device so we have to check the correct identification
    def __is_wanted_flow( self, orig_flow_DOM, resp_flow_DOM ):
        identif_list = ['priority', 'cookie', 'match']
        for ident in identif_list :
            orig_ident_node = orig_flow_DOM.getElementsByTagName( ident )[0]
            resp_ident_node = resp_flow_DOM.getElementsByTagName( ident )[0]
            orig_ident_dict = XMLtoDictParserTools.parseDOM_ToDict( orig_ident_node,
                                                                    ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
            resp_ident_dict = XMLtoDictParserTools.parseDOM_ToDict( resp_ident_node,
                                                                    ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
            if ( orig_ident_dict != resp_ident_dict ) :
                return False
        return True



    def __update_flow_input( self ):
        # action only for yet
        xml_dom_input = md.parseString( self.xml_input_stream )
        actionList = xml_dom_input.getElementsByTagName( 'action' )
        if actionList is not None and len( actionList ) > 0 :
            action = actionList[0]
            for child in action.childNodes :
                if child.nodeType == Element.ELEMENT_NODE:
                    nodeKey = ( child.localName ).encode( 'utf-8', 'ignore' )
                    if nodeKey != 'order' :
                        if nodeKey != 'drop-action' :
                            new_act = child.ownerDocument.createElement( 'drop-action' )
                        else :
                            new_act = child.ownerDocument.createElement( 'dec-mpls-ttl' )
                        child.parentNode.replaceChild( new_act, child )
        return xml_dom_input.toxml( encoding = 'utf-8' )
