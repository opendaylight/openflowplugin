import logging
import os
import re
from xml.etree import ElementTree as ET

class Loader():

    @staticmethod
    def loadXml(file_name):
        path_to_xml = os.path.join('', file_name)
        with open(path_to_xml) as f:
                xml_string = f.read()
                xml_string = re.sub(' xmlns="[^"]+"', '', xml_string, count=1)

        tree = ET.fromstring(xml_string)
        return tree, xml_string

    @staticmethod
    def buildXmlDocDictionaryForComarableElements(element, flow_dict, p_elm_name=None, kwd=None, akwd=None, mkwd=None):
        act_key_dict = kwd if (kwd > None) else akwd if (akwd > None) else mkwd if (mkwd > None) else None
        if element > None :
            elm_alias = element.tag if (act_key_dict.get(element.tag, None) > None) else None
            if ((element.getchildren() > None) & (len(element.getchildren()) > 0)):
                for child in element.getchildren() :
                    if (element.tag == 'match') :
                        Loader.buildXmlDocDictionaryForComarableElements(child, flow_dict, mkwd=mkwd)
                    elif (element.tag == 'actions') :
                        Loader.buildXmlDocDictionaryForComarableElements(child, flow_dict, akwd=akwd)
                    else :
                        Loader.buildXmlDocDictionaryForComarableElements(child, flow_dict, elm_alias, kwd, akwd, mkwd);
            else :
                if element.text > None :
                    text = re.sub( '[\s]+','', element.text, count=1)
                    a_key = p_elm_name if (p_elm_name > None) else element.tag
                    flow_dict[a_key] = text;
        return

type_int = 1
type_boolean = 2
type_ethernet = 3
type_ipv4 = 4
type_ipv6 = 5

class Field():
    """
        fields to check, arguments:
        key: element tag from keywords and xml
        bits: expected length in bits
        prerequisites: dictionary of elements tag from xml which are required for this field and their values in list
                       or [None] if value is undefined or it's irrelevant (we just need to check if tag is set)
        convert_from: format in which is value, that is checked against prerequisite values stored in xml

        e.g.:
        key:'ipv4-source'
        bits:32
        prerequisites: {'ethernet-type': [2048]}
        convert_from: 10

        OF_IPV4_SRC = Field('ipv4-source', 32, {'ethernet-type': [2048]}, 10)
        IN_PHY_PORT = Field('in-phy-port', 32, {'in-port': [None]}, 10)
    """

    def __init__(self, key, bits, prerequisites=None, convert_from=10, value_type=type_int):
        self.key = str(key)
        self.bits = bits
        if prerequisites is not None:
            self.prerequisites = dict(prerequisites)
        else:
            self.prerequisites = None
        self.convert_from = convert_from
        self.value_type = value_type

    def __str__(self):
        return "Field: {}, size: {}, prerequisites: {}"\
            .format(self.key, self.bits, self.prerequisites)


