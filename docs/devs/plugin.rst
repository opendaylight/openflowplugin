OpenFlow Plugin Project Developer Guide
=======================================

This section covers topics which are developer specific and which have
not been covered in the user guide. Please see the OpenFlow
plugin user guide first.

It can be found on `the OpenDaylight software download
page <https://www.opendaylight.org/downloads>`__.

Event Sequences
---------------

Session Establishment
~~~~~~~~~~~~~~~~~~~~~

The OpenFlow Protocol
Library provides
interface **SwitchConnectionHandler** which contains method
*onSwitchConnected* (step 1). This event is raised in the OpenFlow
Protocol Library when an OpenFlow device connects to OpenDaylight and
caught in the **ConnectionManagerImpl** class in the OpenFlow plugin.

There the plugin creates a new instance of the **ConnectionContextImpl**
class (step 1.1) and also instances of **HandshakeManagerImpl** (which
uses **HandshakeListenerImpl**) and **ConnectionReadyListenerImpl**.
**ConnectionReadyListenerImpl** contains method *onConnectionReady()*
which is called when connection is prepared. This method starts the
handshake with the OpenFlow device (switch) from the OpenFlow plugin
side. Then handshake can be also started from device side. In this case
method *shake()* from **HandshakeManagerImpl** is called (steps 1.1.1
and 2).

The handshake consists of an exchange of HELLO messages in addition to
an exchange of device features (steps 2.1. and 3). The handshake is
completed by **HandshakeManagerImpl**. After receiving device features,
the **HandshakeListenerImpl** is notifed via the
*onHanshakeSuccessfull()* method. After this, the device features, node
id and connection state are stored in a **ConnectionContext** and the
method *deviceConnected()* of **DeviceManagerImpl** is called.

When *deviceConnected()* is called, it does the following:

1. creates a new transaction chain (step 4.1)

2. creates a new instance of **DeviceContext** (step 4.2.2)

3. initializes the device context: the static context of device is
   populated by calling *createDeviceFeaturesForOF<version>()* to
   populate table, group, meter features and port descriptions (step
   4.2.1 and 4.2.1.1)

4. creates an instance of **RequestContext** for each type of feature

When the OpenFlow device responds to these requests (step 4.2.1.1) with
multipart replies (step 5) they are processed and stored to MD-SAL
operational datastore. The *createDeviceFeaturesForOF<version>()* method
returns a **Future** which is processed in the callback (step 5.1) (part
of *initializeDeviceContext()* in the *deviceConnected()* method) by
calling the method *onDeviceCtxLevelUp()* from **StatisticsManager**
(step 5.1.1).

The call to *createDeviceFeaturesForOF<version>()*: . creates a new
instance of **StatisticsContextImpl** (step 5.1.1.1).

1. calls *gatherDynamicStatistics()* on that instance which returns a
   **Future** which will produce a value when done

   a. this method calls methods to get dynamic data (flows, tables,
      groups) from the device (step 5.1.1.2, 5.1.1.2.1, 5.1.1.2.1.1)

   b. if everything works, this data is also stored in the MD-SAL
      operational datastore

If the **Future** is successful, it is processed (step 6.1.1) in a
callback in **StatisticsManagerImpl** which:

1. schedules the next time to poll the device for statistics

2. sets the device state to synchronized (step 6.1.1.2)

3. calls *onDeviceContextLevelUp()* in **RpcManagerImpl**

The *onDeviceContextLevelUp()* call:

1. creates a new instance of **RequestContextImpl**

2. registers implementation for supported services

3. calls *onDeviceContextLevelUp()* in **DeviceManagerImpl** (step
   6.1.1.2.1.2) which causes the information about the new device be be
   written to the MD-SAL operational datastore (step 6.1.1.2.2)

.. figure:: ./images/openflowplugin/odl-ofp-session-establishment.jpg
   :alt: Session establishment

   Session establishment

Handshake
~~~~~~~~~

The first thing that happens when an OpenFlow device connects to
OpenDaylight is that the OpenFlow plugin gathers basic information about
the device and establishes agreement on key facts like the version of
OpenFlow which will be used. This process is called the handshake.

The handshake starts with HELLO message which can be sent either by the
OpenFlow device or the OpenFlow plugin. After this, there are several
scenarios which can happen:

1. if the first HELLO message contains a *version bitmap*, it is
   possible to determine if there is a common version of OpenFlow or
   not:

   a. if there is a single common version use it and the **VERSION IS
      SETTLED**

   b. if there are more than one common versions, use the highest
      (newest) protocol and the **VERSION IS SETTLED**

   c. if there are no common versions, the device is **DISCONNECTED**

2. if the first HELLO message does not contain a *version bitmap*, then
   STEB-BY-STEP negotiation is used

3. if second (or more) HELLO message is received, then STEP-BY-STEP
   negotiation is used

STEP-BY-STEP negotiation:
^^^^^^^^^^^^^^^^^^^^^^^^^

-  if last version proposed by the OpenFlow plugin is the same as the
   version received from the OpenFlow device, then the **VERSION IS
   SETTLED**

-  if the version received in the current HELLO message from the device
   is the same as from previous then negotiation has failed and the
   device is **DISCONNECTED**

-  if the last version from the device is greater than the last version
   proposed from the plugin, wait for the next HELLO message in the hope
   that it will advertise support for a lower version

-  if the last version from the device is is less than the last version
   proposed from the plugin:

   -  propose the highest version the plugin supports that is less than
      or equal to the version received from the device and wait for the
      next HELLO message

   -  if if the plugin doesn’t support a lower version, the device is
      **DISCONNECTED**

After selecting of version we can say that the **VERSION IS SETTLED**
and the OpenFlow plugin can ask device for its features. At this point
handshake ends.

.. figure:: ./images/openflowplugin/odl-ofp-handshake.png
   :alt: Handshake process

   Handshake process

Adding a Flow
~~~~~~~~~~~~~

