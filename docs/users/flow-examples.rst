.. _ofp-flow-examples:

Flow Examples
-------------

Overview
~~~~~~~~

The flow examples on this page are tested to work with OVS.

Use, for example, POSTMAN with the following parameters:

::

    PUT http://<ctrl-addr>:8181/restconf/config/opendaylight-inventory:nodes/node/<Node-id>/table/<Table-#>/flow/<Flow-#>

    - Accept: application/xml
    - Content-Type: application/xml

For example:

::

    PUT http://localhost:8181/restconf/config/opendaylight-inventory:nodes/node/openflow:1/table/2/flow/127

Use RFC 8040 URL, for example, POSTMAN with the following parameters:

::

    PUT http://<ctrl-addr>:8181/rests/data/opendaylight-inventory:nodes/node=<Node-id>/table=<Table-#>/flow=<Flow-#>

    - Accept: application/json
    - Content-Type: application/json

For example:

::

    PUT http://localhost:8181/rests/data/opendaylight-inventory:nodes/node=openflow%3A1/table=2/flow=127

Make sure that the Table-# and Flow-# in the URL and in the XML match.

The format of the flow-programming XML is determined by the grouping
*flow* in the opendaylight-flow-types yang model: MISSING LINK.

Match Examples
~~~~~~~~~~~~~~

The format of the XML that describes OpenFlow matches is determined by
the opendaylight-match-types yang model: .

IPv4 Dest Address
^^^^^^^^^^^^^^^^^

-  Flow=124, Table=2, Priority=2,
   Instructions=\\{Apply\_Actions={dec\_nw\_ttl}},
   match=\\{ipv4\_destination\_address=10.0.1.1/24}

-  Note that ethernet-type MUST be 2048 (0x800)

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>124</id>
            <cookie_mask>255</cookie_mask>
            <installHw>false</installHw>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2048</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv4-destination>10.0.1.1/24</ipv4-destination>
            </match>
            <hard-timeout>12</hard-timeout>
            <cookie>1</cookie>
            <idle-timeout>34</idle-timeout>
            <flow-name>FooXf1</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab :: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "124",
                    "table_id": 2,
                    "installHw": false,
                    "barrier": false,
                    "flow-name": "FooXf1",
                    "strict": false,
                    "idle-timeout": 34,
                    "priority": 2,
                    "hard-timeout": 12,
                    "cookie_mask": 255,
                    "match": {
                        "ipv4-destination": "10.0.1.1/24",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            }
                        }
                    },
                    "cookie": 1,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Ethernet Src Address
^^^^^^^^^^^^^^^^^^^^

-  Flow=126, Table=2, Priority=2,
   Instructions=\\{Apply\_Actions={drop}},
   match=\\{ethernet-source=00:00:00:00:00:01}

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <drop-action/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>126</id>
            <cookie_mask>255</cookie_mask>
            <installHw>false</installHw>
            <match>
                <ethernet-match>
                    <ethernet-source>
                        <address>00:00:00:00:00:01</address>
                    </ethernet-source>
                </ethernet-match>
            </match>
            <hard-timeout>12</hard-timeout>
            <cookie>3</cookie>
            <idle-timeout>34</idle-timeout>
            <flow-name>FooXf3</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "126",
                    "table_id": 2,
                    "installHw": false,
                    "barrier": false,
                    "flow-name": "FooXf3",
                    "strict": false,
                    "idle-timeout": 34,
                    "priority": 2,
                    "hard-timeout": 12,
                    "cookie_mask": 255,
                    "match": {
                        "ethernet-match": {
                            "ethernet-source": {
                                "address": "00:00:00:00:00:01"
                            }
                        }
                    },
                    "cookie": 3,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "drop-action": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Ethernet Src & Dest Addresses, Ethernet Type
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Flow=127, Table=2, Priority=2,
   Instructions=\\{Apply\_Actions={drop}},
   match=\\{ethernet-source=00:00:00:00:23:ae,
   ethernet-destination=ff:ff:ff:ff:ff:ff, ethernet-type=45}

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-mpls-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>127</id>
            <cookie_mask>255</cookie_mask>
            <installHw>false</installHw>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>45</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:ff:ff:ff:ff</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:00:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
            </match>
            <hard-timeout>12</hard-timeout>
            <cookie>4</cookie>
            <idle-timeout>34</idle-timeout>
            <flow-name>FooXf4</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "127",
                    "table_id": 2,
                    "installHw": false,
                    "barrier": false,
                    "flow-name": "FooXf4",
                    "strict": false,
                    "idle-timeout": 34,
                    "priority": 2,
                    "hard-timeout": 12,
                    "cookie_mask": 255,
                    "match": {
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 45
                            },
                            "ethernet-source": {
                                "address": "00:00:00:00:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:ff:ff:ff:ff"
                            }
                        }
                    },
                    "cookie": 4,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-mpls-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }


Ethernet Src & Dest Addresses, IPv4 Src & Dest Addresses, Input Port
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34887 (0x8847)

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-mpls-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>128</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34887</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:ff:ff:ff:ff</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:00:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <ipv4-source>10.1.2.3/24</ipv4-source>
                <ipv4-destination>20.4.5.6/16</ipv4-destination>
                <in-port>0</in-port>
            </match>
            <hard-timeout>12</hard-timeout>
            <cookie>5</cookie>
            <idle-timeout>34</idle-timeout>
            <flow-name>FooXf5</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "128",
                    "table_id": 2,
                    "barrier": false,
                    "flow-name": "FooXf5",
                    "strict": false,
                    "idle-timeout": 34,
                    "priority": 2,
                    "hard-timeout": 12,
                    "cookie_mask": 255,
                    "match": {
                        "ipv4-source": "10.1.2.3/24",
                        "ipv4-destination": "20.4.5.6/16",
                        "in-port": "0",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34887
                            },
                            "ethernet-source": {
                                "address": "00:00:00:00:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:ff:ff:ff:ff"
                            }
                        }
                    },
                    "cookie": 5,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-mpls-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Ethernet Src & Dest Addresses, IPv4 Src & Dest Addresses, IP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Protocol #, IP DSCP, IP ECN, Input Port

-  Note that ethernet-type MUST be 2048 (0x800)

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>130</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2048</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:ff:ff:ff:aa</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:11:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <ipv4-source>10.1.2.3/24</ipv4-source>
                <ipv4-destination>20.4.5.6/16</ipv4-destination>
                <ip-match>
                    <ip-protocol>56</ip-protocol>
                    <ip-dscp>15</ip-dscp>
                    <ip-ecn>1</ip-ecn>
                </ip-match>
                <in-port>0</in-port>
            </match>
            <hard-timeout>12000</hard-timeout>
            <cookie>7</cookie>
            <idle-timeout>12000</idle-timeout>
            <flow-name>FooXf7</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "130",
                    "table_id": 2,
                    "barrier": false,
                    "flow-name": "FooXf7",
                    "strict": false,
                    "idle-timeout": 12000,
                    "priority": 2,
                    "hard-timeout": 12000,
                    "cookie_mask": 255,
                    "match": {
                        "ipv4-source": "10.1.2.3/24",
                        "ipv4-destination": "20.4.5.6/16",
                        "ip-match": {
                            "ip-dscp": 15,
                            "ip-protocol": 56,
                            "ip-ecn": 1
                        },
                        "in-port": "0",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            },
                            "ethernet-source": {
                                "address": "00:00:00:11:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:ff:ff:ff:aa"
                            }
                        }
                    },
                    "cookie": 7,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Ethernet Src & Dest Addresses, IPv4 Src & Dest Addresses, TCP Src &
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Dest Ports, IP DSCP, IP ECN, Input Port

