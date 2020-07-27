.. _ofp-operation:

OpenFlow Plugin Operation
=========================

Overview
--------

The OpenFlow standard describes a communications protocol that allows
an external application, such as an SDN Controller, to access and
configure the forwarding plane of a network device normally called
the OpenFlow-enabled switch.

The switch and the controller communicate by exchanging OpenFlow
protocol messages, which the controller uses to add, modify, and delete
flows in the switch. By using OpenFlow, it is possible to control
various aspects of the network, such as traffic forwarding, topology
discovery, Quality of Service, and so on.

For more information about OpenFlow, refer to the Open Networking
Foundation website openflow-specs_.

The OpenFlow Plugin provides the following RESTCONF APIs:

- OpenFlow Topology
- OpenFlow Statistics
- OpenFlow Programming

OpenFlow Topology
-----------------

The controller provides a centralized logical view of the OpenFlow network.

The controller uses Link Layer Discovery Protocol (LLDP) messages to discover
the links between the connected OpenFlow devices. The topology manager
stores and manages the information (nodes and links) in the controller
data stores.

This works as follows:

-  LLDP speaker application sends LLDP packets to all the node connectors of
   all the switches that are connected.

-  LLDP speaker application also monitors status events for a node connector.
   If the status of a node connector for the connected switch changes from up
   to down, the LLDP speaker does not send packets out to that node connector.
   If the status changes from down to up, the LLDP speaker sends packets to
   that node connector.

-  The LLDP discovery application monitors the LLDP packets that are sent by a
   switch to the controller and notifies the topology manager of a new
   link-discovery event. The information includes: source node, source node
   connector, destination node, and destination node connector, from the
   received LLDP packets.

-  The LLDP discovery application also checks for an expired link and notifies
   the topology manager. A link expires when it does not receive an update from
   the switch for the three LLDP speaker cycles.

Retrieving topology details by using RESTCONF
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can retrieve OpenFlow topology information (nodes and links) from the
controller by sending a RESTCONF request. The controller fetches the topology
data from the operational datastore and returns it in response to the RESTCONF
request.

To view the topology data for all the connected nodes, send the following
request to the controller:

**URL:** ``/restconf/operational/network-topology:network-topology/topology/flow:1``

**RFC8040 URL:** ``/rests/data/network-topology:network-topology/topology=flow%3A1?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

            <topology xmlns="urn:TBD:params:xml:ns:yang:network-topology">
                <topology-id>flow: 1</topology-id>
                <node>
                    <node-id>openflow: 2</node-id>
                    <termination-point>
                        <tp-id>openflow: 2: 2</tp-id>
                        <inventory-node-connector-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 2']/a:node-connector[a:id='openflow: 2: 2']
                    </inventory-node-connector-ref>
                    </termination-point>
                    <termination-point>
                        <tp-id>openflow: 2: 1</tp-id>
                        <inventory-node-connector-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 2']/a:node-connector[a:id='openflow: 2: 1']
                    </inventory-node-connector-ref>
                    </termination-point>
                    <termination-point>
                        <tp-id>openflow: 2:LOCAL</tp-id>
                        <inventory-node-connector-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 2']/a:node-connector[a:id='openflow: 2:LOCAL']
                    </inventory-node-connector-ref>
                    </termination-point>
                    <inventory-node-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 2']
                    </inventory-node-ref>
                </node>
                <node>
                    <node-id>openflow: 1</node-id>
                    <termination-point>
                        <tp-id>openflow: 1: 1</tp-id>
                        <inventory-node-connector-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 1']/a:node-connector[a:id='openflow: 1: 1']
                    </inventory-node-connector-ref>
                    </termination-point>
                    <termination-point>
                        <tp-id>openflow: 1:LOCAL</tp-id>
                        <inventory-node-connector-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 1']/a:node-connector[a:id='openflow: 1:LOCAL']
                    </inventory-node-connector-ref>
                    </termination-point>
                    <termination-point>
                        <tp-id>openflow: 1: 2</tp-id>
                        <inventory-node-connector-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 1']/a:node-connector[a:id='openflow: 1: 2']
                    </inventory-node-connector-ref>
                    </termination-point>
                    <inventory-node-ref xmlns="urn:opendaylight:model:topology:inventory" xmlns:a="urn:opendaylight:inventory">/a:nodes/a:node[a:id='openflow: 1']
                </inventory-node-ref>
                </node>
                <link>
                    <link-id>openflow:1:2</link-id>
                    <destination>
                        <dest-tp>openflow:2:2</dest-tp>
                        <dest-node>openflow:2</dest-node>
                    </destination>
                    <source>
                        <source-node>openflow:1</source-node>
                        <source-tp>openflow:1:2</source-tp>
                    </source>
                </link>
                <link>
                    <link-id>openflow:2:2</link-id>
                    <destination>
                        <dest-tp>openflow:1:2</dest-tp>
                        <dest-node>openflow:1</dest-node>
                    </destination>
                    <source>
                        <source-node>openflow:2</source-node>
                        <source-tp>openflow:2:2</source-tp>
                    </source>
                </link>
            </topology>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

            {
                "topology": [
                    {
                        "topology-id": "flow:1",
                        "node": [
                            {
                                "node-id": "openflow:2",
                                "termination-point": [
                                    {
                                        "tp-id": "openflow:2:2",
                                        "opendaylight-topology-inventory:inventory-node-connector-ref":
                                        "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:2']/opendaylight-inventory:node-connector[opendaylight-inventory:id='openflow:2:2']"
                                    },
                                    {
                                        "tp-id": "openflow:2:1",
                                        "opendaylight-topology-inventory:inventory-node-connector-ref":
                                        "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:2']/opendaylight-inventory:node-connector[opendaylight-inventory:id='openflow:2:1']"
                                    },
                                    {
                                        "tp-id": "openflow:2:LOCAL",
                                        "opendaylight-topology-inventory:inventory-node-connector-ref":
                                        "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:2']/opendaylight-inventory:node-connector[opendaylight-inventory:id='openflow:2:LOCAL']"
                                    }
                                ],
                                "opendaylight-topology-inventory:inventory-node-ref": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:2']"
                            },
                            {
                                "node-id": "openflow:1",
                                "termination-point": [
                                    {
                                        "tp-id": "openflow:1:1",
                                        "opendaylight-topology-inventory:inventory-node-connector-ref":
                                        "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:1']/opendaylight-inventory:node-connector[opendaylight-inventory:id='openflow:1:1']"
                                    },
                                    {
                                        "tp-id": "openflow:1:LOCAL",
                                        "opendaylight-topology-inventory:inventory-node-connector-ref":
                                        "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:1']/opendaylight-inventory:node-connector[opendaylight-inventory:id='openflow:1:LOCAL']"
                                    },
                                    {
                                        "tp-id": "openflow:1:2",
                                        "opendaylight-topology-inventory:inventory-node-connector-ref":
                                        "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:1']/opendaylight-inventory:node-connector[opendaylight-inventory:id='openflow:1:2']"
                                    }
                                ],
                                "opendaylight-topology-inventory:inventory-node-ref": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:1']"
                            }
                        ],
                        "link": [
                            {
                                "link-id": "openflow:1:2",
                                "destination": {
                                    "dest-tp": "openflow:2:2",
                                    "dest-node": "openflow:2"
                                },
                                "source": {
                                    "source-node": "openflow:1",
                                    "source-tp": "openflow:1:2"
                                }
                            },
                            {
                                "link-id": "openflow:2:2",
                                "destination": {
                                    "dest-tp": "openflow:1:2",
                                    "dest-node": "openflow:1"
                                },
                                "source": {
                                    "source-node": "openflow:2",
                                     "source-tp": "openflow:2:2"
                                    }
                            }
                        ]
                    }
                ]
            }