There are two ways to add a flow in in the OpenFlow plugin: adding it to
the MD-SAL config datastore or calling an RPC. Both of these can either
be done using the native MD-SAL interfaces or using RESTCONF. This
discussion focuses on calling the RPC.

If user send flow via REST interface (step 1) it will cause that
*invokeRpc()* is called on **RpcBroker**. The **RpcBroker** then looks
for an appropriate implementation of the interface. In the case of the
OpenFlow plugin, this is the *addFlow()* method of
**SalFlowServiceImpl** (step 1.1). The same thing happens if the RPC is
called directly from the native MD-SAL interfaces.

The *addFlow()* method then

1. calls the *commitEntry()* method (step 2) from the OpenFlow Protocol
   Library which is responsible for sending the flow to the device

2. creates a new **RequestContext** by calling *createRequestContext()*
   (step 3)

3. creates a callback to handle any events that happen because of
   sending the flow to the device

The callback method is triggered when a barrier reply message (step 2.1)
is received from the device indicating that the flow was either
installed or an appropriate error message was sent. If the flow was
successfully sent to the device, the RPC result is set to success (step
5). // **SalFlowService** contains inside method *addFlow()* other
callback which caught notification from callback for barrier message.

At this point, no information pertaining to the flow has been added to
the MD-SAL operational datastore. That is accomplished by the periodic
gathering of statistics from OpenFlow devices.

The **StatisticsContext** for each given OpenFlow device periodically
polls it using *gatherStatistics()* of **StatisticsGatheringUtil** which
issues an OpenFlow OFPT\_MULTIPART\_REQUEST - OFPMP\_FLOW. The response
to this request (step 7) is processed in **StatisticsGatheringUtil**
class where flow data is written to the MD-SAL operational datastore via
the *writeToTransaction()* method of **DeviceContext**.

.. figure:: ./images/openflowplugin/odl-ofp-add-flow.png
   :alt: Add flow

   Add flow

Description of OpenFlow Plugin Modules
--------------------------------------

The OpenFlow plugin project contains a variety of OpenDaylight modules,
which are loaded using the configuration subsystem. This section
describes the YANG files used to model each module.

**General model (interfaces)** - openflow-plugin-cfg.yang.

-  the provided module is defined (``identity openflow-provider``)

-  and target implementation is assigned (``...OpenflowPluginProvider``)

.. code::

    module openflow-provider {
       yang-version 1;
       namespace "urn:opendaylight:params:xml:ns:yang:openflow:common:config[urn:opendaylight:params:xml:ns:yang:openflow:common:config]";
       prefix "ofplugin-cfg";

       import config {prefix config; revision-date 2013-04-05; }
       description
           "openflow-plugin-custom-config";
       revision "2014-03-26" {
           description
               "Initial revision";
       }
       identity openflow-provider{
           base config:service-type;
           config:java-class "org.opendaylight.openflowplugin.openflow.md.core.sal.OpenflowPluginProvider";
       }
    }

**Implementation model** - openflow-plugin-cfg-impl.yang

-  the implementation of module is defined
   (``identity openflow-provider-impl``)

   -  class name of generated implementation is defined
      (ConfigurableOpenFlowProvider)

-  via augmentation the configuration of module is defined:

   -  this module requires instance of binding-aware-broker
      (``container binding-aware-broker``)

   -  and list of openflow-switch-connection-provider (those are
      provided by openflowjava, one plugin instance will orchestrate
      multiple openflowjava modules)

.. code::

    module openflow-provider-impl {
       yang-version 1;
       namespace "urn:opendaylight:params:xml:ns:yang:openflow:common:config:impl[urn:opendaylight:params:xml:ns:yang:openflow:common:config:impl]";
       prefix "ofplugin-cfg-impl";

       import config {prefix config; revision-date 2013-04-05;}
       import openflow-provider {prefix openflow-provider;}
       import openflow-switch-connection-provider {prefix openflow-switch-connection-provider;revision-date 2014-03-28;}
       import opendaylight-md-sal-binding { prefix md-sal-binding; revision-date 2013-10-28;}


       description
           "openflow-plugin-custom-config-impl";

       revision "2014-03-26" {
           description
               "Initial revision";
       }

       identity openflow-provider-impl {
           base config:module-type;
           config:provided-service openflow-provider:openflow-provider;
           config:java-name-prefix ConfigurableOpenFlowProvider;
       }

       augment "/config:modules/config:module/config:configuration" {
           case openflow-provider-impl {
               when "/config:modules/config:module/config:type = 'openflow-provider-impl'";

               container binding-aware-broker {
                   uses config:service-ref {
                       refine type {
                           mandatory true;
                           config:required-identity md-sal-binding:binding-broker-osgi-registry;
                       }
                   }
               }
               list openflow-switch-connection-provider {
                   uses config:service-ref {
                       refine type {
                           mandatory true;
                           config:required-identity openflow-switch-connection-provider:openflow-switch-connection-provider;
                       }
                   }
               }
           }
       }
    }

Generating config and sal classes out of yangs
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In order to involve suitable code generators, this is needed in pom:

