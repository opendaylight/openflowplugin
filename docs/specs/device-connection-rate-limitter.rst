.. contents:: Table of Contents
      :depth: 3

==============================
Device connection rate limiter
==============================

https://git.opendaylight.org/gerrit/#/c/70157/
https://git.opendaylight.org/gerrit/#/c/70772/
https://git.opendaylight.org/gerrit/#/c/70696/

When many devices trying to connect to the controller at the same time this feature helps to reduce load on ODL controller,
by limitting the number of devices connecting in a minute.

User should set device-connection-rate-limit-per-min property of openflowplugin.cfg file before starting the controller
to limit the number of device connections.


Problem description
===================

When many devices try to connect to the ODL controller via openflowplugin at the same time, controller gets overloaded
from the processing of too many device connection requests and controller gets overwhelmed. Hence this can largely impact
the performance of the controller.

Device connection rate limiter is intended to overcome this problem by limiting the number of devices connecting to the
controller, there by reducing the load on the controller. Due to which only a few number of devices will be able to connect
to the controller in a limited period of time. The remaining devices which didn't get the permit to connect, will be
trying continuously to connect to the controller and will be succeeded in subsequent minutes when they acquire the permit
as per rate limiter logic.

Use Cases
---------
1. If user is not specifying any value to the device-connection-rate-limit-per-min property in openflowplugin.cfg file,
   by default a value of zero is considered. There by the device connection rate limiting functionality will be disabled,
   allowing any number of devices connecting to the ODL controller.

2. If the property is set to any non-zero positive value, then those many number of devices are allowed to connect to
   the controller in a minute.

Proposed change
===============
1. Device connection rate limiter service is created as part of blueprint container initialization for openflowplugin-impl
   module.

2. Rate limiter service is created using Ratelimiter entity/class of Google's concurrency framework. ConnectionManager
   will be creating rate limiter service and HandshakeManager will be holding the reference to the rate limiter service.

3. Based on the value of device-connection-rate-limit-per-min property present in openflowplugin.cfg file, the rate limiter
   value is decided. If the value is zero, then rate limiting functionality is disabled or else rate limiting functionality
   will be enabled with specified number of permits per minute.

4. At the openflow handshake phase after fetching the device features, if the rate limiter is enabled then an attempt will
   be made to acquire a connection permit for the device. If device is able to get the permit, then the handshake process
   will be continued or else the device will be rejected to connect to the ODL controller. Then a disconnection event will
   be sent to the device. The device will be succeeded to connect in subsequent retries.

5. As device-connection-rate-limit-per-min is a static property, any change in the property value will be effective only when
   controller is started with changed value.

Implementation Details:
-----------------------
Following new changes are added as part of rate limiter implementation.

1. DeviceConnectionRateLimiter class

     /* specifies whether rate limiter is enabled or disabled*/
     private final boolean doRateLimit;

     /* This method will be called by the HandshakeManager to acquire a permit while processing connection request */
     /* of each device and the return value will be true if there are enough permits or else it will be false */
     public boolean tryAquire()

2. device-connection-rate-limit-per-min property is added as a property in ConfigurationProperty and subsequent  yang changes
   are done.

doRateLimit:
-----------
After reading device-connection-rate-limit-per-min property value from OpenflowProviderConfig (which is loaded from openflowplugin.cfg
file) if the value is zero, then this boolean variable is set to false which disbales rate limiter functionality. If the value of the
property is any non zero positive integer, then this variable is set to a true value, enabling the rate limiter functionality.

Command Line Interface (CLI):
-----------------------------
None.

Pipeline changes
----------------
None.

Yang changes
------------
openflow-provider-config.yang file is modified to define the rate limiter property.

        leaf device-connection-rate-limit-per-min {
            type uint16;
            default 0;
        }

Configuration impact
--------------------
A new property with name device-connection-rate-limit-per-min is added to openflowplugin.cfg file.

Clustering considerations
-------------------------
The device connection rate limiiter service will be per controller basis even if controllers
are connected in a clustered environment.

Other Infra considerations
--------------------------
N.A.

Security considerations
-----------------------
None.

Scale and Performance Impact
----------------------------
As this feature will control the number of device connections to the ODL controller, it will improve
the performance of controller by reducing the load in connection request processing.

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
None.

CLI
---
None.

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
1. Verifying the device connection by setting device-connection-rate-limit-per-min property value to zero.
2. Verifying the rate at which the devices will be connected in case of the property is having any non-zero
   positive value with many devices trying to connect at the same time.

Unit Tests
----------
None added newly.

Integration Tests
-----------------

CSIT
----

Documentation Impact
====================


References
==========

