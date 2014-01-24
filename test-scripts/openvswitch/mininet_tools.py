'''
Created on Jan 24, 2014

@author: vdemcak
'''

import logging
import mininet.net
from mininet.node import OVSKernelSwitch, RemoteController
import mininet.topo
import mininet.util
import re


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
    
    @staticmethod
    def __mininet_parse_response(resp_str='', x_dict={}, ikwd={}):
        for elm in re.split('\s', resp_str.strip()) :
            elm_prop = re.split('=',elm,1)
            a_key = (elm_prop[0]).strip()
            if (ikwd.get(a_key, None) is None) :
                a_value = ''
                if (len(elm_prop) > 1):
                    if len(elm_prop[1].split('=')) > 1 :
                        new_dict={}
                        MininetTools.__mininet_parse_response(elm_prop[1],new_dict,ikwd)
                        a_value = new_dict
                    else :
                        a_value = elm_prop[1]
                        a_value = a_value.strip() if isinstance(a_value,str) else (str(a_value)).strip()
                x_dict[a_key] = a_value

    
    @staticmethod
    def get_dict_of_flows(net, ikwd={}):
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

        # dictionary for return
        flows = {}
        # flow command prompt output
        output = MininetTools.get_flows_string(net)
        # prepare cmd for parsing to dictionary
        output = output.replace(',',' ') ;
        output = output.replace('  ',' ')

        # action has to be parsed in different way
        if (len(re.split('actions=', output, 1)) > 0) :
            action_str = re.split('actions=',output,1)[1]
            action_dict = {}
            MininetTools.__mininet_parse_response(action_str, action_dict, ikwd)
            flows['actions'] = str(action_dict)
        else :
            flows['actions'] = ''

        # remove actions from string (always last) and parse everything else
        output= re.split('actions=',output,1)[0]
        MininetTools.__mininet_parse_response(output, flows, ikwd)

        return flows

    @staticmethod
    def get_flows_string(net=None):
        """
        Return flows from switch in string format 
        same as by a call 'ovs-ofctl -O OpenFlow13 dump-flows sx'
        """
        if net is None:
            return []

        switch = net.switches[0]
        output = switch.cmdPrint(
        'ovs-ofctl -O OpenFlow13 dump-flows %s' % switch.name)

        return output.splitlines()[1:]