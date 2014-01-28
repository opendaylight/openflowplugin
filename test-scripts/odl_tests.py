#!/usr/bin/env python

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
        {'remote': RemoteController}, 'remote,ip=%s,port=%s' % (controller_ip,
                                                           controller_port))


    net = mininet.net.Mininet(topo=topo, switch=switch, controller=controller)

    return net


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
    def parse_matches(flow, matches):
        flow['matches'] = {}

        for match in matches:
            split_match = match.split('=', 1)
            if len(split_match) == 1:
                flow['matches'][split_match[0]] = None
            else:
                flow['matches'][split_match[0]] = split_match[1].rstrip(',')

    switch = net.switches[0]
    output = switch.cmdPrint(
        'ovs-ofctl -O OpenFlow13 dump-flows %s' % switch.name)
#    output = switch.cmdPrint(
#        'ovs-ofctl -F openflow10 dump-flows %s' % switch.name)

    log.debug('switch flow table: {}'.format(output))

    flows = []

    for line in output.splitlines()[1:]:
        flow = {}
        for word in line.split():
            word.rstrip(',')
            try:
                key, value = word.split('=', 1)
            except ValueError:
                #TODO: need to figure out what to do here?
                continue

            if key == 'priority':
                values = value.split(',')
                flow[key] = values[0]
                parse_matches(flow, values[1:])
            else:
                flow[key] = value.rstrip(',')

        flows.append(flow)

    # sort by duration 
    return sorted(flows, key=lambda x: x['duration'].rstrip('s'))


def translate_to_flow(flow, name, dictionary):
    switch_flow_name = dictionary[name]

    key_err = '{} needs to be present in flow definition. Flow definition ' \
              'was: {}.'.format(switch_flow_name, flow)
    assert switch_flow_name in flow, key_err
    return switch_flow_name


def get_text_value(element):
    return element.childNodes[0].nodeValue


def compare_elements(expected_match, actual_match, kw, comparators, default):
    for child in expected_match.childNodes:
        if child.nodeType is expected_match.TEXT_NODE:
            continue
      
        comparator = comparators.get(child.nodeName, default)
        comparator(child, actual_match, kw)


def fallback_comparator(xml_element, switch_flow, kw):
    """
    A comparator for simple strings. Used when no special handling is required.
    """
    # print 'fallback_comparator-xml_element', xml_element.toxml()
    # print 'fallback_comparator: switch_flow', switch_flow
    # print 'fallback_comparator: kw', kws

    name = translate_to_flow(switch_flow, xml_element.nodeName, kw)

    actual = switch_flow[name]
    expected = xml_element.childNodes[0].nodeValue

    data = xml_element.toxml(), name, actual
    # print 'fallback_comparator: data', data
    assert expected == actual, 'xml part: %s && switch %s=%s' % data


def default_comparator(xml_element, switch_flow):
    """
    The comparatorA parametrized comparator for simple strings - uses  the 
    top level keyword set.
    """
    fallback_comparator(xml_element, switch_flow, keywords)


def integer_comparator(expected, actual, kw, base):
    """
    Comparator for integers in different formats; in xml, integers are always
    decimal, but OVS displays number either as decimal or as hexadecimal.
    This is a generic comparator that can use either base.
    """
    expected_value = int(expected.childNodes[0].data)

    name = kw.get(expected.nodeName)
    actual_value = int(actual[name], base)

    data = expected.toxml(), name, actual
    assert expected_value == actual_value, \
        'xml value: %s && actual value %s=%s' % data


def cookie_comparator(cookie, switch_flow):
    """
    Compare cookie values. Cookies are displays in hex in OVS
    """
    integer_comparator(cookie, switch_flow, keywords, 16)


def ethernet_address_comparator(child, actual_match, kw):
    """ Compare ethernet addresses. """
    expected_address = child.getElementsByTagName("address")[0].childNodes[0].data
    actual_address = actual_match[kw.get(child.nodeName)]

    data = child.toxml(), kw.get(child.nodeName), actual_address

    assert lower(expected_address) == lower(actual_address), \
        'xml address: %s && actual address %s=%s' % data


