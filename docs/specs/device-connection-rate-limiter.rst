.. contents:: Table of Contents
      :depth: 3

==============================
Device Connection Rate Limiter
==============================

`Device Connection Rate Limiter Reviews <https://git.opendaylight.org/gerrit/#/c/70157>`__

When many openflow devices try to connect at the same time, this feature helps to reduce load on ODL controller, by
limiting the number of devices connecting per minute.

Before starting the contoller, user should uncomment and configure ``device-connection-rate-limit-per-min`` property
value in ``openflowplugin.cfg`` file for limiting the number of device connections.


Problem Description
===================
When many openflow devices try to connect to the ODL controller via openflowplugin at the same time, controller gets
overloaded by processing too many device connection requests, port information, switch feature information and supported
statistics. Due to which controller gets overwhelmed, that can result in device disconnection and message drops. Hence
this can largely impact the performance of the controller.

Device connection rate limiter is intended to overcome this problem by limiting the number of openflow devices
connecting to the ODL controller, there by reducing the load on the controller. Due to which only configured number of
devices will be able to connect to the ODL controller per minute. The remaining devices which are not able to get the
permit, will be disconnected. The disconnected devices will keep on trying to connect and will be succeeded in
subsequent retries, when they acquire the permit as per rate limiter logic.

Use Cases
---------
1. By default device connection rate limiter feature will be disabled. So there will be no effect on the rate at which
   openflow devices connect to the ODL controller.

2. The property can be uncommented and set to any non-zero positive value in openflowplugin.cfg file, then those many
   number of openflow devices are allowed to connect to the ODL controller in a minute.

Proposed Change
===============
1. Device connection rate limiter service is created as part of blueprint container initialization for
   openflowplugin-impl module.

2. Rate limiter service is created using Ratelimiter entity/class of Google's concurrency framework. ConnectionManager
   will be creating rate limiter service and HandshakeManager will be holding the reference to the rate limiter service.

3. Based on the value of device-connection-rate-limit-per-min property present in openflowplugin.cfg file, the rate
   limiter value is decided. If the value is zero, then the rate limiting functionality will be disabled or else the
   functionality will be enabled by allowing specified number of permits per minute.

4. At the openflow handshake phase after fetching the device features, if the rate limiter is enabled then an attempt
   will be made to acquire a connection permit for the openflow device. If device is able to get the permit, then the
   handshake process will be continued or else the device will be rejected to connect to the ODL controller. Then a
   disconnection event will be sent to the openflow device. The device will be succeeded to connect in subsequent
   retries.

5. As device-connection-rate-limit-per-min is a static property, any change in the property value will be effective only
   when the ODL controller is started with changed value.

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
openflow-provider-config.yang file is modified to define the rate limiter property.

.. code-block:: none
   :caption: openflow-provider-config.yang

   leaf device-connection-rate-limit-per-min {
        type uint16;
        default 0;
   }

Configuration impact
--------------------
New property ``device-connection-rate-limit-per-min`` added to openflowplugin.cfg file.

.. code-block:: none
   :caption: openflowplugin.cfg

   # To limit the number of datapath nodes to be connected to the controller instance
   # per minute. When the default value of zero is set, then the device connection rate
   # limiter will be disabled. If it is set to any value, then only those many
   # number of datapath nodes are allowed to connect to the controller in a minute
   #
   # device-connection-rate-limit-per-min=0

Clustering considerations
-------------------------
The device connection rate limiter service will be per controller basis even if controllers are connected in a clustered
environment.

Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
None.

Scale and Performance Impact
----------------------------
As this feature will control the rate at which the openflow devices connect to the ODL controller, it will improve the
performance of controller by reducing the load in connection request processing during controller/cluster reboot.

Targeted Release
----------------
Fluorine.

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
 - Gobinath Suganthan (gobinath@ericsson.com)


Work Items
----------
N.A.

Dependencies
============
This doesn't add any new dependencies.


Testing
=======
1. Verifying the number of openflow device connections to the ODL controller without doing any modification to the
   openflowplugin.cfg file.
2. Verifying the rate at which the openflow devices connecting to the ODL controller in case if the property is having
   any non-zero positive value, with many devices trying to connect at the same time.

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

