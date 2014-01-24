'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on May 11, 2014

@author: <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
'''
import requests
from setuptools.tests.doctest import UnexpectedException

from openvswitch.parser_tools import ParseTools
from tools.crud_test_with_param_superclass import OF_CRUD_Test_Base
from tools.xml_parser_tools import XMLtoDictParserTools
import xml.dom.minidom as md


GROUP_ID_TAG_NAME = 'group-id'
# TODO : fix groups ignored tags
IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON = ['group-name', 'barrier', 'weight']

class OF_CRUD_Test_Groups( OF_CRUD_Test_Base ):


    def setUp( self ):
        super( OF_CRUD_Test_Groups, self ).setUp()
        # ----- PUT -----
        ids = ParseTools.get_values( self.xml_input_tree, GROUP_ID_TAG_NAME )
        data = ( self.host, self.port, ids[GROUP_ID_TAG_NAME] )
        self.conf_url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/group/%s' % data
        self.oper_url = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
              '/node/openflow:1/group/%s' % data
        # ----- POST -----
        data = ( self.host, self.port )
        self.conf_url_post = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/' % data
        # ----- SAL SERVICE OPERATIONS -----
        self.oper_url_add = 'http://%s:%d/restconf/operations/sal-group:add-group' % data
        self.oper_url_upd = 'http://%s:%d/restconf/operations/sal-group:update-group' % data
        self.oper_delete_url = 'http://%s:%d/restconf/operations/sal-group:remove-group' % data
        # Modify input data
        data_from_file_input = ''
        for node in self.xml_input_tree.documentElement.childNodes :
            data_from_file_input += node.toxml( encoding = 'utf-8' )

        # The xml body without data - data come from file (all meter subtags)
        self.oper_input_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                                    '<input xmlns="urn:opendaylight:meter:service">\n' \
                                        '%s' \
                                        '<node xmlns:inv="urn:opendaylight:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                                    '</input>' % data_from_file_input
        # Modify input data for delete
        data_from_file_input = ''
        for node in self.xml_input_tree.documentElement.childNodes :
            nodeKey = None if node.localName == None else ( node.localName ).encode( 'utf-8', 'ignore' )
            if ( nodeKey is None or nodeKey != 'buckets' ) :
                data_from_file_input += node.toxml( encoding = 'utf-8' )

        # The xml body without data - data come from file (all group subtags)
        self.oper_delete_input_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                                        '<input xmlns="urn:opendaylight:group:service">\n' \
                                            '%s' \
                                            '<node xmlns:inv="urn:opendaylight:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                                        '</input>' % data_from_file_input


    def tearDown( self ) :
        # cleaning configuration DataStore and device without a response validation
        requests.delete( self.conf_url, auth = self._get_auth(),
                         headers = self._get_xml_result_header() )
        requests.post( self.oper_delete_url, data = self.oper_delete_input_stream,
                       auth = self._get_auth(), headers = self._get_xml_request_result_header() )
        super( OF_CRUD_Test_Groups, self ).tearDown()


    def test_conf_PUT( self ):
        self.log.info( "--- Group conf. PUT test ---" )
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
        self.__validate_contain_group( response, self.xml_input_tree )

        # -------------- UPDATE -------------------
        # TODO: add update

        # -------------- DELETE -------------------
        # Delte data from config DataStore
        response = self.post_REST_XML_request( self.oper_delete_url, self.oper_delete_input_stream )
        response = self.delete_REST_XML_response( self.conf_url )
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Group operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_group( response, self.xml_input_tree, False )


    def test_conf_POST( self ):
        self.log.info( "--- Group conf. POST test ---" )
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
        self.__validate_contain_group( response, self.xml_input_tree )
        # test error for double create (POST could create data only)
        response = self.post_REST_XML_repeat_request( self.conf_url_post, self.xml_input_stream )

        # Delte data from config DataStore
        response = self.post_REST_XML_request( self.oper_delete_url, self.oper_delete_input_stream )
        response = self.delete_REST_XML_response( self.conf_url )
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Group operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response( self.oper_url_get )
        self.__validate_contain_group( response, self.xml_input_tree, False )


    # sal-group services
    def test_operations_POST( self ):
        self.log.info( "--- Group operations sal-service test ---" )
        # -------------- CREATE -------------------
        # send request via RESTCONF
        self.post_REST_XML_request( self.oper_url_add, self.oper_input_stream )
        # TODO : check no empty transaction_id from post add_service

        # check request content against restconf's config datastore
        # operation service don't change anything in a Config. Data Store
        # so we expect 404 response code (same as a check after delete
        self.get_REST_XML_deleted_response( self.conf_url )
        # check request content against restconf's operational datastore
        # operational Data Store has to present new group
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_group( response, self.xml_input_tree )

        # -------------- UPDATE -------------------
        # TODO: add update

        # -------------- DELETE -------------------
        # Delte data from config DataStore
        response = self.post_REST_XML_request( self.oper_delete_url, self.__get_oper_delete_input_stream() )
        # Data never been added, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response( self.conf_url )
        # Group operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response( self.oper_url )
        self.__validate_contain_group( response, self.xml_input_tree, False )


# --------------- HELP METHODS ---------------


    def __validate_contain_group( self, oper_resp, orig_tree, exp_contain = True ):
        xml_resp_stream = ( oper_resp.text ).encode( 'utf-8', 'ignore' )
        xml_resp_tree = md.parseString( xml_resp_stream )
        nodeListOperGroup = xml_resp_tree.getElementsByTagName( 'group-desc' )
        if ( nodeListOperGroup.length > 1 ) :
            raise UnexpectedException( '\n !!! Operational Data Store has more groups as one \n' )

        origDict = XMLtoDictParserTools.parseTreeToDict( orig_tree._get_documentElement(),
                                                        ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
        origDict['group-desc'] = origDict.pop( 'group' )
        nodeDict = {}
        for node in nodeListOperGroup :
            nodeDict = XMLtoDictParserTools.parseTreeToDict( node,
                                                            ignoreList = IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON )
        if exp_contain :
            assert nodeDict == origDict, '\n !!! Loaded operation statistics doesn\'t contain expected group \n' \
                ' expected:      %s\n found:         %s\n differences:   %s\n' \
                '' % ( origDict, nodeDict, XMLtoDictParserTools.getDifferenceDict( origDict, nodeDict ) )
        else :
            assert nodeDict != origDict, '\n !!! Loaded operation statistics contains expected group \n' \
                ' found:         %s\n' % ( nodeDict )
