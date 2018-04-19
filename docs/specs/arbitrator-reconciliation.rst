.. contents:: Table of Contents
   :depth: 3

===============================================
Arbitrator Reconciliation using OpenFlow bundle
===============================================

`Arbitrator Reconciliation Reviews <https://git.opendaylight.org/gerrit/#/q/topic:arbitrator-reconcile>`__

This spec addresses following enhancement in openflowplugin module:

Addition of new reconciliation mode in openflowplugin which will allow applications to program flow/group within
reconciliation window instead of frm reads and pushes the configuration down to the openflow switch.

AUTONOMOUS mode implies that Openflowplugin shall perform reconciliation autonomously as it does now without any change
in the workflow - ie. Switch-connection-handling followed by flow-based/bundle-based reconciliation execution followed
by publishing of switch to the Inventory Operational datastore. This will be the default mode until arbitrated mode is enabled.

ARBITRATED mode implies that the default openflowplugin reconciliation will be disabled and consumer application will
have to initiate and complete the reconciliation including any error-handling.
In the current implementation ARBITRATED mode will only supported bundle based reconciliation.

Openflowplugin will switch to arbitrator reconciliation mode based on the upgradeState provided by ServiceUtils.

Problem description
===================
During replay based upgrade, the inventory configuration DS will be empty and applications has to program flows/groups
based on the configuration pushed by user or external orchestrator. These new configurations has to applied on the
switch without datapath disruption.

This can be achieved using OpenFlow bundles. Bundle is a sequence of OpenFlow requests from odl controller that switch
will apply in atomic transaction.

Use Cases
---------
Application controlled reconciliation of OpenFlow devices after controller re/start.

Proposed change
---------------
Arbitrator Reconciliation using bundles support will be provided. Openflowplugin will switch to arbitrator reconciliation
based on the upgradeState provided by ServiceUtils. Orchestrator can enable or disable this mode as per their deployment
requirements.

upgradeInProgress presents in ServiceUtils project and can be changed to true to enable arbitrator reconciliation.

.. code-block:: none
   :caption: serviceutils-upgrade-config.xml

   <upgrade-config xmlns="urn:opendaylight:serviceutils:upgrade">
       <upgradeInProgress>false</upgradeInProgress>
   </upgrade-config>


ArbitratorReconciliation module registers itself with reconciliation framework with priority 1.

When OpenFlow switch connect event received by Openflowplugin, it notifies Reconciliation Framework(RF).

FlowNode Reconciliation will be notified first by RF as it registered with higher priority. FlowNode reconciliation
module is the one responsible for reconciliation of OpenFlow node. It can be done either via flow/group based or
OpenFlow bundle based.

When upgradeInProgress is set to true, FlowNode reconciliation will be skipped as the config datastore will be empty
and return success to the RF.

RF callbacks Arbitrator Reconcilition to executes its task.

Arbitrator Reconcilition will do the following steps in arbitrator-reconcilition(upgradeInProgress) mode

* Open OpenFlow bundle on the connected OpenFlow switch and stores the bundle id in the local cache
* Send delete-all groups and delete-all flows message to the opened bundle in the OpenFlow switch

NOTE: Above clean up step is needed during upgrade to clean the previous version controller states, but the real switch
clean-up will only happen when controller will commit the bundle.

Arbitrator Reconciliation module sends success to RF if the previous steps are successful or it sends failure.

RF notifies Openflowplugin with the completion state.

* Success: Openflowplugin writes the OpenFlow node information into operational inventory datastore.
* Failure: OpenFlow node will be disconnected and all the above steps will be repeated on the next reconnect till the
  mode is in arbitrator reconciliation

Consumer application listening to inventory data store will receive Node added, Port status Data Tree Change Notification(DTCN)
from data store.

Applications programs flows and groups into config inventory datastore and Forwarding Rules Manager(FRM) application in
in Openflowplugin receives DTCN from config inventory for the flows and groups.

`Arbitrator Reconciliation exposes rpc to get Active bundle id for the OpenFlow node.`

FRM Flow/Group Forwarder invokes get-active-bundle rpc and gets the bundle id.

GetActiveBundle will executes the following steps.

* Check if bundle commit is in progress for the requested node, if yes wait on commit bundle future
* Returns Active bundle id and the same will be used by FRM forwarder to push the configuration via bundle add messages.
* This call will return null in case of arbitrator-reconciliation disabled and FRM will push the configuration via normal
  Flow/Group messages.

