.. contents:: Table of Contents
      :depth: 3

==============
Bundles-Resync
==============
Test Suite for testing Bundles-Reconciliation functionality.

Test Setup
==========
Test setup consists of ODL and two switches(Openflow nodes) connected
to ODL via OpenflowPlugin Channel (6653).

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

* Bring up ODL with `odl-openflowplugin-flow-services-rest` installed.
* Add bridge br-int to openflow node
* Connect bridge to OpenFlow using `ovs-vsctl set-controller`
* Repeat above steps for other openflow nodes


Test Suite Cleanup
------------------
Following steps are followed at the end of test suite:

* Delete bridge br-int on openflow node
* Repeat the same for other openflow nodes

Debugging
---------
Following DataStore models are captured at end of each test case:

* config/opendaylight-inventory:nodes
* operational/opendaylight-inventory:nodes

Test Cases
==========
Testcases covered in automation:

* Verify the Bundle based reconciliation with switch(OVS) restart scenario
* Verify the Bundle based reconciliation by pushing group dependent flow with switch(OVS) restart scenario
* Verify the Bundle Based reconciliation by connecting a new switch(OVS)

Verify the default reconciliation
---------------------------------
This Verifies the default reconciliation (bundle-based-reconciliation-enabled=false)

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the switch fail mode to Secure.
#. Push flow via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Restart the switch
#. Check that the flows pushed via Rest call should be present and the static flow added
#. Capture via Wireshark and check that the OFPT_EXP messages are not captured.
#. Check in the karaf.log and confirm if EXP messages are not logged
#. Flap the management interface of the switch.
#. Also ensure that the static flow added would be present.
#. Capture via Wireshark and check that the OFPT_EXP messages are not captured.
#. Check in the karaf.log and confirm if EXP messages are not logged


   #. Verify the Test Procedure.

Troubleshooting
^^^^^^^^^^^^^^^
N.A.


Verify the Bundle based reconciliation by enabling the flag to True
-------------------------------------------------------------------
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
------------------------------------------------------------------------
The Objective of this Testcase to verify bundle based reconciliation with ovs restart scenario.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Push flow via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
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

Verify the Bundle based reconciliation by pushing group dependent flow with switch(OVS) restart scenario
--------------------------------------------------------------------------------------------------------
The Objective of this Testcase to verify bundle based reconciliation with ovs restart scenario.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Push flow via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
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
----------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation by connecting a new switch to the controller.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true
#. Push group dependent flow via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
#. Set the Switch fail-mode set to secure.
#. Check if the flag set event is logged in karaf.log.
#. Check if the pushed flows are there in the OVS.
#. Get a new switch connected to the Controller.
#. Push flow via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow to the newly added switch
#. Flap the management interface of the new switch.
#. Ensure the flows are pushed via bundles to the new switch.
#. Flows remain intact in the switch that was already connected.

   #. Verify the Test Steps

Troubleshooting
^^^^^^^^^^^^^^^
N.A.

Verify the Bundle based reconciliation by killing the OVS Switch Process
------------------------------------------------------------------------
The Objective of this Testcase to verify the bundle based reconciliation by killing the ovs switch.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

#. Bring up the Controller.
#. Set the bundle-based-reconciliation-enabled=true.
#. Push flow via Rest call and add a flow in the ovs-switch via ovs-ofctl add-flow
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

Implementation
==============

Assignee(s)
-----------

Primary assignee:
  Fathima Thasneem  (a.fathima.thasneem@ericsson.com)

Other contributors:
  N.A.

Work Items
----------
N.A.

Links
-----

* https://git.opendaylight.org/gerrit/#/c/68364/
* Script path test/csit/suites/openflowplugin/Bundlebased_Reconciliation/010_bundle_resync.robot

References
==========
