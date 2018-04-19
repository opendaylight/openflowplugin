.. contents:: Table of Contents
      :depth: 3

============================
Reconciliation CLI and Alarm
============================

This spec addresses following enhancement in Openflowplugin module:

Addition of user triggered reconciliation via cli/rest in openflowplugin.

Problem description
===================
Whenever there is a state (flow/group) mismatch between config inventory and openflow switch, user has to either
restart the openflow switch/controller. This will sync the state again between odl controller and openflow switch.

Reconciliation
==============
User can trigger reconciliation to sync the state between controller and openflow switch. It can be done either via cli
or rest.

Reconciliation Alarm
====================
Reconciliation alarm will be generated whenever user trigger the reconciliation via cli/rest and the same will be
cleared once reconciliation completed.

Use Cases
---------

a. Trigger reconciliation for a single openflow switch
b. Trigger reconciliation for a list of openflow switch
c. Trigger reconciliation for all the connected openflow switches
d. Raise alarm whenever user triggered reconciliation for a openflow switch
e. Clear the alarm when the reconciliation completes for a openflow swtich

Proposed change
---------------
Karaf CLI command will be added to trigger reconciliation for a given openflow nodes.
RPC will be exposed to trigger reconciliation for a given openflow nodes.

Feature ``odl-openflowplugin-app-southbound-cli`` should be installed in order to get these cli's and rest. This
feature is not part of any existing openflowplugin features.

Below two CLIs will be added,

* openflow:reconcile
* openflow:getreconciliationcount

Pipeline changes
----------------
None

Yang changes
------------

.. code-block:: none
   :caption: reconciliation.yang

   container reconciliation-counter {
      description "Number of reconciliation triggered for openflow nodes";
      list reconcile-counter {
         key node-id;
         uses counter;
      }
   }

   grouping counter {
      leaf node-id {
          type uint64;
      }
      leaf success-count {
          type uint32;
          default 0;
      }
      leaf failure-count {
          type uint32;
          default 0;
      }
      leaf last-request-time {
           description "Timestamp when reconciliation was last requested";
           type string;
      }
   }

   rpc reconcile {
      description "Requests the execution of reconciliation between the controller and one or several or all Nodes";
      input {
          leaf-list nodes {
              description "List of nodes to be reconciled";
              type uint64;
          }

          leaf reconcile-all-nodes {
              description "Flag to indicate that all nodes to be reconciled";
              type boolean;
              mandatory false;
              default false;
          }
      }

      output {
           leaf result {
               type boolean;
           }
      }
   }

Targeted Release
----------------
Flourine

Alternatives
------------
None

REST API
--------

* POST: http://localhost:8181/restconf/operations/reconciliation:reconcile
* GET: http://localhost:8181/restconf/operational/reconciliation:reconciliation-counter

Usage
=====
Install ``odl-openflowplugin-app-southbound-cli`` feature.

CLI:
----
Trigger reconciliation for a connected openflow node via cli command ``openflow:reconcile``.

.. code-block:: bash
   :caption: openflow:reconcile

   opendaylight-user@root>openflow:reconcile 244711506862915
   reconcile successfully completed for the nodes

Trigger reconciliation for all the connected openflow nodes via cli command ``openflow:reconcile -all``.

.. code-block:: bash
   :caption: openflow:reconcile -all

         opendaylight-user@root>openflow:reconcile -all
         reconcile successfully completed for the nodes

Get details about number of times user triggered reconciliation for openflow nodes via ``openflow:getreconciliationcount``.

.. code-block:: bash
   :caption: openflow:getreconciliationcount

   opendaylight-user@root>openflow:getreconcilecount
   NodeId              ReconcileSuccessCount     ReconcileFailureCount     LastReconcileTime
   ------------------------------------------------------------------------------------------------
   244711506862915     2                         0                         2018-06-06T11:51:51.989

REST:
-----
Trigger reconciliation for a single datapath node.

.. code-block:: bash
   :caption: http://localhost:8181/restconf/operations/reconciliation:reconcile

   POST /restconf/operations/reconciliation:reconcile
   {
     "input" :  {
       "nodes":["244711506862915"]
     }
   }


Get reconciliation counter details

.. code-block:: bash
   :caption: http://localhost:8181/restconf/operational/reconciliation:reconciliation-counter

   GET /restconf/operational/reconciliation:reconciliation-counter

   OUTPUT:
   =======
   Request URL
   http://localhost:8181/restconf/operational/reconciliation:reconciliation-counter

   Response Body
   {
     "reconciliation-counter": {
       "reconcile-counter": [
         {
           "node-id": 244711506862915,
           "success-count": 4,
           "last-request-time": "2018-06-06T12:09:53.325"
         }
       ]
     }
   }


Trigger reconciliation for a openflow switch using routed rpc, this will directly invoke reconciliation without going
via affecting the counter and alarm.

.. code-block:: bash
   :caption: http://localhost:8181/restconf/operations/reconciliation:reconcile-node

   POST /restconf/operations/reconciliation:reconcile-node
   {
     "input": {
       "nodeId": "244711506862915",
       "node": "/opendaylight-inventory:nodes/opendaylight-inventory:node[opendaylight-inventory:id='openflow:244711506862915']"
     }
   }

   Request URL
   http://localhost:8181/restconf/operations/reconciliation:reconcile-node

   Response Body
   {
     "output": {
       "result": true
     }
   }

Implementation
==============
Assignee(s)
-----------
Primary assignee:
* Arunprakash D <d.arunprakash@ericsson.com>

Contributors:
* Suja T <suja.t@ericsson.com>
* Somashekhar Javalagi <somashekhar.manohara.javalagi@ericsson.com>

Work Items
----------
* Implementation of cli to trigger reconciliation for openflow node(s).
* Implementation of reconciliation alarm for user triggered reconciliation.

Dependencies
============
No new dependencies.

Testing
=======
Unit Tests
----------
#. Verify reconciliation for single openflow node
#. Verify reconciliation for list of openflow nodes
#. Verify reconciliation for all the openflow nodes
#. Verify reconciliation alarm generated for user triggered reconciliation node
#. Verify reconciliation alarm cleared once the reconciliation completed

CSIT
----
None

Documentation Impact
====================
None

References
==========
None