.. code:: xml

    <build> ...
      <plugins>
        <plugin>
          <groupId>org.opendaylight.yangtools</groupId>
          <artifactId>yang-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>generate-sources</goal>
              </goals>
              <configuration>
                <codeGenerators>
                  <generator>
                    <codeGeneratorClass>
                      org.opendaylight.controller.config.yangjmxgenerator.plugin.JMXGenerator
                    </codeGeneratorClass>
                    <outputBaseDir>${project.build.directory}/generated-sources/config</outputBaseDir>
                    <additionalConfiguration>
                      <namespaceToPackage1>
                        urn:opendaylight:params:xml:ns:yang:controller==org.opendaylight.controller.config.yang
                      </namespaceToPackage1>
                    </additionalConfiguration>
                  </generator>
                  <generator>
                    <codeGeneratorClass>
                      org.opendaylight.yangtools.maven.sal.api.gen.plugin.CodeGeneratorImpl
                    </codeGeneratorClass>
                    <outputBaseDir>${project.build.directory}/generated-sources/sal</outputBaseDir>
                  </generator>
                  <generator>
                    <codeGeneratorClass>org.opendaylight.yangtools.yang.unified.doc.generator.maven.DocumentationGeneratorImpl</codeGeneratorClass>
                    <outputBaseDir>${project.build.directory}/site/models</outputBaseDir>
                  </generator>
                </codeGenerators>
                <inspectDependencies>true</inspectDependencies>
              </configuration>
            </execution>
          </executions>
          <dependencies>
            <dependency>
              <groupId>org.opendaylight.controller</groupId>
              <artifactId>yang-jmx-generator-plugin</artifactId>
              <version>0.2.5-SNAPSHOT</version>
            </dependency>
            <dependency>
              <groupId>org.opendaylight.yangtools</groupId>
              <artifactId>maven-sal-api-gen-plugin</artifactId>
              <version>${yangtools.version}</version>
              <type>jar</type>
            </dependency>
          </dependencies>
        </plugin>
        ...

-  JMX generator (target/generated-sources/config)

-  sal CodeGeneratorImpl (target/generated-sources/sal)

Altering generated files
~~~~~~~~~~~~~~~~~~~~~~~~

Those files were generated under src/main/java in package as referred in
yangs (if exist, generator will not overwrite them):

-  ConfigurableOpenFlowProviderModuleFactory

       here the **instantiateModule** methods are extended in order to
       capture and inject osgi BundleContext into module, so it can be
       injected into final implementation - **OpenflowPluginProvider** +
       ``module.setBundleContext(bundleContext);``

-  ConfigurableOpenFlowProviderModule

       here the **createInstance** method is extended in order to inject
       osgi BundleContext into module implementation +
       ``pluginProvider.setContext(bundleContext);``

Configuration xml file
~~~~~~~~~~~~~~~~~~~~~~

Configuration file contains

-  required capabilities

   -  modules definitions from openflowjava

   -  modules definitions from openflowplugin

-  modules definition

   -  openflow:switch:connection:provider:impl (listening on port 6633,
      name=openflow-switch-connection-provider-legacy-impl)

   -  openflow:switch:connection:provider:impl (listening on port 6653,
      name=openflow-switch-connection-provider-default-impl)

   -  openflow:common:config:impl (having 2 services (wrapping those 2
      previous modules) and binding-broker-osgi-registry injected)

-  provided services

   -  openflow-switch-connection-provider-default

   -  openflow-switch-connection-provider-legacy

   -  openflow-provider

.. code:: xml

    <snapshot>
     <required-capabilities>
       <capability>urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider:impl?module=openflow-switch-connection-provider-impl&revision=2014-03-28</capability>
       <capability>urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider?module=openflow-switch-connection-provider&revision=2014-03-28</capability>
       <capability>urn:opendaylight:params:xml:ns:yang:openflow:common:config:impl?module=openflow-provider-impl&revision=2014-03-26</capability>
       <capability>urn:opendaylight:params:xml:ns:yang:openflow:common:config?module=openflow-provider&revision=2014-03-26</capability>
     </required-capabilities>

     <configuration>


         <modules xmlns="urn:opendaylight:params:xml:ns:yang:controller:config">
           <module>
             <type xmlns:prefix="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider:impl">prefix:openflow-switch-connection-provider-impl</type>
             <name>openflow-switch-connection-provider-default-impl</name>
             <port>6633</port>
             <switch-idle-timeout>15000</switch-idle-timeout>
           </module>
           <module>
             <type xmlns:prefix="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider:impl">prefix:openflow-switch-connection-provider-impl</type>
             <name>openflow-switch-connection-provider-legacy-impl</name>
             <port>6653</port>
             <switch-idle-timeout>15000</switch-idle-timeout>
           </module>


           <module>
             <type xmlns:prefix="urn:opendaylight:params:xml:ns:yang:openflow:common:config:impl">prefix:openflow-provider-impl</type>
             <name>openflow-provider-impl</name>

             <openflow-switch-connection-provider>
               <type xmlns:ofSwitch="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider">ofSwitch:openflow-switch-connection-provider</type>
               <name>openflow-switch-connection-provider-default</name>
             </openflow-switch-connection-provider>
             <openflow-switch-connection-provider>
               <type xmlns:ofSwitch="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider">ofSwitch:openflow-switch-connection-provider</type>
               <name>openflow-switch-connection-provider-legacy</name>
             </openflow-switch-connection-provider>


             <binding-aware-broker>
               <type xmlns:binding="urn:opendaylight:params:xml:ns:yang:controller:md:sal:binding">binding:binding-broker-osgi-registry</type>
               <name>binding-osgi-broker</name>
             </binding-aware-broker>
           </module>
         </modules>

         <services xmlns="urn:opendaylight:params:xml:ns:yang:controller:config">
           <service>
             <type xmlns:prefix="urn:opendaylight:params:xml:ns:yang:openflow:switch:connection:provider">prefix:openflow-switch-connection-provider</type>
             <instance>
               <name>openflow-switch-connection-provider-default</name>
               <provider>/modules/module[type='openflow-switch-connection-provider-impl'][name='openflow-switch-connection-provider-default-impl']</provider>
             </instance>
             <instance>
               <name>openflow-switch-connection-provider-legacy</name>
               <provider>/modules/module[type='openflow-switch-connection-provider-impl'][name='openflow-switch-connection-provider-legacy-impl']</provider>
             </instance>
           </service>

           <service>
             <type xmlns:prefix="urn:opendaylight:params:xml:ns:yang:openflow:common:config">prefix:openflow-provider</type>
             <instance>
               <name>openflow-provider</name>
               <provider>/modules/module[type='openflow-provider-impl'][name='openflow-provider-impl']</provider>
             </instance>
           </service>
         </services>


     </configuration>
    </snapshot>