-  Note that ethernet-type MUST be 2048 (0x800)

-  Note that IP Protocol Type MUST be 6

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>131</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2048</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:29:01:19:61</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:11:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <ipv4-source>17.1.2.3/8</ipv4-source>
                <ipv4-destination>172.168.5.6/16</ipv4-destination>
                <ip-match>
                    <ip-protocol>6</ip-protocol>
                    <ip-dscp>2</ip-dscp>
                    <ip-ecn>2</ip-ecn>
                </ip-match>
                <tcp-source-port>25364</tcp-source-port>
                <tcp-destination-port>8080</tcp-destination-port>
                <in-port>0</in-port>
            </match>
            <hard-timeout>1200</hard-timeout>
            <cookie>8</cookie>
            <idle-timeout>3400</idle-timeout>
            <flow-name>FooXf8</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "131",
                    "table_id": 2,
                    "barrier": false,
                    "flow-name": "FooXf8",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv4-source": "17.1.2.3/8",
                        "ipv4-destination": "172.168.5.6/16",
                        "ip-match": {
                            "ip-dscp": 2,
                            "ip-protocol": 6,
                            "ip-ecn": 2
                        },
                        "in-port": "0",
                        "tcp-source-port": 25364,
                        "tcp-destination-port": 8080,
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            },
                            "ethernet-source": {
                                "address": "00:00:00:11:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:29:01:19:61"
                            }
                        }
                    },
                    "cookie": 8,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Ethernet Src & Dest Addresses, IPv4 Src & Dest Addresses, UDP Src &
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Dest Ports, IP DSCP, IP ECN, Input Port

-  Note that ethernet-type MUST be 2048 (0x800)

-  Note that IP Protocol Type MUST be 17

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>132</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2048</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>20:14:29:01:19:61</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:11:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <ipv4-source>19.1.2.3/10</ipv4-source>
                <ipv4-destination>172.168.5.6/18</ipv4-destination>
                <ip-match>
                    <ip-protocol>17</ip-protocol>
                    <ip-dscp>8</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <udp-source-port>25364</udp-source-port>
                <udp-destination-port>8080</udp-destination-port>
                <in-port>0</in-port>
            </match>
            <hard-timeout>1200</hard-timeout>
            <cookie>9</cookie>
            <idle-timeout>3400</idle-timeout>
            <flow-name>FooXf9</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "132",
                    "table_id": 2,
                    "barrier": false,
                    "flow-name": "FooXf9",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv4-source": "19.1.2.3/10",
                        "ipv4-destination": "172.168.5.6/18",
                        "ip-match": {
                            "ip-dscp": 8,
                            "ip-protocol": 17,
                            "ip-ecn": 3
                        },
                        "in-port": "0",
                        "udp-source-port": 25364,
                        "udp-destination-port": 8080,
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            },
                            "ethernet-source": {
                                "address": "00:00:00:11:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "20:14:29:01:19:61"
                            }
                        }
                    },
                    "cookie": 9,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }


Ethernet Src & Dest Addresses, IPv4 Src & Dest Addresses, ICMPv4
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Type & Code, IP DSCP, IP ECN, Input Port

-  Note that ethernet-type MUST be 2048 (0x800)

-  Note that IP Protocol Type MUST be 1

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>134</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2048</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:29:01:19:61</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:11:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <ipv4-source>17.1.2.3/8</ipv4-source>
                <ipv4-destination>172.168.5.6/16</ipv4-destination>
                <ip-match>
                    <ip-protocol>1</ip-protocol>
                    <ip-dscp>27</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <icmpv4-match>
                    <icmpv4-type>6</icmpv4-type>
                    <icmpv4-code>3</icmpv4-code>
                </icmpv4-match>
                <in-port>0</in-port>
            </match>
            <hard-timeout>1200</hard-timeout>
            <cookie>11</cookie>
            <idle-timeout>3400</idle-timeout>
            <flow-name>FooXf11</flow-name>
            <priority>2</priority>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "134",
                    "table_id": 2,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv4-source": "17.1.2.3/8",
                        "ipv4-destination": "172.168.5.6/16",
                        "ip-match": {
                            "ip-dscp": 27,
                            "ip-protocol": 1,
                            "ip-ecn": 3
                        },
                        "icmpv4-match": {
                            "icmpv4-type": 6,
                            "icmpv4-code": 3
                        },
                        "in-port": "0",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            },
                            "ethernet-source": {
                                "address": "00:00:00:11:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:29:01:19:61"
                            }
                        }
                    },
                    "cookie": 11,
                    "flow-name": "FooXf11",
                    "strict": false,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    },
                    "idle-timeout": 3400
                }
            ]
        }

Ethernet Src & Dest Addresses, ARP Operation, ARP Src & Target
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Transport Addresses, ARP Src & Target Hw Addresses

-  Note that ethernet-type MUST be 2054 (0x806)

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                        <action>
                            <order>1</order>
                            <dec-mpls-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>137</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2054</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:ff:ff:FF:ff</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:FC:01:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <arp-op>1</arp-op>
                <arp-source-transport-address>192.168.4.1/10</arp-source-transport-address>
                <arp-target-transport-address>10.21.22.23/25</arp-target-transport-address>
                <arp-source-hardware-address>
                    <address>12:34:56:78:98:AB</address>
                </arp-source-hardware-address>
                <arp-target-hardware-address>
                    <address>FE:DC:BA:98:76:54</address>
                </arp-target-hardware-address>
            </match>
            <hard-timeout>12</hard-timeout>
            <cookie>14</cookie>
            <idle-timeout>34</idle-timeout>
            <flow-name>FooXf14</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "137",
                    "table_id": 2,
                    "priority": 2,
                    "hard-timeout": 12,
                    "cookie_mask": 255,
                    "match": {
                        "arp-source-transport-address": "192.168.4.1/10",
                        "arp-target-hardware-address": {
                            "address": "FE:DC:BA:98:76:54"
                        },
                        "arp-op": 1,
                        "arp-source-hardware-address": {
                            "address": "12:34:56:78:98:AB"
                        },
                        "arp-target-transport-address": "10.21.22.23/25",
                        "ethernet-match": {
                            "ethernet-source": {
                                "address": "00:00:FC:01:23:ae"
                            },
                            "ethernet-type": {
                                "type": 2054
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:ff:ff:FF:ff"
                            }
                        }
                    },
                    "barrier": false,
                    "cookie": 14,
                    "flow-name": "FooXf14",
                    "strict": false,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        },
                                        {
                                            "order": 1,
                                            "dec-mpls-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    },
                    "idle-timeout": 34
                }
            ]
        }

