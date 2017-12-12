. contents:: Table of Contents
      :depth: 3

=================
Bundles-Resync
=================
Test Suite for testing Bundles-Resync functionality.

Test Setup
==========
Test setup consists of ODL and two switches(DPNs) connected
to ODL over OVSDB and OpenflowPlugin.

Testbed Topologies
------------------
This suit uses the default topology.

Default Topology
^^^^^^^^^^^^^^^^

.. literalinclude:: topologies/default-topology.txt


Hardware Requirements
---------------------
N.A.

Software Requirements
---------------------
OVS 2.6+

Test Suite Requirements
=======================

Test Suite Bringup
------------------
Following steps are followed at beginning of test suite:

* Bring up ODL with `odl-netvirt-openstack` and `odl-restconf-all` installed.
* Add bridge to DPN
* Add `tap` interfaces to bridge created above
* Add OVSDB manager to DPN using `ovs-vsctl set-manager`
* Connect bridge to OpenFlow using `ovs-vsctl set-controller`
* Repeat above steps for other DPNs


Test Suite Cleanup
------------------
Following steps are followed at beginning of test suite:

* Delete bridge DPN
* Delete OVSDB manager 'ovs-vsctl del-manager'
* Repeat above steps for other DPNs

Debugging
---------
Following DataStore models are captured at end of each test case:

* config/itm:transport-zones
* config/network-topology:network-topology
* operational/network-topology:network-topology

Test Cases
==========

Verify the default reconciliation
-----------------------------------
This Verifies the default reconciliation (bundle-based-reconciliation-enabled=false)

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the switch fail mode to Secure.
#. Push 5 flows via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Restart the switch
#. Check that the flows pushed via Rest call should be present and not the static flow added
#. Capture via Wireshark and check that the OFPT_EXP messages are not captured.
#. Check in the karaf.log and confirm if EXP messages are not logged
#. Flap the management interface of the switch.
#. Verify step 5 &6
#. Also ensure that the static flow added would be present as OVS doesn’t have stale marking.


   #. Verify the Test Procedure.

Troubleshooting
^^^^^^^^^^^^^^^
N.A.


Verify the Bundle based reconciliation by enabling the flag to True
---------------------------------------------------------------------
The Objective of this Testcase is to check the bundle based resync mechanism by enabling the flag

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Check if the flag set event is logged in karaf.log.
#. Set the bundle-based-reconciliation-enabled=false.
#. Check if the flag set event is logged in karaf.log.


   #. Verify the Test steps.

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation with switch(OVS) restart scenario
-------------------------------------------------------------------------
The Objective of this Testcase to verify bundle based reconciliation with ovs restart scenario.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Push 5 flows via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Set the Switch fail-mode set to secure.
#. Check if the flag set event is logged in karaf.log.
#. Restart the switch
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Check for the same messages to be logged in the karaf.log.
#. Repeat the same with fail-mode set to stand-alone
#. Static flow should not be present in both stand-alone and secure mode as the switch is restarted.


   #. Verify the Test steps.

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle Based reconciliation by connecting a new switch(OVS)
-----------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation by connecting a new switch to the controller.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true
#. Push 5 flows via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Set the Switch fail-mode set to secure.
#. Check if the flag set event is logged in karaf.log.
#. Check if the pushed flows are there in the OVS.
#. Get a new switch connected to the Controller.
#. Push 5 flows via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow to the newly added switch
#. Flap the management interface of the new switch.
#. Ensure the flows are pushed via bundles to the new switch.
#. Flows remain intact in the switch that was already connected.

   #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation by killing the OVS Switch Process
---------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation by killing the ovs switch.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Push 5 flows via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Set the Switch fail-mode set to secure.
#. Check if the flag set event is logged in karaf.log.
#. Kill the OVS Switch process
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Check for the same messages to be logged in the karaf.log.
#. Repeat the same with fail-mode set to stand-alone
#. Static flow should not be present in both stand-alone and secure mode.


   #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation with Leader Node reboot
-----------------------------------------------------------------
The Objective of this Testcase to verify bundle based reconciliation with Leader Node reboot

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Configure the 3 controller nodes to be in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Bring up the karaf in all three nodes.
#. Set the bundle-based-reconciliation-enabled=true in all three nodes.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via
     “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Identify the entity owner by providing this command
      "curl --silent -u admin:admin -X GET http://<node1 ip>:8181/restconf/operational/entity-owners:entity-owners/"
#. Push 5 flows via Rest call from the current entity owner and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Reboot the current leader node.
#. Now flows must be pushed from the next elected entity owner.
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Check for the same messages to be logged in the karaf.log.
#. Static flow should not be present as the flows are pushed from the IC datastore of the next elected entity owner.


   #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation with switch(OVS) disconnected from Leader node and reconnected to Follower Node
-----------------------------------------------------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation with OVS disconnected from the leader node and reconnected to the follower node.

Test Steps and Pass Criteria