.. note:: In the examples above the OpenFlow node is represented as openflow:1
          where 1 is the datapath ID of the OpenFlow-enabled device.

.. note:: In the examples above the OpenFlow node connector is represented as
          openflow:1:2 where 1 is the datapath ID and 2 is the port ID of the
          OpenFlow-enabled device.

OpenFlow Statistics
-------------------

The controller provides the following information for the connected
OpenFlow devices:

**Inventory information:**

-  **Node description:** Description of the OpenFlow-enabled device, such as
   the switch manufacturer, hardware revision, software revision, serial number,
   and so on.

-  **Flow table features:** Features supported by flow tables of the switch.

-  **Port description:** Properties supported by each node connector of the
   node.

-  **Group features:** Features supported by the group table of the switch.

-  **Meter features:** Features supported by the meter table of the switch.

**Statistics:**

-  **Individual flow statistics:** Statistics related to individual flow
   installed in the flow table.

-  **Aggregate flow statistics:** Statistics related to aggregate flow for
   each table level.

-  **Flow table statistics:** Statistics related to the individual flow table
   of the switch.

-  **Port statistics:** Statistics related to all node connectors of the node.

-  **Group description:** Description of the groups installed in the switch
   group table.

-  **Group statistics:** Statistics related to an individual group installed
   in the group table.

-  **Meter configuration:** Statistics related to the configuration of the
   meters installed in the switch meter table.

-  **Meter statistics:** Statistics related to an individual meter installed
   in the switch meter table.

-  **Queue statistics:** Statistics related to the queues created on each
   node connector of the switch.

The controller fetches both inventory and statistics information whenever a
node connects to the controller. After that the controller fetches statistics
periodically within a time interval of three seconds. The controller augments
inventory information and statistics fetched from each connected node to its
respective location in the operational data store. The controller also cleans
the stale statistics at periodic intervals.

You can retrieve the inventory information (nodes, ports, and tables) and
statistics (ports, flows, groups and meters) by sending a RESTCONF request.
The controller fetches the inventory data from the operational data store
and returns it in response to the RESTCONF request.

The following sections provide a few examples for retrieving inventory and
statistics information.

Example of node inventory data
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view the inventory data of a connected node, send the following request to
the controller:

