'''
Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.

This program and the accompanying materials are made available under the
terms of the Eclipse Public License v1.0 which accompanies this distribution,
and is available at http://www.eclipse.org/legal/epl-v10.html

Created on May 11, 2014

@author: <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
'''
import xml.dom.minidom as md
from xml.dom.minidom import Element

import requests

from openvswitch.parser_tools import ParseTools
from tools.crud_test_with_param_superclass import OF_CRUD_Test_Base
from tools.xml_parser_tools import XMLtoDictParserTools


METER_ID_TAG_NAME = 'meter-id'
# TODO : find why band-burst-size has same value as dscp-remark (same for band-rate)
IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON = [
    'meter-name',
    'container-name',
    'band-burst-size',
    'band-rate',
    'flags',
    'perc_level',
    'barrier']
METER_TAGS_FOR_UPDATE = ['band-burst-size', 'band-rate']


class OF_CRUD_Test_Meters(OF_CRUD_Test_Base):

    def setUp(self):
        super(OF_CRUD_Test_Meters, self).setUp()
        # ----- PUT -----
        ids = ParseTools.get_values(self.xml_input_DOM, METER_ID_TAG_NAME)
        data = (self.host, self.port, ids[METER_ID_TAG_NAME])
        self.conf_url = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                        '/node/openflow:1/meter/%s' % data
        self.oper_url = 'http://%s:%d/restconf/operational/opendaylight-inventory:nodes' \
                        '/node/openflow:1/meter/%s' % data
        # ----- POST -----
        data = (self.host, self.port)
        self.conf_url_post = 'http://%s:%d/restconf/config/opendaylight-inventory:nodes' \
                             '/node/openflow:1/' % data
        # ----- SAL SERVICE OPERATIONS -----
        self.oper_url_add = 'http://%s:%d/restconf/operations/sal-meter:add-meter' % data
        self.oper_url_upd = 'http://%s:%d/restconf/operations/sal-meter:update-meter' % data
        self.oper_url_del = 'http://%s:%d/restconf/operations/sal-meter:remove-meter' % data
        # Modify input data
        self.data_from_file_input = ''
        for node in self.xml_input_DOM.documentElement.childNodes:
            self.data_from_file_input += node.toxml(encoding='utf-8')

        # The xml body without data - data come from file (all meter subtags)
        self.oper_input_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                                 '<input xmlns="urn:opendaylight:meter:service">\n' \
                                 '  %s' \
                                 '  <node xmlns:inv="urn:opendaylight:inventory">' \
                                 '/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                                 '</input>' % self.data_from_file_input

    def tearDown(self):
        # cleaning configuration DataStore without a response validation
        self.log.info(self._paint_msg_cyan('Uncontrolled cleaning after meter test'))
        requests.delete(self.conf_url,
                        auth=self._get_auth(),
                        headers=self._get_xml_result_header())
        # cleaning device without a response validation
        requests.post(self.oper_url_del,
                      data=self.oper_input_stream,
                      auth=self._get_auth(),
                      headers=self._get_xml_request_result_header())
        super(OF_CRUD_Test_Meters, self).tearDown()

    def test_conf_PUT(self):
        self.log.info("--- Meter conf. PUT test ---")
        # -------------- CREATE -------------------
        self.log.info(self._paint_msg_yellow(" CREATE Meter by PUT REST"))
        # send request via RESTCONF
        self.put_REST_XML_conf_request(self.conf_url, self.xml_input_stream)
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response(self.conf_url)
        xml_resp_stream = (response.text).encode('utf-8', 'ignore')
        xml_resp_DOM = md.parseString(xml_resp_stream)
        self.assertDataDOM(self.xml_input_DOM, xml_resp_DOM)
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, self.xml_input_DOM)

        # -------------- UPDATE -------------------
        self.log.info(self._paint_msg_yellow(" UPDATE Meter by PUT REST"))
        xml_updated_stream = self.__update_meter_input()
        self.put_REST_XML_conf_request(self.conf_url, xml_updated_stream)
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response(self.conf_url)
        xml_resp_stream = (response.text).encode('utf-8', 'ignore')
        xml_resp_DOM = md.parseString(xml_resp_stream)
        xml_upd_DOM = md.parseString(xml_updated_stream)
        self.assertDataDOM(xml_upd_DOM, xml_resp_DOM)
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, xml_upd_DOM)

        # -------------- DELETE -------------------
        self.log.info(self._paint_msg_yellow(" DELETE Meter by DELETE REST "))
        # Delte data from config DataStore
        response = self.delete_REST_XML_response(self.conf_url)
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response(self.conf_url)
        # Meter operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, self.xml_input_DOM, False)

    def test_conf_POST(self):
        self.log.info("--- Meter conf. POST test ---")
        # -------------- CREATE -------------------
        self.log.info(self._paint_msg_yellow(" CREATE Meter by POST REST"))
        # send request via RESTCONF
        self.post_REST_XML_request(self.conf_url_post, self.xml_input_stream)
        # check request content against restconf's config datastore
        response = self.get_REST_XML_response(self.conf_url)
        xml_resp_stream = (response.text).encode('utf-8', 'ignore')
        xml_resp_DOM = md.parseString(xml_resp_stream)
        self.assertDataDOM(self.xml_input_DOM, xml_resp_DOM)
        # check request content against restconf's operational datastore
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, self.xml_input_DOM)
        # test error for double create (POST could create data only)
        self.log.info(self._paint_msg_yellow(" UPDATE Meter by POST REST"))
        response = self.post_REST_XML_repeat_request(self.conf_url_post, self.xml_input_stream)

        # -------------- DELETE -------------------
        self.log.info(self._paint_msg_yellow(" DELETE Meter by DELETE REST"))
        # Delte data from config DataStore
        response = self.delete_REST_XML_response(self.conf_url)
        # Data has been deleted, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response(self.conf_url)
        # Meter operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, self.xml_input_DOM, False)

    # sal-meter services
    def test_operations_POST(self):
        self.log.info("--- Meter operations sal-service test ---")
        # -------------- CREATE -------------------
        self.log.info(self._paint_msg_yellow(" CREATE Meter by add-sal-service"))
        # send request via RESTCONF
        self.post_REST_XML_request(self.oper_url_add, self.oper_input_stream)
        # TODO : check no empty transaction_id from post add_service

        # check request content against restconf's config datastore
        # operation service don't change anything in a Config. Data Store
        # so we expect 404 response code (same as a check after delete
        self.get_REST_XML_deleted_response(self.conf_url)
        # check request content against restconf's operational datastore
        # operational Data Store has to present new meter
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, self.xml_input_DOM)

        # -------------- UPDATE -------------------
        self.log.info(self._paint_msg_yellow(" UPDATE Meter by update-sal-service"))
        xml_updated_stream = self.__update_meter_input()
        xml_updated_DOM = md.parseString(xml_updated_stream)
        data_from_updated_stream = ''
        for node in xml_updated_DOM.documentElement.childNodes:
            data_from_updated_stream += node.toxml(encoding='utf-8')

        # The xml body without data - data come from file (all meters's subtags)
        oper_update_stream = '<?xml version="1.0" encoding="UTF-8" standalone="no"?>\n' \
                             '<input xmlns="urn:opendaylight:meter:service">\n' \
                             '  <original-meter>\n' \
                             '    %s' \
                             '  </original-meter>\n' \
                             '  <updated-meter>\n' \
                             '    %s' \
                             '  </updated-meter>\n' \
                             '  <node xmlns:inv="urn:opendaylight:inventory">' \
                             '/inv:nodes/inv:node[inv:id="openflow:1"]</node>\n' \
                             '</input>' % (self.data_from_file_input, data_from_updated_stream)

        self.post_REST_XML_request(self.oper_url_upd, oper_update_stream)
        # TODO : check no empty transaction_id from post add_service

        # check request content against restconf's config datastore
        # operation service don't change anything in a Config. Data Store
        # so we expect 404 response code (same as a check after delete
        self.get_REST_XML_deleted_response(self.conf_url)
        # check request content against restconf's operational datastore
        # operational Data Store has to present updated meter
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, xml_updated_DOM)

        # -------------- DELETE -------------------
        self.log.info(self._paint_msg_yellow(" DELETE Meter by remove-sal-service"))
        # Delte data from config DataStore
        response = self.post_REST_XML_request(self.oper_url_del, self.oper_input_stream)
        # Data never been added, so we expect the 404 response code
        response = self.get_REST_XML_deleted_response(self.conf_url)
        # Meter operational data has a specific content
        # and the comparable data has to be filtered before comparison
        response = self.get_REST_XML_response(self.oper_url)
        self.__validate_contain_meter(response, self.xml_input_DOM, False)