def masked_value_hex_comparator(child, actual_match, kw, vname, kname):
    """
    Generic function to compar value-mask pairs that occur quite often in XML
    TODO: finish mask parsing and processing.
    """
    print 'masked_value_hex_comparator', child.toxml(), actual_match, \
        vname, kname, child.nodeName

    emd = int(child.getElementsByTagName(vname)[0].childNodes[0].data)

    name = kw.get(vname)
    data = child.toxml(), name, actual_match
    print 'masked_value_hex_comparator', name

    amd = int(actual_match[name], 16)

    emasks = child.getElementsByTagName(kname)
    if len(emasks) != 0:
        print 'masked_value_hex_comparator - mask present:', \
            emasks[0].childNodes[0].data

    assert emd == amd, 'metadata: expected %s && actual %s=%s' % data



def proto_match_comparator(expected_match, actual_match, kw):

    def compare_base10_integer(expected_match, actual_match, kw):
        integer_comparator(expected_match, actual_match, kw, 10)

    def compare_vlan_id(expected_match, actual_match, kw):
        integer_comparator(expected_match.getElementsByTagName('vlan-id')[0], \
                           actual_match, kw, 10)

    def compare_pbb(expected, actual, kw):
        masked_value_hex_comparator(expected, actual, kw, \
                                    'pbb-isid', 'pbb-mask')

    PROTO_COMPARATORS = {
        'vlan-id': compare_vlan_id,
        'pbb': compare_pbb
    }    

    # print 'ethernet_match_comparator-expected_match:', expected_match.toxml()
    # print 'ethernet_match_comparator-actual_match:', actual_match

    compare_elements(expected_match, actual_match, kw, \
                     PROTO_COMPARATORS, compare_base10_integer)


#def masked_value_hex_comparator(child, actual_match, kw):
#    emd = int(child.getElementsByTagName("metadata")[0].childNodes[0].data)
#
#    name = kw.get(child.nodeName)
#    data = child.toxml(), name, actual_match
#
#    amd = int(actual_match[kw.get(name)], 16)
#
#    emasks = child.getElementsByTagName("metadata-mask")
#    if len(emasks) != 0:
#        print 'mask present'
#
#    assert emd == amd, 'metadata: expected %s && actual %s=%s' % data



def ethernet_match_comparator(expected_match, actual_match, kw):
    def compare_etype(child, actual_match, kw):
        expected_etype = \
            int(child.getElementsByTagName("type")[0].childNodes[0].data)
        name = kw.get(child.nodeName)
        data = child.toxml(), name, actual_match

        if expected_etype == 2048: # IPv4
            assert((actual_match.get('ip', 'IP Not-present') is None) or \
                   (actual_match.get('tcp', 'TCP Not-present') is None) or \
                   (actual_match.get('icmp', 'ICMP Not-present') is None) or \
                   (actual_match.get('sctp', 'SCTP Not-present') is None) or \
                   (actual_match.get('udp', 'UDP Not-present') is None)), \
                'Expected etype %s && actual etype %s=%s' % data
 
        elif expected_etype == 2054: # ARP
            assert actual_match.get('arp', 'ARP Not-present') is None, \
                     'Expected etype %s && actual etype %s=%s' % data

        elif expected_etype == 34887: # MPLS
            assert actual_match.get('mpls', 'MPLS Not-present') is None, \
                     'Expected etype %s && actual etype %s=%s' % data

        elif expected_etype == 34525: # IPv6
            assert((actual_match.get('ipv6', 'IPv6 Not-present') is None) or \
                   (actual_match.get('tcp6', 'TCP6 Not-present') is None) or \
                   (actual_match.get('icmp6', 'ICMP6 Not-present') is None) or \
                   (actual_match.get('sctp6', 'SCTP6 Not-present') is None) or \
                   (actual_match.get('udp6', 'UDP6 Not-present') is None)), \
                'Expected etype %s && actual etype %s=%s' % data

        else:
            actual_etype = int(actual_match[name], 16)

            assert expected_etype == actual_etype, \
                'xml etype: %s && actual etype %s=%s' % data


    ETH_COMPARATORS = {
        'ethernet-type': compare_etype, 
        'ethernet-source': ethernet_address_comparator,
        'ethernet-destination': ethernet_address_comparator,
    }    

    # print 'ethernet_match_comparator-expected_match:', expected_match.toxml()
    # print 'ethernet_match_comparator-actual_match:', actual_match

    compare_elements(expected_match, actual_match, kw, \
                     ETH_COMPARATORS, fallback_comparator)
            