**URL:** ``/restconf/operational/opendaylight-inventory:nodes/node/openflow:1``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <node xmlns="urn:opendaylight:inventory">
                <id>openflow:1</id>
                <serial-number xmlns="urn:opendaylight:flow:inventory">None</serial-number>
                <port-number xmlns="urn:opendaylight:flow:inventory">45170</port-number>
                <description xmlns="urn:opendaylight:flow:inventory">None</description>
                <hardware xmlns="urn:opendaylight:flow:inventory">Open vSwitch</hardware>
                <manufacturer xmlns="urn:opendaylight:flow:inventory">Nicira, Inc.</manufacturer>
                <switch-features xmlns="urn:opendaylight:flow:inventory">
                    <max_tables>254</max_tables>
                    <capabilities>flow-feature-capability-queue-stats</capabilities>
                    <capabilities>flow-feature-capability-flow-stats</capabilities>
                    <capabilities>flow-feature-capability-port-stats</capabilities>
                    <capabilities>flow-feature-capability-table-stats</capabilities>
                    <max_buffers>256</max_buffers>
                </switch-features>

              --- Omitted output —--

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "opendaylight-inventory:nodes": {
                    "node": [
                        {
                            "id": "openflow:1",
                            "flow-node-inventory:serial-number": "None",
                            "flow-node-inventory:port-number": 45170,
                            "flow-node-inventory:description": "None",
                            "flow-node-inventory:hardware": "Open vSwitch",
                            "flow-node-inventory:manufacturer": "Nicira, Inc.",
                            "flow-node-inventory:switch-features": {
                                "max_tables": 254,
                                "capabilities": [
                                    "flow-node-inventory:flow-feature-capability-queue-stats",
                                    "flow-node-inventory:flow-feature-capability-flow-stats",
                                    "flow-node-inventory:flow-feature-capability-port-stats",
                                    "flow-node-inventory:flow-feature-capability-table-stats"
                                ],
                                "max_buffers": 256
                            },
                            "flow-node-inventory:software": "2.0.2",
                            "flow-node-inventory:ip-address": "192.168.56.106",
                            "flow-node-inventory:table": [
                                {
                                    "id": 88,
                                    "opendaylight-flow-table-statistics:flow-table-statistics": {
                                        "packets-looked-up": "0",
                                        "active-flows": 0,
                                        "packets-matched": "0"
                                    }
                                },

              --- Omitted output —--

.. note:: In the examples above the OpenFlow node is represented as openflow:1
          where 1 is the datapath ID of the OpenFlow-enabled device.

Example of port description and port statistics
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view the port description and port statistics of a connected node, send the
following request to the controller:

**URL:** ``/restconf/operational/opendaylight-inventory:nodes/node/openflow:1/node-connector/openflow:1:2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/node-connector=openflow%3A1%3A2?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <node-connector xmlns="urn:opendaylight:inventory">
               <id>openflow:1:2</id>
               <supported xmlns="urn:opendaylight:flow:inventory"></supported>
               <peer-features xmlns="urn:opendaylight:flow:inventory"></peer-features>
               <port-number xmlns="urn:opendaylight:flow:inventory">2</port-number>
               <hardware-address xmlns="urn:opendaylight:flow:inventory">4e:92:4a:c8:4c:fa</hardware-address>
               <current-feature xmlns="urn:opendaylight:flow:inventory">ten-gb-fd copper</current-feature>
               <maximum-speed xmlns="urn:opendaylight:flow:inventory">0</maximum-speed>
               <reason xmlns="urn:opendaylight:flow:inventory">update</reason>
               <configuration xmlns="urn:opendaylight:flow:inventory"></configuration>
               <advertised-features xmlns="urn:opendaylight:flow:inventory"></advertised-features>
               <current-speed xmlns="urn:opendaylight:flow:inventory">10000000</current-speed>
               <name xmlns="urn:opendaylight:flow:inventory">s1-eth2</name>
               <state xmlns="urn:opendaylight:flow:inventory">
                   <link-down>false</link-down>
                   <blocked>false</blocked>
                   <live>true</live>
               </state>
               <flow-capable-node-connector-statistics xmlns="urn:opendaylight:port:statistics">
                   <receive-errors>0</receive-errors>
                   <packets>
                       <transmitted>444</transmitted>
                       <received>444</received>
                   </packets>
                   <receive-over-run-error>0</receive-over-run-error>
                   <transmit-drops>0</transmit-drops>
                   <collision-count>0</collision-count>
                   <receive-frame-error>0</receive-frame-error>
                   <bytes>
                       <transmitted>37708</transmitted>
                       <received>37708</received>
                   </bytes>
                   <receive-drops>0</receive-drops>
                   <transmit-errors>0</transmit-errors>
                   <duration>
                       <second>2181</second>
                       <nanosecond>550000000</nanosecond>
                   </duration>
                   <receive-crc-error>0</receive-crc-error>
               </flow-capable-node-connector-statistics>
           </node-connector>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "node-connector": [
                    {
                        "id": "openflow:1:2",
                        "flow-node-inventory:hardware-address": "ca:56:91:bf:07:c9",
                        "flow-node-inventory:supported": "",
                        "flow-node-inventory:peer-features": "",
                        "flow-node-inventory:advertised-features": "",
                        "flow-node-inventory:name": "s1-eth2",
                        "flow-node-inventory:port-number": 2,
                        "flow-node-inventory:current-speed": 10000000,
                        "flow-node-inventory:configuration": "",
                        "flow-node-inventory:current-feature": "ten-gb-fd copper",
                        "flow-node-inventory:maximum-speed": 0,
                        "flow-node-inventory:state": {
                            "blocked": false,
                            "link-down": false,
                            "live": false
                        },
                        "opendaylight-port-statistics:flow-capable-node-connector-statistics": {
                            "packets": {
                                "transmitted": 203,
                                "received": 203
                            },
                            "receive-frame-error": 0,
                            "collision-count": 0,
                            "receive-errors": 0,
                            "transmit-errors": 0,
                            "bytes": {
                                "transmitted": 17255,
                                "received": 17255
                            },
                            "receive-crc-error": 0,
                            "duration": {
                                "nanosecond": 246000000,
                                "second": 1008
                            },
                            "receive-drops": 0,
                            "transmit-drops": 0,
                            "receive-over-run-error": 0
                        }
                    }
                ]
            }

.. note:: In the examples above the OpenFlow node connector is represented as
          openflow:1:2 where 1 is the datapath ID and 2 is the port ID of the
          OpenFlow-enabled device.

.. _example-of-table-statistics:

Example of flow table and aggregated statistics
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view the flow table and flow aggregated statistics for a connected node,
send the following request to the controller:

**URL:** ``/restconf/operational/opendaylight-inventory:nodes/node/openflow:1/table/0/``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/table=0?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <table xmlns="urn:opendaylight:flow:inventory">
                <id>0</id>
                <flow-table-statistics xmlns="urn:opendaylight:flow:table:statistics">
                    <packets-looked-up>1570</packets-looked-up>
                    <active-flows>1</active-flows>
                    <packets-matched>1570</packets-matched>
                </flow-table-statistics>
                <flow>
                    <id>#UF$TABLE*0-1</id>
                    <table_id>0</table_id>
                    <flow-statistics xmlns="urn:opendaylight:flow:statistics">
                        <duration>
                            <second>4004</second>
                            <nanosecond>706000000</nanosecond>
                        </duration>
                        <packet-count>786</packet-count>
                        <byte-count>66810</byte-count>
                    </flow-statistics>
                    <priority>0</priority>
                    <hard-timeout>0</hard-timeout>
                    <match/>
                    <cookie_mask>0</cookie_mask>
                    <cookie>10</cookie>
                    <flags>SEND_FLOW_REM</flags>
                    <instructions>
                        <instruction>
                            <order>0</order>
                            <apply-actions>
                                <action>
                                    <order>0</order>
                                    <output-action>
                                        <output-node-connector>CONTROLLER</output-node-connector>
                                        <max-length>65535</max-length>
                                    </output-action>
                                </action>
                            </apply-actions>
                        </instruction>
                    </instructions>
                    <idle-timeout>0</idle-timeout>
                </flow>
            </table>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:table": [
                    {
                        "id": 0,
                        "opendaylight-flow-table-statistics:flow-table-statistics": {
                            "packets-looked-up": 1581,
                            "active-flows": 1,
                            "packets-matched": 1581
                        },
                        "flow": [
                            {
                                "id": "#UF$TABLE*0-1",
                                "table_id": 0,
                                "opendaylight-flow-statistics:flow-statistics": {
                                    "duration": {
                                        "second": 4056,
                                        "nanosecond": 4000000
                                    },
                                    "packet-count": 797,
                                    "byte-count": 67745
                                },
                                "priority": 0,
                                "hard-timeout": 0,
                                "cookie_mask": 0,
                                "cookie": 10,
                                "flags": "SEND_FLOW_REM",
                                "instructions": {
                                    "instruction": [
                                        {
                                            "order": 0,
                                            "apply-actions": {
                                                "action": [
                                                    {
                                                        "order": 0,
                                                        "output-action": {
                                                            "output-node-connector": "CONTROLLER",
                                                            "max-length": 65535
                                                        }
                                                    }
                                                ]
                                            }
                                        }
                                    ]
                                },
                                "idle-timeout": 0
                            }
                        ]
                    }
                ]
            }

.. note:: In the examples above the OpenFlow node table is 0.

.. _example-of-individual-flow-statistics:

Example of flow description and flow statistics
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view the individual flow statistics, send the following request to the
controller but before that :

**URL:** ``/restconf/operational/opendaylight-inventory:nodes/node/openflow:1/table/0/flow/fm-sr-link-discovery``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/table=0/flow=fm-sr-link-discovery?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

                   <flow>
                       <id>fm-sr-link-discovery</id>
                       <flow-statistics xmlns="urn:opendaylight:flow:statistics">
                           <packet-count>536</packet-count>
                           <duration>
                               <nanosecond>174000000</nanosecond>
                               <second>2681</second>
                           </duration>
                           <byte-count>45560</byte-count>
                       </flow-statistics>
                       <priority>99</priority>
                       <table_id>0</table_id>
                       <cookie_mask>0</cookie_mask>
                       <hard-timeout>0</hard-timeout>
                       <match>
                           <ethernet-match>
                               <ethernet-type>
                                   <type>35020</type>
                               </ethernet-type>
                           </ethernet-match>
                       </match>
                       <cookie>1000000000000001</cookie>
                       <flags></flags>
                       <instructions>
                           <instruction>
                               <order>0</order>
                               <apply-actions>
                                   <action>
                                       <order>0</order>
                                       <output-action>
                                           <max-length>65535</max-length>
                                           <output-node-connector>CONTROLLER</output-node-connector>
                                       </output-action>
                                   </action>
                               </apply-actions>
                           </instruction>
                       </instructions>
                       <idle-timeout>0</idle-timeout>
                   </flow>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "fm-sr-link-discovery",
                        "table_id": 0,
                        "opendaylight-flow-statistics:flow-statistics": {
                            "duration": {
                                "second": 2681,
                                "nanosecond": 174000000
                            },
                            "packet-count": 536,
                            "byte-count": 45560
                        },
                        "priority": 99,
                        "hard-timeout": 0,
                        "match": {
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 35020
                                }
                            }
                        },
                        "cookie_mask": 0,
                        "cookie": 1000000000000001,
                        "flags": "",
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "CONTROLLER",
                                                    "max-length": 65535
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 0
                    }
                ]
            }

