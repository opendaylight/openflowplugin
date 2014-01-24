import unittest
import os
import re
import sys
import logging
import time
import argparse
import threading
import requests

from multiprocessing import Process
import xml.dom.minidom as md
from xml.etree import ElementTree as ET

from openvswitch.mininet_tools import MininetTools
from openvswitch.parser_tools import ParseTools


# Delay time value is important for slow machines 
# value mean nr. of seconds for waiting for controller 
CONTROLLER_DELAY = 50
# value mean nr. of seconds for waiting for mininet 
MININET_START_DELAY = 15


FLOW_ID_TEMPLATE = 'FLOW_ID_TEMPLATE'
COOKIE_TEMPLATE = 'COOKIE_TEMPLATE'
HARD_TO_TEMPLATE = 'HARD_TO_TEMPLATE'
FLOW_NAME_TEMPLATE = 'FLOW_NAME_TEMPLATE'
IPV4DST_TEMPLATE = 'IPV4DST_TEMPLATE'
PRIORITY_TEMPLATE = 'PRIORITY_TEMPLATE'

xml_template = '<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>' \
'<flow xmlns=\"urn:opendaylight:flow:inventory\">' \
    '<strict>false</strict>' \
    '<instructions><instruction><order>0</order><apply-actions><action><order>0</order><dec-nw-ttl/>' \
    '</action></apply-actions></instruction></instructions><table_id>2</table_id>' \
    '<id>'+FLOW_ID_TEMPLATE+'</id><cookie_mask>255</cookie_mask><installHw>false</installHw>' \
    '<match><ethernet-match><ethernet-type><type>2048</type></ethernet-type></ethernet-match>' \
    '<ipv4-destination>'+IPV4DST_TEMPLATE+'</ipv4-destination></match><hard-timeout>'+HARD_TO_TEMPLATE+'</hard-timeout>' \
    '<flags>FlowModFlags [_cHECKOVERLAP=false, _rESETCOUNTS=false, _nOPKTCOUNTS=false, _nOBYTCOUNTS=false, _sENDFLOWREM=false]</flags>' \
    '<cookie>'+COOKIE_TEMPLATE+'</cookie><idle-timeout>34</idle-timeout><flow-name>'+FLOW_NAME_TEMPLATE+'</flow-name><priority>'+PRIORITY_TEMPLATE+'</priority>' \
    '<barrier>false</barrier></flow>'