API changes
~~~~~~~~~~~

In order to provide multiple instances of modules from openflowjava
there is an API change. Previously OFPlugin got access to
SwitchConnectionProvider exposed by OFJava and injected collection of
configurations so that for each configuration new instance of tcp
listening server was created. Now those configurations are provided by
configSubsystem and configured modules (wrapping the original
SwitchConnectionProvider) are injected into OFPlugin (wrapping
SwitchConnectionHandler).

Providing config file (IT, local distribution/base, integration/distributions/base)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

openflowplugin-it
^^^^^^^^^^^^^^^^^

Here the whole configuration is contained in one file (controller.xml).
Required entries needed in order to startup and wire OEPlugin + OFJava
are simply added there.

OFPlugin/distribution/base
^^^^^^^^^^^^^^^^^^^^^^^^^^

Here new config file has been added
(src/main/resources/configuration/initial/42-openflow-protocol-impl.xml)
and is being copied to config/initial subfolder of build.

integration/distributions/build
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In order to push the actual config into config/initial subfolder of
distributions/base in integration project there was a new artifact in
OFPlugin created - **openflowplugin-controller-config**, containing only
the config xml file under src/main/resources. Another change was
committed into integration project. During build this config xml is
being extracted and copied to the final folder in order to be accessible
during controller run.

Internal message statistics API
-------------------------------

To aid in testing and diagnosis, the OpenFlow plugin provides
information about the number and rate of different internal events.

The implementation does two things: collects event counts and exposes
counts. Event counts are grouped by message type, e.g.,
**PacketInMessage**, and checkpoint, e.g.,
*TO\_SWITCH\_ENQUEUED\_SUCCESS*. Once gathered, the results are logged
as well as being exposed using OSGi command line (deprecated) and JMX.

Collect
~~~~~~~

Each message is counted as it passes through various processing
checkpoints. The following checkpoints are defined as a Java enum and
tracked:

.. code:: java

      /**
        * statistic groups overall in OFPlugin
        */
      enum STATISTIC_GROUP {
           /** message from switch, enqueued for processing */
           FROM_SWITCH_ENQUEUED,
           /** message from switch translated successfully - source */
           FROM_SWITCH_TRANSLATE_IN_SUCCESS,
           /** message from switch translated successfully - target */
           FROM_SWITCH_TRANSLATE_OUT_SUCCESS,
           /** message from switch where translation failed - source */
           FROM_SWITCH_TRANSLATE_SRC_FAILURE,
           /** message from switch finally published into MD-SAL */
           FROM_SWITCH_PUBLISHED_SUCCESS,
           /** message from switch - publishing into MD-SAL failed */
           FROM_SWITCH_PUBLISHED_FAILURE,

           /** message from MD-SAL to switch via RPC enqueued */
           TO_SWITCH_ENQUEUED_SUCCESS,
           /** message from MD-SAL to switch via RPC NOT enqueued */
           TO_SWITCH_ENQUEUED_FAILED,
           /** message from MD-SAL to switch - sent to OFJava successfully */
           TO_SWITCH_SUBMITTED_SUCCESS,
           /** message from MD-SAL to switch - sent to OFJava but failed*/
           TO_SWITCH_SUBMITTED_FAILURE
      }

When a message passes through any of those checkpoints then counter
assigned to corresponding checkpoint and message is incremented by 1.

Expose statistics
~~~~~~~~~~~~~~~~~

As described above, there are three ways to access the statistics:

-  OSGi command line (this is considered deprecated)

       ``osgi> dumpMsgCount``

-  OpenDaylight logging console (statistics are logged here every 10
   seconds)

       required logback settings :
       ``<logger name="org.opendaylight.openflowplugin.openflow.md.queue.MessageSpyCounterImpl" level="DEBUG"\/>``

-  JMX (via JConsole)

       start OpenFlow plugin with the ``-jmx`` parameter

       start JConsole by running ``jconsole``

       the JConsole MBeans tab should contain
       org.opendaylight.controller

       RuntimeBean has a msg-spy-service-impl

       Operations provides makeMsgStatistics report functionality

Example results
^^^^^^^^^^^^^^^

.. figure:: ./images/openflowplugin/odl-ofp-ofplugin-debug-stats.png
   :alt: OFplugin Debug stats.png

   OFplugin Debug stats.png