.. note:: In the examples above the flow ID fm-sr-link-discovery is internal to
          the controller and has to match the datastore configured flow ID.
          For more information see flow ID match section
          :ref:`flow-id-match-function`.

.. _example-of-group-description-and-group-statistics:

Example of group description and group statistics
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view the group description and group statistics, send the following request
to the controller:

**URL:** ``/restconf/operational/opendaylight-inventory:nodes/node/openflow:1/group/2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/group=2?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <group xmlns="urn:opendaylight:flow:inventory">
              <group-id>2</group-id>
              <buckets>
                   <bucket>
                       <bucket-id>0</bucket-id>
                       <action>
                           <order>1</order>
                           <output-action>
                               <max-length>0</max-length>
                               <output-node-connector>2</output-node-connector>
                           </output-action>
                       </action>
                       <action>
                           <order>0</order>
                           <pop-mpls-action>
                               <ethernet-type>34887</ethernet-type>
                           </pop-mpls-action>
                       </action>
                       <watch_group>4294967295</watch_group>
                       <weight>0</weight>
                       <watch_port>2</watch_port>
                   </bucket>
              </buckets>
              <group-type>group-ff</group-type>
              <group-statistics xmlns="urn:opendaylight:group:statistics">
                   <buckets>
                       <bucket-counter>
                           <bucket-id>0</bucket-id>
                           <packet-count>0</packet-count>
                           <byte-count>0</byte-count>
                       </bucket-counter>
                   </buckets>
                   <group-id>2</group-id>
                   <packet-count>0</packet-count>
                   <byte-count>0</byte-count>
                   <duration>
                       <second>4116</second>
                       <nanosecond>746000000</nanosecond>
                   </duration>
                   <ref-count>1</ref-count>
              </group-statistics>
           </group>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:group": [
                    {
                        "group-id": 2,
                        "buckets": {
                            "bucket": [
                                {
                                    "bucket-id": 0,
                                    "watch_group": 4294967295,
                                    "action": [
                                        {
                                            "order": 0,
                                            "pop-mpls-action": {
                                                "ethernet-type": 34887
                                            }
                                        },
                                        {
                                            "order": 1,
                                            "output-action": {
                                                "output-node-connector": "2",
                                                "max-length": 0
                                            }
                                        }
                                    ],
                                    "weight": 0,
                                    "watch_port": 2
                                }
                            ]
                        },
                        "group-type": "group-ff",
                        "opendaylight-group-statistics:group-statistics": {
                            "byte-count": 0,
                            "group-id": 2,
                            "buckets": {
                                "bucket-counter": [
                                    {
                                        "bucket-id": 0,
                                        "packet-count": 0,
                                        "byte-count": 0
                                    }
                                ]
                            },
                            "duration": {
                                "nanosecond": 746000000,
                                "second": 4116
                            },
                            "ref-count": 1,
                            "packet-count": 0
                        }
                    }
                ]
            }

.. note:: In the examples above the group ID 2 matches the switch stored
          group ID.

.. _example-of-meter-description-and-meter-statistics:

Example of meter description and meter statistics
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To view the meter description and meter statistics, send the following request
to the controller:

**URL:** ``/restconf/operational/opendaylight-inventory:nodes/node/openflow:1/meter/2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/meter=2?content=nonconfig``

**Method:** ``GET``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <?xml version="1.0"?>
           <meter xmlns="urn:opendaylight:flow:inventory">
             <meter-id>2</meter-id>
             <flags>meter-kbps</flags>
             <meter-statistics xmlns="urn:opendaylight:meter:statistics">
               <packet-in-count>0</packet-in-count>
               <byte-in-count>0</byte-in-count>
               <meter-band-stats>
                 <band-stat>
                   <band-id>0</band-id>
                   <byte-band-count>0</byte-band-count>
                   <packet-band-count>0</packet-band-count>
                 </band-stat>
               </meter-band-stats>
               <duration>
                 <nanosecond>364000000</nanosecond>
                 <second>114</second>
               </duration>
               <meter-id>2</meter-id>
               <flow-count>0</flow-count>
             </meter-statistics>
             <meter-band-headers>
               <meter-band-header>
                 <band-id>0</band-id>
                 <band-rate>100</band-rate>
                 <band-burst-size>0</band-burst-size>
                 <meter-band-types>
                   <flags>ofpmbt-drop</flags>
                 </meter-band-types>
                 <drop-burst-size>0</drop-burst-size>
                 <drop-rate>100</drop-rate>
               </meter-band-header>
             </meter-band-headers>
           </meter>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:meter": [
                   {
                        "meter-id": 2,
                        "meter-band-headers": {
                            "meter-band-header": [
                                {
                                    "band-id": 0,
                                    "drop-rate": 100,
                                    "drop-burst-size": 0,
                                    "band-rate": 100,
                                    "band-burst-size": 0,
                                    "meter-band-types": {
                                        "flags": "ofpmbt-drop"
                                    }
                                }
                            ]
                        },
                        "opendaylight-meter-statistics:meter-statistics": {
                            "packet-in-count": 0,
                            "flow-count": 0,
                            "meter-id": 2,
                            "byte-in-count": 0,
                            "meter-band-stats": {
                                "band-stat": [
                                    {
                                        "band-id": 0,
                                        "packet-band-count": 0,
                                        "byte-band-count": 0
                                    }
                                ]
                            },
                            "duration": {
                                "nanosecond": 364000000,
                                "second": 114
                            }
                        },
                        "flags": "meter-kbps"
                    }
                ]
            }

