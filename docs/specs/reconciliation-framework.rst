.. contents:: Table of Contents
      :depth: 3

========================
Reconciliation Framework
========================

`Reconciliation Framework Reviews <https://git.opendaylight.org/gerrit/#/q/topic:bug/8902>`__

This feature aims to overcome the drawbacks of the current reconciliation implementation. As part of this enhancement,
reconciliation framework will be introduced which will coordinate the reconciliation across various applications.

Applications should register themself with reconciliation framework with a specific priority.
Application should decide the priority and the reconciliation framework will use it for executing in an priority.


Problem description
===================

When a switch connected to controller, the current ODL reconciliation implementation pushes all the
table/meters/groups/flows from the inventory configuration datastore to the switch.

When the switch is connected, all the applications including FRM(Forwarding Rules Manager) will receive the node added
DTCN(Data Tree Change Listener) and starts pushing the flows for the openflow switch. FRM reconciliation will read the
data from the config and starts pushing the flows one by one.
In the meantime, applications can react to the node added DTCN change and will start pushing
the flows through the config DS. With this, there is a high chance the application flow can be overwritten by the old
flows by FRM via reconciliation.

With framework, the problem will be avoided by doing the reconciliation for all the registered services including FRM
and then the openflow switch will be submitted to the DS. With this, applications won't receive the node added DTCN until
registered applications are done with reconciliation for the switch.

The current reconciliation mechanism lacks an ordered execution of tasks  across multiple applications resulting
in the forwarding plane not correctly reflecting the changes in the control plane.
The issue becomes more prominent in case of multi-application scenarios, resulting in errors.

Use Cases
---------
Priority based/Ordered  Coordination of Reconciliation across multiple applications.

Proposed change
===============
Reconciliation Framework will be introduced, framework will coordinate the reconciliation across applications.
The Openflow switch won't be advertised to application until Openflow switch is in KNOWN state.

``KNOWN state`` controller and switch state should be in sync(reconciliation), once the switch connects.

Application participating in reconciliation needs to register with framework.

* Application can either be FRM, FRS or any other application(s).
* Application(s) registering with Reconciliation module is encouraged since: Applications would know the right
  Flows/Groups/Meters which needs to be replayed (Add/Delete/Update). FRM/FRS(Forwarding Rules Sync) would not have
  application view of flows/group, it would blindly replay the flows/groups. Also flows having idle/hard timeout
  can be gracefully handled by application rather than FRM/FRS.

As applications register with reconciliation module

* Reconciliation module maintains the numbers of application registered in an order based on the priority.
* Applications will be executed in the priority order of higher to lower, 1 - Highest n - lowest
* Reconciliation will be triggered as per the priority, applications with same priority will be processed in parallel,
  once the higher priority application completed, next priority of applications will be processed.

Openflow switch establishes connections with openflowplugin.

* Openflow switch sends connection request.
* Openflowplugin accepts connection and than establishes the connection.

Openflowplugin after establishing the connection with openflow switch, elects the mastership and invokes reconciliation
framework through ReconciliationFrameworkEvent onDevicePrepared.

* Before invoking the reconciliation API, all the RPCs are registered with MD-SAL by openflowplugin.
* Reconciliation framework will register itself with the MastershipChangeServiceManager.

All registered applications would be indicated to start the reconciliation.
* DeviceInfo would be passed for the API/Event and it contains all the information needed by application.

Application(s) would than fetch the flows / groups for that particular Node, which needs to be replayed.

Application(s) would than replay the selected flows / group on to the switch.

Application(s) would also wait for error from switch, for pre-defined time.

Application(s) would inform the reconciliation status to reconciliation module.

Reconciliation framework would co-relate result status from all the applications and decides the final status.
If success, framework will report back DO_NOTHING and in case of failure it will be DISCONNECT.

Based on result state, openflowplugin should do the following

* On success case, openflowplugin should continue with the openflow switch --> write the switch to the operational datastore.
* On failure case, openflowplugin should disconnect the openflow switch.
* When the switch reconnects, the same steps will be followed again.

When there is a disconnect/mastership change while the reconciliation is going on, openflowplugin should notify the
framework and the framework should halt the current reconciliation.

Implementation Details
======================
Following new interface will be introduced from Reconciliation framework (RF).

* ReconciliationManager
* ReconciliationNotificationListener

ReconciliationManager
---------------------
.. code-block:: java

     /* Application who are interested in reconciliation should use this API to register themself to the RF */
     /* NotificationRegistration will be return to the registered application, who needs to take of closing the registration */
     NotificationRegistration registerService(ReconciliationNotificationListener object);

     /* API exposed by RF for get list of registered services */
     Map<Integer, List<ReconciliationNotificationListener>> getRegisteredServices();

ReconciliationNotificationListener
----------------------------------
.. code-block:: java

     /* This method will be a callback from RF to start the application reconciliation */
     ListenableFuture<Boolean> startReconciliation(DeviceInfo deviceInfo);

     /* This method will be a callback from RF when openflow switch disconnects during reconciliation */
     ListenableFuture<Boolean> endReconciliation(DeviceInfo deviceInfo);

     /* Priority of the application */
     int getPriority();

     /* Name of the application */
     String getName();

     /* Application's intent when the application's reconciliation fails */
     ResultState getResultState();

Priority
--------
Framework will maintain the list of registered applications in an order based on the priority. Applications having the
same priority will be executed in parallel and once those are done. Next priority applications will be called.
Consider 2 applications, A and B. A is handling of programming groups and flows and B is handling of programming
flows which is dependent of the groups programmed by A. So, B has to register with lower priority than A.

Application don't do any conflict resolution or guarantee any specific order among the application registered at the
same priority level.

Result State - Intent Action
----------------------------
When the application fails to reconcile, what is the action that framework should take.

* DO_NOTHING - continue with the next reconciliation
* DISCONNECT - disconnect the switch (reconciliation will start again once the switch connects back)

Name
----
Name of the application who wants to register for reconciliation

ReconciliationNotificationListener
----------------------------------
Applications who wants to register should implement ReconciliationNotificationListener interface.

* ReconciliationNotificationListener having api's like startReconciliation and endReconciliation
* startReconciliation --> applications can take action to trigger reconciliation
* endReconciliation --> application can take action to cancel their current reconcile tasks

Command Line Interface (CLI)
============================
CLI interface will be provided to get all the registered services and their status

* List of registered services
* Status of each application for respective openflow switch

Other Changes
=============

Pipeline changes
----------------
None.

Yang changes
------------
None

Configuration impact
--------------------
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
----------------
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
None

CLI
---
None

Implementation
==============
Assignee(s)
-----------
Primary assignee:
 - Prasanna Huddar <prasanna.k.huddar@ericsson.com>
 - Arunprakash D <d.arunprakash@ericsson.com>
 - Gobinath Suganthan <gobinath@ericsson.com>

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
None

Integration Tests
-----------------
None

CSIT
----
None

Documentation Impact
====================
This feature will not require any change in User Guide.

References
==========
[1] `Openflowplugin reconciliation enhancements <https://wiki-archive.opendaylight.org/view/OpenDaylight_OpenFlow_Plugin:Reconciliation#Future_Enhancements>`__