^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Configure the 3 controller nodes to be in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Bring up the karaf in all three nodes.
#. Set the bundle-based-reconciliation-enabled=true in all three nodes.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via
        “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Confiure openvswitch as tcp manager and tcp controller via
        "sudo ovs-vsctl set-controller br-int tcp:<node1 ip>:6653"
        "sudo ovs-vsctl set-manager tcp:<node1 ip>:6640"
#. Push 5 flows via Rest call from the current entity owner and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Disconnect from the node1 and reconnect it to node2 as below
        "Sudo ovs-vsctl del-manager"
        "Sudo ovs-vsctl del-controller br-int"
        "Sudo ovs-vsctl set-controller br-int tcp:<node"2 ip>:6653"
        "Sudo ovs-vsctl set-manager tcp:<node2 ip>:6640
#. Now flows must be pushed from the node2.
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Check for the same messages to be logged in the karaf.log.
#. Static flow should not be present as the flows are pushed from the IC datastore of node 2 which has the DS synced being in a cluster.


   #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation with Controller Cluster reboot
-----------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation with Controller Cluster reboot

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Configure the 3 controller nodes to be in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Bring up the karaf in all three nodes.
#. Set the bundle-based-reconciliation-enabled=true in all three nodes.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via
     “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Identify the entity owner by providing this command
      "curl --silent -u admin:admin -X GET http://<node1 ip>:8181/restconf/operational/entity-owners:entity-owners/"
#. Push 5 flows via Rest call from the current entity owner and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Stop the karaf in all the nodes.
#. Restart the karaf again in all the nodes.
#. Now flows must be pushed from the current entity owner.
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Check for the same messages to be logged in the karaf.log.
#. Static flow should not be present as the flows are pushed from the IC datastore of the next elected entity owner.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.


Verify the Bundle Based reconciliation with Netvirt Application [DHCP and ELAN]
-------------------------------------------------------------------------------
The Objective of this Testcase is to check the bundle based reconciliation with DHCP,ELAN in a single dpn.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up the DPN with two tenant vms up.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms ping each other.
#. Restart the switch.
#. And immediately boot up a new tenant vm after the switch restart.
#. Make sure the DHCP & ELAN flows of the 2 tenant vms to be sent via bundle messages.
#. Ensure the same via wireshark capture.
#. DHCP & ELAN flow of the new tenant vm would be pushed as flow mod messages.
#. Data Traffic loss is expected as the switch is restarted.
#. Ensure the functionality to be same with OVS2.7 too as in OVS2.6

    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle Based reconciliation with Netvirt Application[DHCP, ITM and ELAN]
-----------------------------------------------------------------------------------
The Objective of this Testcase is to check the bundle based reconciliation with DHCP,ELAN & ITM in 2 Dpn setup.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up 2 DPN’s with two tenant vms ups in each DPN.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Disconnect the switch from the controller.
#. Data traffic should progress successfully without loss as the switch is in secure mode.
#. Reconnect the switch back to the controller.
#. Add  a new tenant vm via “nova boot” right after reconnecting the switch to the controller.
#. Make sure the DHCP & ELAN flows of the existing tenant vms to be sent via bundle messages.
#. Ensure the same via wireshark capture.
#. DHCP & ELAN flow of the new tenant vm would be pushed as flow mod messages.
#. Data Traffic loss is expected as the switch is restarted.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for ELAN with Leader Node Reboot
------------------------------------------------------------------------
The Objective of this Testcase is to check the bundle based reconciliation with leader node reboot

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
#. Bring up the Controller in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up 2 DPN’s with two tenant vms ups in each DPN.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via 
       “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Identify the entity owner by providing this command
       curl --silent -u admin:admin -X GET http://<node1 ip>:8181/restconf/operational/entity-owners:entity-owners/
#. Reboot the leader node.
#. Make sure the DHCP & ELAN flows of the existing tenant vms to be sent via bundle messages.
#. Ensure the same via Wireshark capture.
#. Data Traffic loss shouldn’t be there as the switch is in secure mode.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for ELAN with Controller Cluster reboot
-------------------------------------------------------------------------------
The Objective of this Testcase is to check bundle based reconciliation with cluster reboot

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
#. Bring up the Controller in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up 2 DPN’s with two tenant vms ups in each DPN.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via 
          “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Identify the entity owner by providing this command
          "curl --silent -u admin:admin -X GET http://<node1 ip>:8181/restconf/operational/entity-owners:entity-owners/"
#. Stop the karaf in all the nodes
#. Restart the karaf again in all the nodes
#. Make sure the DHCP & ELAN flows of the existing tenant vms  are sent via bundle messages.
#. Ensure the same via Wireshark capture.
#. Data Traffic loss shouldn’t be there as the switch is in secure mode.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for ELAN with multiple restarts of an ovs instance
------------------------------------------------------------------------------------------