Ethernet Src & Dest Addresses, Ethernet Type, VLAN ID, VLAN PCP
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <table_id>2</table_id>
            <id>138</id>
            <cookie_mask>255</cookie_mask>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>2048</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:29:01:19:61</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:11:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <vlan-match>
                    <vlan-id>
                        <vlan-id>78</vlan-id>
                        <vlan-id-present>true</vlan-id-present>
                    </vlan-id>
                    <vlan-pcp>3</vlan-pcp>
              </vlan-match>
            </match>
            <hard-timeout>1200</hard-timeout>
            <cookie>15</cookie>
            <idle-timeout>3400</idle-timeout>
            <flow-name>FooXf15</flow-name>
            <priority>2</priority>
            <barrier>false</barrier>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "138",
                    "table_id": 2,
                    "barrier": false,
                    "flow-name": "FooXf15",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "vlan-match": {
                            "vlan-id": {
                                "vlan-id-present": true,
                                "vlan-id": 78
                            },
                            "vlan-pcp": 3
                        },
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 2048
                            },
                            "ethernet-source": {
                                "address": "00:00:00:11:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:29:01:19:61"
                            }
                        }
                    },
                    "cookie": 15,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Ethernet Src & Dest Addresses, MPLS Label, MPLS TC, MPLS BoS
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <flow-name>FooXf17</flow-name>
            <id>140</id>
            <cookie_mask>255</cookie_mask>
            <cookie>17</cookie>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <priority>2</priority>
            <table_id>2</table_id>
            <strict>false</strict>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34887</type>
                    </ethernet-type>
                    <ethernet-destination>
                        <address>ff:ff:29:01:19:61</address>
                    </ethernet-destination>
                    <ethernet-source>
                        <address>00:00:00:11:23:ae</address>
                    </ethernet-source>
                </ethernet-match>
                <protocol-match-fields>
                    <mpls-label>567</mpls-label>
                    <mpls-tc>3</mpls-tc>
                    <mpls-bos>1</mpls-bos>
                </protocol-match-fields>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "140",
                    "table_id": 2,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "protocol-match-fields": {
                            "mpls-bos": 1,
                            "mpls-tc": 3,
                            "mpls-label": 567
                        },
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34887
                            },
                            "ethernet-source": {
                                "address": "00:00:00:11:23:ae"
                            },
                            "ethernet-destination": {
                                "address": "ff:ff:29:01:19:61"
                            }
                        }
                    },
                    "cookie": 17,
                    "flow-name": "FooXf17",
                    "strict": false,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    },
                    "idle-timeout": 3400
                }
            ]
        }

IPv6 Src & Dest Addresses
^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34525

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf18</flow-name>
            <id>141</id>
            <cookie_mask>255</cookie_mask>
            <cookie>18</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34525</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv6-source>fe80::2acf:e9ff:fe21:6431/128</ipv6-source>
                <ipv6-destination>aabb:1234:2acf:e9ff::fe21:6431/64</ipv6-destination>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "141",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf18",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv6-source": "fe80::2acf:e9ff:fe21:6431/128",
                        "ipv6-destination": "aabb:1234:2acf:e9ff::fe21:6431/64",
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34525
                            }
                        }
                    },
                    "cookie": 18,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Metadata
^^^^^^^^

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf19</flow-name>
            <id>142</id>
            <cookie_mask>255</cookie_mask>
            <cookie>19</cookie>
            <table_id>2</table_id>
            <priority>1</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <metadata>
                    <metadata>12345</metadata>
                </metadata>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "142",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf19",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 1,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "metadata": {
                            "metadata": 12345
                        }
                    },
                    "cookie": 19,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Metadata, Metadata Mask
^^^^^^^^^^^^^^^^^^^^^^^

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf20</flow-name>
            <id>143</id>
            <cookie_mask>255</cookie_mask>
            <cookie>20</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <metadata>
                    <metadata>12345</metadata>
                    <metadata-mask>0xFF</metadata-mask>
                </metadata>
            </match>
        </flow>

    .. code-tab:: json

      {
            "flow-node-inventory:flow": [
                {
                    "id": "143",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf20",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "metadata": {
                            "metadata": 12345,
                            "metadata-mask": 255
                        }
                    },
                    "cookie": 20,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

IPv6 Src & Dest Addresses, Metadata, IP DSCP, IP ECN, UDP Src & Dest Ports
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34525

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf21</flow-name>
            <id>144</id>
            <cookie_mask>255</cookie_mask>
            <cookie>21</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34525</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                <ipv6-destination>fe80::2acf:e9ff:fe21:6431/128</ipv6-destination>
                <metadata>
                    <metadata>12345</metadata>
                </metadata>
                <ip-match>
                    <ip-protocol>17</ip-protocol>
                    <ip-dscp>8</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <udp-source-port>25364</udp-source-port>
                <udp-destination-port>8080</udp-destination-port>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "144",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf21",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                        "ipv6-destination": "fe80::2acf:e9ff:fe21:6431/128",
                        "metadata": {
                            "metadata": 12345
                        },
                        "ip-match": {
                            "ip-dscp": 8,
                            "ip-protocol": 17,
                            "ip-ecn": 3
                        },
                        "udp-source-port": 25364,
                        "udp-destination-port": 8080,
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34525
                            }
                        }
                    },
                    "cookie": 21,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

IPv6 Src & Dest Addresses, Metadata, IP DSCP, IP ECN, TCP Src & Dest Ports
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34525

-  Note that IP Protocol MUST be 6

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf22</flow-name>
            <id>145</id>
            <cookie_mask>255</cookie_mask>
            <cookie>22</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34525</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                <ipv6-destination>fe80:2acf:e9ff:fe21::6431/94</ipv6-destination>
                <metadata>
                    <metadata>12345</metadata>
                </metadata>
                <ip-match>
                    <ip-protocol>6</ip-protocol>
                    <ip-dscp>60</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <tcp-source-port>183</tcp-source-port>
                <tcp-destination-port>8080</tcp-destination-port>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "145",
                    "table_id": 2,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "installHw": false,
                    "cookie_mask": 255,
                    "match": {
                        "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                        "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/94",
                        "metadata": {
                            "metadata": 12345
                        },
                        "ip-match": {
                            "ip-dscp": 60,
                            "ip-protocol": 6,
                            "ip-ecn": 3
                        },
                        "tcp-source-port": 183,
                        "tcp-destination-port": 8080,
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34525
                            }
                        }
                    },
                    "cookie": 22,
                    "flow-name": "FooXf22",
                    "strict": false,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    },
                    "idle-timeout": 3400
                }
            ]
        }


IPv6 Src & Dest Addresses, Metadata, IP DSCP, IP ECN, TCP Src & Dest Ports, IPv6 Label
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34525

