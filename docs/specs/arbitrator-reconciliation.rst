.. contents:: Table of Contents
   :depth: 3

=========================================
Arbitrator Reconciliation using OF bundle
=========================================

`Arbitrator Reconciliation Reviews <https://git.opendaylight.org/gerrit/#/q/topic:arbitrator-reconcile>`__

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
need. Default: arbitrator-reconcile-enabled will be kept as disabled.

Orchestrator can enable arbitrator reconciliation via the following config model. Blueprint xml option will be provided
as well for enabling. This should be done before starting the controller.

.. code-block:: none
   :caption: arbitrator-reconcile.yang

   container arbitrator-config {
       leaf arbitrator-reconcile-enabled {
           type boolean;
           default false;
       }
   }

.. code-block:: none
   :caption: openflowplugin-arbitrator-reconcile-config.xml

   <arbitrator-config xmlns="urn:opendaylight:params:xml:ns:yang:openflowplugin:app:arbitrator">
         <arbitrator-reconcile-enabled>false</arbitrator-reconcile-enabled>
   </arbitrator-config>

ArbitratorReconcile module registers itself with reconciliation framework with priority 2.

When openflow switch connect event receives at openflowplugin, it notifies reconciliation framework.

Flownode Reconciliation module will be called first by RF as it was registered with higher priority. Flownode
reconciliation module is the one responsible for reconciliation of openflow node. It will be done either via
flow/group based or bundle based. When arbitrator-reconcile-enabled is set to true, reconciliation will be
skipped and sends success to the framework.

Reconciliation framework notifies the ArbitratorReconcile module to executes its task.

ArbitratorReconcile module will do the following steps in arbitrator-reconcile mode

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

.. code-block:: none
   :caption: openflowplugin-arbitrator-reconcile-config.xml

   rpc commit-bundle-node {
       description "Commits the active available bundle in openflowplugin";
       input {
           uses "inv:node-context-ref";
           leaf node {
               description "Node for which the commit bundle to be executed";
               type uint64;
           }
       }

       output {
           leaf result {
               type boolean;
           }
       }
   }

Upgrade orchestrator calls commit-bundle-node rpc with openflow node id

* ArbitratorReconcile commits the bundle on the openflow node and stores the future till it gets completed.
* When bundle commit is in progress, flow push via config ds will be blocked on the commit future. This will make
  sure the new state is not getting lost. As the logic in upgrade bundle wil clear all the existing flows and groups
  and programs the new state. If we allow the flow programming during commit bundle phase, we might loose the state.
* Once commit bundle future is done, it will return the rpc result to the orchestrator and removes the future from
  the local cache.
* Subsequent flow/group provisioning will be done via flow-mod/group-mod messages.
* Orchestrator can decide further actions based on the rpc result.

Once commit bundle executes on all the available openflow switch, orchestrator can disable the arbitrator
reconciliation, by calling invoking the rest.

Subsequent openflow switch connect/re-connect will go follow normal reconciliation.

Note: default bundle timeout logic will be added.

Pipeline changes
----------------
None

Yang changes
------------
Below yang changes will done for enabling arbitrator-reconcile.

.. code-block:: none
   :caption: arbitrator-reconcile.yang

   container arbitrator-config {
       leaf arbitrator-reconcile-enabled {
           type boolean;
           default false;
       }
   }

RPC will be exposed for orchestrator to commit the bundle per openflow switch.

.. code-block:: none
   :caption: openflowplugin-arbitrator-reconcile-config.xml

   rpc commit-bundle-node {
       description "Commits the active available bundle in openflowplugin";
       input {
           uses "inv:node-context-ref";
           leaf node-id {
               description "Node for which the commit bundle to be executed";
               type uint64;
           }
       }
       output {
           leaf result {
               type boolean;
           }
       }
   }

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
.. code-block:: none
   :caption: http://localhost:8181/restconf/config/arbitrator-reconcile:arbitrator-config

   {
      "arbitrator-config": {
         "arbitrator-reconcile-enabled": true
      }
   }

.. code-block:: none
   :caption: http://localhost:8181/restconf/operations/arbitrator-reconcile:commit-bundle-node

   {
      "input": {
         "node": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:<openflow datapath id>']",
         "node-id": "<openflow datapath id>"
      }
   }

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
* Implementation of arbritrator reconcile module
* Changes in FRM for flow/group programming via openflow bundle
* Expose yang model to enable arbitrator reconciliation
* Expose RPC to commit bundle for a openflow node

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
None