The Objective of this Testcase is to check bundle based reconciliation after multiple restarts of and ovs switch (allowing the first resync to settle down before we trigger the next restart)

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up 2 DPN’s with two tenant vms ups in each DPN.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Restart the switch.
#. Check if the application flows are pushed as bundle messages via wireshark.
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Restart the switch again after the resync is completed and check for steps 8&9
#. Repeat step 10 twice.
#. Ensure flows are pushed as OFPT_EXP messages after every restart.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for ELAN by restarting multiple ovs instance (2 ovs instance)
-----------------------------------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation by restarting multiple ovs switch.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up 2 DPN’s with two tenant vms ups in each DPN.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Restart both the switches .
#. Check if the application flows are pushed as bundle messages via wireshark.
#. Check if the Wireshark has the OFPT_EXP messages captured.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for ELAN with multiple immediate restarts of an ovs instance
---------------------------------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation with multiple immediate restarts of an ovs instance.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up 2 DPN’s with two tenant vms ups in each DPN.
#. Check for the ELAN & DHCP flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Restart the switch.
#. Restart the switch twice even before the resync is completed.
#. Check if the application flows are pushed as bundle messages via wireshark.
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Ensure flows are pushed as OFPT_EXP messages after restart.

    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.


Verify the Bundle Based reconciliation with Netvirt Application[L3VPN] without Data path Test
----------------------------------------------------------------------------------------------
The Objective of this Testcase is to verify bundle based reconciliation with L3VPN Application.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up the L3VPN setup (MPLS over GRE tunnel between OVS & DC Gateway)
#. Check for the L3VPN flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Restart the switch
#. And immediately boot up a new tenant vm after the switch restart.
#. Make sure the L3VPN flows of the 2 tenant vms to be sent via bundle messages.
#. Ensure the same via Wireshark capture.
#. L3VPN flows of the new tenant vm would be pushed as flow mod messages.
#. Data Traffic loss is expected as the switch is restarted.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for L3VPN with Leader Node Reboot)
-------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation with Leader node reboot(l3vpn application).


Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up the L3VPN setup (MPLS over GRE tunnel between OVS & DC Gateway)
#. Check for the  flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Set the switch to secure mode.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via
       “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Identify the entity owner by providing this command
        "curl --silent -u admin:admin -X GET http://<node1 ip>:8181/restconf/operational/entity-owners:entity-owners/"
#. Reboot the leader node.
#. Make sure the l3vpn flows of the existing tenant vms to be sent via bundle messages.
#. Ensure the same via Wireshark capture.
#. Data Traffic loss shouldn’t be there as the switch is in secure mode.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation for L3VPN with Controller Cluster reboot
--------------------------------------------------------------------------------
The Objective of this Testcase is to check bundle based reconciliation with cluster reboot

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
#. Bring up the Controller in cluster via “./configure_cluster.sh 1 <node1 ip> <node2 ip> <node3 ip>”
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up the L3VPN setup (MPLS over GRE tunnel between OVS & DC Gateway)
#. Check for the L3VPN flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Identify the datastore cluster leader by providing this command in all 3 nodes via
     “sudo curl -u admin:admin -X GET -H "Content Type:application/json" http://<node ip>:8080/jolokia/read/org.opendaylight.controller:Category=Shards,name=member-1-shard-inventory-config,type=DistributedConfigDatastore | python -mjson.tool | grep RaftState”
#. Identify the entity owner by providing this command
      "curl --silent -u admin:admin -X GET http://<node1 ip>:8181/restconf/operational/entity-owners:entity-owners/"
#. Stop the karaf in all the nodes
#. Restart the karaf again in all the nodes
#. Make sure the L3VPN flows of the existing tenant vms  are sent via bundle messages.
#. Ensure the same via Wireshark capture.
#. Data Traffic loss shouldn’t be there as the switch is in secure mode.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.


Verify the Bundle based reconciliation for L3VPN with multiple restarts of ovs instance
----------------------------------------------------------------------------------------
The Objective of this Testcase is to check bundle based reconciliation with cluster reboot (allowing the resync to settle down before the next restart)

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up the L3VPN setup (MPLS over GRE tunnel between OVS & DC Gateway)
#. Check for the L3VPN flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Set the switch to secure mode.
#. Restart the switch.
#. Check if the application flows are pushed as bundle messages via wireshark.
#. Check if the Wireshark has the OFPT_EXP messages captured.
#. Restart the switch again after the resync is completed and check for steps 8&9
#. Repeat step 10 twice.
#. Ensure flows are pushed as OFPT_EXP messages after every restart.


    #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.


Verify the Bundle based reconciliation for L3VPN by restarting multiple ovs instance (2 ovs instance)
-------------------------------------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation by restarting multiple ovs switch.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Bring up the L3VPN setup (MPLS over GRE tunnel between OVS & DC Gateway)
#. Check for the L3VPN flows to be present via “sudo ovs-ofctl dump-flows br-int -OOpenflow13”
#. Make sure the tenant vms across DPN ping each other.
#. Set the switch to secure mode.
#. Restart both the switches.
#. Check if the application flows are pushed as bundle messages via wireshark.
#. Check if the Wireshark has the OFPT_EXP messages captured.


   #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Implementation
==============

Assignee(s)
-----------

Primary assignee:
  <developer-a>

Other contributors:
  <developer-b>
  <developer-c>


Work Items
----------
N.A.

Links
-----

* Link to implementation patche(s) in CSIT - TBD

References
==========
