import os
import re
import sys
import time
import logging
import argparse
import unittest
import xml.dom.minidom as md
from xml.etree import ElementTree as ET
from string import lower

import requests
from netaddr import IPNetwork
import mininet.node
import mininet.topo
import mininet.net
import mininet.util
from mininet.node import RemoteController
from mininet.node import OVSKernelSwitch

import xmltodict
from xmlvalidator import XMLValidator

# Delay time value is important for slow machines 
# value mean nr. of seconds for waiting for controller
TEST_TIME_DELAY = 0

class TestOpenFlowXml_Base(unittest.TestCase):
    """
    Base TEST class extends unittest.TestCase and
    it provides possibilty to add parameters for 
    all subclasses by call a static constructor:
    
    TestOpenFlowXml_Base.load_file_name(sub_class_name, param)
    """
    
    def __init__(self, methodName='runTest', path_to_xml=None):
        """
        private defalut constructor
        """
        super(TestOpenFlowXml_Base, self).__init__(methodName)
        self.path_to_xml = path_to_xml
        
    @staticmethod
    def load_file_name(clazz, path_to_xml=None):
        """
        static constructor for all subclasses with param
        param -> path_to_xml (default None)
        """
        testloader = unittest.TestLoader()
        testnames = testloader.getTestCaseNames(clazz)        
        suite = unittest.TestSuite()
        for name in testnames:
            suite.addTest(clazz(name, path_to_xml=path_to_xml))
        return suite


class ConvertorTools():
    """
    Tool class contains static conversion method
    for the value conversions
    """
    CONVERTORS = {
        'cookie': hex, 
        'metadata': hex
    }  
    
    @staticmethod
    def base_tag_values_conversion(key, value):
        """
        Check a need to conversion and convert if need
        """
        if value is None : return ''
        else:
            convertor = ConvertorTools.CONVERTORS.get(key, None)
            if convertor is None : return value
            else :
                return convertor(int(value))


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
        result = {tag: None for tag in tags}
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


class MininetTools():
    """
    Tool class provides static method for Open_vswitch
    mininet out of box controls 
    """
    @staticmethod
    def create_network(controller_ip, controller_port):
        """Create topology and mininet network."""
        topo = mininet.topo.Topo()

        topo.addSwitch('s1')
        topo.addHost('h1')
        topo.addHost('h2')

        topo.addLink('h1', 's1')
        topo.addLink('h2', 's1')

        switch=mininet.util.customConstructor(
            {'ovsk':OVSKernelSwitch}, 'ovsk,protocols=OpenFlow13')

        controller=mininet.util.customConstructor(
            {'remote': RemoteController}, 'remote,ip=%s,port=%s' % (controller_ip,controller_port))


        net = mininet.net.Mininet(topo=topo, switch=switch, controller=controller)

        return net
    
#     @staticmethod #TODO VD finish it
#     def __mininet_parse_response(resp_str='', x_dict={}, ikwd={}):
#         for elm in resp_str.split(',') :
#             if len(elm.split('=')) > 1 :
#                 x_key = elm.split('=')[0]
#                 x_val = elm.split
#                 x_dict[elm] = MininetTools.__mininet_parse_response(elm.split('='), x_dict, ikwd)
            
    
    @staticmethod
    def get_flows(net, ikwd={}):
        """Get list of flows from network's first switch.

        Return list of all flows on switch, sorted by duration (newest first)
        One flow is a dictionary with all flow's attribute:value pairs. Matches
        are stored under 'matches' key as another dictionary.
        Example:

        {
            'actions': 'drop',
            'cookie': '0xa,',
            'duration': '3.434s,',
            'hard_timeout': '12,',
            'idle_timeout': '34,',
            'matches': {
                'ip': None,
                'nw_dst': '10.0.0.0/24'
            },
            'n_bytes': '0,',
            'n_packets': '0,',
            'priority': '2',
            'table': '1,'
        }

        """
        log = logging.getLogger(__name__)

        switch = net.switches[0]
        output = switch.cmdPrint(
            'ovs-ofctl -O OpenFlow13 dump-flows %s' % switch.name)

        log.debug('switch flow table: {}'.format(output))

        # dictionary for return
        flows = {}

        for line in output.splitlines()[1:] :
            output = line;
        
        if (len(re.split('actions=', output, 1)) > 0) :
            try :
                action_str = re.split('actions=',output,1)[1]
                flows['actions'] = ', '.join((action_str.split(','))) if (len(action_str.split(',')) > 0) else action_str.strip()
                # TODO: VD look at actions with own param (xml24) __mininet_parse_resp
            except Exception, e :
                log.error(e)
            
        else :
            flows['actions'] = ''

        output= re.split('actions=',output,1)[0]

        for e in output.split(',') :
            elem = e.split('=')
            a_key = (elem[0]).strip()
            if (ikwd.get(a_key, None) is None) :
                a_value = elem[1] if (len(elem) > 1) else None
                flows[a_key] = a_value.strip() if isinstance(a_value,str) else (str(a_value)).strip()

        return flows