def ip_subnet_comparator(expected_match, actual_match, kw):
    """
    Compare IP addresses; OVS applies the address mask, so we need to compare 
    subnet as opposed to strings.
    """
    # print 'ip_comparator:', expected_match.toxml(), actual_match
    # print 'ip_comparator-actual_match:', actual_match

    expected_value = expected_match.childNodes[0].data
    actual_value = actual_match[kw.get(expected_match.nodeName)]

    data = expected_match.toxml(), kw.get(expected_match.nodeName), actual_value

    assert IPNetwork(expected_value) == IPNetwork(actual_value),\
        'xml part: %s && address %s=%s' % data


def ip_match_comparator(expected_match, actual_match, kw):
    def compare_proto(child, actual_match, kw):
        print 'compare_proto:', child.toxml(), actual_match
        expected_proto = int(child.childNodes[0].data)

        name = child.nodeName
        data = expected_match.toxml(), name, actual_match

        if expected_proto == 1: # ICMP
            assert ((actual_match.get('icmp', 'ICMP Not-present') is None) or \
                    (actual_match.get('icmp6', 'ICMP6 Not-present') is None)), \
                'ip protocol type: expected %s, actual %s=%s' % data

        elif expected_proto == 6: # TCP
            assert ((actual_match.get('tcp', 'TCP Not-present') is None) or \
                    (actual_match.get('tcp6', 'TCP6 Not-present') is None)), \
                'ip protocol type: expected %s, actual %s=%s' % data

        elif expected_proto == 17: #UDP
            assert ((actual_match.get('udp', 'UDP Not-present') is None) or \
                    (actual_match.get('udp6', 'UDP6 Not-present') is None)), \
                'ip protocol type: expected %s, actual %s=%s' % data

        elif expected_proto == 58: # ICMP
            assert actual_match.get('icmp6', 'ICMP6 Not-present') is None, \
                'ip protocol type: expected %s, actual %s=%s' % data

        elif expected_proto == 132: #SCTP
            assert ((actual_match.get('sctp', 'SCTP Not-present') is None) or \
                    (actual_match.get('sctp6', 'SCTP6 Not-present') is None)), \
                'ip protocol type: expected %s, actual %s=%s' % data

        else:
            fallback_comparator(child, actual_match, kw)


    def compare_dscp(child, actual_match, kw):
        # print 'compare_dscp:', child.toxml(), actual_match

        expected_dscp = int(child.childNodes[0].data)
        name = kw.get(child.nodeName)
        actual_dscp = int(actual_match[name])
        
        data = child.toxml(), name, actual_match

        assert (expected_dscp * 4) == actual_dscp, \
            'dscp: expected %s, actual %s=%s' % data


    IP_MATCH_COMPARATORS = {
        'ip-protocol': compare_proto, 
        'ip-dscp': compare_dscp,
    }    

    # print 'ip_match_comparator:', expected_match.toxml(), actual_match
    compare_elements(expected_match, actual_match, kw, \
                     IP_MATCH_COMPARATORS, fallback_comparator)


