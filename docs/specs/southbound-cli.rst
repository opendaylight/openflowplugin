.. contents:: Table of Contents
   :depth: 3

==============
Southbound CLI
==============

`Southbound CLI Reviews <https://git.opendaylight.org/gerrit/#/c/63521/>`__

This spec addresses following enhancement in Openflowplugin module:

Addition of new karaf feature `odl-openflowplugin-app-southbound-cli` under openflowplugin moduel to add user helpful
CLIs. This feature won't be part of any existing openflowpluigin feature and user needs to install addition to any
existing feature.

Problem description
===================
Currently there is formatted way of getting the list of openflow nodes connected under odl controller. User has to read
and search from operational inventory for the list of connected nodes. Even to get the list of ports available under a
openflow node, user need to search the complete inventory dump.

Southbound CLI
==============
New karaf feature and cli's will be added to help user in retrieving the list of connected openflow nodes and the ports
available under each openflow node.

Use Cases
---------
* List all openflow node(s) connected under odl controller either standalone/cluster environment.
* List ports information available under a openflow node

Proposed change
---------------
New karaf feature `odl-openflowplugin-app-southbound-cli` will be added and this will not be part of any existing
openflowplugin feature. User has to explicity install the feature to get the available CLIs.

Following 2 CLIs will be added:

* `openflow:getallnodes`
* `openflow:shownode`

`openflow:getallnodes` will read the operational inventory and display information like NodeId and
NodeName(datapath description) for all the connected nodes.

`openflow:shownode` will read the operational inventory and display information like NodeId,
NodeName(datapath description) and Ports for a given openflow node.

Yang changes
------------
None

Targeted Release
----------------
Oxygen

Alternatives
------------
Use REST to get operational inventory details.

Usage
=====
Install ``odl-openflowplugin-app-southbound-cli`` feature as it doesn't part of any existing openflowplugin features.

List the connected openflow nodes under odl controller either in standalone/cluster environment.

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
* Implementation of cli to list the connected openflow nodes across standalone/cluster environment.
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