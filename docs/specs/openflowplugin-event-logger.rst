.. contents:: Table of Contents
      :depth: 3

========================================
Openflowplugin Event Logging Using Log4j
========================================

`Openflowplugin Event Logging Reviews <https://git.opendaylight.org/gerrit/#/c/75415/>`__

Openflowplugin event logger is the feature which is used to log some important events of openflowplugin into a separate
file using log4j.

User should configure log4j appender configuration for openflowplugin event logger in ''etc/org.ops4j.pax.logging.cfg''
file to achieve this.


Problem Description
===================
When many log events are available in karaf.log file, it will be difficult for user to quickly find the main events with
respect to openflow southbound connection. And also, as there will be huge amount of karaf logs, there are chances of
log events getting rolled out in karaf.log files. Due to which we may tend to miss some of the events related to
openflowplugin.

Openflowplugin event logger feature is intended to overcome this problem by logging important events of openflowplugin
into a separate file using log4j appender, so that user can quickly refer to these event logs to identify  important
events of openflowplugin related to connection, disconnection, reconciliation, port events, errors, failures, etc.

Use Cases
---------
1. By default openflowplugin event logging feature will not be enabled without any configuration changes in logging
   configuration file.

2. User can configure log4j appender for openflowplugin event logger(as mentioned in the configuration section) to
   log the important logs of openflowplugin in a separate file at the path mentioned in configuration file.

Proposed Change
===============
1. A log4j logger with name ''OfEventLog'' will be created and used to log the event at the time of connection,
   disconnection, reconciliation, etc.

2. By default the event logger logging level is fixed to DEBUG level. Unless there will be a appender configuration
   present in logging configuration file, the events will not be in enqueued for logging.

3. The openflowplugin event logs will be having a pattern consisting of time stamp of the event, description of event
   followed by the datapathId of the switch for which events are related.

4. The event logs will be moved to a separate file(data/events/openflow/openflow.log file as per the configuration
   mentioned in configuration section) and this can be configured to different path as per the need.

5. The file roll over strategy is chosen as to roll events into other file if the current file reaches maximum size(10MB
   as per configuration) and the event logs will be overwritten if such 10 files(as per configuration) are completed.

Command Line Interface (CLI)
============================
None.

Other Changes
=============

Pipeline changes
----------------
None.

Yang changes
------------
None.

Configuration impact
--------------------
Below log4j configuration changes should be added in ''etc/org.ops4j.pax.logging.cfg'' file for logging openflowplugin
events into a separate file.

.. code-block:: none
   :caption: org.ops4j.pax.logging.cfg

   log4j2.logger.ofp.name = OfEventLog
   log4j2.logger.ofp.level = DEBUG
   log4j2.logger.ofp.additivity = false
   log4j2.logger.ofp.appenderRef.OfEventRollingFile.ref = OfEventRollingFile

   log4j2.appender.ofp.type = RollingRandomAccessFile
   log4j2.appender.ofp.name = OfEventRollingFile
   log4j2.appender.ofp.fileName = \${karaf.data}/events/openflow/openflow.log
   log4j2.appender.ofp.filePattern = \${karaf.data}/events/openflow/openflow.log.%i
   log4j2.appender.ofp.append = true
   log4j2.appender.ofp.layout.type = PatternLayout
   log4j2.appender.ofp.layout.pattern = %d{ISO8601} | %m%n
   log4j2.appender.ofp.policies.type = Policies
   log4j2.appender.ofp.policies.size.type = SizeBasedTriggeringPolicy
   log4j2.appender.ofp.policies.size.size = 10MB
   log4j2.appender.ofp.strategy.type = DefaultRolloverStrategy
   log4j2.appender.ofp.strategy.max = 10
   log4j2.appender.ofp.strategy.fileIndex = min

Clustering considerations
-------------------------
The openflowplugin event logger will be configured in the controller and are related to log events only in that
controller. This will not be affecting cluster environment in any way.

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
Sodium.

Alternatives
------------
N.A.

Usage
=====

Features to Install
-------------------
included with common openflowplugin features.

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
 - Somashekhar Javalagi(somashekhar.manohara.javalagi@ericsson.com)

Other contributors:
 - D Arunprakash (d.arunprakash@ericsson.com)


Work Items
----------
N.A.

Dependencies
============
This doesn't add any new dependencies.


Testing
=======
1. Verifying the event logs in karaf.log file, when there is no appender configuration added in logger configuration
   file.
2. Making appender configuration in logger configuration file and verifying the important events of openflowplugin in
   the log file specified in configuration.

Unit Tests
----------
None added newly.

Integration Tests
-----------------
None

CSIT
----
None

Documentation Impact
====================

References
==========

