.. contents:: Table of Contents
   :depth: 3

==============
Southbound CLI
==============

`Southbound CLI Reviews <https://git.opendaylight.org/gerrit/#/c/63521/>`__

This spec addresses following enhancement in Openflowplugin module:

Addition of new Karaf feature `odl-openflowplugin-app-southbound-cli` under openflowplugin module that provides useful
CLIs for users. This feature won't be part of any existing openflowplugin feature and user needs to explicitly install
it in addition to the existing features.

Problem description
===================
Currently there is no way of getting the formatted list of openflow nodes connected to the OpenDaylight controller. User
has to fetch operational inventory using Restconf and search for all the connected nodes. Even to get the list of ports
available under a OpenFlow node, user need to search the entire inventory dump. From user experience perspective it's
not really very helpful, and at scale fetching the entire inventor from data store can cause CPU spike for the
controller because of the huge data present under inventory tree.

Southbound CLI
==============
New Karaf feature is developed that will provide command line interface to the user using which user can retrieving
the list of connected OpenFlow nodes and the ports available under each OpenFlow node.

Use Cases
---------
* List of all OpenFlow node(s) connected to the OpenDaylight controller in either standalone or cluster environment.
* List ports information available under a connected OpenFlow node

Proposed change
---------------
New karaf feature `odl-openflowplugin-app-southbound-cli` will be added and it will not be part of any existing
openflowplugin feature. User will have to explicitly install the feature to get the available CLIs.

Following 2 CLIs will be added:

* `openflow:getallnodes`
* `openflow:shownode`

`openflow:getallnodes` will display information like NodeId and NodeName(datapath description) for all the connected
nodes.

`openflow:shownode` will display information like NodeId, NodeName(datapath description) and Ports for a given
openflow node.

Yang changes
------------
None

Targeted Release
----------------
Oxygen

Alternatives
------------
Use RestConf to fetch entire operational inventory and parse through it.

Usage
=====
Install ``odl-openflowplugin-app-southbound-cli`` feature as it is not part of any existing openflowplugin features.

List the connected openflow nodes under odl controller either in standalone or cluster environment. In clustered
environment user need to install this feature on all the three nodes if it wants to use any node to run these CLI
commands, but user also can choose to install it on a dedicated node only if that's the master node to run CLI commands.
This feature can be install at any point of time during or after controller start.

.. code-block:: bash
   :caption: openflow:getallnodes

   opendaylight-user@root>openflow:getallnodes
   Number of nodes: 1
   NodeId              NodeName

   --------------------------------------------------------------------------
   137313212546623     None


List the available ports under openflow node.

.. code-block:: bash
   :caption: openflow:shownode

   opendaylight-user@root>openflow:shownode -d 137313212546623
   OFNode                   Name                 Ports
   ------------------------------------------------------------------------------------
   137313212546623          None                 br-int


Implementation
==============
Assignee(s)
-----------
Primary assignee:
* Arunprakash D <d.arunprakash@ericsson.com>

Contributors:
* Gobinath Suganthan <gobinath@ericsson.com>

Work Items
----------
* Implementation of cli to list the connected openflow nodes across standalone or clustered environment.
* Implementation of cli to list the ports available under openflow node.

Dependencies
============
No new dependencies.

Testing
=======
Unit Tests
----------
#. Verify CLI to list all the connected openflow nodes
#. Verify CLI to list all the ports under openflow node

CSIT
----
None

Documentation Impact
====================
None

References
==========
None
