import argparse
import logging
import sys
import time
import unittest

from openvswitch.flow_tools import FlowAdderThread, FlowRemoverThread, loglevels, \
    WAIT_TIME
from openvswitch.mininet_tools import MininetTools
from openvswitch.testclass_components import CheckOperFlowsComponent, \
    CheckConfigFlowsComponent, CheckSwitchDump


REMOVE_FLOWS_PER_THREAD = 250

class MultiTest(unittest.TestCase):

    log = logging.getLogger('MultiTest')
    total_errors = 0
    total_flows = 0
    stored_before_test_flows = 0

    def setUp(self):
        MultiTest.log.info('test setup...')
        self.threads_count = 50
        self.thread_pool = list()

        self.active_map = dict()

        self.__start_MN()
        self.__setup_threads()
        self.__run_threads()


    def tearDown(self):
        MultiTest.log.info('test cleanup...')
        MultiTest.total_deleted = 0
        self.__delete_flows()

    def inc_error(self, value=1):
        MultiTest.total_errors += value

    def inc_flow(self, flow_id= None, cookie_id=None):
        if flow_id is not None and cookie_id is not None:
            self.active_map[cookie_id] = flow_id

    def delete_flow_from_map(self, flow_id, cookie_id):
	del self.active_map[cookie_id]

    def __start_MN(self):
        self.net = MininetTools.create_network(self.host, self.mn_port)
        self.net.start()
        MultiTest.log.info('mininet stared')
        MultiTest.log.info('waiting {0} seconds...'.format(WAIT_TIME))
        time.sleep(WAIT_TIME)
        self.stored_before_test_flows = len(MininetTools.get_flows_string(self.net))

    def __setup_threads(self):
        if in_args.threads is not None:
            self.threads_count = int(in_args.threads)

        for i in range(0, self.threads_count):
            t = FlowAdderThread(self, i, self.host, self.port, self.net,\
                                flows_ids_from=i*MultiTest.flows + 1, flows_ids_to=i*MultiTest.flows + MultiTest.flows)

            self.thread_pool.append(t)

    def __run_threads(self):
        for t in self.thread_pool:
            t.start()

        for t in self.thread_pool:
            t.join()

    def test(self):
        switch_flows = 0
        total_flows = len(self.active_map.keys())

        assert total_flows > 0, ('Stored flows should be greater than 0, actual is {0}'.format(total_flows))

        MultiTest.log.info('\n\n---------- preparation finished, running test ----------')

        # check config
        flows_on_controller = CheckConfigFlowsComponent().check_config_flows(self.host, self.port, self.active_map)

        #check operational
        flows_on_controller_operational = CheckOperFlowsComponent().check_oper_flows_loop(self.host, self.port, self.active_map)

        # check switch
        switch_flows_list = MininetTools.get_flows_string(self.net)
        MultiTest.log.info('flow dump has {0} entries (including informational)'.format(len(switch_flows_list)))
        for item in switch_flows_list:
            if CheckSwitchDump().get_id_by_entry(item, self.active_map) is not None:
                MultiTest.log.debug('flow_id:{0} = {1}'.format(CheckSwitchDump().get_id_by_entry(item, self.active_map), item))
                switch_flows += 1

        # print info
        MultiTest.log.info('{0} flows are stored by results from threads, {1} errors'.format(total_flows, MultiTest.total_errors))
        MultiTest.log.info('{0} flows are stored in controller config'.format(flows_on_controller))
        MultiTest.log.info('{0} flows are stored in controller operational'.format(flows_on_controller_operational))
        MultiTest.log.info('{0} flows are stored on switch'.format(switch_flows))

        assert total_flows == switch_flows, 'Added amount of flows to switch should be equal to successfully added flows to controller {0} <> {1}'.format(switch_flows,total_flows)

    def __delete_flows(self):
        MultiTest.log.info('deleting flows added during test')

        # using threads to delete to speed up cleanup
        items_to_delete = list(self.active_map.items())
        self.thread_pool = []
        thread_id = 0
        slice_from = REMOVE_FLOWS_PER_THREAD * thread_id
        slice_to = REMOVE_FLOWS_PER_THREAD * (thread_id + 1)

        while(slice_from < len(items_to_delete)):
            self.thread_pool.append(FlowRemoverThread(self, thread_id, self.host, self.port, self.net, items_to_delete[slice_from:slice_to]))
            thread_id += 1
            slice_from = REMOVE_FLOWS_PER_THREAD * thread_id
            slice_to = REMOVE_FLOWS_PER_THREAD * (thread_id + 1)

        for t in self.thread_pool:
            t.start()

        for t in self.thread_pool:
            t.join()


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
    parser.add_argument('--mnport', type=int, default=6633, help='port on '
                        'which odl\'s controller is listening  (default = 6633)')
    parser.add_argument('--threads', default=50, help='how many threads '
                        'should be used  (default = 50)')
    parser.add_argument('--flows', default=20, help='how many flows will add'
                        ' one thread  (default = 20)')
    parser.add_argument('--log', default='info', help='log level, permitted values are'
                        ' debug/info/warning/error  (default = info)')
#     parser.add_argument('--xmls', default=None, help='generete tests only '
#                         'from some xmls (i.e. 1,3,34)  (default = None)')
    in_args = parser.parse_args()

    #logging.basicConfig(level=logging.DEBUG)
    logging.basicConfig(level=loglevels.get(in_args.log, logging.INFO))

    # set host and port of ODL controller for test cases
    MultiTest.port = in_args.odlport
    MultiTest.host = in_args.odlhost
    MultiTest.mn_port = in_args.mnport
    MultiTest.threads = int(in_args.threads)
    MultiTest.flows = int(in_args.flows)

    del sys.argv[1:]

    suite = unittest.TestSuite()
    test = MultiTest('test')
    suite.addTest(test)

    try:
        unittest.TextTestRunner(verbosity=2).run(suite)
    finally:
        test.net.stop()