class FileLoaderTools():

    log = logging.getLogger('FileLoaderTools')

    @staticmethod
    def load_file_to_string(path_to_file):
        output_string = None

        try:
            with open(path_to_file) as f:
                output_string = f.read()
        except IOError, e:
            FileLoaderTools.log.error('cannot find {}: {}'.format(path_to_file, e.strerror), exc_info=True)

        return output_string

    @staticmethod
    def load_file_to_dict(path_to_file):
        dictionary = None
        
        try :
            with open(path_to_file) as f:
                dictionary = dict(line.strip().split(';') for line in f
                            if not line.startswith('#'))
        except IOError, e:
            FileLoaderTools.log.error('cannot find {}: {}'.format(path_to_file, e.strerror), exc_info=True)
        return dictionary


class Comparator():

    log = logging.getLogger('Comparator')

    @staticmethod
    def compare_results(actual, expected):
        #print 'ACT: ', actual
        #print 'EXP: ', expected

        list_unused = list(set(actual.keys()) - set(expected.keys()))
        if len(list_unused) > 0:
            Comparator.log.info('unchecked tags: {}'.format(list_unused))

        list_duration = ['duration','hard_timeout','idle_timeout']

        Comparator.test_duration(actual, expected)

        # compare results from actual flow (mn dump result) and expepected flow (stored result)
        for k in expected.keys():
            if k not in list_duration:
                assert k in actual, 'cannot find key {} in flow {}'.format(k, actual)
                assert actual[k] == expected[k], 'key:{}, actual:{} != expected:{}'.format(k, actual[k], expected[k])

    @staticmethod
    def test_duration(actual, expected):
        duration_key = 'duration'
        hard_to_key = 'hard_timeout'

        if duration_key in expected.keys():
            assert duration_key in actual.keys(), '{} is not set in {}'.format(duration_key, actual)
            try:
                duration = float(expected['duration'].rstrip('s'))
                hard_timeout = int(actual['hard_timeout'])
                assert duration <= hard_timeout, 'duration is higher than hard_timeout, {} > {}'.format(duration, hard_timeout)
            except KeyError as e:
                Comparator.log.warning('cannot find keys to test duration tag', exc_info=True)
        else:
            # VD - what should we do in this case
            pass


class TestOpenFlowXml(TestOpenFlowXml_Base):        
        
    def test_xml(self):
        test_number = int(self.path_to_xml[6:-4])
        log = logging.getLogger('test_xml_%04d' %test_number)
        
        xml_string = FileLoaderTools.load_file_to_string(self.path_to_xml)
        tree = md.parseString(xml_string)
        
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
        log.info('sending request to url: {}'.format(url))
        rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                           headers=headers)
        log.info('received status code: {}'.format(rsp.status_code))
        log.debug('received content: {}'.format(rsp.text))
        assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                            ' code returned %d' % rsp.status_code
        time.sleep(TEST_TIME_DELAY)
        
        try:
            # check request content against restconf's datastore
            response = requests.get(url, auth=('admin', 'admin'),
                                    headers={'Accept': 'application/xml'})
            assert response.status_code == 200
            req = (xmltodict.parse(ET.tostring(ET.fromstring(xml_string))))
            res = (xmltodict.parse(ET.tostring(ET.fromstring(response.text))))
            assert req == res, 'uploaded and stored xml, are not the same\n' \
                'uploaded: %s\nstored:%s' % (req, res)
    
            # collect flow table state on switch
            switch_flows = MininetTools.get_flows(net, ignore_keywords)
