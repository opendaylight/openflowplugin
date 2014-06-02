'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on May 11, 2014

@author: <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
'''
import argparse
import logging
import os
import sys
import unittest

from crud.odl_flow_test import OF_CRUD_Test_Flows
from crud.odl_group_tests import OF_CRUD_Test_Groups
from crud.odl_meter_test import OF_CRUD_Test_Meters
from tools.crud_test_with_param_superclass import OF_CRUD_Test_Base
from tools.test_with_param_superclass import OF_TestXmlInputs_Base


# The input values for a mininet argument
class Input_MininetSwitchEnum( object ) :
    OVS = 1
    CPqD = 2


def __returnFiles( test_nr = None, prefix_letter = None, path = 'xmls' ):
    if prefix_letter is None :
        raise ValueError( "Missing the test file input prefix" )

    if test_nr is not None :
        xml = map( int, test_nr.split( ',' ) )
    else :
        xml = None

    xmlfiles = None
    if xml is not None :
        xmlfiles = ( "%s%d.xml" % ( prefix_letter, flowId ) for flowId in xml if flowId != 0 )
    else :
        xmlfiles = ( xmlFile for xmlFile in os.listdir( path )
                    if ( xmlFile.startswith( prefix_letter ) & xmlFile.endswith( '.xml' ) ) )

    return xmlfiles


# Test Suite Builder
def suite( path = 'xmls' ):
    suite = unittest.TestSuite()
    # load the flow tests for every input file
    flowXmlFiles = __returnFiles( in_args.fxmls, 'f', path )
    for flow_input_file in flowXmlFiles :
        # OF_CRUD_Test_Flows
        suite.addTest( OF_TestXmlInputs_Base.load_file_name( OF_CRUD_Test_Flows, path_to_xml = os.path.join( path, flow_input_file ) ) )
    # groups and meters are not implemented by OVS yet
    if ( in_args.mininet == Input_MininetSwitchEnum.CPqD ) :
        # load the meter tests for every input file
        meterXmlFiles = __returnFiles( in_args.mxmls, 'm', path )
        for meter_input_file in meterXmlFiles :
            suite.addTest( OF_TestXmlInputs_Base.load_file_name( OF_CRUD_Test_Meters, path_to_xml = os.path.join( path, meter_input_file ) ) )
        # load the group tests for every input file
        groupXmlFiles = __returnFiles( in_args.gxmls, 'g', path )
        for group_ipnut_file in groupXmlFiles :
            suite.addTest( OF_TestXmlInputs_Base.load_file_name( OF_CRUD_Test_Groups, path_to_xml = os.path.join( path, group_ipnut_file ) ) )
    # returns the completed test suite
    return suite


if __name__ == "__main__":
    # logging level dict
    log_level_dict = {1 : logging.DEBUG, 2 : logging.INFO, 3 : logging.ERROR}
    # parse cmdline arguments
    parser = argparse.ArgumentParser( description = 'ODL CRUD tests defined by the xml inputs.' )

    parser.add_argument( '--odlhost', default = '127.0.0.1', help = 'host where '
                        'odl controller is running  (default = 127.0.0.1) ' )
    parser.add_argument( '--odlport', type = int, default = 8080, help = 'port on '
                        'which odl\'s RESTCONF is listening  (default = 8080) ' )
    parser.add_argument( '--loglev', type = int, default = 1, help = 'logging Level '
                        'definition (DEBUG = 1 | INFO = 2 | ERROR = 3 )  (default = 1)' )
    parser.add_argument( '--mininet', type = int, default = 1, help = 'mininet can be implemented '
                        'by OVS = 1 or by CPqD = 2  (default = 1)' )
    parser.add_argument( '--fxmls', default = None, help = 'generate the Flow tests for the selected xml from xmls DIR '
                        'only (i.e. 1,3,34), 0 means no test and None means all tests  (default = None)' )
    parser.add_argument( '--mxmls', default = None, help = 'generate the Meter tests for the selected xml from xmls DIR '
                        'only (i.e. 1,3,34), 0 means no test and None means all tests  (default = None)' )
    parser.add_argument( '--gxmls', default = None, help = 'generate the Group tests for the selected xml from xmls DIR '
                        'only (i.e. 1,3,34), 0 means no test and None means all tests  (default = None)' )
    parser.add_argument( '--confresp', type = int, default = 0, help = 'delay to the Configuration Data Store ' \
                         '(default = 0 second) Increase this value is important for a weaker controller machine' )
    parser.add_argument( '--operresp', type = int, default = 3, help = 'delay to the Operational Data Store ' \
                         '(default = 3 second) Increase this value is important for a weaker controller machine' )
    parser.add_argument( '--coloring', type = int, default = 0, help = 'coloring output '
                        'definition (coloring enabled = 1 | coloring disabled = 0 )  (default = 0)' )

    in_args = parser.parse_args()

    # check python version
    current_ver = sys.version_info
    if current_ver[0] != 2 or current_ver[1] != 6 :
        print "Python in ver. 2.6 is required !"
    else :
        # set up logging
        logging.basicConfig( level = log_level_dict[in_args.loglev],
                             filename = 'crud_test.log',
#                              format = "%(asctime)s %(levelname)s %(message)s",
                             format = '[%(asctime)s] {%(pathname)s:%(lineno)d} %(levelname)s - %(message)s',
                             datefmt = '%Y-%m-%d %H:%M:%S' )

        # set the host and the port input values for ODL controller to the test suite
        OF_CRUD_Test_Base.port = in_args.odlport
        OF_CRUD_Test_Base.host = in_args.odlhost
        OF_CRUD_Test_Base.mininet = in_args.mininet
        OF_CRUD_Test_Base.CONTROLLER_DELAY = in_args.confresp
        OF_CRUD_Test_Base.CONTROLLER_OPERATION_DELAY = in_args.operresp
        OF_CRUD_Test_Base.COLORING = in_args.coloring

        # set console logger
        console = logging.StreamHandler()
        console.setLevel( log_level_dict[in_args.loglev] )
        formatter = logging.Formatter( "%(asctime)s : %(levelname)s  -  %(message)s", "%H:%M:%S" )
        console.setFormatter( formatter )
        logging.getLogger( '' ).addHandler( console )

        # TODO print input values
#         print 'CRUD test'

        odl_crud_suite = suite()
        del sys.argv[1:]
        unittest.TextTestRunner().run( odl_crud_suite )
