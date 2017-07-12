.. contents:: Table of Contents
      :depth: 3

=========================
Reconciliation Framework
=========================

https://git.opendaylight.org/gerrit/#/q/topic:recon-framework

This feature aims to overcome the drawbacks of the current reconciliation implementation.
As part of this enhancement, reconciliation framework will be introduced which will
coordinate the reconciliation across various applications.


Problem description
===================

When a switch connected to controller, the current ODL reconciliation implementation pushes
all the table/meters/groups/flows from the inventory configuration datastore to the DPN.

When the switch is connected, all the applications including FRM will received the node added
DTCN and starts pushing the flows for the DPN. FRM reconciliation will read the data from the
config and starts pushing the flows one by one. In the meantime, applications can react to the
node added DTCN change and will start pushing the flows through the config DS. With this, there
is a high chance the application flow can be overwritten by the old flows by FRM.

With framework, the problem will be avoided by doing the reconciliation for all the registered
services including FRM and then the noded added will be submitted to the DS. With this, applications
won't receive the node added DTCN.

Use Cases
---------
Coordination of Reconciliation across multiple applications on a priority based.

Proposed change
===============
1. Reconciliation Framework will be introduced, framework will coordinate the reconciliation across
applications. The OF switch wont be advertised to application until OF switch is in KNOWN state.

2. Application participating in Reconciliation needs to register with framework.
    i)  APP can either be FRM, FRS or any other application(s).
    ii) Application(s) registering with Reconciliation module is encouraged since:
        - Applications would know the right Flows and group which needs to be replayed with write operation(Add / delete / update).
        - FRM / FRS would not have application view of flows / group, it would blindly replay the flows / groups.
        - Also flows having idle / hard timeout can be gracefully handled by application rather than FRM / FRS.

3. As applications register with reconciliation module
    Reconciliation module maintains the numbers of application registered in priority order.
    Reconciliation will be triggered as per the priority, applications with same higher priority will be processed
    in parallel, once the higher priority application completed, next level of application will be processed.

4. OF switch establishes connections with ODL.
     OVS sends connection request.
     ODL accepts connection and than establishes the connection.

5. OpenFlow plugin after establishing the connection with OVS, invokes reconciliation framework.
     Before invoking the Reconciliation API the RPC are registered with MD-SAL by OF plugin
     Based of switch feature advertised the application can take a call which Reconciliation algorithm to use.

6. Reconciliation module would check
     Whether this is the only Master connection.
          ODL currently supports Master-Slave mode, the workflow would use isMaster to identify the master.
          When ODL gets enhanced to support Equal mode than we need to have locking mechanism.
     Check if Reconciliation is needed, if yes, go to Step 7
     If Reconciliation not needed, go to Step 13.1

7. All registered application would be invoked / be sent event, indicating to start the Reconciliation
     API / Event would contain all the information needed by application like Node-Id, etc.

8. Application(s) would than fetch the flows / groups for that particular Node, which needs to be replayed.

9. Application(s) would than replay the selected flows / group on to the switch.
     Application(s) can use Bundles, proprietary solution to re-play the flows /groups on to switch

10. Application(s) would also wait for error from switch, for pre-defined time.

11. Application(s) would inform the Reconciliation status to Reconciliation module.

12. Reconciliation module would co-relate status from all the modules and make sure all apps have reported there status.

13. Based on co-relation data
          When all modules report success, Reconciliation module will advertise new OF switch connection to all the Application(s).
          Else if any module fails to replay the flows / group, Reconciliation module will request Openflowplugin either to disconnect disconnect the switch and update the switch state to unknown.
          Or log appropriate messages and continue with the operation. The decision to DISCONNECT/DO_NOTHING depends on the applications decision.

Implementation Details:
-----------------------
Following new interface will be introduced from Reconciliation framework.

1. ReconciliationManager
2. ReconciliationNotificationListener

1. ReconciliationManager

     /* Application who are interested in reconcilation should use this API to register their services to the RF */
     void registerService(ReconciliationNotificationListener object);

     /* Plugin will use this api to trigger reconciliation when dpn connects */
     Future<IntentType> triggerReconciliation(NodeId nodeId);

     /* Plugin will use this api to stop reconciliation when dpn disconnects */
     void haltReconciliation(NodeId nodeId);

     /* API exposed by RF for get list of registered services
     Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices();

2. ReconciliationNotificationListener

     /* This method will be a callback from RF to start the application reconciliation */
     Future<NodeId> startReconciliation(NodeId nodeId);

     /* This method will be a callback from RF when dpn disconnects during reconcilation */
     Future<NodeId> endReconciliation(NodeId nodeId);

     /* Priority of the application */
     int getPriority();

     /* Name of the application */
     String getName();

     /* Application's intent when the application's reconcilation fails */
     IntentType getIntentType();

3 IntentType

     /* Don't do anything when application reports failure for reconciliation */
     DO_NOTHING

     /* Disconnect the DPN when the application reports failure for reconciliation */
     DISCONNECT


Priority:
---------
Framework will maintain the list of registered applications in a priority order. All the applications having the
same priority will be executed in parallel and once those are done. Next priority applications will be called.
Consider 2 applications, A and B. A is handling of programming groups and flows and B is handling of programming
flows which is dependent of the groups programmed by A. So, B has to register with lower priority than A.

Intent Action:
-------------
When the application fails to reconcile, what is the action that framework should take.
DO_NOTHING - continue with the next reconciliation
DISCONNECT - disconnect the switch (reconciliation will start again once the switch connects back)

Name:
-----
Name of the application who wants to register for reconcile

ReconciliationNotificationListener:
-----------------------------------
Applications who wants to register should provide a ReconciliationNotificationListener object.
     ReconciliationNotificationListener should implement api's like startReconciliation and cancelReconciliation
     startReconciliation --> applications can take action to trigger reconcilation
     cancelReconciliation --> application can take action to cancel their current reconcile tasks

CLI
---
CLI interface will be provided to get all the registered services and their status
     List of registered services
     Status of each application for respective DPN

TO BE UPDATED:


Pipeline changes
----------------
None.

Yang changes
------------
None


Configuration impact
---------------------
None

Clustering considerations
-------------------------
None

Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
None.

Scale and Performance Impact
----------------------------
None.

Targeted Release
-----------------
Nitrogen.

Alternatives
------------
N.A.

Usage
=====

Features to Install
-------------------
Will be updated

REST API
--------

CLI
---

Implementation
==============

Assignee(s)
-----------
Primary assignee:
 - Prasanna Huddar(prasanna.k.huddar@ericsson.com)
 - Arunprakash D (d.arunprakash@ericsson.com)
 - Gobinath Suganthan (gobinath@ericsson.com)

Other contributors:


Work Items
----------
N.A.

Dependencies
============
This doesn't add any new dependencies.


Testing
=======
Capture details of testing that will need to be added.

Unit Tests
----------

Integration Tests
-----------------

CSIT
----

Documentation Impact
====================
This feature will not require any change in User Guide.


References
==========
[1] https://wiki.opendaylight.org/view/OpenDaylight_OpenFlow_Plugin:Reconciliation#Future_Enhancements