.. note:: In the examples above the meter ID 2 matches the switch stored
          meter ID.

.. _openflow-programming-overview:

OpenFlow Programming
--------------------

The controller provides interfaces that can be used to program the connected
OpenFlow devices. These interfaces interact with the OpenFlow southbound plugin
that uses OpenFlow modification messages to program flows, groups and meters
in the switch.

The controller provides the following RESTCONF interfaces:

-  **Configuration Datastore:** allows user to configure flows, groups and
   meters. The configuration is stored in the controller datastore, persisted
   in disk and replicated in the controller cluster. The OpenFlow southbound
   plugin reads the configuration and sends the appropriate OpenFlow
   modification messages to the connected devices.

-  **RPC Operations:** allows user to configure flows, groups and meters
   overriding the datastore. In this case the OpenFlow southbound plugin
   translates the use configuration straight into an OpenFlow modification
   message that is sent to the connected device.

Example of flow programming by using config datastore
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example programs a flow that matches IPv4 packets (ethertype 0x800)
with destination address in the 10.0.10.0/24 subnet and sends them to port 1.
The flow is installed in table 0 of the switch with datapath ID 1.

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/table/0/flow/1``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/table=0/flow=1``

**Method:** ``PUT``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <?xml version="1.0" encoding="UTF-8" standalone="no"?>
           <flow xmlns="urn:opendaylight:flow:inventory">
               <hard-timeout>0</hard-timeout>
               <idle-timeout>0</idle-timeout>
               <cookie>1</cookie>
               <priority>2</priority>
               <flow-name>flow1</flow-name>
               <match>
                   <ethernet-match>
                       <ethernet-type>
                           <type>2048</type>
                       </ethernet-type>
                   </ethernet-match>
                   <ipv4-destination>10.0.10.0/24</ipv4-destination>
               </match>
               <id>1</id>
               <table_id>0</table_id>
               <instructions>
                   <instruction>
                       <order>0</order>
                       <apply-actions>
                           <action>
                               <output-action>
                                   <output-node-connector>1</output-node-connector>
                               </output-action>
                               <order>0</order>
                           </action>
                       </apply-actions>
                   </instruction>
               </instructions>
           </flow>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "1",
                        "priority": 2,
                        "table_id": 0,
                        "hard-timeout": 0,
                        "match": {
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 2048
                                }
                            },
                            "ipv4-destination": "10.0.10.0/24"
                        },
                        "cookie": 1,
                        "flow-name": "flow1",
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "1"
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 0
                    }
                ]
            }

.. note:: In the examples above the flow ID 1 is internal to the controller and
          the same ID can be found when retrieving the flow statistics if
          controller finds a match between the configured flow and the flow
          received from switch. For more information see flow ID match section
          :ref:`flow-id-match-function`.

.. note:: To use a different flow ID or table ID, ensure that the URL and the
          request body are synchronized.

.. note:: For more examples of flow programming using datastore, refer to
          the OpenDaylight OpenFlow plugin :ref:`ofp-flow-examples`.

For more information about flow configuration options check the
opendaylight_models_.

To verify that the flow has been correctly programmed in the switch, issue the
RESTCONF request as provided in :ref:`example-of-individual-flow-statistics`.

Deleting flows from config datastore:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example deletes the flow with ID 1 in table 0 of the switch with datapath
ID 1.

**Headers:**

-  **Content-type:** ``application/xml``

-  **Accept:** ``application/xml``

-  **Authentication:** ``admin:admin``

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/table/0/flow/1``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/table=0/flow=1``

**Method:** ``DELETE``

You can also use the below URL to delete all flows in table 0 of the switch
with datapath ID 1:

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/table/0``

To verify that the flow has been correctly removed in the switch, issue the
RESTCONF request as provided in :ref:`example-of-table-statistics`.

Example of flow programming by using RPC operation
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example programs a flow that matches IPv4 packets (ethertype 0x800)
with destination address in the 10.0.10.0/24 subnet and sends them to port 1.
The flow is installed in table 0 of the switch with datapath ID 1.

**URL:** ``/restconf/operations/sal-flow:add-flow``

**RFC8040 URL:** ``/rests/operations/sal-flow:add-flow``

**Method:** ``POST``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <?xml version="1.0" encoding="UTF-8" standalone="no"?>
           <input xmlns="urn:opendaylight:flow:service">
               <node xmlns:inv="urn:opendaylight:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>
               <table_id>0</table_id>
               <priority>2</priority>
               <match>
                   <ethernet-match>
                       <ethernet-type>
                           <type>2048</type>
                       </ethernet-type>
                   </ethernet-match>
                   <ipv4-destination>10.0.1.0/24</ipv4-destination>
               </match>
               <instructions>
                   <instruction>
                       <order>0</order>
                       <apply-actions>
                           <action>
                               <output-action>
                                   <output-node-connector>1</output-node-connector>
                               </output-action>
                               <order>0</order>
                           </action>
                       </apply-actions>
                   </instruction>
               </instructions>
           </input>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "input": {
                    "node": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:1']",
                    "table_id": 0,
                    "priority": 2,
                    "match": {
                        "ipv4-destination": "10.0.1.0/24",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            }
                        }
                    },
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "output-action": {
                                                "output-node-connector": "1",
                                                "max-length": 0
                                            }
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            }

.. note:: This payload does not require flow ID as this value is internal to
          controller and only used to store flows in the datastore. When
          retrieving flow statistics users will see an alien flow ID for flows
          created this way. For more information see flow ID match section
          :ref:`flow-id-match-function`.

To verify that the flow has been correctly programmed in the switch, issue the
RESTCONF request as provided in :ref:`example-of-table-statistics`.

Deleting flows from switch using RPC operation:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example removes a flow that matches IPv4 packets (ethertype 0x800)
with destination address in the 10.0.10.0/24 subnet from table 0 of the switch
with datapath ID 1.

**URL:** ``/restconf/operations/sal-flow:remove-flow``

**RFC8040 URL:** ``/rests/operations/sal-flow:remove-flow``

**Method:** ``POST``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <?xml version="1.0" encoding="UTF-8" standalone="no"?>
           <input xmlns="urn:opendaylight:flow:service">
               <node xmlns:inv="urn:opendaylight:inventory">/inv:nodes/inv:node[inv:id="openflow:1"]</node>
               <table_id>0</table_id>
               <priority>2</priority>
               <strict>true</strict>
               <match>
                   <ethernet-match>
                       <ethernet-type>
                           <type>2048</type>
                       </ethernet-type>
                   </ethernet-match>
                   <ipv4-destination>10.0.10.0/24</ipv4-destination>
               </match>
           </input>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "input": {
                    "node": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:1']",
                    "table_id": 0,
                    "priority": 2,
                    "strict": true,
                    "match": {
                        "ipv4-destination": "10.0.1.0/24",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            }
                        }
                    }
                }
            }