::

    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_ENQUEUED: MSG[PortStatusMessage] -> +0 | 1
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_ENQUEUED: MSG[MultipartReplyMessage] -> +24 | 81
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_ENQUEUED: MSG[PacketInMessage] -> +8 | 111
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_IN_SUCCESS: MSG[PortStatusMessage] -> +0 | 1
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_IN_SUCCESS: MSG[MultipartReplyMessage] -> +24 | 81
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_IN_SUCCESS: MSG[PacketInMessage] -> +8 | 111
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[QueueStatisticsUpdate] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[NodeUpdated] -> +0 | 3
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[NodeConnectorStatisticsUpdate] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[GroupDescStatsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[FlowsStatisticsUpdate] -> +3 | 19
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[PacketReceived] -> +8 | 111
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[MeterFeaturesUpdated] -> +0 | 3
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[GroupStatisticsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[GroupFeaturesUpdated] -> +0 | 3
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[MeterConfigStatsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[MeterStatisticsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[NodeConnectorUpdated] -> +0 | 12
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_OUT_SUCCESS: MSG[FlowTableStatisticsUpdate] -> +3 | 8
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_TRANSLATE_SRC_FAILURE: no activity detected
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[QueueStatisticsUpdate] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[NodeUpdated] -> +0 | 3
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[NodeConnectorStatisticsUpdate] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[GroupDescStatsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[FlowsStatisticsUpdate] -> +3 | 19
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[PacketReceived] -> +8 | 111
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[MeterFeaturesUpdated] -> +0 | 3
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[GroupStatisticsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[GroupFeaturesUpdated] -> +0 | 3
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[MeterConfigStatsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[MeterStatisticsUpdated] -> +3 | 7
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[NodeConnectorUpdated] -> +0 | 12
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_SUCCESS: MSG[FlowTableStatisticsUpdate] -> +3 | 8
    DEBUG o.o.o.s.MessageSpyCounterImpl - FROM_SWITCH_PUBLISHED_FAILURE: no activity detected
    DEBUG o.o.o.s.MessageSpyCounterImpl - TO_SWITCH_ENQUEUED_SUCCESS: MSG[AddFlowInput] -> +0 | 12
    DEBUG o.o.o.s.MessageSpyCounterImpl - TO_SWITCH_ENQUEUED_FAILED: no activity detected
    DEBUG o.o.o.s.MessageSpyCounterImpl - TO_SWITCH_SUBMITTED_SUCCESS: MSG[AddFlowInput] -> +0 | 12
    DEBUG o.o.o.s.MessageSpyCounterImpl - TO_SWITCH_SUBMITTED_FAILURE: no activity detected

Application: Forwarding Rules Synchronizer
------------------------------------------

Basics
~~~~~~

Description
^^^^^^^^^^^

Forwarding Rules Synchronizer (FRS) is a newer version of Forwarding
Rules Manager (FRM). It was created to solve most shortcomings of FRM.
FRS solving errors with retry mechanism. Sending barrier if needed.
Using one service for flows, groups and meters. And it has less changes
requests send to device since calculating difference and using
compression queue.

It is located in the Java package:

.. code:: java

    package org.opendaylight.openflowplugin.applications.frsync;

Listeners
^^^^^^^^^

-  1x config - FlowCapableNode

-  1x operational - Node

System of work
^^^^^^^^^^^^^^

-  one listener in config datastore waiting for changes

   -  update cache

   -  skip event if operational not present for node

   -  send syncup entry to reactor for synchronization

      -  node added: after part of modification and whole operational
         snapshot

      -  node updated: after and before part of modification

      -  node deleted: null and before part of modification

-  one listener in operational datastore waiting for changes

   -  update cache

   -  on device connected

      -  register for cluster services

   -  on device disconnected remove from cache

      -  remove from cache

      -  unregister for cluster services

   -  if registered for reconciliation

      -  do reconciliation through syncup (only when config present)

-  reactor *(provides syncup w/decorators assembled in this order)*

   -  Cluster decorator - skip action if not master for device

   -  FutureZip decorator (FutureZip extends Future decorator)

      -  Future - run delegate syncup in future - submit task to
         executor service

      -  FutureZip - provides state compression - compress optimized
         config delta if waiting for execution with new one

   -  Guard decorator - per device level locking

   -  Retry decorator - register for reconciliation if syncup failed

   -  Reactor impl - calculate diff from after/before parts of syncup
      entry and execute

Strategy
^^^^^^^^

In the *old* FRM uses an incremental strategy with all changes made one
by one, where FRS uses a flat batch system with changes made in bulk. It
uses one service SalFlatBatchService instead of three (flow, group,
meter).

Boron release
^^^^^^^^^^^^^

FRS is used in Boron as separate feature and it is not loaded by any
other feature. It has to be run separately.

::

    odl-openflowplugin-app-forwardingrules-sync

FRS additions
~~~~~~~~~~~~~

Retry mechanism
^^^^^^^^^^^^^^^

-  is started when change request to device return as failed (register
   for reconcile)

-  wait for next consistent operational and do reconciliation with
   actual config (not only diff)

ZipQueue
^^^^^^^^

-  only the diff (before/after) between last config changes is sent to
   device

-  when there are more config changes for device in a row waiting to be
   processed they are compressed into one entry (after is still replaced
   with the latest)

Cluster-aware
^^^^^^^^^^^^^

-  FRS is cluster aware using ClusteringSingletonServiceProvider from
   the MD-SAL

-  on mastership change reconciliation is done (register for reconcile)

SalFlatBatchService
^^^^^^^^^^^^^^^^^^^

FRS uses service with implemented barrier waiting logic between
dependent objects

Service: SalFlatBatchService
----------------------------

Basics
~~~~~~

SalFlatBatchService was created along forwardingrules-sync application
as the service that should application used by default. This service uses
only one input with bag of flow/group/meter objects and their common
add/update/remove action. So you practically send only one input (of specific
bags) to this service.

-  interface: *org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.SalFlatBatchService*

-  implementation: *org.opendaylight.openflowplugin.impl.services.SalFlatBatchServiceImpl*

-  method: *processFlatBatch(input)*

-  input: *org.opendaylight.yang.gen.v1.urn.opendaylight.flat.batch.service.rev160321.ProcessFlatBatchInput*

Usage benefits
^^^^^^^^^^^^^^

-  possibility to use only one input bag with particular failure analysis preserved

-  automatic barrier decision (chain+wait)

-  less RPC routing in cluster environment (since one call encapsulates all others)

ProcessFlatBatchInput
~~~~~~~~~~~~~~~~~~~~~

Input for SalFlatBatchService (ProcessFlatBatchInput object) consists of:

-  node - NodeRef

-  batch steps - List<Batch> - defined action + bag of objects + order for failures analysis

   -  BatchChoice - yang-modeled action choice (e.g. FlatBatchAddFlowCase) containing batch bag of objects (e.g. flows to be added)

   -  BatchOrder - (integer) order of batch step (should be incremented by single action)

-  exitOnFirstError - boolean flag

Workflow
~~~~~~~~
#. prepare **list of steps** based on input

#. **mark barriers** in steps where needed

#. prepare particular **F/G/M-batch** service calls from **Flat-batch** steps

   -  F/G/M-batch services encapsulate bulk of single service calls

   -  they actually chain barrier after processing all single calls if actual step is marked as barrier-needed

#. **chain** futures and **start** executing

   - start all actions that can be run simultaneously (chain all on one starting point)

   -  in case there is a step marked as barrier-needed

      -  wait for all fired jobs up to one with barrier

      - merge rpc results (status, errors, batch failures) into single one

      - the latest job with barrier is new starting point for chaining

Services encapsulation
^^^^^^^^^^^^^^^^^^^^^^

-  SalFlatBatchService

   -  SalFlowBatchService

      -  SalFlowService

   -  SalGroupBatchService

      -  SalGroupService

   -  SalMeterBatchService

      -  SalMeterService

Barrier decision
^^^^^^^^^^^^^^^^

-  decide on actual step and all previous steps since the latest barrier

-  if condition in table below is satisfied the latest step before actual is marked as barrier-needed

+---------------------------+------------------------------------------------------------------+
| actual step               | previous steps contain                                           |
+===========================+==================================================================+
| FLOW_ADD *or* FLOW_UPDATE | GROUP_ADD *or* METER_ADD                                         |
+---------------------------+------------------------------------------------------------------+
| GROUP_ADD                 | GROUP_ADD *or* GROUP_UPDATE                                      |
+---------------------------+------------------------------------------------------------------+
| GROUP_REMOVE              | FLOW_UPDATE *or* FLOW_REMOVE *or* GROUP_UPDATE *or* GROUP_REMOVE |
+---------------------------+------------------------------------------------------------------+
| METER_REMOVE              | FLOW_UPDATE *or* FLOW_REMOVE                                     |
+---------------------------+------------------------------------------------------------------+

Error handling
^^^^^^^^^^^^^^

There is flag in ProcessFlatBatchInput to stop process on the first error.

-  *true* - if partial step is not successful stop whole processing

-  *false* (default) - try to process all steps regardless partial results

If error occurs in any of partial steps upper FlatBatchService call will return as unsuccessful in both cases.
However every partial error is attached to general flat batch result along with BatchFailure (contains BatchOrder
and BatchItemIdChoice to identify failed step).

Cluster singleton approach in plugin
------------------------------------

Basics
~~~~~~

Description
^^^^^^^^^^^

The existing OpenDaylight service deployment model assumes symmetric
clusters, where all services are activated on all nodes in the cluster.
However, many services require that there is a single active service
instance per cluster. We call such services *singleton services*. The
Entity Ownership Service (EOS) represents the base Leadership choice for
one Entity instance. Every Cluster Singleton service **type** must have
its own Entity and every Cluster Singleton service **instance** must
have its own Entity Candidate. Every registered Entity Candidate should
be notified about its actual role. All this "work" is done by MD-SAL so
the Openflowplugin need "only" to register as service in
**SingletonClusteringServiceProvider** given by MD-SAL.

Change against using EOS service listener
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In this new clustering singleton approach plugin uses API from the
MD-SAL project: SingletonClusteringService which comes with three
methods.

::

    instantiateServiceInstance()
    closeServiceInstance()
    getIdentifier()

This service has to be registered to a
SingletonClusteringServiceProvider from MD-SAL which take care if
mastership is changed in cluster environment.

First method in SingletonClusteringService is being called when the
cluster node becomes a MASTER. Second is being called when status
changes to SLAVE or device is disconnected from cluster. Last method
plugins returns NodeId as ServiceGroupIdentifier Startup after device is
connected

On the start up the plugin we need to initialize first four managers for
each working area providing information and services

-  Device manager

-  RPC manager

-  Role manager

-  Statistics manager

After connection the device the listener Device manager get the event
and start up to creating the context for this connection. Startup after
device connection

Services are managed by SinlgetonClusteringServiceProvider from MD-SAL
project. So in startup we simply create a instance of LifecycleService
and register all contexts into it.

Role change
~~~~~~~~~~~

Plugin is no longer registered as Entity Ownership Service (EOS)
listener therefore does not need to and cannot respond on EOS ownership
changes.

Service start
^^^^^^^^^^^^^

Services start asynchronously but the start is managed by
LifecycleService. If something goes wrong LifecycleService stop starting
services in context and this speeds up the reconnect process. But the
services haven’t changed and plugin need to start all this:

-  Activating transaction chain manager

-  Initial gathering of device statistics

-  Initial submit to DS

-  Sending role MASTER to device

-  RPC services registration

-  Statistics gathering start

Service stop
^^^^^^^^^^^^

If closeServiceInstance occurred plugin just simply try to store all
unsubmitted transactions and close the transaction chain manager, stop
RPC services, stop Statistics gathering and after that all unregister
txEntity from EOS.

Yang models and API
-------------------

+--------------------------------------------------------+
| Model                                                  |
+========================================================+
| ***Openflow basic types***                             |
+--------------------------------------------------------+
| `opendaylight-table-types.yang <https://git.opendaylig |
| ht.org/gerrit/gitweb?p=openflowplugin.git;f=model/mode |
| l-flow-base/src/main/yang/opendaylight-table-types.yan |
| g;a=blob;hb=refs/heads/stable/boron>`__                |
+--------------------------------------------------------+
| `opendaylight-action-types.yang <https://git.opendayli |
| ght.org/gerrit/gitweb?p=openflowplugin.git;f=model/mod |
| el-flow-base/src/main/yang/opendaylight-action-types.y |
| ang;a=blob;hb=refs/heads/stable/boron>`__              |
+--------------------------------------------------------+
| `opendaylight-flow-types.yang <https://git.opendayligh |
| t.org/gerrit/gitweb?p=openflowplugin.git;f=model/model |
| -flow-base/src/main/yang/opendaylight-flow-types.yang; |
| a=blob;hb=refs/heads/stable/boron>`__                  |
+--------------------------------------------------------+
| `opendaylight-meter-types.yang <https://git.opendaylig |
| ht.org/gerrit/gitweb?p=openflowplugin.git;f=model/mode |
| l-flow-base/src/main/yang/opendaylight-meter-types.yan |
| g;a=blob;hb=refs/heads/stable/boron>`__                |
+--------------------------------------------------------+
| `opendaylight-group-types.yang <https://git.opendaylig |
| ht.org/gerrit/gitweb?p=openflowplugin.git;f=model/mode |
| l-flow-base/src/main/yang/opendaylight-group-types.yan |
| g;a=blob;hb=refs/heads/stable/boron>`__                |
+--------------------------------------------------------+
| `opendaylight-match-types.yang <https://git.opendaylig |
| ht.org/gerrit/gitweb?p=openflowplugin.git;f=model/mode |
| l-flow-base/src/main/yang/opendaylight-match-types.yan |
| g;a=blob;hb=refs/heads/stable/boron>`__                |
+--------------------------------------------------------+
| `opendaylight-port-types.yang <https://git.opendayligh |
| t.org/gerrit/gitweb?p=openflowplugin.git;f=model/model |
| -flow-base/src/main/yang/opendaylight-port-types.yang; |
| a=blob;hb=refs/heads/stable/boron>`__                  |
+--------------------------------------------------------+
| `opendaylight-queue-types.yang <https://git.opendaylig |
| ht.org/gerrit/gitweb?p=openflowplugin.git;f=model/mode |
| l-flow-base/src/main/yang/opendaylight-queue-types.yan |
| g;a=blob;hb=refs/heads/stable/boron>`__                |
+--------------------------------------------------------+
| ***Openflow services***                                |
+--------------------------------------------------------+
| `sal-table.yang <https://git.opendaylight.org/gerrit/g |
| itweb?p=openflowplugin.git;f=model/model-flow-service/ |
| src/main/yang/sal-table.yang;a=blob;hb=refs/heads/stab |
| le/boron>`__                                           |
+--------------------------------------------------------+
| `sal-group.yang <https://git.opendaylight.org/gerrit/g |
| itweb?p=openflowplugin.git;f=model/model-flow-service/ |
| src/main/yang/sal-group.yang;a=blob;hb=refs/heads/stab |
| le/boron>`__                                           |
+--------------------------------------------------------+
| `sal-queue.yang <https://git.opendaylight.org/gerrit/g |
| itweb?p=openflowplugin.git;f=model/model-flow-service/ |
| src/main/yang/sal-queue.yang;a=blob;hb=refs/heads/stab |
| le/boron>`__                                           |
+--------------------------------------------------------+
| `flow-errors.yang <https://git.opendaylight.org/gerrit |
| /gitweb?p=openflowplugin.git;f=model/model-flow-servic |
| e/src/main/yang/flow-errors.yang;a=blob;hb=refs/heads/ |
| stable/boron>`__                                       |
+--------------------------------------------------------+
| `flow-capable-transaction.yang <https://git.opendaylig |
| ht.org/gerrit/gitweb?p=openflowplugin.git;f=model/mode |
| l-flow-service/src/main/yang/flow-capable-transaction. |
| yang;a=blob;hb=refs/heads/stable/boron>`__             |
+--------------------------------------------------------+
| `sal-flow.yang <https://git.opendaylight.org/gerrit/gi |
| tweb?p=openflowplugin.git;f=model/model-flow-service/s |
| rc/main/yang/sal-flow.yang;a=blob;hb=refs/heads/stable |
| /boron>`__                                             |
+--------------------------------------------------------+
| `sal-meter.yang <https://git.opendaylight.org/gerrit/g |
| itweb?p=openflowplugin.git;f=model/model-flow-service/ |
| src/main/yang/sal-meter.yang;a=blob;hb=refs/heads/stab |
| le/boron>`__                                           |
+--------------------------------------------------------+
| `flow-topology-discovery.yang <https://git.opendayligh |
| t.org/gerrit/gitweb?p=openflowplugin.git;f=model/model |
| -flow-service/src/main/yang/flow-topology-discovery.ya |
| ng;a=blob;hb=refs/heads/stable/boron>`__               |
+--------------------------------------------------------+
| `node-errors.yang <https://git.opendaylight.org/gerrit |
| /gitweb?p=openflowplugin.git;f=model/model-flow-servic |
| e/src/main/yang/node-errors.yang;a=blob;hb=refs/heads/ |
| stable/boron>`__                                       |
+--------------------------------------------------------+
| `node-config.yang <https://git.opendaylight.org/gerrit |
| /gitweb?p=openflowplugin.git;f=model/model-flow-servic |
| e/src/main/yang/node-config.yang;a=blob;hb=refs/heads/ |
| stable/boron>`__                                       |
+--------------------------------------------------------+
| `sal-echo.yang <https://git.opendaylight.org/gerrit/gi |
| tweb?p=openflowplugin.git;f=model/model-flow-service/s |
| rc/main/yang/sal-echo.yang;a=blob;hb=refs/heads/stable |
| /boron>`__                                             |
+--------------------------------------------------------+
| `sal-port.yang <https://git.opendaylight.org/gerrit/gi |
| tweb?p=openflowplugin.git;f=model/model-flow-service/s |
| rc/main/yang/sal-port.yang;a=blob;hb=refs/heads/stable |
| /boron>`__                                             |
+--------------------------------------------------------+
| `packet-processing.yang <https://git.opendaylight.org/ |
| gerrit/gitweb?p=openflowplugin.git;f=model/model-flow- |
| service/src/main/yang/packet-processing.yang;a=blob;hb |
| =refs/heads/stable/boron>`__                           |
+--------------------------------------------------------+
| `flow-node-inventory.yang <https://git.opendaylight.or |
| g/gerrit/gitweb?p=openflowplugin.git;f=model/model-flo |
| w-service/src/main/yang/flow-node-inventory.yang;a=blo |
| b;hb=refs/heads/stable/boron>`__                       |
+--------------------------------------------------------+
| ***Openflow statistics***                              |
+--------------------------------------------------------+
| `opendaylight-queue-statistics.yang <https://git.opend |
| aylight.org/gerrit/gitweb?p=openflowplugin.git;f=model |
| /model-flow-statistics/src/main/yang/opendaylight-queu |
| e-statistics.yang;a=blob;hb=refs/heads/stable/boron>`_ |
+--------------------------------------------------------+
| `opendaylight-flow-table-statistics.yang <https://git. |
| opendaylight.org/gerrit/gitweb?p=openflowplugin.git;f= |
| model/model-flow-statistics/src/main/yang/opendaylight |
| -flow-table-statistics.yang;a=blob;hb=refs/heads/stabl |
| e/boron>`__                                            |
+--------------------------------------------------------+
| `opendaylight-port-statistics.yang <https://git.openda |
| ylight.org/gerrit/gitweb?p=openflowplugin.git;f=model/ |
| model-flow-statistics/src/main/yang/opendaylight-port- |
| statistics.yang;a=blob;hb=refs/heads/stable/boron>`__  |
+--------------------------------------------------------+
| `opendaylight-statistics-types.yang <https://git.opend |
| aylight.org/gerrit/gitweb?p=openflowplugin.git;f=model |
| /model-flow-statistics/src/main/yang/opendaylight-stat |
| istics-types.yang;a=blob;hb=refs/heads/stable/boron>`_ |
+--------------------------------------------------------+
| `opendaylight-group-statistics.yang <https://git.opend |
| aylight.org/gerrit/gitweb?p=openflowplugin.git;f=model |
| /model-flow-statistics/src/main/yang/opendaylight-grou |
| p-statistics.yang;a=blob;hb=refs/heads/stable/boron>`_ |
+--------------------------------------------------------+
| `opendaylight-flow-statistics.yang <https://git.openda |
| ylight.org/gerrit/gitweb?p=openflowplugin.git;f=model/ |
| model-flow-statistics/src/main/yang/opendaylight-flow- |
| statistics.yang;a=blob;hb=refs/heads/stable/boron>`__  |
+--------------------------------------------------------+
| `opendaylight-meter-statistics.yang <https://git.opend |
| aylight.org/gerrit/gitweb?p=openflowplugin.git;f=model |
| /model-flow-statistics/src/main/yang/opendaylight-mete |
| r-statistics.yang;a=blob;hb=refs/heads/stable/boron>`_ |
+--------------------------------------------------------+

Karaf feature tree
------------------

.. figure:: ./images/openflowplugin/odl-ofp-feature-tree.png
   :alt: Openflow plugin karaf feature tree

   Openflow plugin karaf feature tree

Short
`HOWTO <https://wiki.opendaylight.org/view/OpenDaylight_OpenFlow_Plugin:FeatureTreeHowto>`__
create such a tree.

Wiring up notifications
-----------------------

Introduction
~~~~~~~~~~~~

We need to translate OpenFlow messages coming up from the OpenFlow
Protocol Library into
MD-SAL Notification objects and then publish them to the MD-SAL.

Mechanics
~~~~~~~~~

1. Create a Translator class

2. Register the Translator

3. Register the notificationPopListener to handle your Notification
   Objects

Create a Translator class
^^^^^^^^^^^^^^^^^^^^^^^^^

You can see an example in
`PacketInTranslator.java <https://git.opendaylight.org/gerrit/gitweb?p=openflowplugin.git;a=blob;f=openflowplugin/src/main/java/org/opendaylight/openflowplugin/openflow/md/core/translator/PacketInTranslator.java;hb=refs/heads/stable/boron>`__.

First, simply create the class

::

    public class PacketInTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

Then implement the translate function:

::

    public class PacketInTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

        protected static final Logger LOG = LoggerFactory
                .getLogger(PacketInTranslator.class);
        @Override
        public PacketReceived translate(SwitchConnectionDistinguisher cookie,
                SessionContext sc, OfHeader msg) {
                ...
        }

Make sure to check that you are dealing with the expected type and cast
it:

::

    if(msg instanceof PacketInMessage) {
        PacketInMessage message = (PacketInMessage)msg;
        List<DataObject> list = new CopyOnWriteArrayList<DataObject>();

Do your transation work and return

::

    PacketReceived pktInEvent = pktInBuilder.build();
    list.add(pktInEvent);
    return list;

Register your Translator Class
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Next you need to go to
`MDController.java <https://git.opendaylight.org/gerrit/gitweb?p=openflowplugin.git;a=blob;f=openflowplugin/src/main/java/org/opendaylight/openflowplugin/openflow/md/core/MDController.java;hb=refs/heads/stable/boron>`__
and in init() add register your Translator:

::

    public void init() {
            LOG.debug("Initializing!");
            messageTranslators = new ConcurrentHashMap<>();
            popListeners = new ConcurrentHashMap<>();
            //TODO: move registration to factory
            addMessageTranslator(ErrorMessage.class, OF10, new ErrorTranslator());
            addMessageTranslator(ErrorMessage.class, OF13, new ErrorTranslator());
            addMessageTranslator(PacketInMessage.class,OF10, new PacketInTranslator());
            addMessageTranslator(PacketInMessage.class,OF13, new PacketInTranslator());

Notice that there is a separate registration for each of OpenFlow 1.0
and OpenFlow 1.3. Basically, you indicate the type of OpenFlow Protocol
Library message you wish to translate for, the OpenFlow version, and an
instance of your Translator.

Register your MD-SAL Message for Notification to the MD-SAL
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Now, also in MDController.init() register to have the
notificationPopListener handle your MD-SAL Message:

::

    addMessagePopListener(PacketReceived.class, new NotificationPopListener<DataObject>());

You are done
^^^^^^^^^^^^

That’s all there is to it. Now when a message comes up from the OpenFlow
Protocol Library, it will be translated and published to the MD-SAL.

Message Order Preservation
--------------------------

While the Helium release of OpenFlow Plugin relied on queues to ensure
messages were delivered in order, subsequent releases instead ensure
that all the messages from a given device are delivered using the same
thread and thus message order is guaranteed without queues. The OpenFlow
plugin allocates a number of threads equal to twice the number of
processor cores on machine it is run, e.g., 8 threads if the machine has
4 cores.

.. note::

    While each device is assigned to one thread, multiple devices can be
    assigned to the same thread.