def match_comparator(expected_match, switch_flow):
    """ The main function to compare flow matches """
    def compare_metadata(expected, actual, kw):
        masked_value_hex_comparator(expected, actual, kw, \
                                    'metadata', 'metadata-mask')

    def compare_ipv6_label(expected, actual, kw):
        print 'compare_ipv6_label', expected.toxml(), actual
        masked_value_hex_comparator(expected, actual, kw, \
                                    'ipv6-flabel', 'flabel-mask')


    def compare_tunnel_id(expected, actual, kw):
        masked_value_hex_comparator(expected, actual, kw, \
                                    'tunnel-id', 'tunnel-mask')


    def compare_ipv6_ext_header(expected, actual, kw):
        masked_value_hex_comparator(expected, actual, kw, \
                                    'ipv6-exthdr', 'ipv6-exthdr-mask')


    MATCH_COMPARATORS = {
        'arp-source-hardware-address': ethernet_address_comparator,
        'arp-target-hardware-address': ethernet_address_comparator,
        'metadata': compare_metadata,
        'ipv6-label': compare_ipv6_label,
        'ipv6-ext-header': compare_ipv6_ext_header,
        'tunnel': compare_tunnel_id,
        'protocol-match-fields': proto_match_comparator,
        'vlan-match': proto_match_comparator,
        'ethernet-match': ethernet_match_comparator,
        'ip-match': ip_match_comparator,
        'icmpv4-match': ip_match_comparator,
        'icmpv6-match': ip_match_comparator,
        'ipv4-destination': ip_subnet_comparator,
        'ipv4-source': ip_subnet_comparator,
        'ipv6-destination': ip_subnet_comparator,
        'ipv6-source': ip_subnet_comparator,
    }

    actual_match = switch_flow['matches']

    # print 'match_comparator-expected_match:', expected_match.toxml()
    # print 'match_comparator-actual_match:', actual_match
    # print 'match_comparator: keywords', keywords

    compare_elements(expected_match, actual_match, match_keywords, \
                     MATCH_COMPARATORS, fallback_comparator)


def actions_comparator(actions, switch_flow):
    """
    The main function to compare actions
    """

    def process_action(name, node):
        """
        Function that includes other helper functions to cover special cases and        to process actions with their arguments
        """

        def process_set_mpls_ttl_output(output, node):
           """
               parse the set_mpls_ttl output from the ovs
               Eg: set_mpls_ttl(1)
           """
           print "NODE:", node
           print "OUTPUT: ", output
           ttl_value = node.getElementsByTagName('mpls-ttl')[0].childNodes[0].data
           print "TTL: ", ttl_value
           action = output + "(" + ttl_value + ")"
           return action


        def process_set_field_output(output, node):
            """
               parse the set_field output from the ovs
               Eg: set_field:2059->tcp_src
            """
            OVS_SET_FIELD_ACTION_MAP = {
                'tcp-source-port': 'tcp_src',
                'tcp-destination-port': 'tcp_dst',
                'udp-source-port': 'udp_src',
                'udp-destination-port': 'udp_dst',
                'sctp-source-port': 'sctp_src',
                'sctp-destination-port': 'sctp_dst',
            }
            field_name = node.childNodes[1].nodeName
            field_value = node.getElementsByTagName(field_name)[0].childNodes[0].data
            action = output + ":" + field_value + "->" + OVS_SET_FIELD_ACTION_MAP.get(field_name)
            print "ACTION: ", action
            return action

        def process_vlan_output(output, node):
            """
               take push_vlan(output), extract the ethernet type in hex
               join them with a ':' and retrun
            """
            ether_type = node.getElementsByTagName("ethernet-type")[0].childNodes[0].data
            action = output + ":" + hex(int(ether_type))
            return action

        def process_output_port(output, node):

            def hasNumbers(inputString):
                return any(char.isdigit() for char in inputString)


            output_port = node.getElementsByTagName("output-node-connector")[0].childNodes[0].data

            if hasNumbers(output_port) is True:
                # TODO: proper parsing of the input port. For now we just 
                # assume that the port_number string is a number.
                action = "%s:%s" % (output, output_port)
                print 'name: ', action

            elif output_port == 'INPORT':
                action = 'IN_PORT'

            elif output_port == 'CONTROLLER':
                max_length = node.getElementsByTagName("max-length")[0].childNodes[0].data
                action = "%s:%s" % (output_port, max_length)

            else:
                action = output_port

            return action


        def passthrough(name, node):
            return name


        NAME_PROCESSORS = {
            'output': process_output_port,
            'push_vlan': process_vlan_output,
            'set_field': process_set_field_output,
            'set_mpls_ttl': process_set_mpls_ttl_output,
        }

        return NAME_PROCESSORS.get(name, passthrough)(name, node)


    actual_actions = switch_flow['actions'].split(",")

    print 'actions_comparator:', actions.toxml(), actual_actions

    for action in actions.childNodes:
        if action.nodeType is actions.TEXT_NODE:
            continue

        name = action.childNodes[3].nodeName

        print 'actions_comparator:', name

        expected_action = action_keywords.get(name)
        print 'actions_comparator expected_action:', expected_action
        expected_action = process_action(expected_action, \
                                         action.childNodes[3])
        print 'actions_comparator processed expected_action:', expected_action

        data = action.toxml(), expected_action
        # print 'actions_comparator:', data
        print str(data)

        assert expected_action in actual_actions, \
            'xml part:\n%s\n expected action: %s' % data