To verify that the flow has been correctly programmed in the switch, issue the
RESTCONF request as provided in :ref:`example-of-table-statistics`.

Example of a group programming by using config datastore
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example programs a select group to equally load balance traffic across
port 1 and port 2 in switch with datapath ID 1.

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/group/2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/group=2``

**Method:** ``PUT``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <?xml version="1.0" encoding="UTF-8" standalone="no"?>
           <group xmlns="urn:opendaylight:flow:inventory">
             <group-type>group-select</group-type>
             <buckets>
                 <bucket>
                  <weight>1</weight>
                     <action>
                         <output-action>
                             <output-node-connector>1</output-node-connector>
                         </output-action>
                         <order>1</order>
                     </action>
                     <bucket-id>1</bucket-id>
                 </bucket>
                 <bucket>
                   <weight>1</weight>
                     <action>
                         <output-action>
                             <output-node-connector>2</output-node-connector>
                         </output-action>
                         <order>1</order>
                     </action>
                     <bucket-id>2</bucket-id>
                 </bucket>
             </buckets>
             <barrier>false</barrier>
             <group-name>SelectGroup</group-name>
             <group-id>2</group-id>
           </group>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:group": [
                    {
                        "group-id": 2,
                        "barrier": false,
                        "group-name": "SelectGroup",
                        "buckets": {
                            "bucket": [
                                {
                                    "bucket-id": 1,
                                    "weight": 1,
                                    "action": [
                                        {
                                            "order": 1,
                                            "output-action": {
                                                "output-node-connector": "1"
                                            }
                                        }
                                    ]
                                },
                                {
                                    "bucket-id": 2,
                                    "weight": 1,
                                    "action": [
                                        {
                                            "order": 1,
                                            "output-action": {
                                                "output-node-connector": "2"
                                            }
                                        }
                                    ]
                                }
                            ]
                        },
                        "group-type": "group-select"
                    }
                ]
            }

.. note:: In the example above the group ID 1 will be stored in the switch
          and will be used by the switch to report group statistics.

.. note:: To use a different group ID, ensure that the URL and the request
          body are synchronized.

For more information about group configuration options check the
opendaylight_models_.

To verify that the group has been correctly programmed in the switch,
issue the RESTCONF request as provided in
:ref:`example-of-group-description-and-group-statistics`.

To add a group action in a flow just add this statement in the flow body:

.. code-block:: none

   <apply-actions>
       <action>
           <group-action>
               <group-id>1</group-id>
           </group-action>
           <order>1</order>
       </action>
   </apply-actions>

Deleting groups from config datastore
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example deletes the group ID 1 in the switch with datapath ID 1.

**Headers:**

-  **Content-type:** ``application/xml``

-  **Accept:** ``application/xml``

-  **Authentication:** ``admin:admin``

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/group/2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/group=2``

**Method:** ``DELETE``

Example of a meter programming by using config datastore
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example programs a meter to drop traffic exceeding 256 kbps with a burst
size of 512 in switch with datapath ID 1.

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/meter/2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/meter=2``

**Method:** ``PUT``

.. tabs::

    .. tab:: XML

        **Headers:**

        **Content-type:** ``application/xml``

        **Accept:** ``application/xml``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           <?xml version="1.0" encoding="UTF-8" standalone="no"?>
           <meter xmlns="urn:opendaylight:flow:inventory">
               <flags>meter-kbps</flags>
               <meter-band-headers>
                   <meter-band-header>
                       <band-id>0</band-id>
                       <drop-rate>256</drop-rate>
                       <drop-burst-size>512</drop-burst-size>
                       <meter-band-types>
                           <flags>ofpmbt-drop</flags>
                       </meter-band-types>
                   </meter-band-header>
               </meter-band-headers>
               <meter-id>2</meter-id>
               <meter-name>Foo</meter-name>
           </meter>

    .. tab:: JSON

        **Headers:**

        **Content-type:** ``application/json``

        **Accept:** ``application/json``

        **Authentication:** ``admin:admin``

        .. code-block:: none

           {
                "flow-node-inventory:meter": [
                    {
                        "meter-id": 2,
                        "meter-band-headers": {
                            "meter-band-header": [
                                {
                                    "band-id": 0,
                                    "drop-rate": 256,
                                    "drop-burst-size": 512,
                                    "meter-band-types": {
                                        "flags": "ofpmbt-drop"
                                    }
                                }
                            ]
                        },
                        "flags": "meter-kbps",
                        "meter-name": "Foo"
                    }
                ]
            }

.. note:: In the example above the meter ID 1 will be stored in the switch
          and will be used by the switch to report group statistics.

.. note:: To use a different meter ID, ensure that the URL and the request
          body are synchronized.

For more information about meter configuration options check the
opendaylight_models_.

To verify that the meter has been correctly programmed in the switch,
issue the RESTCONF request as provided in
:ref:`example-of-meter-description-and-meter-statistics`.

To add a meter instruction in a flow just add this statement in the flow body:

.. code-block:: none

   <instructions>
      <instruction>
          <order>1</order>
          <meter>
            <meter-id>1</meter-id>
          </meter>
      </instruction>
   </instructions>

Deleting meters from config datastore
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This example deletes the meter ID 1 in the switch with datapath ID 1.

**Headers:**

-  **Content-type:** ``application/xml``

-  **Accept:** ``application/xml``

-  **Authentication:** ``admin:admin``

**URL:** ``/restconf/config/opendaylight-inventory:nodes/node/openflow:1/meter/2``

**RFC8040 URL:** ``/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/meter=2``

**Method:** ``DELETE``

.. _flow-id-match-function:

Flow ID match function
----------------------

When the controller receives flow information from a switch, this information
is compared with all flows stored in the configuration datastore, in case of
a match the flow ID in the flow configuration is automatically added to the
flow operational information. This way we can easily relate flows stored
in controller with flows received from the switch.

However in case of flows added via RPC or in general when the controller
cannot match received flow information with any flow in datastore, it adds
an alien ID in the flow operational information like in the example below.