#             switch_flows_actions = re.sub('[\s]', '', switch_flows, count=1))
            assert len(switch_flows) > 0
            assert switch_etalon == switch_flows, 'expected and stored switch settings are not the same \n'\
                'expected: %s\nstored: %s' %(switch_etalon,switch_flows)

            # TODO VD remove dead code
            # compare requested object and flow table state
            ## TODO look at action parsing separatly from a flow
#             switch_flow_dict = ParseTools.get_switchflow_dict(switch_flows[0], ignore_keywords)
#             assert switch_etalon == switch_flow_dict, 'expected and stored switch settings are not the same \n'\
#                 'expected: %s\nstored: %s' %(switch_etalon,switch_flow_dict)
#         if mn_string is not None:
#             #log.info('running tests')
#             Comparator.compare_results(switch_flows[0], ParseTools.dump_string_to_dict(mn_string))
#         else:
#             log.error('cannot find test results - comparison skipped')
        except Exception, e :
            log.error(e)
            print '\n'
            raise e

        finally:
            response = requests.delete(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
            assert response.status_code == 200
            print '\n\n\n'    
            time.sleep(TEST_TIME_DELAY)

def suite(path='xmls', test_class='TestOpenFlowXml_Base') :
    suite = unittest.TestSuite()
    if args.xmls is not None:
        xmls = map(int, args.xmls.split(','))
    else :
        xmls = None
    
    xmlfiles = None
    if xmls is not None:
        xmlfiles = ('f%d.xml' % fid for fid in xmls)
    else:
        xmlfiles = (xml for xml in os.listdir(path) if xml.endswith('.xml'))

    #create xml validator
    validator = XMLValidator(keywords, action_keywords, match_keywords, logging.ERROR)
    validator.fill_fields()

    for xmlfile in xmlfiles:
        #fill validator with data from xml and validate them - just logging to hint what can be wrong, test wont be stopped by invalid xml
        validator.create_dictionaries(os.path.join(path, xmlfile))
        validator.validate()

        suite.addTest(TestOpenFlowXml_Base.load_file_name(test_class, path_to_xml=os.path.join(path, xmlfile)))
    return suite

if __name__ == '__main__':
    # set up logging
    logging.basicConfig(level=logging.DEBUG)

    # parse cmdline arguments
    parser = argparse.ArgumentParser(description='Run switch <-> ODL tests '
                                     'defined by xmls.')
    parser.add_argument('--odlhost', default='127.0.0.1', help='host where '
                        'odl controller is running')
    parser.add_argument('--odlport', type=int, default=8080, help='port on '
                        'which odl\'s RESTCONF is listening')
    parser.add_argument('--mnport', type=int, default=6653, help='port on '
                        'which odl\'s controller is listening')
    parser.add_argument('--xmls', default=None, help='generete tests only '
                        'from some xmls (i.e. 1,3,34) ')
    args = parser.parse_args()

    # set and start mininet
    net = MininetTools.create_network(args.odlhost, args.mnport)
    net.start()
    time.sleep(15)

    try:
        # set host and port of ODL controller for test cases
        TestOpenFlowXml.port = args.odlport
        TestOpenFlowXml.host = args.odlhost
        TestOpenFlowXml.mn_port = args.mnport
        
        # set keyword dictionaries
        keywords = FileLoaderTools.load_file_to_dict('keywords.csv')
        match_keywords = FileLoaderTools.load_file_to_dict('match-keywords.csv')
        action_keywords = FileLoaderTools.load_file_to_dict('action-keywords.csv')
        ignore_keywords = FileLoaderTools.load_file_to_dict('ignore-keywords.csv')
    
#         # fix arguments for unittest
#         del sys.argv[1:]
#     
        odl_suite = suite(test_class=TestOpenFlowXml)
        unittest.TextTestRunner().run(odl_suite)
    finally:
        # stop mininet
        net.stop()