def null_comparator(element, switch_flow):
    pass


def instructions_comparator(instructions_element, switch_flow):
    INSTRUCTION_COMPARATORS = {
        'apply-actions': actions_comparator,
        'default': null_comparator,
    }
    # print 'instructions_comparator:', instructions_element, switch_flow

    instructions = instructions_element.childNodes

    for instruction in instructions_element.childNodes:
        if instruction.nodeType is instructions_element.TEXT_NODE:
            continue
        
        for itype in instruction.childNodes:
            if itype.nodeType is itype.TEXT_NODE:
                continue

            comparator = INSTRUCTION_COMPARATORS.get(itype.nodeName,
                                        INSTRUCTION_COMPARATORS['default'])
            comparator(itype, switch_flow)


COMPARATORS = {
    'cookie': cookie_comparator,
    'instructions': instructions_comparator,
    'match': match_comparator,
    'default': default_comparator,
}

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


def check_elements(xmlstr, keywords):
    # namespace = 'urn:opendaylight:flow:inventory'
    tree = md.parseString(xmlstr)

    for element in all_nodes(tree.documentElement):
        # switch flow object contains only some data from xml
        if element.nodeName not in keywords:
            # print 'check_elements: element.nodeName', element.nodeName, 'NOT in keywords'
            continue

        yield element

    raise StopIteration()


