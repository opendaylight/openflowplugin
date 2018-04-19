.. contents:: Table of Contents
   :depth: 3

=========================================
Arbitrator Reconciliation using OF bundle
=========================================

`Arbitrator Reconciliation Reviews <>`__

This spec addresses following enhancement in openflowplugin module:

Addition of new reconciliation mechanism using openflow bunlde, which will allow applications to program flows and
groups during reconciliation window instead of frm reads and pushes flows and groups.

Problem description
===================
Current reconciliation mechanism exists in FRM will read config inventory and push all groups and flows via
group/flow mod messages or via bundle messages if bundle reconciliation enabled.

During replay based controller upgrade, config inventory will be empty and applications will program states based on
the configurations. During upgrade, autonomous reconciliation should be skipped and arbitrator reconciliaton should be
enabled.


Arbitrator Reconciliation
-------------------------


Use Cases
---------


Proposed change
---------------


Pipeline changes
----------------
None

Yang changes
------------


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


REST API
--------


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
* Implementation of arbitrator reconciliation using OF bundles
* FRM changes to push states via bundles during arbitrator reconciliation
* Addition of configuration flag to enable/disable arbitrator reconciliation
* Addition of configuration flag for default bundle commit timeout


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

