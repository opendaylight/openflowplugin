/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.configuration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The overall contents of {$code configPid="org.opendaylight.openflowplugin"}.
 *
 * @see OpenflowProviderConfig
 */
@NonNullByDefault
@ObjectClassDefinition
public @interface OSGiConfiguration {
    /**
     * Persistent ID of OpenFlowPlugin configuration file.
     */
    String CONFIGURATION_PID = "org.opendaylight.openflowplugin";

    @AttributeDefinition(min = "1", max = "66535", description = "Quota for maximum number of RPC requests")
    int rpc$_$requests$_$quota() default 20_000;

    @AttributeDefinition(description = """
        This parameter indicates whether it is mandatory for switch to support OF1.3 features: table, flow, meter, \
        group. If this is set to true and switch does not support these features its connection will be denied""")
    boolean switch$_$features$_$mandatory() default false;

    @AttributeDefinition(min = "0", max = "4294967295", description = "Global notification quota")
    long global$_$notification$_$quota() default 64_000;

    @AttributeDefinition(description = "If enabled, periodic statistics gathering will be turned on")
    boolean is$_$statistics$_$polling$_$on() default true;

    @AttributeDefinition(description = "If enabled, periodic table statistics gathering will be turned on")
    boolean is$_$table$_$statistics$_$polling$_$on() default true;

    @AttributeDefinition(description = "If enabled, periodic flow statistics gathering will be turned on")
    boolean is$_$flow$_$statistics$_$polling$_$on() default true;

    @AttributeDefinition(description = "If enabled, periodic group statistics gathering will be turned on")
    boolean is$_$group$_$statistics$_$polling$_$on() default true;

    @AttributeDefinition(description = "If enabled, periodic meter statistics gathering will be turned on")
    boolean is$_$meter$_$statistics$_$polling$_$on() default true;

    @AttributeDefinition(description = "If enabled, periodic port statistics gathering will be turned on")
    boolean is$_$port$_$statistics$_$polling$_$on() default true;

    @AttributeDefinition(description = "If enabled, periodic queue statistics gathering will be turned on")
    boolean is$_$queue$_$statistics$_$polling$_$on() default true;

    @Deprecated
    @AttributeDefinition(description = """
        Expose backward compatible statistics RPCs providing result in form of asynchronous notification. This is
        deprecated, use direct statistics instead.""")
    boolean is$_$statistics$_$rpc$_$enabled() default false;

    @AttributeDefinition(min = "1", max = "4294967295", description = "Barrier timeout")
    long barrier$_$interval$_$timeout$_$limit() default 500;

    @AttributeDefinition(min = "1", max = "65535", description = "Barrier limit")
    int barrier$_$count$_$limit() default 25_600;

    @AttributeDefinition(min = "0", max = "65535", description = "Minimum (starting) number of threads in thread pool")
    int thread$_$pool$_$min$_$threads() default 1;

    // FIXME: cap at a more reasonable default (or unlimited)
    @AttributeDefinition(min = "1", max = "65535", description = "Maximum number of threads in thread pool")
    int thread$_$pool$_$max$_$threads() default 32_000;

    @AttributeDefinition(min = "0", max = "4294967295", description = """
        After how much time (in seconds) of inactivity will be threads in pool terminated""")
    long thread$_$pool$_$timeout() default 60;

    @AttributeDefinition(description = "Turning on flow removed notification")
    boolean enable$_$flow$_$removed$_$notification() default true;

    @AttributeDefinition(description = """
        Ability to skip pulling and storing of large table features. These features are still available via rpc but if
        set to true then maintenance in DS will be omitted""")
    boolean skip$_$table$_$features() default true;

    @AttributeDefinition(min = "1", max = "4294967295", description = """
        Initial delay used in polling the statistics, value is in milliseconds""")
    int basic$_$timer$_$delay() default 3_000;

    @AttributeDefinition(min = "1", max = "4294967295", description = """
        Maximum timer delay is the wait time to collect next statistics used in polling the statistics, value is in \
        milliseconds""")
    long maximum$_$timer$_$delay() default 900_000;

