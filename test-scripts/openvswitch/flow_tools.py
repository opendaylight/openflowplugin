import logging
import requests
import threading

from openvswitch.parser_tools import ParseTools
import xml.dom.minidom as md


TO_GET = 30
TO_PUT = 10
TO_DEL = 30
WAIT_TIME = 15
OPERATIONAL_DELAY = 11
FLOWS_PER_SECOND = 5

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
    '<cookie>'+COOKIE_TEMPLATE+'</cookie><idle-timeout>34000</idle-timeout><flow-name>'+FLOW_NAME_TEMPLATE+'</flow-name><priority>'+PRIORITY_TEMPLATE+'</priority>' \
    '<barrier>false</barrier></flow>'

class MapNames():
    TEST = 'TEST'
    DUMMY = 'DUMMY'

loglevels = {
    'debug' : logging.DEBUG,
    'info' : logging.INFO,
    'warning' : logging.WARNING,
    'error' : logging.ERROR
}


class FlowAdderThread(threading.Thread):
    """
    Thread to remove flows from TestClassAdd
    
    TestClassAdd should implement methods inc_flow(value, key) and inc_error()
    """

    def __init__(self, test_class, thread_id, host, port, net, flows_ids_from=0, flows_ids_to=1):
        """
        test_class: should be type of TestClassAdd
        thread_id: id of thread
        host: controller's ip address
        port: controller's port
        net: mininet instance
        flows_ids_from: minimum id of flow to be added (including)
        flows_ids_to: maximum id of flow to be added (excluding)
        """

        threading.Thread.__init__(self)
        self.thread_id = thread_id
        self.flows = 0
        self.errors = 0
        self.flows_ids_from = flows_ids_from
        self.flows_ids_to = flows_ids_to

        self.test_class = test_class
        self.net = net
        self.host = host
        self.port = port

        self.flows = 0
        self.errors = 0

        self.log = logging.getLogger('FlowAdderThread: ' + str(thread_id))
        self.log.propagate = False
        self.channel = logging.StreamHandler()
        self.log.addHandler(self.channel)
        formatter = logging.Formatter('THREAD A{0}: %(levelname)s: %(message)s'.format(self.thread_id))
        self.channel.setFormatter(formatter)

        self.log.info('created new FlowAdderThread->id:{0}, flows id: {1} -> {2}'.format(self.thread_id, self.flows_ids_from, self.flows_ids_to))

    def make_cookie_marker(self, val_input, number=0):
        return '0x' + "{0:x}".format(int(''.join("{0:x}".format(ord(c)) for c in (val_input)), 16) + number)

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
        cookie_id = None

        try:
            self.log.debug('adding flow id: {0}'.format(act_flow_id))

            cookie_id = self.make_cookie_marker('stress', act_flow_id)

            xml_string = str(xml_template).replace(FLOW_ID_TEMPLATE, str(act_flow_id))\
            .replace(COOKIE_TEMPLATE, str(int(cookie_id, 16)))\
            .replace(HARD_TO_TEMPLATE, '1200').replace(FLOW_NAME_TEMPLATE,'FooXf{0}'.format(act_flow_id))\
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
            self.log.debug('sending request to url: {0}'.format(url))
            rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                               headers=headers, timeout=TO_PUT)
            self.log.debug('received status code: {0}'.format(rsp.status_code))
            self.log.debug('received content: {0}'.format(rsp.text))
            assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                            ' code returned %d' % rsp.status_code

            # we expect that controller doesn't fail to store flow on switch
            self.test_class.inc_flow(flow_id=act_flow_id, cookie_id=cookie_id)
            self.flows += 1
        except Exception as e:
            self.log.error('Error storing flow id:{0}, cookie-id:{1}, reason: {2}'.format(act_flow_id, cookie_id, str(e)))
            self.test_class.inc_error()
            self.errors += 1

    def run(self):
        self.flows, self.errors = 0, 0
        self.log.info('adding flows {0} to {1}'.format(self.flows_ids_from, self.flows_ids_to))
        for i in range(self.flows_ids_from, self.flows_ids_to + 1):
            self.__add_flows(i)

        self.log.info('finished, added {0} flows, {1} errors'.format(self.flows,self.errors))



class FlowRemoverThread(threading.Thread):
    """
    Thread to remove flows from TestClassDelete
    
    TestClassDelete should implement method delete_flows_from_map(value, key)
    """


    def __init__(self, test_class, thread_id, host, port, net, flows_to_delete=[]):
        """
        test_class: should be type of TestClassDelete
        thread_id: id of thread
        host: controller's ip address
        port: controller's port
        net: mininet instance
        flows_to_delete: dictionary of flows to delete with items to match method delete_flows_from_map(value, key)
        """

        threading.Thread.__init__(self)
        self.thread_id = thread_id
        self.flows_to_delete = flows_to_delete
        self.test_class = test_class


        self.removed = 0
        self.errors = 0

        self.net = net
        self.host = host
        self.port = port

        self.log = logging.getLogger('FlowRemoverThread: ' + str(thread_id))
        self.log.propagate = False
        self.channel = logging.StreamHandler()
        self.log.addHandler(self.channel)
        formatter = logging.Formatter('THREAD R{0}: %(levelname)s: %(message)s'.format(self.thread_id))
        self.channel.setFormatter(formatter)

        self.log.info('created new FlowRemoverThread->id:{0}'.format(self.thread_id))

    def __remove_flows(self, act_flow_id, cookie_id):
        headers = {'Content-Type': 'application/xml', 'Accept': 'application/xml'}
        url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                '/node/openflow:1/table/2/flow/%s' % (self.host, self.port, act_flow_id)

        try:
            response = requests.delete(url, auth=('admin','admin'), headers=headers, timeout=TO_DEL)
            self.log.debug('deletion flow: {0} from controller: response: {1}'.format(act_flow_id, response.status_code))

            assert response.status_code == 200 or response.status_code == 204, 'Delete response should be 200 or 204 is {0}'.format(response.status_code)
            self.test_class.delete_flow_from_map(act_flow_id, cookie_id)
            self.removed += 1

        except Exception as e:
            self.log.error('Error deleting flow:{0}, reason: {1}'.format(act_flow_id, str(e)))
            self.errors += 1
        except requests.exceptions.Timeout as te:
            self.log.error('Error deleting flow: {0}, timeout reached: {1}'.format(act_flow_id, str(te)))
            self.errors += 1


    def run(self):
        self.log.debug('started removing flows')
        for flow_ids in set(self.flows_to_delete):
            self.__remove_flows(flow_ids[1], flow_ids[0])

        self.log.info('finished removing {0} flows, {1} errors'.format(self.removed, self.errors))