.. code-block:: none

   <flow>
       <id>#UF$TABLE*0-555</id>
       <flow-statistics xmlns="urn:opendaylight:flow:statistics">
           <packet-count>5227</packet-count>
           <duration>
               <nanosecond>642000000</nanosecond>
               <second>26132</second>
           </duration>
           <byte-count>444295</byte-count>
       </flow-statistics>
       <priority>99</priority>
       <table_id>0</table_id>
       <cookie_mask>0</cookie_mask>
       <hard-timeout>0</hard-timeout>
       <match>
           <ethernet-match>
               <ethernet-type>
                   <type>35020</type>
               </ethernet-type>
           </ethernet-match>
       </match>
       <cookie>1000000000000001</cookie>
       <flags></flags>
       <instructions>
           <instruction>
               <order>0</order>
               <apply-actions>
                   <action>
                       <order>0</order>
                       <output-action>
                           <max-length>65535</max-length>
                           <output-node-connector>CONTROLLER</output-node-connector>
                       </output-action>
                   </action>
               </apply-actions>
           </instruction>
       </instructions>
       <idle-timeout>0</idle-timeout>
   </flow>


OpenFlow clustering
-------------------

For high availability, it is recommended a three-node cluster setup in
which each switch is connected to all nodes in the controller cluster.

.. note:: Normal OpenFlow operations, such as adding a flow, can be done on
          any cluster member. For more information about OpenFlow operations,
          refer to :ref:`openflow-programming-overview`.

In OpenFlow 1.3, one of the following roles is assigned to each
switch-controller connection:

-  Master: All synchronous and asynchronous messages are sent to the
   master controller. This controller has write privileges on the
   switch.

-  Slave: Only synchronous messages are sent to this controller. Slave
   controllers have only read privileges on the switch.

-  Equal: When the equal role is assigned to a controller, it has the
   same privileges as the master controller. By default, a controller is
   assigned the equal role when it first connects to the switch.

A switch can be connected to one or more controllers. Each controller
communicates the OpenFlow channel role through an OFTP\_ROLE\_REQUEST
message. The switch must retain the role of each switch connection; a
controller may change this role at any time.

If a switch connects to multiple controllers in the cluster, the cluster
selects one controller as the master controller; the remaining
controllers assume the slave role. The election of a master controller
proceeds as follows.

#. Each controller in the cluster that is handling switch connections
   registers to the Entity Ownership Service (EOS) as a candidate for
   switch ownership.

   .. note:: The EOS is a clustering service that plays the role of the
             arbiter to elect an owner (master) of an entity from a registered
             set of candidates.

#. The EOS then selects one controller as the owner.

   .. note:: Master ownership is for each device; each individual controller
             can be a master for a set of connected devices and a slave for the
             remaining set of connected devices.

#. The selected owner then sends an OFTP\_ROLE\_REQUEST message to the
   switch to set the connection to the master role, and the other
   controllers send the role message to set the slave role.

When the switch master connection goes down, the election of a new
master controller proceeds as follows.

#. The related controller deregisters itself as a candidate for Entity
   Ownership from the EOS.

#. The EOS then selects a new owner from the remaining candidates.

#. The new owner accordingly sends an OFTP\_ROLE\_REQUEST message to the
   switch to set the connection to the master role.

If a controller that currently has the master role is shut down, a new
master from the remaining candidate controllers is selected.

Verifying the EOS owner and candidates by using RESTCONF
--------------------------------------------------------

To verify the EOS owner and candidates in an OpenFlow cluster, send the
following request to the controller:

**Headers:**

- **Content-type:** ``application/json``

- **Accept:** ``application/json``

- **Authentication:** ``admin:admin``

**URL:** ``/restconf/operational/entity-owners:entity-owners``

**RFC8040 URL:** ``/rests/data/entity-owners:entity-owners?content=nonconfig``

**Method:** ``GET``

**Sample JSON output:**

.. code-block:: none

   {
          "entity-owners":{
             "entity-type":[
                {
                   "type":"org.opendaylight.mdsal.ServiceEntityType",
                   "entity":[
                      {
                         "id":"/odl-general-entity:entity[odl-general-entity:name='openflow:1']",
                         "candidate":[
                            {
                               "name":"member-3"
                            },
                            {
                               "name":"member-2"
                            },
                            {
                               "name":"member-1"
                            }
                         ],
                         "owner":"member-3"
                      },
                      {
                         "id":"/odl-general-entity:entity[odl-general-entity:name='openflow:2']",
                         "candidate":[
                            {
                               "name":"member-1"
                            },
                            {
                               "name":"member-3"
                            },
                            {
                               "name":"member-2"
                            }
                         ],
                         "owner":"member-1"
                      },
                      {
                         "id":"/odl-general-entity:entity[odl-general-entity:name='openflow:3']",
                         "candidate":[
                            {
                               "name":"member-1"
                            },
                            {
                               "name":"member-2"
                            },
                            {
                               "name":"member-3"
                            }
                         ],
                         "owner":"member-1"
                      }
                   ]
                },
                {
                   "type":"org.opendaylight.mdsal.AsyncServiceCloseEntityType",
                   "entity":[
                      {
                         "id":"/odl-general-entity:entity[odl-general-entity:name='openflow:1']",
                         "candidate":[
                            {
                               "name":"member-3"
                            }
                         ],
                         "owner":"member-3"
                      },
                      {
                         "id":"/odl-general-entity:entity[odl-general-entity:name='openflow:2']",
                         "candidate":[
                            {
                               "name":"member-1"
                            }
                         ],
                         "owner":"member-1"
                      },
                      {
                         "id":"/odl-general-entity:entity[odl-general-entity:name='openflow:3']",
                         "candidate":[
                            {
                               "name":"member-1"
                            }
                         ],
                         "owner":"member-1"
                      }
                   ]
                }
             ]
          }
       }

In the above sample outputs, ``member 3`` is the master controller
(EOS owner) for the OpenFlow device with datapath ID ``1``, and
``member-1`` is the master controller (EOS owner) for the OpenFlow
devices with the datapath IDs of ``2`` and ``3``.

Configuring the OpenFlow Plugin
-------------------------------

OpenFlow plugin configuration file is in the opendaylight /etc folder:
``opendaylight-0.9.0/etc/org.opendaylight.openflowplugin.cfg``

The ``org.opendaylight.openflowplugin.cfg`` file can be modified at any
time, however a controller restart is required for the changes to take
effect.

This configuration is local to a given node. You must repeat these steps
on each node to enable the same functionality across the cluster.

.. _ofp-tls-guide:

Configuring OpenFlow TLS
------------------------

This section describes how to secure OpenFlow connections between
controller and OpenFlow devices using Transport Layer Security (TLS).

TLS Concepts
~~~~~~~~~~~~

TLS uses digital certificates to perform remote peer authentication,
message integrity and data encryption. Public Key Infrastructure (PKI)
is required to create, manage and verify digital certificates.

For OpenFlow symmetric authentication (controller authenticates device
and device authenticates controller) both controller and device require:

#. A private key: used to generate own public certificate and therefore
   required for own authentication at the other end.

#. A public certificate or a chain of certificates if public certificate
   is signed by an intermediate (not root) CA: the chain contains the public
   certificate as well as all the intermediate CA certificates used to
   validate the public certificate, this public information is sent to the
   other peer during the TLS negotiation and it is used for own
   authentication at the other end.

