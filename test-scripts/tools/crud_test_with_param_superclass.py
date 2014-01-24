'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on May 18, 2014

@author: <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
'''
import logging
import requests
import sys
import time
import unittest

from tools.file_loader_tool import FileLoaderTools
from tools.xml_parser_tools import XMLtoDictParserTools
import xml.dom.minidom as md


class OF_CRUD_Test_Base( unittest.TestCase ):

    def __init__( self, methodName = 'testCRUD', path_to_xml = None ):
        """
        private constructor
            * logger initialization (Child Class Name + xml input file nr.
            * add a path for the xml input file to the local variable
        """
        super( OF_CRUD_Test_Base, self ).__init__( methodName )
        self.path_to_xml = path_to_xml
        self.log = logging.getLogger( '%s CRUD_test_xml_%04d - '
            % ( self.__class__.__name__ , ( int( self.path_to_xml[6:-4] ) ) ) )
        self.log.info( "\n ------- test has STARTED -------- \n" )

    def setUp( self ):
        """
        setting up the test
            * load the input file to the local variable 'xml_input_stream' like a string 
            * parse the input string as DOM Object and save it in local variable 'xml_input_tree'
            
            raise ValueEror when a path is None
        """
        if ( self.path_to_xml is not None ) :
            self.xml_input_stream = FileLoaderTools.load_file_to_string( self.path_to_xml )
            self.xml_input_tree = md.parseString( self.xml_input_stream )
        else :
            raise ValueError( "Path to XML is None" )


    def tearDown( self ):
        self.log.info( "\n ------- test has ENDED -------- \n\n\n" )


    def test_conf_PUT( self ):
        """
        test_conf_PUT - conf. PUT CRUD test here is only a mock implementation 
                   which has to be overwritten by the subclasses 
                   for a Flow, Meter, Group test suite
        
        always raise NotImplementedError
        """
        raise NotImplementedError( "Please Implement this method" )


    def test_conf_POST( self ):
        """
        test_conf_POST - conf. POST create test here is only a mock implementation 
                   which has to be overwritten by the subclasses 
                   for a Flow, Meter, Group test suite
        
        always raise NotImplementedError
        """
        raise NotImplementedError( "Please Implement this method" )


    def test_operations_POST( self ):
        """
        test_operations_POST - sal service CRUD test here is only a mock implementation
                   which has to be overwritten by the subclasses
                   for a Flow, Meter, Group test suite

        always raise NotImplementedError
        """
        raise NotImplementedError( "Please Implement this method" )


    def assertDataTrees( self, original_tree, response_tree ):
        """
        assertDataTrees - help method for assertion the two DOM data tree
                          e.g. the request xml and the config Data Store result
                          has to be same always
        """
        origDict = XMLtoDictParserTools.parseTreeToDict( original_tree._get_documentElement() )
        respDict = XMLtoDictParserTools.parseTreeToDict( response_tree._get_documentElement() )
        assert respDict == origDict, '\n !!! Uploaded and stored xml, are not the same\n' \
                ' uploaded:      %s\n stored:        %s\n differences:   %s\n' \
                '' % ( origDict, respDict, XMLtoDictParserTools.getDifferenceDict( origDict, respDict ) )


    def _get_auth( self ):
        return ( 'admin', 'admin' )


    def _get_xml_result_header( self ):
        return {'Accept': 'application/xml'}


    def _get_xml_request_result_header( self ):
        return {
            'Content-Type': 'application/xml',
            'Accept': 'application/xml',
        }


    def __log_request( self, url, data = None ):
        self.log.info( ' Running method is "%s"' % sys._getframe( 1 ).f_code.co_name )
        self.log.info( ' REQUEST is sending to URL : {0} '.format( url ) )
        if data is not None :
            self.log.debug( ' REQUEST data : \n\n%s\n' % data )
        else :
            self.log.debug( ' REQUEST data: None \n' )


    def __log_response( self, response ):
        self.log.info( ' Running method is "%s" ' % sys._getframe( 1 ).f_code.co_name )
        self.log.info( ' RECEIVED status code: {0} '.format( response.status_code ) )
        self.log.debug( ' RECEIVED data : \n\n%s\n' % response.content )


    def put_REST_XML_conf_request( self, put_config_url, config_data ):
        '''
        Method uses REST interface for PUT a request data to device from the input URL
        '''
        self.__log_request( put_config_url, config_data )
        response = requests.put( put_config_url,
                                 data = config_data,
                                 auth = self._get_auth(),
                                 headers = self._get_xml_request_result_header() )
        self.__log_response( response )
        assert response.status_code == 204 or response.status_code == 200, '\n !!! %s Status code returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
        time.sleep( self.CONTROLLER_DELAY )
        return response


    def get_REST_XML_response( self, get_url ):
        '''
        Method uses REST interface to GET a response from the input URL
            * call GET REST method
            * validate an expectation that the data is exist
        '''
        self.__log_request( get_url )
        time.sleep( self.CONTROLLER_OPERATION_DELAY )
        response = requests.get( get_url,
                                auth = self._get_auth(),
                                headers = self._get_xml_result_header() )
        self.__log_response( response )
        assert response.status_code == 200, '\n !!! %s Expected status code 200, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
        time.sleep( self.CONTROLLER_DELAY )
        return response


    def get_REST_XML_deleted_response( self, get_url ):
        '''
        Method uses REST interface to GET data for input URL
            * call GET REST method
            * validate an expectation that the data is not exist 
        '''
        self.__log_request( get_url )
        response = requests.get( get_url,
                                auth = self._get_auth(),
                                headers = self._get_xml_result_header() )
        self.__log_response( response )
        assert response.status_code == 404, '\n !!! %s Expected status code 404, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
        time.sleep( self.CONTROLLER_DELAY )
        return response


    def delete_REST_XML_response( self, delete_url ):
        self.__log_request( delete_url )
        response = requests.delete( delete_url,
                                   auth = self._get_auth(),
                                   headers = self._get_xml_result_header() )
        self.__log_response( response )
        assert response.status_code == 200, '\n !!! %s Expected status code 200, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
        time.sleep( self.CONTROLLER_DELAY )
        return response


    def post_REST_XML_request( self, post_url, post_data ):
        self.__log_request( post_url, post_data )
        response = requests.post( post_url,
                                  data = post_data,
                                  auth = self._get_auth(),
                                  headers = self._get_xml_request_result_header() )
        self.__log_response( response )
        assert response.status_code == 204 or response.status_code == 200, '\n !!! %s Status code returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
        time.sleep( self.CONTROLLER_DELAY )
        return response


    def post_REST_XML_repeat_request( self, post_url, post_data ):
        self.__log_request( post_url, post_data )
        response = requests.post( post_url,
                                  data = post_data,
                                  auth = self._get_auth(),
                                  headers = self._get_xml_request_result_header() )
        self.__log_response( response )
        assert response.status_code == 409, '\n !!! %s Expected status code 409, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
        time.sleep( self.CONTROLLER_DELAY )
        return response
