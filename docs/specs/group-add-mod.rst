.. contents:: Table of Contents
   :depth: 3

==============================================================
Openflowplugin: Support for new group command OFPGC_ADD_OR_MOD
==============================================================

https://git.opendaylight.org/gerrit/#/q/topic:group-add-mod

This spec addresses following enhancement in Openflowplugin module:

Addition of new command OFPGC_ADD_OR_MOD for OFPT_GROUP_MOD message that adds a new group that
does not exist (like ADD) or modifies an existing groups like (MODIFY).

OFPGC_ADD_OR_MOD support exits only for OVS2.6 and above.

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

The current implementation of reconciliation is to read the complete set of groups from config inventory
and start pushing the groups one by one. The propability of getting GROUP_ALREADY_EXITS error is high as
the reconciliation will always send GROUP ADD.

This can be avoided by reading the groups from switch and compare with the list from inventory config
and push only the delta. This is an overhead comparision and can be simply avoided by updating the
group command as OFPGC_ADD_OR_MOD.

Use Cases
---------

a. Normal group provisioning via FRM: this might end up in error state if there is a difference
   between openflow switch state and the state in inventory config.

b. Reconciliation of groups will throw error as the openflowplugin will always send group add
   OFPGC_ADD irrespective of the state of the switch.

Proposed change
---------------
The implementation of OFPGC_ADD_OR_MOD command is specific to OVS2.6 and above for now and the same can
be widened to other openflow switch based on the support by them.

New configuration parameter will be introduced in openflowplugin.cfg file, which can be modified by users
to enable the GROUP ADD MOD support.

Openflowplugin.cfg:
# GROUP ADD MOD Support for OVS2.6 and above
# group-add-mod-supported=false

By default the group-add-mod-supported flag will be kept as false, which means existing group commands
OFPGC_ADD/OFPGC_MODIFY will be used.

FRM will use the above flag to determine which group command should be sent out for add/update group.

Pipeline changes
----------------
None

Yang changes
------------

Below yang changes is required to add new command support under typedef group-mod-command.
New command OFPGC_ADD_OR_MOD will be added with value as 32768.

openflow-protocol-api/src/main/yang/openflow-types.yang
-------------------------------------------------------

    typedef group-mod-command {
        /* ofp_group_mod_command */
        type enumeration {
            enum OFPGC_ADD {
              value 0;
              description "New group.";
            }
            enum OFPGC_MODIFY {
              value 1;
              description "Modify all matching groups.";
            }
            enum OFPGC_DELETE {
              value 2;
              description "Delete all matching groups.";
            }
            enum OFPGC_ADD_OR_MOD {
              /* Hexa value for OFPGC_ADD_OR_MOD = 0x8000 */
              value 32768;
              description "Create new or modify existing group.";
            }
        }
    }

Below yang model changes is required to provide a rpc fro add/update group.

model/model-flow-service/src/main/yang/sal-group.yang
-----------------------------------------------------

    rpc add-update-group {
        description "adding/Updating group on openflow device";
        input {
            uses tr:transaction-metadata;
            leaf group-ref {
                type group-type:group-ref;
            }
            uses node-group;
            }
         output {
            uses tr:transaction-aware;
        }
    }

Configuration impact
---------------------
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
-----------------
Oxygen

Alternatives
------------
None

Usage
=====
None

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
  D Arunprakash <d.arunprakash@ericsson.com>

Other contributors:
  Gobinath Suganthan <gobinath@ericsson.com

Work Items
----------
* Implemention of GROUP ADD MOD support
* Addition of configuration flat to enable/disable group add mod command

Dependencies
============
No new dependencies.

Testing
=======
Unit Tests
----------

#. Verify usual group provisioning via FRM
#. Verify reconciliation via FRM with the new group command

CSIT
----
CSIT test cases will be added in future

Documentation Impact
====================
None

References
==========
https://github.com/openvswitch/ovs/commit/88b87a36123e5ce3704b5e79950e83651db43ef7
