import argparse
import logging
import sys
import time
import unittest
from xml.etree import ElementTree as ET

from openvswitch.flow_tools import FlowAdderThread, FlowRemoverThread, MapNames, \
    WAIT_TIME
from openvswitch.mininet_tools import MininetTools


# Delay time value is important for slow machines 
# value mean nr. of seconds for waiting for controller 
CONTROLLER_DELAY = 50
# value mean nr. of seconds for waiting for mininet 
MININET_START_DELAY = 15


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

    def __setup_threads(self):
        if in_args.threads is not None:
            self.threads_count = int(in_args.threads)

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
    in_args = parser.parse_args()

    # set host and port of ODL controller for test cases
    MultiTest.port = in_args.odlport
    MultiTest.host = in_args.odlhost
    MultiTest.mn_port = in_args.mnport
    MultiTest.flows = int(in_args.flows)

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