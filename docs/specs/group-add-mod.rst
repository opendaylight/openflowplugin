.. contents:: Table of Contents
   :depth: 3

======================================
Group Command OFPGC_ADD_OR_MOD support
======================================

`Group ADD-MOD Reviews <https://git.opendaylight.org/gerrit/#/q/topic:group-add-mod>`__

This spec addresses following enhancement in Openflowplugin module:

Addition of new command OFPGC_ADD_OR_MOD for OFPT_GROUP_MOD message that adds a new group that
does not exist (like ADD) or modifies an existing groups (like MODIFY).

OFPGC_ADD_OR_MOD group command will be supported only for OVS2.6 and above.

Problem description
===================
In OpenFlow 1.x the Group Mod commands OFPGC_ADD and OFPGC_MODIFY have strict semantics:
ADD fails if the group exists, while MODIFY fails if the group does not exist. This requires
a controller to exactly know the state of the switch when programming a group in order not run
the risk of getting an OFP Error message in response. This is hard to achieve and maintain at
all times in view of possible switch and controller restarts or other connection losses between
switch and controller.

Due to the un-acknowledged nature of the Group Mod message programming groups safely and
efficiently at the same time is virtually impossible as the controller has to either query
the existence of the group prior to each Group Mod message or to insert a Barrier Request/Reply
after every group to be sure that no Error can be received at a later stage and require a
complicated roll-back of any dependent actions taken between the failed Group Mod and the Error.

Reconciliation
--------------

The current implementation of reconciliation is to read the complete set of groups from config inventory
and start pushing the groups one by one. This will always end up in GROUP_ALREADY_EXITS error as the
reconciliation will always send GROUP ADD.

This can be avoided by reading the groups from switch and compare with the list from inventory config
and push only the delta. This is an overhead comparision and can be simply avoided by updating the
group command as OFPGC_ADD_OR_MOD.

Use Cases
---------

a. Normal group provisioning via FRM: ADD/UPDATE group should send new command OFPGC_ADD_OR_MOD.

b. Reconciliation of groups should send OFPGC_ADD_OR_MOD. Current implementation of openflowplugin will
   always send group add OFPGC_ADD irrespective of the state of the switch. This results in failure with
   GROUP_ALREADY_EXISTS error.

Proposed change
---------------
The implementation of OFPGC_ADD_OR_MOD command is specific to OVS2.6 and above and the same can be extended
to other openflow switch based on the group command support by them.

New configuration parameter will be introduced in default-openflow-connection-config.xml and
legacy-openflow-connection-config.xml, which can be modified by users to enable the GROUP ADD MOD support.

.. code-block:: none
   :caption: default(legacy)-openflow-connection-config.xml

   <group-add-mod-enabled>false</group-add-mod-enabled>

By default the group-add-mod-enabled flag will be kept as false, which means existing group mod commands
OFPGC_ADD/OFPGC_MODIFY will be used.

GroupMessageSerializer will use the above flag to determine which group command should be set for group add/update.
The above class is applicable for single layer serialization and the for multi-layer serialization changes will be
done in openflowjava GroupModInputMessageFactory java classs.

When flag is enabled, openflowplugin will always send OFPGC_ADD_OR_MOD (32768) for both group add and modify.

Pipeline changes
----------------
None

Yang changes
------------

Below yang changes will be done in order to provide configuration support for group-add-mod-enabled field.

.. code-block:: none
   :caption: openflow-switch-connection-config.yang

   leaf group-add-mod-enabled {
        description "Group Add Mod Enabled";
        type boolean;
        default false;
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
Oxygen

Alternatives
------------
None

Usage
=====
No external rpc/api will be provided. The implementation is internal to openflowplugin.

User can enable OFPGC_ADD_OR_MOD by changing the value to true in below files,

.. code-block:: none
   :caption: default(legacy)-openflow-connection-config.xml

   default-openflow-connection-config.xml  <group-add-mod-enabled>false</group-add-mod-enabled>
   legacy-openflow-connection-config.xml   <group-add-mod-enabled>false</group-add-mod-enabled>

REST API
--------
No new REST API is being added.

CLI
---
No new CLI being added.

Implementation
==============
Assignee(s)
-----------
Primary assignee:
  Arunprakash D <d.arunprakash@ericsson.com>

Other contributors:
  Gobinath Suganthan <gobinath@ericsson.com>

Work Items
----------
* Implementation of GROUP ADD MOD support
* Addition of configuration flag to enable/disable group add mod command

Dependencies
============
No new dependencies.

Testing
=======
Unit Tests
----------
#. Verify group provisioning via FRM with group-add-mod-supported disabled
#. Verify group provisioning via FRM with group-add-mod-supported enabled
#. Verify reconciliation via FRM with with group-add-mod-supported disabled
#. Verify reconciliation via FRM with with group-add-mod-supported enabled

CSIT
----
CSIT test cases will be added in future

Documentation Impact
====================
None

References
==========
`Openvswitch ADD_OR_MOD <https://github.com/openvswitch/ovs/commit/88b87a36123e5ce3704b5e79950e83651db43ef7>`__
