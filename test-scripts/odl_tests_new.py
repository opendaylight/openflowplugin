import os
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


class ParseTools():

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
    @staticmethod
    def get_flows(net):
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

        flows = []

        for line in output.splitlines()[1:]:
            flows.append(ParseTools.dump_string_to_dict(line))

         # sort by duration
        return sorted(flows, key=lambda x: x['duration'].rstrip('s'))


class Loader():

    log = logging.getLogger('Loader')

    @staticmethod
    def get_xml_test_path(test_id, path='xmls'):
        return os.path.join(path, 'f%d.xml' % test_id)

    @staticmethod
    def get_mn_test_path(test_id, path='ofctl'):
        return os.path.join(path, 't%d' % test_id)

    @staticmethod
    def get_xml_test_path(path, test_id):
        return os.path.join(path, 'f%d.xml' % test_id)

    @staticmethod
    def load_test_file_to_string(path_to_file):
        output_string = None

        try:
            with open(path_to_file) as f:
                output_string = f.read()
        except IOError, e:
            Loader.log.error('cannot find {}: {}'.format(path_to_file, e.strerror), exc_info=True)

        return output_string

    @staticmethod
    def get_xml_dict(test_id):
        xml_string = Loader.load_test_file_to_string(Loader.get_xml_test_path(test_id))
        return xmltodict(xml_string)

    @staticmethod
    def get_mn_dict(test_id):
        mn_string = Loader.load_test_file_to_string(Loader.get_mn_test_path(test_id))
        return ParseTools.dump_string_to_dict(mn_string)


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

class TestOpenFlowXMLs(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.net = MininetTools.create_network(cls.host, cls.mn_port)
        cls.net.start()
        time.sleep(15)

    @classmethod
    def tearDownClass(cls):
        cls.net.stop()

def generate_tests_from_xmls(path, xmls=None):
    # generate test function from path to request xml
    def generate_test(path_to_xml, path_to_md):
        xml_string = Loader.load_test_file_to_string(path_to_xml)
        mn_string = Loader.load_test_file_to_string(path_to_md)

        tree = md.parseString(xml_string)
        ids = ParseTools.get_values(tree.documentElement, 'table_id', 'id')

        def new_test(self):
            log = logging.getLogger(__name__)
            # send request throught RESTCONF
            data = (self.host, self.port, ids['table_id'], ids['id'])
            url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  '/node/openflow:1/table/%s/flow/%s' % data
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

            # check request content against restconf's datastore
            response = requests.get(url, auth=('admin', 'admin'),
                                    headers={'Accept': 'application/xml'})
            assert response.status_code == 200
            req = (xmltodict.parse(ET.tostring(ET.fromstring(xml_string))))
            res = (xmltodict.parse(ET.tostring(ET.fromstring(response.text))))
            assert req == res, 'uploaded and stored xml, are not the same\n' \
                'uploaded: %s\nstored:%s' % (req, res)

            # collect flow table state on switch
            switch_flows = MininetTools.get_flows(self.net)
            assert len(switch_flows) > 0

            # compare requested object and flow table state
            if mn_string is not None:
                #log.info('running tests')
                Comparator.compare_results(switch_flows[0], ParseTools.dump_string_to_dict(mn_string))
            else:
                log.error('cannot find test results - comparison skipped')

        return new_test

    # generate list of available xml requests
    xmlfiles = None
    if xmls is not None:
        xmlfiles = ('f%d.xml' % fid for fid in xmls)
    else:
        xmlfiles = (xml for xml in os.listdir(path) if xml.endswith('.xml'))

    # define key getter for sorting
    def get_test_number(test_name):
        return int(test_name[1:-4])

    for xmlfile in xmlfiles:
        test_number = get_test_number(xmlfile)
        test_name = 'test_xml_%04d' % test_number
        setattr(TestOpenFlowXMLs,
                test_name,
                generate_test(os.path.join(path, xmlfile), os.path.join('ofctl', 't{}'.format(test_number))))


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

    # set host and port of ODL controller for test cases
    TestOpenFlowXMLs.port = args.odlport
    TestOpenFlowXMLs.host = args.odlhost
    TestOpenFlowXMLs.mn_port = args.mnport

    keywords = None
    with open('keywords.csv') as f:
        keywords = dict(line.strip().split(';') for line in f
                        if not line.startswith('#'))

    match_keywords = None
    with open('match-keywords.csv') as f:
        match_keywords = dict(line.strip().split(';') for line in f
                              if not line.startswith('#'))

    action_keywords = None
    with open('action-keywords.csv') as f:
        action_keywords = dict(line.strip().split(';') for line in f
                                    if not line.startswith('#'))

    # fix arguments for unittest
    del sys.argv[1:]

    # generate tests for TestOpenFlowXMLs
    if args.xmls is not None:
        xmls = map(int, args.xmls.split(','))
        generate_tests_from_xmls('xmls', xmls)
    else:
        generate_tests_from_xmls('xmls')

    # run all tests
    unittest.main()
