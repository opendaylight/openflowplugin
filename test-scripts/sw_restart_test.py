import unittest
import os
import re
import sys
import logging
import time
import argparse
import itertools
import requests
import xml.dom.minidom as md
from xml.etree import ElementTree as ET

from odl_tests_new import MininetTools, FileLoaderTools, ParseTools

class TestRestartMininet(unittest.TestCase):

    log = logging.getLogger('TestRestartMininet')

    def setUp(self):
        TestRestartMininet.log.info('setUp')
        self.switch_flows_stored = 0
        self.table_id = 2

        self.__start_MN()

    def tearDown(self):
        TestRestartMininet.log.info('tearDown')
        self.net.stop()

    def __start_MN(self):
        wait_time = 15

        self.net = MininetTools.create_network(self.host, self.mn_port)
        self.net.start()
        TestRestartMininet.log.info('mininet stared')
        TestRestartMininet.log.info('waiting {} seconds...'.format(wait_time))
        time.sleep(wait_time)

    def __get_flows_string(self, net=None):
        if net is None:
            net = self.net
        switch = net.switches[0]
        output = switch.cmdPrint(
        'ovs-ofctl -O OpenFlow13 dump-flows %s' % switch.name)

        TestRestartMininet.log.debug('switch flow table: {}'.format(output))

        return output.splitlines()[1:]

    def __load_xmls(self, path='xmls'):
        TestRestartMininet.log.info('loading xmls')
        xmls = None
        suite = unittest.TestSuite()
        if args.xmls is not None:
            xmls = map(int, args.xmls.split(','))

        xmlfiles = None
        if xmls is not None:
            xmlfiles = (path + '/f%d.xml' % fid for fid in xmls)
        else:
            xmlfiles = (path + '/' + xml for xml in os.listdir(path) if xml.endswith('.xml'))

        return xmlfiles

    def __add_flows(self, path_to_xml):
        TestRestartMininet.log.info('adding flow from xml: {}'.format(path_to_xml))
        xml_string = FileLoaderTools.load_file_to_string(path_to_xml)
        #TestRestartMininet.log.info('loaded xml: {}'.format(''.join(xml_string.split())))
        tree = md.parseString(xml_string)

        ids = ParseTools.get_values(tree.documentElement, 'table_id', 'id')

        data = (self.host, self.port, ids['table_id'], ids['id'])

        url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
              '/node/openflow:1/table/%s/flow/%s' % data
        # send request via RESTCONF
        headers = {
            'Content-Type': 'application/xml',
            'Accept': 'application/xml',
        }
        TestRestartMininet.log.info('sending request to url: {}'.format(url))
        rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                           headers=headers)
        TestRestartMininet.log.info('received status code: {}'.format(rsp.status_code))
        TestRestartMininet.log.debug('received content: {}'.format(rsp.text))
        assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                        ' code returned %d' % rsp.status_code
        
        # check request content against restconf's datastore
        response = requests.get(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
        assert response.status_code == 200

        switch_flows = self.__get_flows_string(self.net)
        assert len(switch_flows) > 0

        # store last used table id which got flows for later checkup
        self.table_id = ids['table_id']
        self.switch_flows_stored = len(switch_flows)
        TestRestartMininet.log.info('stored: {} flows'.format(self.switch_flows_stored))

    def test(self):

        xmls = self.__load_xmls()
        for xml in xmls:
            self.__add_flows(xml)
        
        switch_flows = 0

        TestRestartMininet.log.info('---------- preparation finished, running test ----------\n\n')
        assert self.switch_flows_stored > 0, 'don\'t have any stored flows'
        TestRestartMininet.log.info('got {} stored flows'.format(self.switch_flows_stored))

        #STOP mininet and start it again - then check flows
        TestRestartMininet.log.info('restaring mininet...')
        self.net.stop()
        TestRestartMininet.log.info('mininet stopped')
        self.__start_MN()

        url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
            '/node/openflow:1/table/%s/' % (self.host, self.port, self.table_id)
        TestRestartMininet.log.info('checking flows in controller - sending request to url: {}'.format(url))
        response = requests.get(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
        assert response.status_code == 200

        tree = ET.ElementTree(ET.fromstring(response.text))
        flows_on_controller = len(tree.getroot())
        TestRestartMininet.log.info('{} flows are stored in switch config datastore'.format(flows_on_controller))

        current_try = 1

        while current_try <= self.retry and switch_flows != self.switch_flows_stored:
            TestRestartMininet.log.info('trying to get flows from mininet switch: {}/{}...'.format(current_try, self.retry))
            TestRestartMininet.log.info('waiting {} more seconds...'.format(self.wait))
            time.sleep(self.wait)
            switch_flows = len(self.__get_flows_string(self.net))
            TestRestartMininet.log.info('got {} flows...'.format(switch_flows))
            current_try = current_try + 1

        assert self.switch_flows_stored == switch_flows, 'Stored amount of flows on switch should be equal to stored flows on controller'\
            ' %d <> %d' % (switch_flows,self.switch_flows_stored)

if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)

    # parse cmdline arguments
    parser = argparse.ArgumentParser(description='Test for flow addition to'
                        ' switch after the switch has been restarted')
    parser.add_argument('--odlhost', default='127.0.0.1', help='host where '
                        'odl controller is running')
    parser.add_argument('--odlport', type=int, default=8080, help='port on '
                        'which odl\'s RESTCONF is listening')
    parser.add_argument('--mnport', type=int, default=6653, help='port on '
                        'which odl\'s controller is listening')
    parser.add_argument('--xmls', default=None, help='generete tests only '
                        'from some xmls (i.e. 1,3,34) ')
    parser.add_argument('--wait', default=30, help='number of second that '
                        'should test wait before trying to get flows from '
                        'restared mininet switch')
    parser.add_argument('--retry', default=1, help='number of tries to get'
                        'flows from restarted mininet')
    args = parser.parse_args()

    # set host and port of ODL controller for test cases
    TestRestartMininet.port = args.odlport
    TestRestartMininet.host = args.odlhost
    TestRestartMininet.mn_port = args.mnport
    TestRestartMininet.wait = args.wait
    TestRestartMininet.retry = args.retry

    del sys.argv[1:]
    unittest.main()
