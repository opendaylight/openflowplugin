..
 Key points to consider:
  * Use RST format. For help with syntax refer http://sphinx-doc.org/rest.html
  * Use http://rst.ninjs.org/ a web based WYSIWYG RST editor.
  * For diagrams, you can use http://asciiflow.com to make ascii diagrams.
  * MUST READ http://docs.opendaylight.org/en/latest/documentation.html and follow guidelines.
  * All sections should be retained, but can be marked None or N.A.
  * Set depth in ToC as per your doc requirements. Should be at least 2.

.. contents:: Table of Contents
      :depth: 3

===================
Title of Test Suite
===================

Brief introduction of the Test Suite and feature it is testing. This should include
links to relevant documents and specs.

.. note::

  Name of suite and test cases should map exactly to as they appear in Robot reports.


Test Setup
==========

Brief description of test setup.

Testbed Topologies
------------------

Detailed information on testbed topologies, including topology diagrams. Each
should be numbered so it can be referenced by number in Test Cases.

Default Topology
^^^^^^^^^^^^^^^^

.. literalinclude:: topologies/dummy-topology.txt


Hardware Requirements
---------------------

Any specific hardware requirements e.g. SRIOV NICs.

Software Requirements
---------------------

Any specific software and version requirements e.g. Mininet, OVS 2.8 etc. This
should also capture specific versions of OpenDaylight this suit applies to. e.g.
Nitrogen, Nitrogen-SR2 etc. This will be used to determine to which jobs this suite
can/should be added.

Test Suite Requirements
=======================

Test Suite Bringup
------------------

Initial steps before any tests are run. This should include any cleanup, sanity checks,
configuration etc. that needs to be done before test cases in suite are run. This should
also capture if this suite depends on another suite to do bringup for it.

Test Suite Cleanup
------------------

Final steps after all tests in suite are done. This should include any cleanup, sanity checks,
configuration etc. that needs to be done once all test cases in suite are done.

Debugging
---------

Capture any debugging information that is captured at start of suite and end of suite.


Test Cases
==========
This section should capture high level details about all the test cases part of the suite.
Individual test cases should be subsections in the order they should be executed.

Test Case 1
-----------
Give a brief description of the test case including topology used if multiple specified
in `Testbed Topologies`_.

Test Steps and Pass Criteria
^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Step by step procedure of what is done as part of this test.

#. Step 1

   #. Pass Criteria 1
   #. Pass Criteria 2

#. Step 2

   #. Pass Criteria 1
   #. Pass Criteria 2


Troubleshooting
^^^^^^^^^^^^^^^
Any test specific information captured. Specifically mention if it is captured always,
pass only or fail only.


Implementation
==============

Assignee(s)
-----------
Who is contributing test cases? In case of multiple authors, designate a
primary assignee and other contributors. Primary assignee is also expected to
be maintainer once test code is in.

Primary assignee:
  <developer-a>

Other contributors:
  <developer-b>
  <developer-c>


Work Items
----------
Break up work into individual items. For most cases it would be just writing tests
but in some cases will include changes to images, infra etc.


Links
-----

* Link to implementation patche(s) in CSIT


References
==========
Add any useful references. Some examples:

* Links to Summit presentation, discussion etc.
* Links to mail list discussions
* Links to patches in other projects
* Links to external documentation

[1] `OpenDaylight Documentation Guide <http://docs.opendaylight.org/en/latest/documentation.html>`__

[2] https://specs.openstack.org/openstack/nova-specs/specs/kilo/template.html

.. note::

  This template was derived from [2], and has been modified to support our project.

  This work is licensed under a Creative Commons Attribution 3.0 Unported License.
  http://creativecommons.org/licenses/by/3.0/legalcode
