.. contents:: Table of Contents
   :depth: 3

=========================================
Arbitrator Reconciliation using OF bundle
=========================================

`Arbitrator Reconciliation Reviews <https://git.opendaylight.org/gerrit/#/q/topic:arbitrator-reconciliation>`__

This spec addresses following enhancement in openflowplugin module:

Addition of new reconciliation mode in openflowplugin which will allow applications to program flow/group within
reconciliation window instead of frm reads and pushes the states down to the openflow switch.

Reconciliation Mode : <ARBITRATED|AUTONOMOUS>

AUTONOMOUS mode implies that OFPlugin shall perform reconciliation autonomously as it does now without any change in
the workflow - ie. Switch-connection-handling followed by normal/bundle-based reconciliation execution followed by
publishing of switch to the Inventory Operational DS. This will be the default mode until arbitrated mode is enabled.

ARBITRATED mode implies that OFPlugin shall not execute reconciliation by itself but by allowing higher level app
"Reconciliation arbiter / orchestrator" to initiate and complete the bundle-based reconciliation including any
error-handling.


Problem description
===================
During replay based upgrade, the inventory configuration DS will be empty and applications has to program flows/groups
based on the config events replayed by orchestrator. These new states has to applied on the switch without datapath
disruption.

This can be achieved using OF bundles. Bundle is a sequence of openflow requests from the controller that is applied
as a single openflow operation on the switch.

Use Cases
---------
Reconciliation during replay based controller upgrade

Proposed change
---------------
Arbitrator reconciliaton mode will be introduced to handle replay based upgrade. It can be enabled/disabled as per the
need. Default: arbitrator-reconciliation will be kept as disabled.

NOTE: the below configuration will be moved to yang and xml based configuration.
Orchestrator can enable arbitrator-reconciliation in openflowplugin.cfg. This should be enabled before starting the
controller.

.. code-block:: none
   :caption: openflowplugin.cfg

   #
   # Arbitrator reconciliation can be enabled by making the flag to true.
   # By default arbitrator reconciliation is disabled and reconciliation happens
   # via normal flow/group mods/bundle based.
   # NOTE: This option will be effective with disable_reconciliation=false.
   #
   # arbitrator-reconciliation-enabled=false

ArbitratorReconcile module registers itself with reconciliation framework with priority 2.

When openflow switch connect event receives at openflowplugin, it notifies reconciliation framework.

Flownode Reconciliation module will be called first by RF as it was registered with higher priority. Flownode
reconciliation module is the one responsible for reconciliation of openflow node. It can be done either via
flow/group based or bundle based. When arbitrator-reconciliation-enabled is set to true, reconciliation will be
skipped and sends success to the framework.

Reconciliation framework notifies the ArbitratorReconcile to executes its task.

ArbitratorReconcile module will do the following steps in arbitrator-reconciliation mode

* Open bundle on the connected openflow switch and stores the bundle id the cache
* Send delete all groups bundle message and all flows bundle message to the openflow switch

NOTE: Above clean up step is needed during upgrade to clean the previous version controller states.

ArbitratorReconcile module sends success to the RF if the above steps are successful else sends failure

Reconciliation framework notifies openflowplugin with the completion state.

* Success: OFP writes the openflow node (OVS) information into the operational inventory datastore.
* Failure: Openflow node will be disconnected and all the above steps will be executed till the mode is in
arbitrator-reconciliation

Applications like Interface Manager, ITM, Netvirt receives Node Added/port status DTCN.

Applications programs flows and groups into the config inventory datastore and FRM forwarder receives config
DTCN change.

Calls ArbitratorReconcile to get active bundle id for the node.

* Check if there is bundle commit in progress for the requested node, if yes block on commit bundle future
* Return active bundle id and the same will be used by FRM forwarder to push the state via bundle add messages.
* This call will return null in case of arbitrator-reconciliation disabled.

RPC will be exposed for committing the bundle. This rpc will be per node basis and it's orchestrator responsibility
to commit the bundle on all connected openflow node.

Upgrade orchestrator calls commit bundle rpc with openflow node id

* ArbitratorReconcile commits the bundle on the openflow node and stores the future for reference
* When bundle commit is in progress, flow add via config ds will be blocked on the commit future. This will make sure
  the new state is not getting lost. Commit bundle will clear all the existing flows and groups and programs the new
  state. If we allow the flow programming during commit bundle phase, we might loose the state.
* Once commit bundle future is done, it will return the rpc result to the orchestrator and removes the future from
  the local cache. Next flow provisioning will go via flow-mod.
* Orchestrator can decide on the next step based on the rpc result.

Once bundle commit executed for all the openflow node, upgrade orchestrator disables the arbitrator-reconciliation flag.

Next openflow switch connect/re-connect will go through normal reconciliation.

Note: default bundle timeout logic will be added.

Pipeline changes
----------------
None

Yang changes
------------
arbitrator-reconciliation and default-timeout will be added

rpc for commit bundle will be done

Configuration impact
--------------------
None

Clustering considerations
-------------------------
None

Other Infra considerations
--------------------------
None

Security considerations
-----------------------
None

Scale and Performance Impact
----------------------------
Unknown

Targeted Release
----------------
Flourine

Alternatives
------------
None

Usage
=====
None

REST API
--------
None

CLI
---
None.

Implementation
==============
Assignee(s)
-----------
Primary assignee:
  Arunprakash D <d.arunprakash@ericsson.com>
  Muthukumaran K <muthukumaran.k@ericsson.com>
  Gobinath Suganthan <gobinath@ericsson.com>

Work Items
----------

Dependencies
============
No new dependencies.

Testing
=======
Unit Tests
----------


CSIT
----

Documentation Impact
====================
None

References
==========