class XMLValidator():

    log = logging.getLogger('XMLValidator')
    log.propagate=False
    channel = logging.StreamHandler()
    log.addHandler(channel)

    def __init__(self, kwd, akwd, mkwd, loglevel=logging.INFO):

        self.test_name = 'No test loaded'
        XMLValidator.log.setLevel(loglevel)

        self.xml_ok = True
        self.fields = list()
        self.flow_dict = dict()

        self.kwd = kwd
        self.akwd = akwd
        self.mkwd = mkwd

    def create_dictionaries(self, file_name):
        self.test_name = file_name

        formatter = logging.Formatter('TEST {}: %(levelname)s: %(message)s'.format(self.test_name))
        XMLValidator.channel.setFormatter(formatter)

        self.flow_dict = dict()
        treeXml1, self.xml_string = Loader.loadXml(file_name)
        Loader.buildXmlDocDictionaryForComarableElements(treeXml1, self.flow_dict, kwd=self.kwd, akwd=self.akwd, mkwd=self.mkwd)
        XMLValidator.log.debug('loaded dict from xml: {}'.format(self.flow_dict))


    def fill_fields(self):
        Matchers.fill_validator(self)

    def add_field(self, fields):
        self.fields.append(fields)

    def integer_check(self, value, bits, convert_from=10):
        XMLValidator.log.debug('validating integer: {}'.format(value))
        if (int(value, convert_from) / 2**bits) > 0:
            XMLValidator.log.error('value: {} is larger than expected: {}'.format(value, 2**bits))
            raise StandardError

    def boolean_check(self, value, bits):
        XMLValidator.log.debug('validating boolean: {}'.format(value))
        if bits < 1:
            XMLValidator.log.error('value: {} is larger than expected: {}'.format(value, 2**bits))
            raise StandardError

    def ethernet_check(self, a):
        XMLValidator.log.debug('validating ethernet address: {}'.format(a))
        numbers = a.split(':')
        max_range = (2**8) - 1

        for n in numbers:
            if int(n, 16) > max_range:
                XMLValidator.log.error('octet: {} in ethernet address: {} larger than: {}'.format(n, a, max_range))
                raise StandardError

    def ipv4_check(self, a):
        IP_MASK_COMPARE_PATTERNS = {
                '24' : '.0',
                '16' : '.0.0',
                '8' : '.0.0.0'
        }
        XMLValidator.log.debug('validating ipv4 address: {}'.format(a))
        ip_arr = a.split('/')
        if (len(ip_arr) > 1) :
            m_patt = IP_MASK_COMPARE_PATTERNS.get(ip_arr[1], None)
            if (m_patt is None) :
                raise StandardError('{} is not valid ipv4 mask'.format(ip_arr[1]))
            if (ip_arr[0].endswith(m_patt) != True) :
                raise StandardError('ipv4 address mask has to *{}/{}'.format(m_patt, ip_arr[1]))

        mask_pos = a.find('/')
        if mask_pos > 0:
            a = a[:mask_pos]

        numbers = a.split('.')
        max_range = (2**8) - 1

        for n in numbers:
            if int(n) > max_range:
                XMLValidator.log.error('octet: {} in ipv4 address: {} larger than: {}'.format(n, a, max_range))
                raise StandardError

    def ipv6_check(self, a):
        XMLValidator.log.debug('validating ipv6 address: {}'.format(a))
        mask_pos = a.find('/')
        if mask_pos > 0:
            a = a[:mask_pos]

        numbers = a.split(':')
        max_range = (2**16) - 1

        for n in numbers:
            #if n == '' then the number is 0000 which is always smaller than max_range
            if n != '' and int(n, 16) > max_range:
                XMLValidator.log.error('number: {} in ipv6 address: {} larger than: {}'.format(n, a, max_range))
                raise StandardError

    def check_size(self, value, bits, value_type, convert_from=10):
        XMLValidator.log.debug('checking value: {}, size should be {} bits'.format(value, bits))
        ipv6_regexp = re.compile("^[0-9,A-F,a-f]{0,4}(:[0-9,A-F,a-f]{0,4}){1,7}(/[0-9]{1,3})?$")
        ipv4_regexp = re.compile("^([0-9]{1,3}\.){3}[0-9]{1,3}(/[0-9]{1,2})?$")
        ethernet_regexp = re.compile("^[0-9,A-F,a-f]{2}(:[0-9,A-F,a-f]{2}){5}$")

        try:
            if value_type == type_boolean and value in ['true', 'false']:  #boolean values
                    self.boolean_check(value, bits)
            elif value_type == type_ethernet and ethernet_regexp.match(value):  #ethernet address
                self.ethernet_check(value)
            elif value_type == type_ipv4 and ipv4_regexp.match(value):  #IPV4 address
                self.ipv4_check(value)
            elif value_type == type_ipv6 and ipv6_regexp.match(value):  #IPV6 address
                self.ipv6_check(value)
            elif value_type == type_int:  #integer values
                self.integer_check(value, bits, convert_from)
            else:
                raise StandardError

            XMLValidator.log.info('size of: {} < 2^{} validated successfully'.format(value, bits))

        except ValueError:
            XMLValidator.log.error('problem converting value to int or IP addresses: {}'.format(value))
            self.xml_ok = False

        except TypeError:
            XMLValidator.log.error('problem converting value: {}, TypeError'.format(value))
            self.xml_ok = False

        except StandardError:
            XMLValidator.log.error('problem checking size for value: {}'.format(value))
            self.xml_ok = False


    def has_prerequisite(self, key, values, convert_from, flow_dict):
        XMLValidator.log.debug('checking prerequisite: {} - {}'.format(key, values))
        try:
            flow_value_raw = flow_dict[key]

            #if prerequisites values are [None] we don't care about actual value
            if values != [None]:
                flow_value = int(flow_value_raw, convert_from)

                if flow_value not in values:
                    raise StandardError()

            XMLValidator.log.info('prerequisite {}: {} to value {} validated successfully'.format(key, values, flow_value_raw))

        except KeyError:
            XMLValidator.log.error('can\'t find element: {} in xml {} or in keywords {}'.format(key, self.xml_string, self.mkwd.keys()))
            self.xml_ok = False

        except ValueError or TypeError:
            # flow_value_raw is string that cannot be converted to decimal or hex number or None
            if flow_value_raw not in values:
                XMLValidator.log.error('can\'t find element: {} with value value: {} '
                               'in expected values {}'.format(key, flow_value_raw, values))
                self.xml_ok = False
            else:
                XMLValidator.log.info('prerequisite {}: {} to value {} validated successfully'.format(key, values, flow_value_raw))

        except StandardError:
            XMLValidator.log.error('can\'t find element: {} with value value: {} '
                           'in expected values {}'.format(key, flow_value, values))
            self.xml_ok = False

    def check_all_prerequisites(self, prerequisites_dict, convert_from, flow_dict):
        XMLValidator.log.debug('checking prerequisites: {}'.format(prerequisites_dict))
        for k, v in prerequisites_dict.items():
            self.has_prerequisite(k, v, convert_from, flow_dict)

    def check_single_field(self, field, flow_dict):
        """
        @type field MatchField
        @type flow_dict dict
        """

        if field.key not in flow_dict:
            XMLValidator.log.debug('{} is not set in XML, skipping validation'.format(field.key))
            return
        else:
            XMLValidator.log.info('validating: {}'.format(field))

        if field.bits is not None:
            self.check_size(flow_dict[field.key], field.bits, field.value_type, field.convert_from)

        if field.prerequisites is not None:
            self.check_all_prerequisites(field.prerequisites, field.convert_from, flow_dict)

    def validate_fields(self):
        self.xml_ok = True
        XMLValidator.log.info('validating against flow: {}'.format(self.flow_dict))
        for field in self.fields:
            self.check_single_field(field, self.flow_dict)

    def validate_misc_values(self):
        for kw in self.kwd.keys():
            if kw in self.flow_dict.keys():
                XMLValidator.log.info('validating: {}: {}'.format(kw, self.flow_dict[kw]))
                try:
                    value = int(self.flow_dict[kw])
                    if value < 0:
                        XMLValidator.log.error('value: {}: {} should be non-negative'.format(kw, self.flow_dict[kw]))
                        self.xml_ok = False
                    else:
                        XMLValidator.log.info('value: {}: {} validated successfully'.format(kw, self.flow_dict[kw]))
                except StandardError:
                    XMLValidator.log.error('can\'t convert value: {}: {} to integer'.format(kw, self.flow_dict[kw]))
                    self.xml_ok = False
            else:
                XMLValidator.log.debug('{} is not set in XML, skipping validation'.format(kw))

    def validate(self):
        self.validate_fields()
        self.validate_misc_values()

        XMLValidator.log.info('XML valid: {}'.format(self.xml_ok))

        return self.xml_ok