class TestOpenFlowXMLs(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
	print '********Setup Execution******'
        cls.net = create_network(cls.host, cls.mn_port)
        cls.net.start()
        time.sleep(15)

    @classmethod
    def tearDownClass(cls):
	print '*********Tear Down Execution *****'
        cls.net.stop()


def get_values(node, *tags):
    result = {tag: None for tag in tags}
    for node in all_nodes(node):
        if node.nodeName in result and len(node.childNodes) > 0:
            result[node.nodeName] = node.childNodes[0].nodeValue
    return result
    
    
class BadResponseCodeError(Exception):
    def __init__(self, value):
        self.value = value
    def __str__(self):
        return repr('BadResponseCodeError: %s' % self.value)    


def create_test_case(host,port,ids,net,xml_string,testName,testType):
            log = logging.getLogger(__name__)
	    time.sleep(1)
	    if testName == 'Flow':
        	data = (host, port, ids['table_id'], ids['id'])
            	url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  	'/node/openflow:1/table/%s/flow/%s' % data
	    elif testName == 'Meter':
        	data = (host, port, ids['meter-id'])
            	url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  '/node/openflow:1/meter/%s' % data
		print 'URL::',url
	    elif testName == 'Group':
        	data = (host, port, ids['group-id'])
            	url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  	'/node/openflow:1/group/%s' % data
	    elif testName == 'Stats':
        	data = (host, port, ids['stats-id'])
            	url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  	'/node/openflow:1/stats/%s' % data
	    elif testName == 'All' or testName == None:
        	data = (host, port, ids['stats-id'])
            	url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  	'/node/openflow:1/stats/%s' % data
                if ids['table_id'] != None and ids['id'] != None:
        		data = (host, port, ids['table_id'], ids['id'])
            		url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  		'/node/openflow:1/table/%s/flow/%s' % data
             	elif ids['meter-id'] != None:
        		data = (host, port, ids['meter-id'])
            		url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  		'/node/openflow:1/meter/%s' % data
			print 'URL::',url
             	elif ids['group-id'] != None:
        		data = (host, port, ids['group-id'])
            		url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  		'/node/openflow:1/group/%s' % data
             	elif ids['stats-id'] != None:
        		data = (host, port, ids['stats-id'])
            		url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                  		'/node/openflow:1/stats/%s' % data
	    else:
		print 'Invalid option1 ', testName
            headers = {
                'Content-Type': 'application/xml',
                'Accept': 'application/xml',
            }
            log.info('sending request to url: {}'.format(url))
            rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                               headers=headers)                               
	    log.info('received status code: {}'.format(rsp.status_code))
            log.debug('received content: {}'.format(rsp.text))
	    try:
		print 'PUT',url
                assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                    ' code returned %d' % rsp.status_code
	    except AssertionError as e:
	   	log.info('----testName---')
            try:        
	
		#url = url.replace('config','operational')
		print 'Modified GET url',url
		time.sleep(2)	
                # check request content against restconf's datastore
                response = requests.get(url, auth=('admin', 'admin'),
                                        headers={'Accept': 'application/xml'})
                if response.status_code != 200:
                    raise BadResponseCodeError('response: {}'.format(response))
                print 'stored xml ', xml_string
		print 'uploaded xml ', response.text  
                req = xmltodict.parse(ET.tostring(ET.fromstring(xml_string)))
                res = xmltodict.parse(ET.tostring(ET.fromstring(response.text)))
                assert req == res, 'uploaded and stored xml, are not the same\n' \
                    'uploaded: %s\nstored:%s' % (req, res)
		print 'testname-type',testName
		#resultLog.info('Adding',testName,'-Success')
	     	if testName == 'Meter' and testType == 'Modify':
			#Modify functionality for Meter		
		        print 'Modify Meter'		
            		log.info('sending request to url: {}'.format(url))
			xml_string = xml_string.replace('234','111')
			
			print 'Modified XML is:: ', xml_string
            		rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                               headers=headers)                               
	    		log.info('received status code: {}'.format(rsp.status_code))
            		log.debug('received content: {}'.format(rsp.text))
            		assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                    		' code returned %d' % rsp.status_code
		#	result.info('Modify',testName,'-Success')
			print '*******************Modify Meter working fine *******'
		elif testName == 'Flow' and testType == 'Modify':
			#xml_string
			print 'Need to add Flow modify test cases'
		elif testName == 'Group' and testType == 'Modify':
                        #Modify functionality for Group 
                        print 'Modify Group'
		 	xml_string = xml_string.replace('CONTROLLER','FLOOD')
                        log.info('sending request to url: {}'.format(url))
                        print 'Modified XML is ', xml_string
                        rsp = requests.put(url, auth=('admin', 'admin'), data=xml_string,
                               headers=headers)
                        log.info('received status code: {}'.format(rsp.status_code))
                        log.debug('received content: {}'.format(rsp.text))
                        assert rsp.status_code == 204 or rsp.status_code == 200, 'Status' \
                                ' code returned %d' % rsp.status_code
                #       result.info('Modify',testName,'-Success')
		elif testName == 'Stats' and testType == 'Modify':		
			raise AssertionError('No Test Case for Flow Modify')
	     	if testName == 'Flow':
               		 # collect flow table state on switch
                	switch_flows = get_flows(net)
			print 'Testing code::',len(switch_flows)
                	assert len(switch_flows) > 0

                	# compare requested object and flow table state
                	for important_element in check_elements(xml_string, keywords):
                    		# log.info('important element: {}'.format(important_element.nodeName))
                    		comparator = COMPARATORS.get(important_element.nodeName,
                                                 COMPARATORS['default'])

                    	print 'important_element',important_element
		    	print 'actual_element',switch_flows[0] 
		    	comparator(important_element, switch_flows[0])          
	     	else: 
			print 'TestCase:',testName          
            finally:    
                #url = url.replace('operational','config')
                print 'Modified DELETE url',url

                response = requests.delete(url, auth=('admin', 'admin'),
                                    headers={'Accept': 'application/xml'})
                assert response.status_code == 200
                print '\n\n\n'
               