class MultiTest(unittest.TestCase):

    log = logging.getLogger('MultiTest')
    total_errors = 0
    total_flows = 0
    ids = []

    def setUp(self):
        MultiTest.log.info('setUp')
        self.threads_count = 50
        self.thread_pool = list()

        self.__start_MN()
        self.__setup_threads()
        self.__run_threads()

    def tearDown(self):
        MultiTest.log.info('tearDown')
        self.net.stop()

    @staticmethod
    def inc_error(value=1):
        MultiTest.total_errors += value

    @staticmethod
    def inc_flow(value=1):
        MultiTest.total_flows += 1

    @staticmethod
    def add_flow_id(flow_id=None):
        if (flow_id > None) :
            MultiTest.ids.append(flow_id)

    def __start_MN(self):
        self.net = MininetTools.create_network(self.host, self.mn_port)
        self.net.start()
        MultiTest.log.info('mininet stared')
        MultiTest.log.info('waiting {0} seconds...'.format(MININET_START_DELAY))
        time.sleep(MININET_START_DELAY)
        self.total_flows = len(MininetTools.get_flows_string(self.net))


    def __setup_threads(self):
        if args.threads is not None:
            self.threads_count = int(args.threads)

        for i in range(0, self.threads_count):
            #thread will have predetermined flows id to avoid using shared resource
            t = FlowAdderThread(i, self.host, self.port, self.net, flows_ids_from=i*MultiTest.flows + 1, flows_ids_to=(i+1)*MultiTest.flows + 1)

            self.thread_pool.append(t)

    def __run_threads(self):
        # start threads
        for t in self.thread_pool:
            t.start()

        # wait for them to finish
        for t in self.thread_pool:
            t.join()

        # collect results
        #for t in self.thread_pool:
        #    MultiTest.inc_flow(t.flows)
        #    MultiTest.inc_error(t.errors)

    def test(self):

        switch_flows = 0

        assert MultiTest.total_flows > 0, ('Stored flows should be greater than 0, actual is {0}'.format(MultiTest.total_flows))

        MultiTest.log.info('\n\n---------- preparation finished, running test ----------')
        # check config
        url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
            '/node/openflow:1/table/2/' % (self.host, self.port)
        MultiTest.log.info('checking flows in controller - sending request to url: {0}'.format(url))
        response = requests.get(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
        assert response.status_code == 200

        tree = ET.ElementTree(ET.fromstring(response.text))
        flows_on_controller_config = len(tree.getroot())

        MultiTest.log.info('{0} flows are stored by results from threads, {1} errors'.format(MultiTest.total_flows, MultiTest.total_errors))
        MultiTest.log.info('{0} flows are stored in controller config'.format(flows_on_controller_config))

        MultiTest.log.info('waiting for operational data store loading')
        time.sleep(CONTROLLER_DELAY)

        # check operational
        url = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
            '/node/openflow:1/table/2/' % (self.host, self.port)
        MultiTest.log.info('checking flows in controller - sending request to url: {0}'.format(url))
        response = requests.get(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
        assert response.status_code == 200

        tree = ET.ElementTree(ET.fromstring(response.text))
        flows_on_controller_operational = len(tree.getroot())

#         assert flows_on_controller_config == flows_on_controller_operational, 'Added amount of stored flows by controller should be equal for operational and config {0} <> {1}'.format(flows_on_controller_operational,flows_on_controller_config)

        MultiTest.log.info('got response: {0}'.format(response.status_code))
        MultiTest.log.info('{0} flows are stored in operational config \n'.format(flows_on_controller_operational))
        
        

#         switch_flows_list =  ParseTools.get_flows_string(self.net)
#         switch_flows = len(switch_flows_list)
#         MultiTest.log.info('{0} flows are stored on switch'.format(switch_flows))
#         MultiTest.log.debug('switch flow-dump:\n{0}'.format(switch_flows_list))

#         assert MultiTest.total_flows == switch_flows, 'Added amount of flows to switch should be equal to successfully added flows to controller {0} <> {1}'.format(switch_flows,MultiTest.total_flows)
        
        headers = {
                'Content-Type': 'application/xml',
                'Accept': 'application/xml',
            }
        for id in MultiTest.ids :
            url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                '/node/openflow:1/table/2/flow/%s' % (self.host, self.port,id)
            MultiTest.log.info('checking flows in controller - sending request to url: {0}'.format(url))
            response = requests.delete(url, auth=('admin','admin'), headers=headers)
            MultiTest.log.info('delete flows in controller - sending request to url: {0}'.format(url))
            MultiTest.log.info('got response: {0}'.format(response.status_code))
        
        MultiTest.log.info('waiting for operational data store cleaning')
        time.sleep(CONTROLLER_DELAY)
        
        # check config
        url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
            '/node/openflow:1/table/2/' % (self.host, self.port)
        MultiTest.log.info('checking flows in controller - sending request to url: {0}'.format(url))
        response = requests.get(url, auth=('admin', 'admin'),
                                headers={'Accept': 'application/xml'})
        assert response.status_code == 200

        tree = ET.ElementTree(ET.fromstring(response.text))
        flows_on_controller_config = len(tree.getroot())

        MultiTest.log.info('waiting for operational data store cleaning')
        time.sleep(CONTROLLER_DELAY)

        # check operational
        url = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
            '/node/openflow:1/table/2/' % (self.host, self.port)
        MultiTest.log.info('checking flows in controller - sending request to url: {0}'.format(url))
        response = requests.delete(url, auth=('admin', 'admin'), headers={'Accept': 'application/xml'})
        assert response.status_code == 200

        tree = ET.ElementTree(ET.fromstring(response.text))
        flows_on_controller_operational = len(tree.getroot())

        MultiTest.log.info('got response: {0}'.format(response.status_code))
        MultiTest.log.info('{0} flows are stored in operational config \n'.format(flows_on_controller_operational))
        MultiTest.log.info('operational dump:\n{0}'.format(response.text))

        assert flows_on_controller_config == flows_on_controller_operational, 'Added amount of stored flows by controller should be equal for operational and config {0} <> {1}'.format(flows_on_controller_operational,flows_on_controller_config)


class FlowAdderThread(threading.Thread):

    def __init__(self, thread_id, host, port, net, flows_ids_from=0, flows_ids_to=1):
        threading.Thread.__init__(self)
        self.thread_id = thread_id
        self.flows = 0
        self.errors = 0
        self.flows_ids_from = flows_ids_from
        self.flows_ids_to = flows_ids_to

        self.net = net
        self.host = host
        self.port = port

        self.log = logging.getLogger('FlowAdderThread: ' + str(thread_id))
        self.log.propagate = False
        self.channel = logging.StreamHandler()
        self.log.addHandler(self.channel)
        formatter = logging.Formatter('THREAD {0}: %(levelname)s: %(message)s'.format(self.thread_id))
        self.channel.setFormatter(formatter)
        #self.log.setLevel(logging.INFO)

        self.log.info('created new FlowAdderThread->id:{0}, flows id: {1} -> {2}'.format(self.thread_id, self.flows_ids_from, self.flows_ids_to))

    def make_ipv4_address(self, number, octet_count=4, octet_size=255):
        mask = 24
        ip = ['10', '0', '0', '0']

        if number < (255**3):
            for o in range(1, octet_count):
                ip[octet_count - 1 - o] = str(number % octet_size)
                number = number / octet_size
                #mask -= 8
                if number == 0:
                    break

        return '.'.join(ip) + '/{0}'.format(mask)

    def __add_flows(self, act_flow_id):
        try:
            self.log.info('adding flow id: {0}'.format(act_flow_id))
            self.log.debug('flow ip address from id: {0}'.format(self.make_ipv4_address(act_flow_id)))

            xml_string = str(xml_template).replace(FLOW_ID_TEMPLATE, str(act_flow_id)).replace(COOKIE_TEMPLATE, str(act_flow_id))\
            .replace(HARD_TO_TEMPLATE, '12').replace(FLOW_NAME_TEMPLATE,'FooXf{0}'.format(act_flow_id))\
            .replace(IPV4DST_TEMPLATE,self.make_ipv4_address(act_flow_id)).replace(PRIORITY_TEMPLATE,str(act_flow_id))

            #TestRestartMininet.log.info('loaded xml: {0}'.format(''.join(xml_string.split())))
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
            self.log.debug('PUT: sending request to url: {0}'.format(url))
            rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                               headers=headers)
            self.log.debug('received status code: {0}'.format(rsp.status_code))
            self.log.debug('received content: {0}'.format(rsp.text))
            assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                            ' code returned %d' % rsp.status_code

            # we expect that controller doesn't fail to store flow on switch
            self.flows += 1
            MultiTest.inc_flow()
            MultiTest.add_flow_id(act_flow_id)
            
        except AssertionError as e:
            self.errors += 1
            self.log.error('AssertionError storing flow id:{0}, reason: {1}'.format(act_flow_id, str(e)))
            MultiTest.inc_error()
        except Exception as e:
            self.errors += 1
            self.log.error('Error storing flow id:{0}, reason: {1}'.format(act_flow_id, str(e)))
            MultiTest.inc_error()

    def run(self):
        self.log.info('started... adding flows {0} to {1}'.format(self.flows_ids_from, self.flows_ids_to))
        for i in range(self.flows_ids_from, self.flows_ids_to):
            self.__add_flows(i)

        self.log.info('finished, successfully added {0} flows, {1} errors'.format(self.flows,self.errors))


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)

    # parse cmdline arguments
    parser = argparse.ArgumentParser(description='Test for flow addition to'
                        ' switch after the switch has been restarted')
    parser.add_argument('--odlhost', default='127.0.0.1', help='host where '
                        'odl controller is running (default is 127.0.0.1)')
    parser.add_argument('--odlport', type=int, default=8080, help='port on '
                        'which odl\'s RESTCONF is listening (default is 8080)')
    parser.add_argument('--mnport', type=int, default=6653, help='port on '
                        'which odl\'s controller is listening (default is 6653)')
    parser.add_argument('--xmls', default=None, help='generete tests only '
                        'from some xmls (i.e. 1,3,34) (default is None - use internal template)')
    parser.add_argument('--threads', default=5, help='how many threads '
                        'should be used (default is 5)')
    parser.add_argument('--flows', default=2, help='how many flows will add'
                        ' one thread (default is 10)')
    args = parser.parse_args()

    # set host and port of ODL controller for test cases
    MultiTest.port = args.odlport
    MultiTest.host = args.odlhost
    MultiTest.mn_port = args.mnport
    MultiTest.threads = int(args.threads)
    MultiTest.flows = int(args.flows)

    del sys.argv[1:]
    unittest.main()