class Matchers():

    IN_PORT = Field('in-port', 32)
    IN_PHY_PORT = Field('in-phy-port', 32, {'in-port': [None]})
    METADATA = Field('metadata', 64, convert_from=16)

    ETH_DST = Field('ethernet-source', 48, value_type=type_ethernet)
    ETH_SRC = Field('ethernet-destination', 48, value_type=type_ethernet)
    ETH_TYPE = Field('ethernet-type', 16)

    VLAN_VID = Field('vlan-id', 13)
    VLAN_PCP = Field('vlan-pcp', 3, {'vlan-id': [None]})

    IP_DSCP = Field('ip-dscp', 6, {'ethernet-type': [2048, 34525]})
    IP_ENC = Field('ip-ecn', 2, {'ethernet-type': [2048, 34525]})
    IP_PROTO = Field('ip-protocol', 8, {'ethernet-type': [2048, 34525]})

    IPV4_SRC = Field('ipv4-source', 32, {'ethernet-type': [2048]}, value_type=type_ipv4)
    IPV4_DST = Field('ipv4-destination', 32, {'ethernet-type': [2048]}, value_type=type_ipv4)

    TCP_SRC = Field('tcp-source-port', 16, {'ip-protocol': [6]})
    TCP_DST = Field('tcp-destination-port', 16, {'ip-protocol': [6]})
    UDP_SRC = Field('udp-source-port', 16, {'ip-protocol': [17]})
    UDP_DST = Field('udp-destination-port', 16, {'ip-protocol': [17]})
    SCTP_SRC = Field('sctp-source-port', 16, {'ip-protocol': [132]})
    SCTP_DST = Field('sctp-destination-port', 16, {'ip-protocol': [132]})
    ICMPV4_TYPE = Field('icmpv4-type', 8, {'ip-protocol': [1]})
    ICMPV4_CODE = Field('icmpv4-code', 8, {'ip-protocol': [1]})

    ARP_OP = Field('arp-op', 16, {'ethernet-type': [2054]})
    ARP_SPA = Field('arp-source-transport-address', 32, {'ethernet-type': [2054]}, value_type=type_ipv4)
    ARP_TPA = Field('arp-target-transport-address', 32, {'ethernet-type': [2054]}, value_type=type_ipv4)
    ARP_SHA = Field('arp-source-hardware-address', 48, {'ethernet-type': [2054]}, value_type=type_ethernet)
    ARP_THA = Field('arp-target-hardware-address', 48, {'ethernet-type': [2054]}, value_type=type_ethernet)

    IPV6_SRC = Field('ipv6-source', 128, {'ethernet-type': [34525]}, value_type=type_ipv6)
    IPV6_DST = Field('ipv6-destination', 128, {'ethernet-type': [34525]}, value_type=type_ipv6)
    IPV6_FLABEL = Field('ipv6-flabel', 20, {'ethernet-type': [34525]})

    ICMPV6_TYPE = Field('icmpv6-type', 8, {'ip-protocol': [58]})
    ICMPV6_CODE = Field('icmpv6-code', 8, {'ip-protocol': [58]})

    IPV6_ND_TARGET = Field('ipv6-nd-target', 128, {'icmpv6-type': [135, 136]}, value_type=type_ipv6)
    IPV6_ND_SLL = Field('ipv6-nd-sll', 48, {'icmpv6-type': [135]}, value_type=type_ethernet)
    IPV6_ND_TLL = Field('ipv6-nd-tll', 48, {'icmpv6-type': [136]}, value_type=type_ethernet)

    MPLS_LABEL = Field('mpls-label', 20, {'ethernet-type': [34887, 34888]})
    MPLS_TC = Field('mpls-tc', 3, {'ethernet-type': [34887, 34888]})
    MPLS_BOS = Field('mpls-bos', 1, {'ethernet-type': [34887, 34888]})

    PBB_ISID = Field('pbb-isid', 24, {'ethernet-type': [35047]})
    TUNNEL_ID = Field('tunnel-id', 64)
    IPV6_EXTHDR = Field('ipv6-exthdr', 9, {'ethernet-type': [34525]})


    @staticmethod
    def fill_validator(validator):
        """
        @type validator XMLValidator
        """

        validator.add_field(Matchers.IN_PORT)
        validator.add_field(Matchers.IN_PHY_PORT)
        validator.add_field(Matchers.METADATA)
        validator.add_field(Matchers.ETH_DST)
        validator.add_field(Matchers.ETH_SRC)
        validator.add_field(Matchers.ETH_TYPE)
        #validator.add_field(Matchers.VLAN_VID) - incorrenct XML parsing, if vlan-id-present is present its overriden by it, need to fix loader
        validator.add_field(Matchers.VLAN_PCP)
        validator.add_field(Matchers.IP_DSCP)
        validator.add_field(Matchers.IP_ENC)
        validator.add_field(Matchers.IP_PROTO)
        validator.add_field(Matchers.IPV4_SRC)
        validator.add_field(Matchers.IPV4_DST)
        validator.add_field(Matchers.TCP_SRC)
        validator.add_field(Matchers.TCP_DST)
        validator.add_field(Matchers.UDP_SRC)
        validator.add_field(Matchers.UDP_DST)
        validator.add_field(Matchers.SCTP_SRC)
        validator.add_field(Matchers.SCTP_DST)
        validator.add_field(Matchers.ICMPV4_TYPE)
        validator.add_field(Matchers.ICMPV4_CODE)
        validator.add_field(Matchers.ARP_OP)
        validator.add_field(Matchers.ARP_SPA)
        validator.add_field(Matchers.ARP_TPA)
        validator.add_field(Matchers.ARP_SHA)
        validator.add_field(Matchers.ARP_THA)
        validator.add_field(Matchers.IPV6_SRC)
        validator.add_field(Matchers.IPV6_DST)
        validator.add_field(Matchers.IPV6_FLABEL)
        validator.add_field(Matchers.ICMPV6_TYPE)
        validator.add_field(Matchers.ICMPV6_CODE)
        validator.add_field(Matchers.IPV6_ND_TARGET)
        validator.add_field(Matchers.IPV6_ND_SLL)
        validator.add_field(Matchers.IPV6_ND_TLL)
        validator.add_field(Matchers.MPLS_LABEL)
        validator.add_field(Matchers.MPLS_TC)
        validator.add_field(Matchers.MPLS_BOS)
        validator.add_field(Matchers.PBB_ISID)
        validator.add_field(Matchers.TUNNEL_ID)
        validator.add_field(Matchers.IPV6_EXTHDR)


if __name__ == '__main__':

    keywords = None
    with open('keywords.csv') as f:
        keywords = dict(line.strip().split(';') for line in f if not line.startswith('#'))

    #print keywords

    match_keywords = None
    with open('match-keywords.csv') as f:
        match_keywords = dict(line.strip().split(';') for line in f if not line.startswith('#'))

    #print match_keywords

    action_keywords = None
    with open('action-keywords.csv') as f:
        action_keywords = dict(line.strip().split(';') for line in f if not line.startswith('#'))

    paths_to_xml = list()
    for i in range(1, 50):
        #paths_to_xml = ['xmls/f5.xml', 'xmls/f14.xml', 'xmls/f23.xml', 'xmls/f25.xml']
        paths_to_xml.append('xmls/f%d.xml' % i)

    validator = XMLValidator(keywords, action_keywords, match_keywords, logging.ERROR)
    validator.fill_fields()

    for path in paths_to_xml:
        validator.create_dictionaries(path)
        validator.validate()