    @AttributeDefinition(description = """
        When true, openflowplugin won't send any specific role request down to the switch after plugin internally
        decides the ownership of the device using Entity Ownership Service. In this scenario, controller connection \
        for the device will be in equal role. The behavior will be same for single node setup and clustered setup. In \
        a clustered scenario, all the controller will be in equal role for the device. In this case device will send \
        all asynchronous event messages (e.g packet_in) to all the controllers, but openflowplugin will drop these \
        events for the controller instances that is internally not owning the device.""")
    // FIXME: this does not really work with the OFP multi-master w.r.t equal-generation semantics. The counters used
    //        need to evolve from an cluster-wide incrementing counter *on ODL cluster side*. Current implementation
    //        does not do that.
    boolean enable$_$equal$_$role() default false;

    @AttributeDefinition(description = """
        When true, Yang models are serialized and deserialized directly to and from format supported by device, so \
        serialization and deserialization is faster. Otherwise, models are first serialized to Openflow specification \
        models and then to format supported by device, and reversed when deserializing.""")
    boolean use$_$single$_$layer$_$() default true;

    @AttributeDefinition(min = "0", max = "65535", description = """
        To limit the number of datapath nodes to be connected to the controller instance per minute. When the default \
        value of zero is set, then the device connection rate limitter will be disabled. If it is set to any value, \
        then only those many number of datapath nodes are allowed to connect to the controller in a minute.""")
    int device$_$connection$_$rate$_$limit$_$per$_$min() default 0;

    @AttributeDefinition(min = "0", max = "65535", description = """
        Device connection hold time is the least time delay in seconds a device has to maintain between its \
        consecutive connection attempts. If time delay between the previous connection and the current connection is \
        within device connection hold time, the device will not be allowed to connect to the controller.

        Default value of the device connection hold time is 0 seconds.""")
    int device$_$connection$_$hold$_$time$_$in$_$seconds() default 0;

    @AttributeDefinition(min = "1", max = "4294967295", description = """
        Delay (in milliseconds) before device is removed from the operational data store in the event of device \
        disconnection from the controller.""")
    long device$_$datastore$_$removal$_$delay() default 500;

    // FIXME: split these out into a separate property namespace dedicated to ForwardingRulesManagerImpl
    /*
     * Forwarding Rule Manager Application Configuration, see {@code ForwardingRulesManagerConfig}.
     */
    @AttributeDefinition(description = "Disable the default switch reconciliation mechanism")
    boolean disable$_$reconciliation() default false;

    @AttributeDefinition(description = """
        Enable stale marking for switch reconciliation. Once user enable this feature forwarding rule manager will \
        keep track of any change to the config data store while the switch is disconnected from controller. Once \
        switch reconnects to the controller it will apply those changes to the switch and do the reconciliation \
        of other configuration as well.

        NOTE: This option will be effective only if disable-reconciliation is false.""")
    boolean stale$_$marking$_$enabled() default false;

    @AttributeDefinition(min = "0", max = "65535", description = """
        Number of time forwarding rules manager should retry to reconcile any specific configuration.""")
    int reconciliation$_$retry$_$count() default 5;

    @AttributeDefinition(description = """
        Bundle reconciliation can be enabled by making this flag to true. By default bundle reconciliation is disabled \
        and reconciliation happens via normal flow/group mods.

        NOTE: This option will be effective with disable-reconciliation is false.""")
    boolean bundle$_$based$_$reconciliation$_$enabled() default false;

    // FIXME: split these out into a separate property namespace dedicated to topology-lldp-discovery
    /*
     * Topology Lldp Discovery Configuration, see {@code TopologyLldpDiscoveryConfig}
     */
    @AttributeDefinition(min = "1", max = "4294967295", description = """
        Periodic interval for sending LLDP packet for link discovery.""")
    long topology$_$lldp$_$interval() default 5_000;

    @AttributeDefinition(min = "1", max = "4294967295", description = "Timeout duration for LLDP response message")
    long topology$_$lldp$_$expiration$_$interval() default 60_000;
}