.. code-block:: none
   :caption: arbitrator-reconcile.yang

   rpc get-active-bundle {
       description "Fetches the active available bundle in openflowplugin";
       input {
           uses "inv:node-context-ref";
           leaf node-id {
               description "Node for which the bundle active has to be fetched";
               type uint64;
           }
       }
       output {
           leaf result {
               description "The retrieved active bundle for the node";
               type "onf-ext:bundle-id";
           }
       }
   }

Routed RPC will be exposed for committing the bundle on a specified Openflow node. It's orchestrator responsibility to
commit the bundle across connected OpenFlow node.
Configurations will be pushed only via OpenFlow bundles till the commit bundle rpc is invoked.

.. code-block:: none
   :caption: arbitrator-reconcile.yang

   rpc commit-active-bundle {
       description "Commits the active available bundle for the given node in openflowplugin";
       input {
           uses "inv:node-context-ref";
           leaf node-id {
               description "Node for which the commit bundle to be executed";
               type uint64;
           }
       }
       output {
           leaf result {
               description "Success/Failure of the commit bundle for the node";
               type boolean;
           }
       }
   }

Consumer application calls commit-active-bundle rpc with OpenFlow node id

* It commits the current active bundle on the OpenFlow node and stores the future till it gets completed.
* When bundle commit is in progress, configuration pushed via config datastore will be blocked on the commit future.
  This will make sure the new configuration is not lost during the transient state. The logic during arbitrator reconciliation
  will clear all the existing flows and groups and programs the new configuration and if we allow the flow programming
  during commit bundle phase, we might loose the new configuration.
* When commit bundle is done, it will return the rpc result to the orchestrator and removes the future from the cache.
* Subsequent flow/group provisioning will be done via flow-mod/group-mod messages.
* Orchestrator can decide further actions based on the rpc result.

Once commit bundle executes on all the connected OpenFlow switch, orchestrator can disable the arbitrator reconciliation
by invoking rest rpc call on ServiceUtils `http://localhost:8383/restconf/config/odl-serviceutils-upgrade:upgrade-config/`.

Subsequent OpenFlow switch connect/re-connect will go through FlowNode reconciliation.

Note: There is no bundle timeout logic available as of now and the same will be added in future and will be kept as
configurable parameter by user.

Pipeline changes
----------------
None

Yang changes
------------
Below yang changes will done to enable arbitrator reconciliation.

RPC will be exposed to get current active bundle id for the given openflow node.

.. code-block:: none
   :caption: arbitrator-reconcile.yang

   rpc get-active-bundle {
       description "Fetches the active available bundle in openflowplugin";
       input {
           uses "inv:node-context-ref";
           leaf node-id {
               description "Node for which the bundle active has to be fetched";
               type uint64;
           }
       }
       output {
           leaf result {
               description "The retrieved active bundle for the node";
               type "onf-ext:bundle-id";
           }
       }
   }

RPC will be exposed for external application/user/consumer applications to commit the active bundle for OpenFlow switch.

.. code-block:: none
   :caption: arbitrator-reconcile.yang

   rpc commit-active-bundle {
       description "Commits the active available bundle for the given node in openflowplugin";
       input {
           uses "inv:node-context-ref";
           leaf node-id {
               description "Node for which the commit bundle to be executed";
               type uint64;
           }
       }
       output {
           leaf result {
               description "Success/Failure of the commit bundle for the node";
               type boolean;
           }
       }
   }


Configuration impact
--------------------
None

Clustering considerations
-------------------------
User can fire the commit-bundle rpc call to any controller node in the cluster. This rpc will only be executed by the
node that currently be owning the device.

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
Default reconciliation will be used or application can just reconfigure all the configuration using the normal
flow/group add/remove process.

Usage
=====
None

REST API
--------
.. code-block:: none
   :caption: http://localhost:8181/restconf/operations/arbitrator-reconcile:get-active-bundle

   Output:
   ======
   {
    "output": {1}
   }

.. code-block:: none
   :caption: http://localhost:8181/restconf/operations/arbitrator-reconcile:commit-bundle-node

   {
      "input": {
         "node": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:<OpenFlow datapath id>']",
         "node-id": "<OpenFlow datapath id>"
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

Gobinath Suganthan <gobinath@ericsson.com>

Muthukumaran K <muthukumaran.k@ericsson.com>

Work Items
----------
* Implementation of arbritrator reconcile module
* Changes in FRM for flow/group programming via openflow bundle
* Read reconciliation mode(upgradeInProgress) from service utils
* Expose RPC to commit bundle for a given OpenFlow node

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
`Bundle Extension Support <https://wiki.opendaylight.org/view/OpenDaylight_OpenFlow_Plugin:Bundles_extension_support>`__
