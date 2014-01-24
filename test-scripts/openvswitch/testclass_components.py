import logging
import re
import requests
import time
from xml.etree import ElementTree as ET

from openvswitch.flow_tools import TO_GET, OPERATIONAL_DELAY, FLOWS_PER_SECOND


class GetFlowsComponent():

    log = logging.getLogger('GetFlowsComponent')

    @staticmethod
    def get_flows(host, port, store='config'):
            url = 'http://%s:%d/restconf/%s/opendaylight-inventory:nodes' \
                '/node/openflow:1/table/2/' % (host, port, store)
            GetFlowsComponent.log.debug('checking flows in controller - sending request to url: {0}'.format(url))
            response = requests.get(url, auth=('admin', 'admin'),
                                    headers={'Accept': 'application/xml'}, timeout=TO_GET)
            return response

class CheckSwitchDump():

    log = logging.getLogger('CheckSwitchDump')

    def get_id_by_entry(self, dump_flow, id_map):
        flow_id = None
        
        try:
           cookie_regexp = re.compile("cookie=0x[0-9,a-f,A-F]+")
           cookie_match = re.search(cookie_regexp, dump_flow)
           if cookie_match is not None:
               cookie_id = cookie_match.group(0)[7:-1]
               flow_id = id_map[cookie_id]
           else:
               CheckSwitchDump.log.info('skipping parsing dump entry: {0} '.format(dump_flow))

        except KeyError as e:
           CheckSwitchDump.log.error('cookie: {0} is not contained in stored flows'.format(cookie_id))
        except StandardError as e:
           CheckSwitchDump.log.error('problem getting stored flow flow_id from cookie from flow dump:{0} reason:{1}'.format(dump_flow, str(e)))

        return flow_id

            
class CheckConfigFlowsComponent(): 

    log = logging.getLogger('CheckConfigFlowsComponent')

    def check_config_flows(self, host, port, id_map):
        try:
            # check config
            response = GetFlowsComponent.get_flows(host, port)
            assert response.status_code == 200, 'response from config must be 200, is {0}'.format(response.status_code)
            tree = ET.ElementTree(ET.fromstring(response.text))

            return GetXMLFlowsComponent.get_xml_flows_by_map(tree.getroot(), id_map)
        except StandardError as e:
            CheckConfigFlowsComponent.log.error('problem getting flows from config: {0}'.format(str(e)))
            return -1

  
class CheckOperFlowsComponent():            

    log = logging.getLogger('CheckOperFlowsComponent')

    def check_oper_flows(self, host, port, id_map, wait_time=OPERATIONAL_DELAY):
        time.sleep(wait_time)

        try:
            response = GetFlowsComponent.get_flows(host, port, 'operational')
            assert response.status_code == 200, 'response from config must be 200, is {0}'.format(response.status_code)
            CheckOperFlowsComponent.log.debug('got resposnse: {0}'.format(response.status_code))
            CheckOperFlowsComponent.log.debug('operational dump:\n{0}'.format(response.text))

            tree = ET.ElementTree(ET.fromstring(response.text))

            # fliter id's
            return GetXMLFlowsComponent.get_xml_flows_by_map(tree.getroot(), id_map)
        except StandardError as e:
            CheckOperFlowsComponent.log.error('problem getting flows from operational: {0}'.format(str(e)))
            return -1

    def check_oper_flows_loop(self, host, port, id_map, max_tries=2):
        # determine how much time we will wait for flows to get on switch
        target_oper_flows = len(id_map)
        current_try = 0
        current_oper_flows = 0

        wait_for_flows = (target_oper_flows / FLOWS_PER_SECOND) + OPERATIONAL_DELAY

        #check operational - in loop for determined number of tries
        while current_oper_flows < target_oper_flows and max_tries > current_try:
            CheckOperFlowsComponent.log.info('checking operational... waiting {0} seconds, {1}/{2}'.format(wait_for_flows, current_try + 1, max_tries))
            current_oper_flows = self.check_oper_flows(host, port, id_map, wait_for_flows)
            CheckOperFlowsComponent.log.info('got {0} flows on {1}. try'.format(current_oper_flows, current_try + 1))
            current_try += 1
        return current_oper_flows
        
class GetXMLFlowsComponent():

    @staticmethod
    def get_xml_flows_by_map(xml_tree, id_map, namespace='{urn:opendaylight:flow:inventory}'):
        element_count = 0

        for e in xml_tree.findall(namespace + 'flow'):
            xml_id = int(e.find(namespace + 'id').text)

            if xml_id in id_map.values():
                element_count += 1

        return element_count    
