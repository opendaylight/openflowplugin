import unittest
import os
import re
import sys
import logging
import time
import argparse
import requests

import xml.dom.minidom as md
from xml.etree import ElementTree as ET

from openvswitch.mininet_tools import MininetTools
from openvswitch.flow_tools import  FlowAdderThread, FlowRemoverThread, MapNames, loglevels, TO_GET, WAIT_TIME, OPERATIONAL_DELAY, FLOWS_PER_SECOND
from openvswitch.testclass_templates import TestClassAdd, TestClassRemove
from openvswitch.testclass_components import CheckConfigFlowsComponent, CheckOperFlowsComponent

class MultiTest(unittest.TestCase, TestClassAdd, TestClassRemove, CheckConfigFlowsComponent, CheckOperFlowsComponent):

    log = logging.getLogger('MultiTest')
    total_errors = 0

    id_maps = dict()
    active_map = dict()

    def setUp(self):
        MultiTest.log.info('test setup...')
        self.__start_MN()

	MultiTest.id_maps[MapNames.TEST] = dict()
	MultiTest.id_maps[MapNames.DUMMY] = dict()

	MultiTest.active_map = MultiTest.id_maps[MapNames.TEST]


    def tearDown(self):
        MultiTest.log.info('test cleanup...')
	self.__set_active_map(MapNames.DUMMY)
        self.remover = FlowRemoverThread(self, 1, self.host, self.port, self.net, list(MultiTest.active_map.items()))
        self.remover.run()

	for k, v in MultiTest.id_maps.items():
            if len(v) > 0:
                MultiTest.log.warning('not all flows were deleted, remaining test flows: {0}, from map: {1}'.format(len(v),k))

    def __set_active_map(self, key):
	try:	
	    MultiTest.active_map = MultiTest.id_maps[key]
	except KeyError as e:
	    MultiTest.log.warning('Error switching between map ids: {0}'.format(str(e)))


    def inc_error(self, value=1):
        MultiTest.total_errors += value

    def inc_flow(self, flow_id= None, cookie_id=1):
        if flow_id is not None and cookie_id is not None:
	    #we dont care about actual value, just need to store flow_id as unique identifier		
            MultiTest.active_map[flow_id] = flow_id

    def delete_flow_from_map(self, flow_id, cookie_id=None):
        del MultiTest.active_map[flow_id]

    def __start_MN(self):
        self.net = MininetTools.create_network(self.host, self.mn_port)
        self.net.start()
        MultiTest.log.info('mininet stared')
        MultiTest.log.info('waiting {0} seconds...'.format(WAIT_TIME))
        time.sleep(WAIT_TIME)

    def test(self):
	# add dummy flows to test removal when there are already some flows in operational
	self.__set_active_map(MapNames.DUMMY)
	self.adder = FlowAdderThread(self, 0, self.host, self.port, self.net, MultiTest.flows + 1, MultiTest.flows + 11)
        self.adder.run()

	self.__set_active_map(MapNames.TEST)
        self.adder = FlowAdderThread(self, 1, self.host, self.port, self.net, 1, MultiTest.flows + 1)
        self.adder.run()

        # if we didn't manage to get any flows on controller there is no point doing test
        assert len(MultiTest.active_map) > 0, ('Stored flows should be greater than 0, actual is {0}'.format(len(MultiTest.active_map)))

        # check numer of flows before deletion
        MultiTest.log.debug('{0} flows are stored by results from threads, {1} errors'.format(len(MultiTest.active_map), MultiTest.total_errors))
        MultiTest.log.debug('{0} flows are stored in controller config'.format(self.check_config_flows(self.host, self.port, self.active_map)))
        MultiTest.log.info('{0} flows are stored in controller operational'.format(self.check_oper_flows_loop(self.host, self.port, self.active_map)))

        self.remover = FlowRemoverThread(self, 0, self.host, self.port, self.net, list(MultiTest.active_map.items()))
        self.remover.run()

        MultiTest.log.info('\n\n---------- preparation finished, running test ----------')

        # check and test after deletion
        flows_oper_after = self.check_oper_flows_loop(self.host, self.port, self.active_map)
        MultiTest.log.debug('{0} flows are stored in controller config'.format(self.check_config_flows(self.host, self.port, self.active_map)))
        MultiTest.log.info('{0} flows are stored in controller operational'.format(flows_oper_after))

        # check if we have managed to delete all test
        if len(MultiTest.active_map) <> 0:
            MultiTest.log.warning('Not all flows added during test have been deleted, ids of remaining flows are: {0}'.format(sorted(MultiTest.active_map)))

        # if we didn't manage to get any flows on controller there is no point doing test
        assert flows_oper_after == len(MultiTest.active_map), 'Number of flows added during test stored in operational should be {0}, is {1}'.format(len(MultiTest.active_map), flows_oper_after)


if __name__ == '__main__':

    requests_log = logging.getLogger("requests")
    requests_log.setLevel(logging.WARNING)

    # parse cmdline arguments
    parser = argparse.ArgumentParser(description='End to end stress tests of flows '
                        'addition from multiple connections')
    parser.add_argument('--odlhost', default='127.0.0.1', help='host where '
                        'odl controller is running  (default = 127.0.0.1) ')
    parser.add_argument('--odlport', type=int, default=8080, help='port on '
                        'which odl\'s RESTCONF is listening  (default = 8080) ')
    parser.add_argument('--mnport', type=int, default=6653, help='port on '
                        'which odl\'s controller is listening  (default = 6653)')
    parser.add_argument('--flows', default=100, help='how many flows will be added'
                        ' (default = 100)')
    parser.add_argument('--log', default='info', help='log level, permitted values are'
                        ' debug/info/warning/error  (default = info)')
    args = parser.parse_args()

    #logging.basicConfig(level=logging.DEBUG)
    logging.basicConfig(level=loglevels.get(args.log, logging.INFO))

    # set host and port of ODL controller for test cases
    MultiTest.port = args.odlport
    MultiTest.host = args.odlhost
    MultiTest.mn_port = args.mnport
    MultiTest.flows = int(args.flows)

    del sys.argv[1:]

    suite = unittest.TestSuite()
    test = MultiTest('test')
    suite.addTest(test)

    try:
        unittest.TextTestRunner(verbosity=2).run(suite)
        #unittest.main()
    finally:
        test.net.stop()
        print 'end'