-  Note that IP Protocol MUST be 6

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf23</flow-name>
            <id>146</id>
            <cookie_mask>255</cookie_mask>
            <cookie>23</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34525</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                <ipv6-destination>fe80:2acf:e9ff:fe21::6431/94</ipv6-destination>
                <metadata>
                    <metadata>12345</metadata>
                </metadata>
                <ipv6-label>
                    <ipv6-flabel>33</ipv6-flabel>
                </ipv6-label>
                <ip-match>
                    <ip-protocol>6</ip-protocol>
                    <ip-dscp>60</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <tcp-source-port>183</tcp-source-port>
                <tcp-destination-port>8080</tcp-destination-port>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "146",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf23",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                        "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/94",
                        "ipv6-label": {
                            "ipv6-flabel": 33
                        },
                        "metadata": {
                            "metadata": 12345
                        },
                        "ip-match": {
                            "ip-dscp": 60,
                            "ip-protocol": 6,
                            "ip-ecn": 3
                        },
                        "tcp-source-port": 183,
                        "tcp-destination-port": 8080,
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34525
                            }
                        }
                    },
                    "cookie": 23,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }


Tunnel ID
^^^^^^^^^

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf24</flow-name>
            <id>147</id>
            <cookie_mask>255</cookie_mask>
            <cookie>24</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <tunnel>
                    <tunnel-id>2591</tunnel-id>
                </tunnel>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "147",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf24",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "tunnel": {
                            "tunnel-id": 2591
                        }
                    },
                    "cookie": 24,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

IPv6 Src & Dest Addresses, Metadata, IP DSCP, IP ECN, ICMPv6 Type & Code, IPv6 Label
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34525

-  Note that IP Protocol MUST be 58

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf25</flow-name>
            <id>148</id>
            <cookie_mask>255</cookie_mask>
            <cookie>25</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34525</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                <ipv6-destination>fe80:2acf:e9ff:fe21::6431/94</ipv6-destination>
                <metadata>
                    <metadata>12345</metadata>
                </metadata>
                <ipv6-label>
                    <ipv6-flabel>33</ipv6-flabel>
                </ipv6-label>
                <ip-match>
                    <ip-protocol>58</ip-protocol>
                    <ip-dscp>60</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <icmpv6-match>
                    <icmpv6-type>6</icmpv6-type>
                    <icmpv6-code>3</icmpv6-code>
                </icmpv6-match>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "148",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf25",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                        "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/94",
                        "ipv6-label": {
                            "ipv6-flabel": 33
                        },
                        "metadata": {
                            "metadata": 12345
                        },
                        "ip-match": {
                            "ip-dscp": 60,
                            "ip-protocol": 58,
                            "ip-ecn": 3
                        },
                        "icmpv6-match": {
                            "icmpv6-type": 6,
                            "icmpv6-code": 3
                        },
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34525
                            }
                        }
                    },
                    "cookie": 25,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

IPv6 Src & Dest Addresses, Metadata, IP DSCP, IP ECN, TCP Src & Dst Ports, IPv6 Label, IPv6 Ext Header
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

-  Note that ethernet-type MUST be 34525

-  Note that IP Protocol MUST be 58