#. A list of root CA certificates: this contains the root CA certificate
   that signed the remote peer certificate or the remote peer intermediate
   CA certificate (in case of certificate chain). This public information
   is used to authenticate the other end.

.. note:: Some devices like Open vSwitch (OVS) do not support certificate
          chains, this means controller can only send its own certificate
          and receive the switch certificate without any intermediate CA
          certificates. For TLS negotiation to be successful in this scenario
          both ends need to store all intermediate CA certificates used by
          the other end (in addition to the remote peer root CA certificate).

Generate Controller Private Key and Certificate
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You may skip this step if you already have the required key and certificate
from an external Public Key Infrastructure (PKI). In the examples below we
use openSSL tool to generate private key and certificates for controller.

#. Generate controller private key

   The command below generates 2048 bytes RSA key:

   .. code-block:: none

       openssl genrsa -out controller.key 2048

   This will generate the private key file controller.key

#. Generate controller certificate

   The command below creates a certificate sign request:

   .. code-block:: none

       openssl req -new -sha256 -key controller.key -out controller.csr

   This will generate the certificate signing request file controller.csr

   Submit the file to the desired Certificate Authority (CA) and get the CA
   signed certificate along with any intermediate CA certificate in the file
   controller.crt (X.509 format).

   The following is not recommended for production but if you want to just
   check the TLS communication you can create a "self-signed" certificate for
   the controller using below command:

   .. code-block:: none

       openssl req -new -x509 -nodes -sha1 -days 1825 -key controller.key -out controller.crt

Create Controller Key Stores
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Controller requires 2 Key Stores for OpenFlow TLS:

- Keystore: Used for controller authentication in the remote device. This
  contains the controller private key (controller.key) and the controller
  certificate or the controller certificate chain (controller.crt) in case
  of an intermediate CA signs the controller certificate.

- Truststore: Used to authenticate remote devices. This contains the root
  CA certificates signing the OpenFlow devices certificates or the
  intermediate CA certificates (in case of certificate chain).

You may skip this step if you already generated the Key Stores from a
previous TLS installation. In the examples below we will use openSSL and
Java keytool tooling to create the Key Stores.

#. Create the controller Keystore

   The command below generates the controller Keystore in PKCS12 format:

   .. code-block:: none

       openssl pkcs12 -export -in controller.crt -inkey controller.key -out keystore.p12 -name controller

   When asked for a password select 'opendaylight' (or anything else).

   This will generate the keystore.p12 file.

   .. note:: If device (e.g. Open vSwitch) does not support certificate chains,
             make sure controller.crt only contains the controller certificate
             with no extra intermediate CA certificates.

#. Create the controller Truststore

   The command below generates the controller Truststore in PKCS12 format
   and adds the device root CA certificates rootca1.crt and rootca2.crt:

   .. code-block:: none

       keytool -importcert -storetype pkcs12 -file rootca1.crt -keystore truststore.p12 -storepass opendaylight -alias root-ca-1
       keytool -importcert -storetype pkcs12 -file rootca2.crt -keystore truststore.p12 -storepass opendaylight -alias root-ca-2

   Note in the examples we use 'opendaylight' as the store password.

   This will generate the truststore.p12 file.

   .. note:: If device (e.g. Open vSwitch) does not support certificate chains,
             make sure you add all device intermediate CA certificates in the
             controller Truststore.

Enable Controller TLS
~~~~~~~~~~~~~~~~~~~~~

Controller listens for OpenFlow connections on ports 6633 and 6653 (TCP).
You can enable TLS in both or just one of the ports.

#. Copy the Key Stores to a controller folder (e.g. opendaylight /etc folder)

#. Enable TLS on port 6633:

   Create file legacy-openflow-connection-config.xml with following content:

   .. code-block:: none

       <switch-connection-config xmlns="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:config">
         <instance-name>openflow-switch-connection-provider-legacy-impl</instance-name>
         <port>6633</port>
         <transport-protocol>TLS</transport-protocol>
         <tls>
           <keystore>etc/keystore.p12</keystore>
           <keystore-type>PKCS12</keystore-type>
           <keystore-path-type>PATH</keystore-path-type>
           <keystore-password>opendaylight</keystore-password>
           <truststore>etc/truststore.p12</truststore>
           <truststore-type>PKCS12</truststore-type>
           <truststore-path-type>PATH</truststore-path-type>
           <truststore-password>opendaylight</truststore-password>
           <certificate-password>opendaylight</certificate-password>
         </tls>
       </switch-connection-config>

   .. note:: Change password 'opendaylight' above if you used different password.

   .. note:: Change the path above of you used different folder than opendaylight /etc.

   Copy the file to opendaylight folder: /etc/opendaylight/datastore/initial/config

#. Enable TLS on port 6653:

   Create file default-openflow-connection-config.xml with following content:

   .. code-block:: none

       <switch-connection-config xmlns="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:config">
         <instance-name>openflow-switch-connection-provider-default-impl</instance-name>
         <port>6653</port>
         <transport-protocol>TLS</transport-protocol>
         <tls>
           <keystore>etc/keystore.p12</keystore>
           <keystore-type>PKCS12</keystore-type>
           <keystore-path-type>PATH</keystore-path-type>
           <keystore-password>opendaylight</keystore-password>
           <truststore>etc/truststore.p12</truststore>
           <truststore-type>PKCS12</truststore-type>
           <truststore-path-type>PATH</truststore-path-type>
           <truststore-password>opendaylight</truststore-password>
           <certificate-password>opendaylight</certificate-password>
         </tls>
       </switch-connection-config>

   .. note:: Change password 'opendaylight' above if you used different password.

   .. note:: Change the path above of you used different folder than opendaylight /etc.

   Copy the file to opendaylght folder /etc/opendaylight/datastore/initial/config

#. Restart Controller

For changes to take effect, controller has to be restarted.

Troubleshooting
---------------

Controller log is in opendaylight /data/log folder:
``opendaylight-0.9.0/data/log/karaf.log``

Logs can be also displayed on karaf console:

.. code-block:: none

   log:display

To troubleshoot OpenFlow plugin enable this TRACE in karaf console:

.. code-block:: none

   log:set TRACE org.opendaylight.openflowplugin.openflow.md.core
   log:set TRACE org.opendaylight.openflowplugin.impl

To restore log settings:

.. code-block:: none

   log:set INFO org.opendaylight.openflowplugin.openflow.md.core
   log:set INFO org.opendaylight.openflowplugin.impl

.. _openflow-specs: https://www.opennetworking.org/software-defined-standards/specifications
.. _opendaylight_models: https://wiki.opendaylight.org/view/OpenDaylight_Controller:Config:Model_Reference

