import argparse
import logging
import os
import requests
import sys
import time
import unittest
from xml.etree import ElementTree as ET
import xmltodict

from openvswitch.mininet_tools import MininetTools
from openvswitch.parser_tools import ParseTools
from openvswitch.xml_validator import XMLValidator
from tools.file_loader_tool import FileLoaderTools
from tools.test_with_param_superclass import OF_TestXmlInputs_Base
import xml.dom.minidom as md


# Delay time value is important for slow machines 
# value mean nr. of seconds for waiting for controller 
CONTROLLER_DELAY = 0
# value mean nr. of seconds for waiting for mininet 
MININET_START_DELAY = 15


class TestOpenFlowXml(OF_TestXmlInputs_Base):        
        
    def test_xml(self):
        test_number = int(self.path_to_xml[6:-4])
        log = logging.getLogger('test_xml_%04d' %test_number)
        
        xml_string = FileLoaderTools.load_file_to_string(self.path_to_xml)
        tree = md.parseString(xml_string)
        
        #TODO add create switch_etalon for the loaded XML with input xml validation
        
        
        switch_etalon = ParseTools.get_switchflow_from_xml(xml_string, 
                                                           key_dict = keywords, 
                                                           action_key_dict = action_keywords, 
                                                           match_key_dict = match_keywords,
                                                           ignore_key_dict = ignore_keywords)
        
        
        ids = ParseTools.get_values(tree.documentElement, 'table_id', 'id')
        data = (self.host, self.port, ids['table_id'], ids['id'])
        url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s/flow/%s' % data
        # send request via RESTCONF
        headers = {
            'Content-Type': 'application/xml',
            'Accept': 'application/xml',
        }
        log.info('sending request to url: {0}'.format(url))
        
        rsp = requests.put(url, data=xml_string, auth=('admin','admin'),
                           headers=headers)
        
        try:
            log.info('received status code: {0}'.format(rsp.status_code))
            log.debug('received content: {0}'.format(rsp.text))
            assert rsp.status_code == 204 or rsp.status_code == 200, 'Status code returned %d' %rsp.status_code
            time.sleep(CONTROLLER_DELAY)
        
            # check request content against restconf's datastore
            response = requests.get(url, auth=('admin', 'admin'),
                                    headers={'Accept': 'application/xml'})
            assert response.status_code == 200
            req = (xmltodict.parse(ET.tostring(ET.fromstring(xml_string))))
            res = (xmltodict.parse(ET.tostring(ET.fromstring(response.text))))
            assert req == res, 'uploaded and stored xml, are not the same\n' \
                'uploaded: %s\nstored:%s' % (req, res)
    
            # collect flow table state on switch
            switch_flows = MininetTools.get_dict_of_flows(net, ignore_keywords)
            
            assert len(switch_flows) > 0
            assert switch_etalon == switch_flows, 'expected and stored switch settings are not the same \n'\
                'expected: %s\nstored: %s' %(switch_etalon,switch_flows)

        except Exception, e :
            log.error(e)
            print '\n'
            raise e

        finally:
            response = requests.delete(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
            assert response.status_code == 200
            print '\n\n'
            time.sleep(CONTROLLER_DELAY)

def suite(path='xmls', base_test_class='OF_TestXmlInputs_Base') :
    suite = unittest.TestSuite()
    if in_args.xmls is not None:
        xmls = map(int, in_args.xmls.split(','))
    else :
        xmls = None
    
    xmlfiles = None
    if xmls is not None:
        xmlfiles = ('f%d.xml' % fid for fid in xmls)
    else:
        xmlfiles = (xml for xml in os.listdir(path) 
                    if (xmls.startswith('f') & xmls.endswith('.xml')))

    #create xml validator
#     validator = XMLValidator(keywords, action_keywords, match_keywords, logging.ERROR)
#     validator.fill_fields()

    for xmlfile in xmlfiles:
        #fill validator with data from xml and validate them - just logging to hint what can be wrong, test wont be stopped by invalid xml
#         validator.create_dictionaries(os.path.join(path, xmlfile))
#         validator.validate()

        suite.addTest(OF_TestXmlInputs_Base.load_file_name(base_test_class, path_to_xml=os.path.join(path, xmlfile)))
    return suite


if __name__ == '__main__':
    # set up logging
    logging.basicConfig(level=logging.DEBUG)

    # parse cmdline arguments
    parser = argparse.ArgumentParser(description='Run switch <-> ODL tests '
                                     'defined by xmls.')
    parser.add_argument('--odlhost', default='127.0.0.1', help='host where '
                        'odl controller is running  (default = 127.0.0.1) ')
    parser.add_argument('--odlport', type=int, default=8080, help='port on '
                        'which odl\'s RESTCONF is listening  (default = 8080) ')
    parser.add_argument('--mnport', type=int, default=6633, help='port on '
                        'which odl\'s controller is listening  (default = 6633)')
    parser.add_argument('--xmls', default=None, help='generete tests only '
                        'from some xmls (i.e. 1,3,34)  (default = None)')
    in_args = parser.parse_args()

    # set and start mininet
    net = MininetTools.create_network(in_args.odlhost, in_args.mnport)
    net.start()
    time.sleep(MININET_START_DELAY)

    try:
        # set host and port of ODL controller for test cases
        TestOpenFlowXml.port = in_args.odlport
        TestOpenFlowXml.host = in_args.odlhost
        TestOpenFlowXml.mn_port = in_args.mnport
        
        # set keyword dictionaries
        keywords = FileLoaderTools.load_file_to_dict('keywords.csv')
        match_keywords = FileLoaderTools.load_file_to_dict('match-keywords.csv')
        action_keywords = FileLoaderTools.load_file_to_dict('action-keywords.csv')
        ignore_keywords = FileLoaderTools.load_file_to_dict('ignore-keywords.csv')
    

        odl_suite = suite(base_test_class=TestOpenFlowXml)
        del sys.argv[1:]
        unittest.TextTestRunner().run(odl_suite)
    finally:
        # stop mininet
        net.stop()