.. tabs::

    .. code-tab:: xml

        <?xml version="1.0" encoding="UTF-8" standalone="no"?>
        <flow xmlns="urn:opendaylight:flow:inventory">
            <strict>false</strict>
            <flow-name>FooXf27</flow-name>
            <id>150</id>
            <cookie_mask>255</cookie_mask>
            <cookie>27</cookie>
            <table_id>2</table_id>
            <priority>2</priority>
            <hard-timeout>1200</hard-timeout>
            <idle-timeout>3400</idle-timeout>
            <installHw>false</installHw>
            <instructions>
                <instruction>
                    <order>0</order>
                    <apply-actions>
                        <action>
                            <order>0</order>
                            <dec-nw-ttl/>
                        </action>
                    </apply-actions>
                </instruction>
            </instructions>
            <match>
                <ethernet-match>
                    <ethernet-type>
                        <type>34525</type>
                    </ethernet-type>
                </ethernet-match>
                <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                <ipv6-destination>fe80:2acf:e9ff:fe21::6431/94</ipv6-destination>
                <metadata>
                    <metadata>12345</metadata>
                </metadata>
                <ipv6-label>
                    <ipv6-flabel>33</ipv6-flabel>
                </ipv6-label>
                <ipv6-ext-header>
                    <ipv6-exthdr>0</ipv6-exthdr>
                </ipv6-ext-header>
                <ip-match>
                    <ip-protocol>6</ip-protocol>
                    <ip-dscp>60</ip-dscp>
                    <ip-ecn>3</ip-ecn>
                </ip-match>
                <tcp-source-port>183</tcp-source-port>
                <tcp-destination-port>8080</tcp-destination-port>
            </match>
        </flow>

    .. code-tab:: json

       {
            "flow-node-inventory:flow": [
                {
                    "id": "150",
                    "table_id": 2,
                    "installHw": false,
                    "flow-name": "FooXf27",
                    "strict": false,
                    "idle-timeout": 3400,
                    "priority": 2,
                    "hard-timeout": 1200,
                    "cookie_mask": 255,
                    "match": {
                        "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                        "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/94",
                        "ipv6-label": {
                            "ipv6-flabel": 33
                        },
                        "ipv6-ext-header": {
                            "ipv6-exthdr": 0
                        },
                        "metadata": {
                            "metadata": 12345
                        },
                        "ip-match": {
                            "ip-dscp": 60,
                            "ip-protocol": 6,
                            "ip-ecn": 3
                        },
                        "tcp-source-port": 183,
                        "tcp-destination-port": 8080,
                        "ethernet-match": {
                            "ethernet-type": {
                                "type": 34525
                            }
                        }
                    },
                    "cookie": 27,
                    "instructions": {
                        "instruction": [
                            {
                                "order": 0,
                                "apply-actions": {
                                    "action": [
                                        {
                                            "order": 0,
                                            "dec-nw-ttl": {}
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                }
            ]
        }

Actions
~~~~~~~

The format of the XML that describes OpenFlow actions is determined by
the opendaylight-action-types yang model:

Apply Actions
^^^^^^^^^^^^^

Output to TABLE
'''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf101</flow-name>
                <id>256</id>
                <cookie_mask>255</cookie_mask>
                <cookie>101</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>TABLE</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>34525</type>
                        </ethernet-type>
                    </ethernet-match>
                    <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                    <ipv6-destination>fe80:2acf:e9ff:fe21::6431/94</ipv6-destination>
                    <metadata>
                        <metadata>12345</metadata>
                    </metadata>
                    <ip-match>
                        <ip-protocol>6</ip-protocol>
                        <ip-dscp>60</ip-dscp>
                        <ip-ecn>3</ip-ecn>
                    </ip-match>
                    <tcp-source-port>183</tcp-source-port>
                    <tcp-destination-port>8080</tcp-destination-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

               {
                    "flow-node-inventory:flow": [
                        {
                            "id": "256",
                            "table_id": 2,
                            "priority": 2,
                            "hard-timeout": 1200,
                            "installHw": false,
                            "cookie_mask": 255,
                            "match": {
                                "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                                "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/94",
                                "metadata": {
                                    "metadata": 12345
                                },
                                "ip-match": {
                                    "ip-dscp": 60,
                                    "ip-protocol": 6,
                                    "ip-ecn": 3
                                },
                                "tcp-source-port": 183,
                                "tcp-destination-port": 8080,
                                "ethernet-match": {
                                    "ethernet-type": {
                                        "type": 34525
                                    }
                                }
                            },
                            "cookie": 101,
                            "flow-name": "FooXf101",
                            "strict": false,
                            "instructions": {
                                "instruction": [
                                    {
                                        "order": 0,
                                        "apply-actions": {
                                            "action": [
                                                {
                                                    "order": 0,
                                                    "output-action": {
                                                        "output-node-connector": "TABLE",
                                                        "max-length": 60
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                ]
                            },
                            "idle-timeout": 3400
                        }
                    ]
                }

Output to INPORT
''''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf102</flow-name>
                <id>257</id>
                <cookie_mask>255</cookie_mask>
                <cookie>102</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>INPORT</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                         </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>2048</type>
                        </ethernet-type>
                        <ethernet-destination>
                            <address>ff:ff:29:01:19:61</address>
                        </ethernet-destination>
                        <ethernet-source>
                            <address>00:00:00:11:23:ae</address>
                        </ethernet-source>
                    </ethernet-match>
                    <ipv4-source>17.1.2.3/8</ipv4-source>
                    <ipv4-destination>172.168.5.6/16</ipv4-destination>
                    <ip-match>
                        <ip-protocol>6</ip-protocol>
                        <ip-dscp>2</ip-dscp>
                        <ip-ecn>2</ip-ecn>
                    </ip-match>
                    <tcp-source-port>25364</tcp-source-port>
                    <tcp-destination-port>8080</tcp-destination-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "257",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv4-source": "17.1.2.3/8",
                            "ipv4-destination": "172.168.5.6/16",
                            "ip-match": {
                                "ip-dscp": 2,
                                "ip-protocol": 6,
                                "ip-ecn": 2
                            },
                            "tcp-source-port": 25364,
                            "tcp-destination-port": 8080,
                            "ethernet-match": {
                                "ethernet-source": {
                                    "address": "00:00:00:11:23:ae"
                                },
                                "ethernet-type": {
                                    "type": 2048
                                },
                                "ethernet-destination": {
                                    "address": "ff:ff:29:01:19:61"
                                }
                            }
                        },
                        "cookie": 102,
                        "flow-name": "FooXf102",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "INPORT",
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to Physical Port
'''''''''''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf103</flow-name>
                <id>258</id>
                <cookie_mask>255</cookie_mask>
                <cookie>103</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>1</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>2048</type>
                        </ethernet-type>
                        <ethernet-destination>
                            <address>ff:ff:29:01:19:61</address>
                        </ethernet-destination>
                        <ethernet-source>
                            <address>00:00:00:11:23:ae</address>
                        </ethernet-source>
                    </ethernet-match>
                    <ipv4-source>17.1.2.3/8</ipv4-source>
                    <ipv4-destination>172.168.5.6/16</ipv4-destination>
                    <ip-match>
                        <ip-protocol>6</ip-protocol>
                        <ip-dscp>2</ip-dscp>
                        <ip-ecn>2</ip-ecn>
                    </ip-match>
                    <tcp-source-port>25364</tcp-source-port>
                    <tcp-destination-port>8080</tcp-destination-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "258",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv4-source": "17.1.2.3/8",
                            "ipv4-destination": "172.168.5.6/16",
                            "ip-match": {
                                "ip-dscp": 2,
                                "ip-protocol": 6,
                                "ip-ecn": 2
                            },
                            "tcp-source-port": 25364,
                            "tcp-destination-port": 8080,
                            "ethernet-match": {
                                "ethernet-source": {
                                    "address": "00:00:00:11:23:ae"
                                },
                                "ethernet-type": {
                                    "type": 2048
                                },
                                "ethernet-destination": {
                                    "address": "ff:ff:29:01:19:61"
                                }
                            }
                        },
                        "cookie": 103,
                        "flow-name": "FooXf103",
                        "strict": false,
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
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to LOCAL
'''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf104</flow-name>
                <id>259</id>
                <cookie_mask>255</cookie_mask>
                <cookie>104</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>LOCAL</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>34525</type>
                        </ethernet-type>
                    </ethernet-match>
                    <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76</ipv6-source>
                    <ipv6-destination>fe80:2acf:e9ff:fe21::6431/94</ipv6-destination>
                    <metadata>
                        <metadata>12345</metadata>
                    </metadata>
                    <ip-match>
                        <ip-protocol>6</ip-protocol>
                        <ip-dscp>60</ip-dscp>
                        <ip-ecn>3</ip-ecn>
                    </ip-match>
                    <tcp-source-port>183</tcp-source-port>
                    <tcp-destination-port>8080</tcp-destination-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "259",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/76",
                            "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/94",
                            "metadata": {
                                "metadata": 12345
                            },
                            "ip-match": {
                                "ip-dscp": 60,
                                "ip-protocol": 6,
                                "ip-ecn": 3
                            },
                            "tcp-source-port": 183,
                            "tcp-destination-port": 8080,
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 34525
                                }
                            }
                        },
                        "cookie": 104,
                        "flow-name": "FooXf104",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "LOCAL",
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to NORMAL
''''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf105</flow-name>
                <id>260</id>
                <cookie_mask>255</cookie_mask>
                <cookie>105</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>NORMAL</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>34525</type>
                        </ethernet-type>
                    </ethernet-match>
                    <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/84</ipv6-source>
                    <ipv6-destination>fe80:2acf:e9ff:fe21::6431/90</ipv6-destination>
                    <metadata>
                        <metadata>12345</metadata>
                    </metadata>
                    <ip-match>
                        <ip-protocol>6</ip-protocol>
                        <ip-dscp>45</ip-dscp>
                        <ip-ecn>2</ip-ecn>
                    </ip-match>
                    <tcp-source-port>20345</tcp-source-port>
                    <tcp-destination-port>80</tcp-destination-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "260",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/84",
                            "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/90",
                            "metadata": {
                                "metadata": 12345
                            },
                            "ip-match": {
                                "ip-dscp": 45,
                                "ip-protocol": 6,
                                "ip-ecn": 2
                            },
                            "tcp-source-port": 20345,
                            "tcp-destination-port": 80,
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 34525
                                }
                            }
                        },
                        "cookie": 105,
                        "flow-name": "FooXf105",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "NORMAL",
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to FLOOD
'''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf106</flow-name>
                <id>261</id>
                <cookie_mask>255</cookie_mask>
                <cookie>106</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>FLOOD</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>34525</type>
                        </ethernet-type>
                    </ethernet-match>
                    <ipv6-source>1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/100</ipv6-source>
                    <ipv6-destination>fe80:2acf:e9ff:fe21::6431/67</ipv6-destination>
                    <metadata>
                        <metadata>12345</metadata>
                    </metadata>
                    <ip-match>
                        <ip-protocol>6</ip-protocol>
                        <ip-dscp>45</ip-dscp>
                        <ip-ecn>2</ip-ecn>
                    </ip-match>
                    <tcp-source-port>20345</tcp-source-port>
                    <tcp-destination-port>80</tcp-destination-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "261",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv6-source": "1234:5678:9ABC:DEF0:FDCD:A987:6543:210F/100",
                            "ipv6-destination": "fe80:2acf:e9ff:fe21::6431/67",
                            "metadata": {
                                "metadata": 12345
                            },
                            "ip-match": {
                                "ip-dscp": 45,
                                "ip-protocol": 6,
                                "ip-ecn": 2
                            },
                            "tcp-source-port": 20345,
                            "tcp-destination-port": 80,
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 34525
                                }
                            }
                        },
                        "cookie": 106,
                        "flow-name": "FooXf106",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "FLOOD",
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to ALL
'''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf107</flow-name>
                <id>262</id>
                <cookie_mask>255</cookie_mask>
                <cookie>107</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>ALL</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>2048</type>
                        </ethernet-type>
                        <ethernet-destination>
                            <address>20:14:29:01:19:61</address>
                        </ethernet-destination>
                        <ethernet-source>
                            <address>00:00:00:11:23:ae</address>
                        </ethernet-source>
                    </ethernet-match>
                    <ipv4-source>19.1.2.3/10</ipv4-source>
                    <ipv4-destination>172.168.5.6/18</ipv4-destination>
                    <ip-match>
                        <ip-protocol>17</ip-protocol>
                        <ip-dscp>8</ip-dscp>
                        <ip-ecn>3</ip-ecn>
                    </ip-match>
                    <udp-source-port>25364</udp-source-port>
                    <udp-destination-port>8080</udp-destination-port>
                    <in-port>0</in-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "262",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv4-source": "19.1.2.3/10",
                            "ipv4-destination": "172.168.5.6/18",
                            "ip-match": {
                                "ip-dscp": 8,
                                "ip-protocol": 17,
                                "ip-ecn": 3
                            },
                            "in-port": "0",
                            "udp-source-port": 25364,
                            "udp-destination-port": 8080,
                            "ethernet-match": {
                                "ethernet-source": {
                                    "address": "00:00:00:11:23:ae"
                                },
                                "ethernet-type": {
                                    "type": 2048
                                },
                                "ethernet-destination": {
                                    "address": "20:14:29:01:19:61"
                                }
                            }
                        },
                        "cookie": 107,
                        "flow-name": "FooXf107",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "ALL",
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to CONTROLLER
''''''''''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf108</flow-name>
                <id>263</id>
                <cookie_mask>255</cookie_mask>
                <cookie>108</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>CONTROLLER</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>2048</type>
                        </ethernet-type>
                        <ethernet-destination>
                            <address>20:14:29:01:19:61</address>
                        </ethernet-destination>
                        <ethernet-source>
                            <address>00:00:00:11:23:ae</address>
                        </ethernet-source>
                    </ethernet-match>
                    <ipv4-source>19.1.2.3/10</ipv4-source>
                    <ipv4-destination>172.168.5.6/18</ipv4-destination>
                    <ip-match>
                        <ip-protocol>17</ip-protocol>
                        <ip-dscp>8</ip-dscp>
                        <ip-ecn>3</ip-ecn>
                    </ip-match>
                    <udp-source-port>25364</udp-source-port>
                    <udp-destination-port>8080</udp-destination-port>
                    <in-port>0</in-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "263",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv4-source": "19.1.2.3/10",
                            "ipv4-destination": "172.168.5.6/18",
                            "ip-match": {
                                "ip-dscp": 8,
                                "ip-protocol": 17,
                                "ip-ecn": 3
                            },
                            "in-port": "0",
                            "udp-source-port": 25364,
                            "udp-destination-port": 8080,
                            "ethernet-match": {
                                "ethernet-source": {
                                    "address": "00:00:00:11:23:ae"
                                },
                                "ethernet-type": {
                                    "type": 2048
                                },
                                "ethernet-destination": {
                                    "address": "20:14:29:01:19:61"
                                }
                            }
                        },
                        "cookie": 108,
                        "flow-name": "FooXf108",
                        "strict": false,
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
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Output to ANY
'''''''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <strict>false</strict>
                <flow-name>FooXf109</flow-name>
                <id>264</id>
                <cookie_mask>255</cookie_mask>
                <cookie>109</cookie>
                <table_id>2</table_id>
                <priority>2</priority>
                <hard-timeout>1200</hard-timeout>
                <idle-timeout>3400</idle-timeout>
                <installHw>false</installHw>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <order>0</order>
                                <output-action>
                                    <output-node-connector>ANY</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>2048</type>
                        </ethernet-type>
                        <ethernet-destination>
                            <address>20:14:29:01:19:61</address>
                        </ethernet-destination>
                        <ethernet-source>
                            <address>00:00:00:11:23:ae</address>
                        </ethernet-source>
                    </ethernet-match>
                    <ipv4-source>19.1.2.3/10</ipv4-source>
                    <ipv4-destination>172.168.5.6/18</ipv4-destination>
                    <ip-match>
                        <ip-protocol>17</ip-protocol>
                        <ip-dscp>8</ip-dscp>
                        <ip-ecn>3</ip-ecn>
                    </ip-match>
                    <udp-source-port>25364</udp-source-port>
                    <udp-destination-port>8080</udp-destination-port>
                    <in-port>0</in-port>
                </match>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "264",
                        "table_id": 2,
                        "priority": 2,
                        "hard-timeout": 1200,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv4-source": "19.1.2.3/10",
                            "ipv4-destination": "172.168.5.6/18",
                            "ip-match": {
                                "ip-dscp": 8,
                                "ip-protocol": 17,
                                "ip-ecn": 3
                            },
                            "in-port": "0",
                            "udp-source-port": 25364,
                            "udp-destination-port": 8080,
                            "ethernet-match": {
                                "ethernet-source": {
                                    "address": "00:00:00:11:23:ae"
                                },
                                "ethernet-type": {
                                    "type": 2048
                                },
                                "ethernet-destination": {
                                    "address": "20:14:29:01:19:61"
                                }
                            }
                        },
                        "cookie": 109,
                        "flow-name": "FooXf109",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "output-action": {
                                                    "output-node-connector": "ANY",
                                                    "max-length": 60
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        },
                        "idle-timeout": 3400
                    }
                ]
            }