# --------------- HELP METHODS ---------------

    def __validate_contain_meter(self, oper_resp, orig_DOM, exp_contain=True):
        xml_resp_stream = (oper_resp.text).encode('utf-8', 'ignore')
        xml_resp_DOM = md.parseString(xml_resp_stream)
        nodeListOperMeters = xml_resp_DOM.getElementsByTagName('meter-config-stats')
        if nodeListOperMeters.length > 1:
            raise AssertionError('\n !!! Operational Data Store has more \'meter-config-stats\' tags as one \n')

        origDict = XMLtoDictParserTools.parseDOM_ToDict(
            orig_DOM._get_documentElement(), ignoreList=IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON)
        origDict['meter-config-stats'] = origDict.pop('meter')
        nodeDict = {}
        for node in nodeListOperMeters:
            nodeDict = XMLtoDictParserTools.parseDOM_ToDict(
                node, ignoreList=IGNORED_TAGS_FOR_OPERATIONAL_COMPARISON)

        if exp_contain:
            if nodeDict != origDict:
                err_msg = '\n !!! Loaded operation statistics doesn\'t contain expected meter \n' \
                            ' expected:      %s\n found:         %s\n differences:   %s\n' \
                            '' % (origDict, nodeDict, XMLtoDictParserTools.getDifferenceDict(origDict, nodeDict))
                self.log.error(self._paint_msg_red(err_msg))
                raise AssertionError(err_msg)
        else:
            if nodeDict == origDict:
                err_msg = '\n !!! Loaded operation statistics contains expected meter \n' \
                            ' found:         %s\n ' % (nodeDict)
                self.log.error(self._paint_msg_red(err_msg))
                raise AssertionError(err_msg)

    def __update_meter_input(self):
        # action only for yet
        xml_dom_input = md.parseString(self.xml_input_stream)
        for tag_name in METER_TAGS_FOR_UPDATE:
            tag_list = xml_dom_input.getElementsByTagName(tag_name)
            if tag_list is not None and len(tag_list) > 0:
                tag_elm = tag_list[0]
                for child in tag_elm.childNodes:
                    if child.nodeType == Element.TEXT_NODE:
                        nodeValue = (child.nodeValue).encode('utf-8', 'ignore')
                        if len(nodeValue.strip(' \t\n\r')) > 0:
                            newValue = self.returnReverseInputTest(nodeValue)
                            newTagEl = child.ownerDocument.createTextNode(newValue)
                            self.log.info(self._paint_msg_cyan(
                                'Meter change for %s from %s to %s' % (tag_name, nodeValue, newValue)))
                            child.parentNode.replaceChild(newTagEl, child)

        return xml_dom_input.toxml(encoding='utf-8')
