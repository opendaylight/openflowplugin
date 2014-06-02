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


class ColorEnum ( object ):
    '''
    Color Enum class for coloring log output
    '''
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE = range( 8 )

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


    def setUp( self ):
        """
        setting up the test
            * load the input file to the local variable 'xml_input_stream' like a string 
            * parse the input string as DOM Object and save it in local variable 'xml_input_DOM'
            
        @raise ValueError: raise ValueEror when a path is None
        """
        if ( self.path_to_xml is not None ) :
            self.xml_input_stream = FileLoaderTools.load_file_to_string( self.path_to_xml )
            self.xml_input_DOM = md.parseString( self.xml_input_stream )
        else :
            raise ValueError( "Path to XML is None" )


    def tearDown( self ):
        self.log.info( "\n ------- test has ENDED -------- \n\n\n" )


    def test_conf_PUT( self ):
        """
        test_conf_PUT - conf. PUT CRUD test here is only a mock implementation 
                   which has to be overwritten by the subclasses 
                   for a Flow, Meter, Group test suite
        
        @raise NotImplementedError: always raise NotImplementedError
        """
        raise NotImplementedError( "Please Implement this method" )


    def test_conf_POST( self ):
        """
        test_conf_POST - conf. POST create test here is only a mock implementation 
                   which has to be overwritten by the subclasses 
                   for a Flow, Meter, Group test suite
        
        @raise NotImplementedError: always raise NotImplementedError
        """
        raise NotImplementedError( "Please Implement this method" )


    def test_operations_POST( self ):
        """
        test_operations_POST - sal service CRUD test here is only a mock implementation
                   which has to be overwritten by the subclasses
                   for a Flow, Meter, Group test suite

        @raise NotImplementedError: always raise NotImplementedError
        """
        raise NotImplementedError( "Please Implement this method" )


    # --------- Response Helper  --------------


    def returnReverseInputTest( self, text ):
        return ''.join( [text[len( text ) - count] for count in xrange( 1, len( text ) + 1 )] )


    def assertDataDOM( self, orig_DOM_Doc, resp_DOM_Doc ):
        """
        assertDataDOM - help method for assertion the two DOM data representation
                          e.g. the request xml and the config Data Store result
                          has to be same always
        @param orig_DOM_Doc: DOM Document sends to controller (e.g. from file input)
        @param resp_DOM_Doc: DOM Document returns from response 
        @raise AssertionError: if response has not the expected 404 code
        """
        origDict = XMLtoDictParserTools.parseDOM_ToDict( orig_DOM_Doc._get_documentElement() )
        respDict = XMLtoDictParserTools.parseDOM_ToDict( resp_DOM_Doc._get_documentElement() )
        if ( respDict != origDict ) :
            err_msg = '\n !!! Uploaded and stored xml, are not the same\n' \
                ' uploaded:      %s\n stored:        %s\n differences:   %s\n' \
                '' % ( origDict, respDict, XMLtoDictParserTools.getDifferenceDict( origDict, respDict ) )
            self.log.error( self._paint_msg_red( err_msg ) )
            raise AssertionError( err_msg )


    def _get_auth( self ):
        return ( 'admin', 'admin' )


    def _get_xml_result_header( self ):
        return {'Accept': 'application/xml'}


    def _get_xml_request_result_header( self ):
        return {
            'Content-Type': 'application/xml',
            'Accept': 'application/xml',
        }


    def put_REST_XML_conf_request( self, put_config_url, config_data ):
        '''
        Method uses REST interface for PUT a request data to device from the input URL
            * call PUT REST operation
            * validate response status code to 204 or 200
        @param url: URL to controller configuration DataStore for PUT method
        @return: response from controller (expected code 204 or 200 OK)
        @raise AssertionError: if response code is not 204 or 200 
        '''
        self.__log_request( put_config_url, config_data )
        response = requests.put( put_config_url,
                                 data = config_data,
                                 auth = self._get_auth(),
                                 headers = self._get_xml_request_result_header() )
        self.__log_response( response )
        if ( response.status_code != 204 and response.status_code != 200 ) :
            err_msg = '\n !!! %s Status code returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
            self.log.error( self._paint_msg_red( err_msg ) )
            raise AssertionError( err_msg )

        self.__time_wait_conf()
        return response


    def get_REST_XML_response( self, get_url ):
        '''
        Method uses REST interface to GET a response from the input URL
            * call GET REST operation
            * validate an expectation that the data is exist
        @param get_url: URL for GETing the node data
        @return: response from controller (expected code 200 + data in payload)
        @raise AssertionError: if response code is not 200
        '''
        self.__log_request( get_url )
        self.__time_wait_oper()
        response = requests.get( get_url,
                                auth = self._get_auth(),
                                headers = self._get_xml_result_header() )
        self.__log_response( response )
        if response.status_code != 200 :
            err_msg = '\n !!! %s Expected status code 200, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
            self.log.error( self._paint_msg_red( err_msg ) )
            raise AssertionError( err_msg )

        self.__time_wait_conf()
        return response


    def get_REST_XML_deleted_response( self, get_url ):
        '''
        Method uses REST interface to GET the deleted data for input URL
            * call GET REST operation
            * validate an expectation that the data is not exist 
        @param get_url: URL - define the deleted node
        @return: response from controller (expect 404 Not Found - No data exists.)
        @raise AssertionError: if response has not the expected 404 code
        '''
        self.__log_request( get_url )
        response = requests.get( get_url,
                                auth = self._get_auth(),
                                headers = self._get_xml_result_header() )
        self.__log_response( response )
        if response.status_code != 404 :
            err_msg = '\n !!! %s Expected status code 404, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
            self.log.error( self._paint_msg_red( err_msg ) )
            raise AssertionError( err_msg )

        self.__time_wait_conf()
        return response


    def delete_REST_XML_response( self, delete_url ):
        '''
        Method uses REST DELETE operation for node data on input URL
            * call DELETE REST operation
            * validate response code 200 (deleted successful)
        @param delete_url: URL - define the data node for delete process
        @return: response from controller (expect 200 OK)
        @raise AssertionError: if response has not the expected 200 code 
        '''
        self.__log_request( delete_url )
        response = requests.delete( delete_url,
                                   auth = self._get_auth(),
                                   headers = self._get_xml_result_header() )
        self.__log_response( response )
        if response.status_code != 200 :
            err_msg = '\n !!! %s Expected status code 200, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
            self.log.error( self._paint_msg_red( err_msg ) )
            raise AssertionError( err_msg )

        self.__time_wait_conf()
        return response


    def post_REST_XML_request( self, post_url, post_data ):
        '''
        Method uses REST POST operation for node data on input URL
            * call POST operation with input data
            * validate response code 200 or 204 
        @param post_url: URL - define the data node or sal-service operation
        @return: response from controller (response code expect 200 or 204)
        @raise AssertionError: if response has not the expected 200 or 204 code
        '''
        self.__log_request( post_url, post_data )
        response = requests.post( post_url,
                                  data = post_data,
                                  auth = self._get_auth(),
                                  headers = self._get_xml_request_result_header() )
        self.__log_response( response )
        if response.status_code != 204 and response.status_code != 200 :
            err_msg = '\n !!! %s Status code returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
            self.log.error( err_msg )
            raise AssertionError( err_msg )

        self.__time_wait_conf()
        return response


    def post_REST_XML_repeat_request( self, post_url, post_data ):
        '''
        Method uses REST POST operation for add input data on node 
        specify by URL, but we expect to have the data, so service 
        has to return 409 exception code
            * call POST operation with input data
            * validate response code 409 Conflict (data exist)
        @param post_url: URL - define the data node
        @return: response from controller (expected Conflict -> Data Exist)
        @raise AssertionError: if response has not the expected 409 Conflict code  
        '''
        self.__log_request( post_url, post_data )
        response = requests.post( post_url,
                                  data = post_data,
                                  auth = self._get_auth(),
                                  headers = self._get_xml_request_result_header() )
        self.__log_response( response )
        if response.status_code != 409 :
            err_msg = '\n !!! %s Expected status code 409, but returned %d \n' % ( sys._getframe( 1 ).f_code.co_name, response.status_code )
            self.log.error( self._paint_msg_red( err_msg ) )
            raise AssertionError( err_msg )

        self.__time_wait_conf()
        return response


    # --------- LOGGING HELP METHODS --------
    def __log_request( self, url, data = None ):
        self.log.info( ' Running method is "%s"' % sys._getframe( 1 ).f_code.co_name )
        self.log.info( ' REQUEST is sending to URL : {0} '.format( self._paint_msg_blue( url ) ) )
        if data is not None :
            self.log.debug( ' REQUEST data : \n\n%s\n' % ( self._paint_msg_green( data ) ) )
        else :
            self.log.debug( ' REQUEST data: %s \n' % ( self._paint_msg_green( 'None' ) ) )


    def __log_response( self, response ):
        self.log.info( ' Running method is "%s" ' % sys._getframe( 1 ).f_code.co_name )
        self.log.info( ' RECEIVED status code: {0} '.format( self._paint_msg_magenta( response.status_code ) ) )
        self.log.debug( ' RECEIVED data : \n\n%s\n' % self._paint_msg_green( response.content ) )


    def __time_wait_oper( self ):
        self.log.info( '......... Waiting for operational DataStore %s sec. ......... ' % self.CONTROLLER_OPERATION_DELAY )
        time.sleep( self.CONTROLLER_OPERATION_DELAY )


    def __time_wait_conf( self ):
        self.log.info( '......... Waiting for controller %s sec. ......... ' % self.CONTROLLER_DELAY )
        time.sleep( self.CONTROLLER_DELAY )


    def _paint_msg_green( self, msg ):
        return self.__paint_msg( msg, 2 )


    def _paint_msg_blue ( self, msg ):
        return self.__paint_msg( msg, ColorEnum.BLUE )


    def _paint_msg_magenta ( self, msg ):
        return self.__paint_msg( msg, ColorEnum.MAGENTA )


    def _paint_msg_cyan ( self, msg ):
        return self.__paint_msg( msg, ColorEnum.CYAN )


    def _paint_msg_red ( self, msg ):
        return self.__paint_msg( msg, ColorEnum.RED )


    def _paint_msg_yellow( self, msg ):
        return self.__paint_msg( msg, ColorEnum.YELLOW )


    def __paint_msg ( self, msg, colorNr ):
        if ( self.COLORING == 1 ) :
            color = '\x1b[3%dm' % colorNr
            return '%s %s %s' % ( color, msg, '\x1b[0m' )
        else :
            return msg