Push VLAN
'''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
               <strict>false</strict>
               <instructions>
                   <instruction>
                       <order>0</order>
                       <apply-actions>
                          <action>
                             <push-vlan-action>
                                 <ethernet-type>33024</ethernet-type>
                             </push-vlan-action>
                             <order>0</order>
                          </action>
                           <action>
                               <set-field>
                                   <vlan-match>
                                        <vlan-id>
                                            <vlan-id>79</vlan-id>
                                            <vlan-id-present>true</vlan-id-present>
                                        </vlan-id>
                                   </vlan-match>
                               </set-field>
                               <order>1</order>
                           </action>
                           <action>
                               <output-action>
                                   <output-node-connector>5</output-node-connector>
                               </output-action>
                               <order>2</order>
                           </action>
                       </apply-actions>
                   </instruction>
               </instructions>
               <table_id>0</table_id>
               <id>31</id>
               <match>
                   <ethernet-match>
                       <ethernet-type>
                           <type>2048</type>
                       </ethernet-type>
                       <ethernet-destination>
                           <address>FF:FF:29:01:19:61</address>
                       </ethernet-destination>
                       <ethernet-source>
                           <address>00:00:00:11:23:AE</address>
                       </ethernet-source>
                   </ethernet-match>
                 <in-port>1</in-port>
               </match>
               <flow-name>vlan_flow</flow-name>
               <priority>2</priority>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "31",
                        "table_id": 0,
                        "priority": 2,
                        "match": {
                            "in-port": "1",
                            "ethernet-match": {
                                "ethernet-source": {
                                    "address": "00:00:00:11:23:AE"
                                },
                                "ethernet-type": {
                                    "type": 2048
                                },
                                "ethernet-destination": {
                                    "address": "FF:FF:29:01:19:61"
                                }
                            }
                        },
                        "flow-name": "vlan_flow",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "push-vlan-action": {
                                                    "ethernet-type": 33024
                                                }
                                            },
                                            {
                                                "order": 1,
                                                "set-field": {
                                                    "vlan-match": {
                                                        "vlan-id": {
                                                            "vlan-id-present": true,
                                                            "vlan-id": 79
                                                        }
                                                    }
                                                }
                                            },
                                            {
                                                "order": 2,
                                                "output-action": {
                                                    "output-node-connector": "5"
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }

Push MPLS
'''''''''

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow
                xmlns="urn:opendaylight:flow:inventory">
                <flow-name>push-mpls-action</flow-name>
                <instructions>
                    <instruction>
                        <order>3</order>
                        <apply-actions>
                            <action>
                                <push-mpls-action>
                                    <ethernet-type>34887</ethernet-type>
                                </push-mpls-action>
                                <order>0</order>
                            </action>
                            <action>
                                <set-field>
                                    <protocol-match-fields>
                                        <mpls-label>27</mpls-label>
                                    </protocol-match-fields>
                                </set-field>
                                <order>1</order>
                            </action>
                            <action>
                                <output-action>
                                    <output-node-connector>2</output-node-connector>
                                </output-action>
                                <order>2</order>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <strict>false</strict>
                <id>100</id>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>2048</type>
                        </ethernet-type>
                    </ethernet-match>
                    <in-port>1</in-port>
                    <ipv4-destination>10.0.0.4/32</ipv4-destination>
                </match>
                <idle-timeout>0</idle-timeout>
                <cookie_mask>255</cookie_mask>
                <cookie>401</cookie>
                <priority>8</priority>
                <hard-timeout>0</hard-timeout>
                <installHw>false</installHw>
                <table_id>0</table_id>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "100",
                        "table_id": 0,
                        "priority": 8,
                        "hard-timeout": 0,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "ipv4-destination": "10.0.0.4/32",
                            "in-port": "1",
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 2048
                                }
                            }
                        },
                        "cookie": 401,
                        "flow-name": "push-mpls-action",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 3,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "push-mpls-action": {
                                                    "ethernet-type": 34887
                                                }
                                            },
                                            {
                                                "order": 1,
                                                "set-field": {
                                                    "protocol-match-fields": {
                                                        "mpls-label": 27
                                                    }
                                                }
                                            },
                                            {
                                                "order": 2,
                                                "output-action": {
                                                    "output-node-connector": "2"
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

Swap MPLS
'''''''''

-  Note that ethernet-type MUST be 34887

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow
                xmlns="urn:opendaylight:flow:inventory">
                <flow-name>push-mpls-action</flow-name>
                <instructions>
                    <instruction>
                        <order>2</order>
                        <apply-actions>
                            <action>
                                <set-field>
                                    <protocol-match-fields>
                                        <mpls-label>37</mpls-label>
                                    </protocol-match-fields>
                                </set-field>
                                <order>1</order>
                            </action>
                            <action>
                                <output-action>
                                    <output-node-connector>2</output-node-connector>
                                </output-action>
                                <order>2</order>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <strict>false</strict>
                <id>101</id>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>34887</type>
                        </ethernet-type>
                    </ethernet-match>
                    <in-port>1</in-port>
                    <protocol-match-fields>
                        <mpls-label>27</mpls-label>
                    </protocol-match-fields>
                </match>
                <idle-timeout>0</idle-timeout>
                <cookie_mask>255</cookie_mask>
                <cookie>401</cookie>
                <priority>8</priority>
                <hard-timeout>0</hard-timeout>
                <installHw>false</installHw>
                <table_id>0</table_id>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "101",
                        "table_id": 0,
                        "priority": 8,
                        "hard-timeout": 0,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "in-port": "1",
                            "protocol-match-fields": {
                                "mpls-label": 27
                            },
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 34887
                                }
                            }
                        },
                        "cookie": 401,
                        "flow-name": "push-mpls-action",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                        "order": 2,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 1,
                                                "set-field": {
                                                    "protocol-match-fields": {
                                                        "mpls-label": 37
                                                    }
                                                }
                                            },
                                            {
                                                "order": 2,
                                                "output-action": {
                                                    "output-node-connector": "2"
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

Pop MPLS
''''''''

-  Note that ethernet-type MUST be 34887

-  Issue with OVS 2.1 `OVS
   fix <http://git.openvswitch.org/cgi-bin/gitweb.cgi?p=openvswitch;a=commitdiff;h=b3f2fc93e3f357f8d05a92f53ec253339a40887f>`_

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
                <flow-name>FooXf10</flow-name>
                <instructions>
                    <instruction>
                        <order>0</order>
                        <apply-actions>
                            <action>
                                <pop-mpls-action>
                                    <ethernet-type>2048</ethernet-type>
                                </pop-mpls-action>
                                <order>1</order>
                            </action>
                            <action>
                                <output-action>
                                    <output-node-connector>2</output-node-connector>
                                    <max-length>60</max-length>
                                </output-action>
                                <order>2</order>
                            </action>
                        </apply-actions>
                    </instruction>
                </instructions>
                <id>11</id>
                <strict>false</strict>
                <match>
                    <ethernet-match>
                        <ethernet-type>
                            <type>34887</type>
                        </ethernet-type>
                    </ethernet-match>
                    <in-port>1</in-port>
                    <protocol-match-fields>
                        <mpls-label>37</mpls-label>
                    </protocol-match-fields>
                </match>
                <idle-timeout>0</idle-timeout>
                <cookie>889</cookie>
                <cookie_mask>255</cookie_mask>
                <installHw>false</installHw>
                <hard-timeout>0</hard-timeout>
                <priority>10</priority>
                <table_id>0</table_id>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "11",
                        "table_id": 0,
                        "priority": 10,
                        "hard-timeout": 0,
                        "installHw": false,
                        "cookie_mask": 255,
                        "match": {
                            "in-port": "1",
                            "protocol-match-fields": {
                                "mpls-label": 37
                            },
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 34887
                                }
                            }
                        },
                        "cookie": 889,
                        "flow-name": "FooXf10",
                        "strict": false,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 1,
                                                "pop-mpls-action": {
                                                    "ethernet-type": 2048
                                                }
                                            },
                                            {
                                                "order": 2,
                                                "output-action": {
                                                    "output-node-connector": "2",
                                                    "max-length": 60
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

Learn
'''''

-  Nicira extension defined in
   https://github.com/osrg/openvswitch/blob/master/include/openflow/nicira-ext.h

-  Example section is -
   https://github.com/osrg/openvswitch/blob/master/include/openflow/nicira-ext.h#L788

.. tabs::

    .. tab:: XML

        .. code-block:: xml

            <?xml version="1.0" encoding="UTF-8" standalone="no"?>
            <flow xmlns="urn:opendaylight:flow:inventory">
              <id>ICMP_Ingress258a5a5ad-08a8-4ff7-98f5-ef0b96ca3bb8</id>
              <hard-timeout>0</hard-timeout>
              <idle-timeout>0</idle-timeout>
              <match>
                <ethernet-match>
                  <ethernet-type>
                    <type>2048</type>
                  </ethernet-type>
                </ethernet-match>
                <metadata>
                  <metadata>2199023255552</metadata>
                  <metadata-mask>2305841909702066176</metadata-mask>
                </metadata>
                <ip-match>
                  <ip-protocol>1</ip-protocol>
                </ip-match>
              </match>
              <cookie>110100480</cookie>
              <instructions>
                <instruction>
                  <order>0</order>
                  <apply-actions>
                    <action>
                      <order>1</order>
                      <nx-resubmit
                        xmlns="urn:opendaylight:openflowplugin:extension:nicira:action">
                        <table>220</table>
                      </nx-resubmit>
                    </action>
                    <action>
                      <order>0</order>
                      <nx-learn
                        xmlns="urn:opendaylight:openflowplugin:extension:nicira:action">
                        <idle-timeout>60</idle-timeout>
                        <fin-idle-timeout>0</fin-idle-timeout>
                        <hard-timeout>60</hard-timeout>
                        <flags>0</flags>
                        <table-id>41</table-id>
                        <priority>61010</priority>
                        <fin-hard-timeout>0</fin-hard-timeout>
                        <flow-mods>
                          <flow-mod-add-match-from-value>
                            <src-ofs>0</src-ofs>
                            <value>2048</value>
                            <src-field>1538</src-field>
                            <flow-mod-num-bits>16</flow-mod-num-bits>
                          </flow-mod-add-match-from-value>
                        </flow-mods>
                        <flow-mods>
                          <flow-mod-add-match-from-field>
                            <src-ofs>0</src-ofs>
                            <dst-ofs>0</dst-ofs>
                            <dst-field>4100</dst-field>
                            <src-field>3588</src-field>
                            <flow-mod-num-bits>32</flow-mod-num-bits>
                          </flow-mod-add-match-from-field>
                        </flow-mods>
                        <flow-mods>
                          <flow-mod-add-match-from-field>
                            <src-ofs>0</src-ofs>
                            <dst-ofs>0</dst-ofs>
                            <dst-field>518</dst-field>
                            <src-field>1030</src-field>
                            <flow-mod-num-bits>48</flow-mod-num-bits>
                          </flow-mod-add-match-from-field>
                        </flow-mods>
                        <flow-mods>
                          <flow-mod-add-match-from-field>
                            <src-ofs>0</src-ofs>
                            <dst-ofs>0</dst-ofs>
                            <dst-field>3073</dst-field>
                            <src-field>3073</src-field>
                            <flow-mod-num-bits>8</flow-mod-num-bits>
                          </flow-mod-add-match-from-field>
                        </flow-mods>
                        <flow-mods>
                          <flow-mod-copy-value-into-field>
                            <dst-ofs>0</dst-ofs>
                            <value>1</value>
                            <dst-field>65540</dst-field>
                            <flow-mod-num-bits>8</flow-mod-num-bits>
                          </flow-mod-copy-value-into-field>
                        </flow-mods>
                        <cookie>110100480</cookie>
                      </nx-learn>
                    </action>
                  </apply-actions>
                </instruction>
              </instructions>
              <installHw>true</installHw>
              <barrier>false</barrier>
              <strict>false</strict>
              <priority>61010</priority>
              <table_id>253</table_id>
              <flow-name>ACL</flow-name>
            </flow>

    .. tab:: JSON

        .. code-block:: json

           {
                "flow-node-inventory:flow": [
                    {
                        "id": "ICMP_Ingress258a5a5ad-08a8-4ff7-98f5-ef0b96ca3bb8",
                        "table_id": 253,
                        "installHw": true,
                        "barrier": false,
                        "flow-name": "ACL",
                        "strict": false,
                        "idle-timeout": 0,
                        "priority": 61010,
                        "hard-timeout": 0,
                        "match": {
                            "metadata": {
                                "metadata": 2199023255552,
                                "metadata-mask": 2305841909702066176
                            },
                            "ip-match": {
                                "ip-protocol": 1
                            },
                            "ethernet-match": {
                                "ethernet-type": {
                                    "type": 2048
                                }
                            }
                        },
                        "cookie": 110100480,
                        "instructions": {
                            "instruction": [
                                {
                                    "order": 0,
                                    "apply-actions": {
                                        "action": [
                                            {
                                                "order": 0,
                                                "openflowplugin-extension-nicira-action:nx-learn": {
                                                    "flags": 0,
                                                    "idle-timeout": 60,
                                                    "fin-idle-timeout": 0,
                                                    "hard-timeout": 60,
                                                    "fin-hard-timeout": 0,
                                                    "flow-mods": [
                                                        {
                                                            "flow-mod-add-match-from-value": {
                                                                "src-ofs": 0,
                                                                "src-field": 1538,
                                                                "flow-mod-num-bits": 16,
                                                                "value": 2048
                                                            }
                                                            },
                                                            {
                                                                "flow-mod-add-match-from-field": {
                                                                "dst-ofs": 0,
                                                                "src-ofs": 0,
                                                                "dst-field": 4100,
                                                                "src-field": 3588,
                                                                "flow-mod-num-bits": 32
                                                            }
                                                        },
                                                        {
                                                            "flow-mod-add-match-from-field": {
                                                                "dst-ofs": 0,
                                                                "src-ofs": 0,
                                                                "dst-field": 518,
                                                                "src-field": 1030,
                                                                "flow-mod-num-bits": 48
                                                            }
                                                        },
                                                        {
                                                            "flow-mod-add-match-from-field": {
                                                                "dst-ofs": 0,
                                                                "src-ofs": 0,
                                                                "dst-field": 3073,
                                                                "src-field": 3073,
                                                                "flow-mod-num-bits": 8
                                                            }
                                                        },
                                                        {
                                                            "flow-mod-copy-value-into-field": {
                                                                "dst-ofs": 0,
                                                                "dst-field": 65540,
                                                                "flow-mod-num-bits": 8,
                                                                "value": 1
                                                            }
                                                        }
                                                    ],
                                                    "cookie": 110100480,
                                                    "priority": 61010,
                                                    "table-id": 41
                                                }
                                            },
                                            {
                                                "order": 1,
                                                "openflowplugin-extension-nicira-action:nx-resubmit": {
                                                    "table": 220
                                                }
                                            }
                                        ]
                                    }
                                }
                            ]
                        }
                    }
                ]
            }
