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
        ids = ParseTools.get_values( self.xml_input_tree, TABLE_ID_TAG_NAME, FLOW_ID_TAG_NAME )
        data = ( self.host, self.port, ids[TABLE_ID_TAG_NAME], ids[FLOW_ID_TAG_NAME] )
        self.conf_url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s/flow/%s' % data
        data = ( self.host, self.port, ids[TABLE_ID_TAG_NAME] )
        self.conf_url_post = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s' % data
        self.oper_url = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s' % data
        # ---- operations ---
        data = ( self.host, self.port )
        self.oper_url_add = 'http://%s:%d/restconf/operations/sal-flow:add-flow' % data
        self.oper_url_upd = 'http://%s:%d/restconf/operations/sal-flow:update-flow' % data
        self.oper_url_del = 'http://%s:%d/restconf/operations/sal-flow:remove-flow' % data
        # Modify input operations data
        data_from_file_input = ''
        for node in self.xml_input_tree.documentElement.childNodes:
            nodeKey = None if node.localName == None else ( node.localName ).encode( 'utf-8', 'ignore' )
            if ( nodeKey is None or nodeKey != 'id' ) :
                data_from_file_input += node.toxml( encoding = 'utf-8' )

        # The xml body without data - data come from file (all flow's subtags)
        self.oper_input_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                                    '<input xmlns="urn:opendaylight:flow:service">\n' \
                                        '%s' \
                                        '<node xmlns:inv="urn:opendaylight:inventory" xmlns:finv="urn:opendaylight:flow:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                                    '</input>' % data_from_file_input


    def tearDown( self ) :
        # # cleaning configuration DataStore and device without a response validation
        requests.delete( self.conf_url, auth = self._get_auth(),
                               headers = self._get_xml_result_header() )
        requests.post( self.oper_url_del, data = self.oper_input_stream, auth = self._get_auth(),
                              headers = self._get_xml_request_result_header() )
        super( OF_CRUD_Test_Flows, self ).tearDown()


    def test_conf_PUT( self ):
        self.log.info( "--- Flow conf. PUT test ---" )
        # -------------- CREATE -------------------
        # send request via RESTCONF
        self.put_REST_XML_conf_request( self.conf_url, self.xml_input_stream )
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response( self.conf_url )
        xml_resp_stream = ( response.text ).encode( 'utf-8', 'ignore' )
        xml_resp_tree = md.parseString( xml_resp_stream )
        self.assertDataTrees( self.xml_input_tree, xml_resp_tree )
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, self.xml_input_tree )

        # -------------- UPDATE -------------------
        xml_updated_stream = self.__update_flow_input();
        self.put_REST_XML_conf_request( self.conf_url, xml_updated_stream )
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response( self.conf_url )
        xml_resp_stream = ( response.text ).encode( 'utf-8', 'ignore' )
        xml_resp_tree = md.parseString( xml_resp_stream )
        xml_upd_tree = md.parseString( xml_updated_stream )
        self.assertDataTrees( xml_upd_tree, xml_resp_tree )
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, xml_upd_tree )

        # -------------- DELETE -------------------
        # Delte data from config DataStore
        response = self.delete_REST_XML_response( self.conf_url )
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Flow data has specific format and it has to be compared in different way
        response = self.get_REST_XML_response( super.oper_url )
        # find a correct flow-statistic (expect -> flow-statistic not exist)
        self.__validate_contain_flow( response, self.xml_input_tree, False )


    def test_conf_POST( self ):
        self.log.info( "--- Flow conf. POST test ---" )
        # -------------- CREATE -------------------
        # send request via RESTCONF
        self.post_REST_XML_request( self.conf_url_post, self.xml_input_stream )
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response( self.conf_url )
        xml_resp_stream = ( response.text ).encode( 'utf-8', 'ignore' )
        xml_resp_tree = md.parseString( xml_resp_stream )
        self.assertDataTrees( self.xml_input_tree, xml_resp_tree )
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, self.xml_input_tree )
        # test error for double create (POST could create data only)
        response = self.post_REST_XML_repeat_request( self.conf_url_post, self.xml_input_stream )

        # -------------- DELETE -------------------
        # Delte data from config DataStore
        response = self.delete_REST_XML_response( self.conf_url )
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Flow data has specific format and it has to be compared in different way
        response = self.get_REST_XML_response( self.oper_url )
        # find a correct flow-statistic (expect -> flow-statistic not exist)
        self.__validate_contain_flow( response, self.xml_input_tree, False )


    def test_operations_POST( self ):
        self.log.info( "--- Flow operations sal-service test ---" )
        # -------------- CREATE -------------------
        # send request via RESTCONF
        self.post_REST_XML_request( self.oper_url_add, self.oper_input_stream )
        # TODO : check no empty transaction_id from post add_service

        # check request content against restconf's config datastore
        # operation service don't change anything in a Config. Data Store
        # so we expect 404 response code (same as a check after delete
        self.get_REST_XML_deleted_response( self.conf_url )
        # check request content against restconf's operational datastore
        # operational Data Store has to present new flow
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, self.xml_input_tree )

        # -------------- UPDATE -------------------
        # TODO: add update

        # -------------- DELETE -------------------
        # Delte data from config DataStore
        response = self.post_REST_XML_request( self.oper_url_del, self.oper_input_stream )
        # Data never been added, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Flow operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_flow( response, self.xml_input_tree, False )


# --------------- HELP METHODS ---------------


    def __validate_contain_flow( self, oper_resp, orig_tree, exp_contain = True ):
        containIt = False
        xml_resp_stream = ( oper_resp.text ).encode( 'utf-8', 'ignore' )
        xml_resp_tree = md.parseString( xml_resp_stream )
        nodeListOperFlows = xml_resp_tree.getElementsByTagName( 'flow-statistics' )
        origDict = XMLtoDictParserTools.parseTreeToDict( orig_tree._get_documentElement(),
                                                        ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
        origDict['flow-statistics'] = origDict.pop( 'flow' )
        reportDict = {}
        index = 0;
        for node in nodeListOperFlows :
            nodeDict = XMLtoDictParserTools.parseTreeToDict( node,
                                                            ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
            XMLtoDictParserTools.addDictValue( reportDict, index, nodeDict )
            index += 1
            if nodeDict == origDict :
                containIt = True
                break
        # TODO try to focus for comparison the unique keys tags (priority, match, cookies)- > after we are able
        # to say -> that one is the correct one
        if exp_contain :
            assert True == containIt, 'loaded operation statistics doesn\'t contain expected flow \n' \
                ' expected:      %s\n found:         %s\n' \
                '' % ( origDict, reportDict )
        else :
            assert False == containIt, 'loaded operation statistics contains expected flow \n' \
                ' found:         %s\n' \
                '' % ( nodeDict )


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



