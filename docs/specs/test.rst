.. contents:: Table of Contents
      :depth: 3

=========================
Reconciliation Framework
=========================

https://git.opendaylight.org/gerrit/#/q/topic:bug/8902

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