def generate_tests_from_xmls(path, xmls=None, testName='Flow', testType='Add'):
    """ generate test function from path to request xml """
    # define key getter for sorting
    def get_test_number(test_name):
        return int(test_name[1:-4])
    # define test case runner


    def test_case_runner_for_list(xmlfilesList = []):
      overAllTestCases = []
      for xmlfiles in xmlfilesList: 
    	if xmlfiles != None: 
       		for xmlfile in xmlfiles:
			print 'xmlfile',xmlfile
			overAllTestCases.append(xmlfile)
    	else:
		print 'Invalid3 testName  ( Allowed are:  Flow/Group/Stats/Meter )'

      for testCase in overAllTestCases:
	print 'TestName', testCase
        setattr(TestOpenFlowXMLs,'test_xml_'+str(testCase),generate_test(os.path.join(path, testCase)))


    def test_case_runner(xmlfiles):
    	if xmlfiles != None: 
       		for xmlfile in xmlfiles:
        		test_name = 'test_xml_%04d' % get_test_number(xmlfile)
        		setattr(TestOpenFlowXMLs,
                		test_name,
                		generate_test(os.path.join(path, xmlfile)))
    	else:
		print 'Invalid3 testName  ( Allowed are:  Flow/Group/Stats/Meter )'
    def generate_test(path_to_xml):
        xml_string = ''
	if os.path.isfile(path_to_xml): 
          with open(path_to_xml) as f:
            xml_string = f.read()
	else: 
	  print 'File',path_to_xml,'doesnt exists'
	  sys.exit()
        tree = md.parseString(xml_string)
        ids = get_values(tree.documentElement, 'table_id', 'id', 'meter-id', 'group-id','stats-id')
        def new_test(self):
          log = logging.getLogger(__name__)
          # send request throught RESTCONF
	  if testName == 'Flow' or testName == 'Group' or testName== 'Meter' or testName == 'Stats':
                create_test_case(self.host,self.port,ids,self.net,xml_string,testName,testType) 
	  elif testName == 'All' or testName == 'None':
	     if ids['table_id'] != None and ids['id'] != None:
                create_test_case(self.host,self.port,ids,self.net,xml_string,'Flow',testType) 
	     elif ids['meter-id'] != None:
                create_test_case(self.host,self.port,ids,self.net,xml_string,'Meter',testType) 
	     elif ids['group-id'] != None:
                create_test_case(self.host,self.port,ids,self.net,xml_string,'Group',testType) 
	     elif ids['stats-id'] != None:
                create_test_case(self.host,self.port,ids,self.net,xml_string,'Stats',testType) 
	  else:
		print 'Invalid etype  ( Allowed are:  Flow/Group/Stats/Meter/All)'
 
        return new_test

    # generate list of available xml requests
    xmlfiles = None
    xmlfiles_flow = None
    xmlfiles_meter = None
    xmlfiles_group = None

    xmlList = []

    print 'xmls content-',xmls

    if testName == 'Flow': 
     	if xmls is not None:
        	xmlfiles = ('f%d.xml' % fid for fid in xmls)
     	else:
        	xmlfiles = (xml for xml in os.listdir(path) if xml.startswith('f') and xml.endswith('.xml'))
	test_case_runner(xmlfiles)
    elif testName == 'Group':
     	if xmls is not None:
        	xmlfiles = ('g%d.xml' % fid for fid in xmls)
     	else:
        	xmlfiles = (xml for xml in os.listdir(path) if xml.startswith('g') and xml.endswith('.xml'))
	test_case_runner(xmlfiles)
    elif testName == 'Meter':
     	if xmls is not None:
        	xmlfiles = ('m%d.xml' % fid for fid in xmls)
     	else:
        	xmlfiles = (xml for xml in os.listdir(path) if xml.startswith('m') and xml.endswith('.xml'))
	test_case_runner(xmlfiles)
    elif testName == 'Stats':
     	if xmls is not None:
        	xmlfiles = ('s%d.xml' % fid for fid in xmls)
     	else:
        	xmlfiles = (xml for xml in os.listdir(path) if xml.startswith('s') and xml.endswith('.xml'))
	test_case_runner(xmlfiles)
    elif testName == 'All' or testName == 'None':
     	if xmls is not None:
        	xmlfiles_flow = ('f%d.xml' % fid for fid in xmls)
        	xmlfiles_meter = ('m%d.xml' % fid for fid in xmls)
        	xmlfiles_group = ('g%d.xml' % fid for fid in xmls)
        	xmlfiles_stats = ('s%d.xml' % fid for fid in xmls)
		
     	else:
        	xmlfiles_flow = (xml for xml in os.listdir(path) if xml.startswith('f') and xml.endswith('.xml'))
        	xmlfiles_meter = (xml for xml in os.listdir(path) if xml.startswith('m') and xml.endswith('.xml'))
        	xmlfiles_group = (xml for xml in os.listdir(path) if xml.startswith('g') and xml.endswith('.xml'))
        	xmlfiles_stats = (xml for xml in os.listdir(path) if xml.startswith('s') and xml.endswith('.xml'))

	xmlList.append(xmlfiles_flow)
	xmlList.append(xmlfiles_meter)
	#xmlList.append(xmlfiles_group)
	#xmlList.append(xmlfiles_stats)
	test_case_runner_for_list(xmlList)
    else:
        xmlfiles = None


    # define key getter for sorting
    def get_test_number(test_name):
        return int(test_name[1:-4])


