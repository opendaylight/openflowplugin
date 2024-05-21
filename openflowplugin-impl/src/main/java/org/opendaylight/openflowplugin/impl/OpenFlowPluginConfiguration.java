/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition
public @interface OpenFlowPluginConfiguration {
    @AttributeDefinition(description = "Quota for maximum number of RPC requests")
    int rpc$_$requests$_$quota() default 20_000;

    @AttributeDefinition(description = """
        This parameter indicates whether it is mandatory for switch to support OF1.3 features: table, flow, meter, \
        group. If this is set to true and switch does not support these features its connection will be denied""")
    boolean switch$_$features$_$mandatory() default false;

    @AttributeDefinition(description = "Global notification quota")
    int global$_$notification$_$quota() default 64_000;

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

    // FIXME: min/max
    @AttributeDefinition(description = "Barrier timeout")
    int barrier$_$interval$_$timeout$_$limit() default 500;

    // FIXME: min/max
    @AttributeDefinition(description = "Barrier limit")
    int barrier$_$count$_$limit() default 25_600;

    // FIXME: min/max
    @AttributeDefinition(description = "How long we should wait for echo reply (value is in milliseconds)")
    int echo$_$reply$_$timeout() default 2_000;

//
//        #
//        # Minimum (starting) number of threads in thread pool
//        #
//        # thread-pool-min-threads=1
//
//        #
//        # Maximum number of threads in thread pool
//        #
//        # thread-pool-max-threads=32000
//
//        #
//        # After how much time (in seconds) of inactivity will be threads in pool
//        # terminated
//        #
//        # thread-pool-timeout=60
//
//        #
//        # Turning on flow removed notification
//        #
//        # enable-flow-removed-notification=true
//
//        #
//        # Ability to skip pulling and storing of large table features. These features
//        # are still available via rpc but if set to true then maintenance in DS will be
//        # omitted
//        #
//        # skip-table-features=true
//
//        #
//        # Initial delay used in polling the statistics, value is in milliseconds
//        #
//        # basic-timer-delay=3000
//
//        #
//        # Maximum timer delay is the wait time to collect next statistics used in
//        # polling the statistics, value is in milliseconds
//        #
//        # maximum-timer-delay=900000
//
//        #
//        # When true, openflowplugin won't send any specific role
//        # request down to the switch after plugin internally decides the
//        # ownership of the device using Entity Ownership Service. In this
//        # scenario, controller connection for the device will be in equal
//        # role. The behavior will be same for single node setup and clustered
//        # setup. In clustered scenario, all the controller will be in equal
//        # role for the device. In this case device will send all asynchronous
//        # event messages (e.g packet_in) to all the controllers, but openflowplugin
//        # will drop these events for the controller instances that is internally
//        # not owning the device.
//        #
//        # enable-equal-role=false
//
//        #
//        # When true, Yang models are serialized and deserialized directly to and from
//        # format supported by device, so serialization and deserialization is faster.
//        # Otherwise, models are first serialized to Openflow specification models and
//        # then to format supported by device, and reversed when deserializing.
//        #
//        # use-single-layer-serialization=true
//
//        #
//        # To limit the number of datapath nodes to be connected to the controller instance
//        # per minute. When the default value of zero is set, then the device connection rate
//        # limitter will be disabled. If it is set to any value, then only those many
//        # number of datapath nodes are allowed to connect to the controller in a minute
//        #
//        # device-connection-rate-limit-per-min=0
//
//        #
//        # Device connection hold time is the least time delay in seconds a device has
//        # to maintain between its consecutive connection attempts. If time delay between
//        # the previous connection and the current connection is within device connection
//        # hold time, the device will not be allowed to connect to the controller.
//        # Default value of the device connection hold time is 0 second
//        #
//        # device-connection-hold-time-in-seconds=0
//
//        #
//        # Delay (in milliseconds) before device is removed from the operational data
//        # store in the event of device disconnection from the controller.
//        #
//        # device-datastore-removal-delay=500
//        #############################################################################
//        #                                                                           #
//        #            Forwarding Rule Manager Application Configuration              #
//        #                                                                           #
//        #############################################################################
//
//        #
//        # Disable the default switch reconciliation mechanism
//        #
//        # disable-reconciliation=false
//
//        #
//        # Enable stale marking for switch reconciliation. Once user enable this feature
//        # forwarding rule manager will keep track of any change to the config data store
//        # while the switch is disconnected from controller. Once switch reconnect to the
//        # controller it will apply those changes to the switch and do the reconciliation
//        # of other configuration as well.
//        # NOTE: This option will be effective only if disable_reconciliation=false.
//        #
//        # stale-marking-enabled=false
//
//        #
//        # Number of time forwarding rules manager should retry to reconcile any specific
//        # configuration.
//        #
//        # reconciliation-retry-count=5
//
//        #
//        # Bundle reconciliation can be enabled by making this flag to true.
//        # By default bundle reconciliation is disabled and reconciliation happens
//        # via normal flow/group mods.
//        # NOTE: This option will be effective with disable_reconciliation=false.
//        #
//        # bundle-based-reconciliation-enabled=false
//
//        #############################################################################
//        #                                                                           #
//        #            Topology Lldp Discovery Configuration                          #
//        #                                                                           #
//        #############################################################################
//
//        # Periodic interval for sending LLDP packet for link discovery
//        # topology-lldp-interval=5000
//
//        # Timeout duration for LLDP response message
//        # topology-lldp-expiration-interval=60000
    }