if __name__ == '__main__':
    # set up logging
    logging.basicConfig(filename='testResults.log', level=logging.DEBUG)

    # parse cmdline arguments
    parser = argparse.ArgumentParser(description='Run switch <-> ODL tests '
                                     'defined by xmls.')
    parser.add_argument('--odlhost', default='127.0.0.1', help='host where '
                        'odl controller is running')
    parser.add_argument('--odlport', type=int, default=8080, help='port on '
                        'which odl\'s RESTCONF is listening')
    parser.add_argument('--mnport', type=int, default=6653, help='port on '
                        'which odl\'s controller is listening')
    parser.add_argument('--etype', default='None', help='Test case to run '
                        'Flow/Group/Meter/Stats')
    parser.add_argument('--run', default='Add', help='Type of the Test case to run '
                        'Add/Modify')
    parser.add_argument('--xmls', default=None, help='generete tests only '
                        'from some xmls (i.e. 1,3,34) ')
    args = parser.parse_args()

    # set host and port of ODL controller for test cases
    TestOpenFlowXMLs.port = args.odlport
    TestOpenFlowXMLs.host = args.odlhost
    TestOpenFlowXMLs.mn_port = args.mnport

    testName = args.etype
    testType = args.run

    if testName == 'None':
	testName = 'Flow'

    print 'Test name::', testName
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

    if testType != 'Add' and testType != 'Modify':
	print 'Add/Modify are the valid values for --run option!'
	sys.exit()

    # fix arguments for unittest
    del sys.argv[1:]

    # generate tests for TestOpenFlowXMLs
    if args.xmls is not None:
        xmls = map(int, args.xmls.split(','))
        generate_tests_from_xmls('xmls', xmls,testName,testType)
    else:
        generate_tests_from_xmls('xmls',None,testName,testType)

    # run all tests
    unittest.main()


