/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 20.4.2015.
 */
public interface MessageSpy<M> extends Runnable {

    /**
     * @param message - message coming to OFP
     */
    void spyIn(M message);

    /**
     * @param message - message from OFP
     */
    void spyOut(M message);

    /**
     * statistic groups overall in OFPlugin
     */
    enum STATISTIC_GROUP {
        /**
         * message from switch
         */
        FROM_SWITCH,
        /**
         * message from switch translated successfully - source
         */
        FROM_SWITCH_TRANSLATE_IN_SUCCESS,
        /**
         * message from switch translated successfully - target
         */
        FROM_SWITCH_TRANSLATE_OUT_SUCCESS,
        /**
         * message from switch where translation failed - source
         */
        FROM_SWITCH_TRANSLATE_SRC_FAILURE,
        /**
         * message from switch finally published into MD-SAL
         */
        FROM_SWITCH_PUBLISHED_SUCCESS,
        /**
         * message from switch - publishing into MD-SAL failed
         */
        FROM_SWITCH_PUBLISHED_FAILURE,

        /**
         * message from MD-SAL to switch via RPC
         */
        TO_SWITCH_SUCCESS,
        /**
         * message from MD-SAL to switch via RPC failed to be sent to device.
         */
        TO_SWITCH_FAILED,
        /**
         * message from MD-SAL to switch - sent to OFJava successfully and classified in DeviceContext
         */
        TO_SWITCH_SUBMITTED_SUCCESS,
        /**
         * message from MD-SAL to switch - sent to OFJava but failed
         */
        TO_SWITCH_SUBMITTED_FAILURE
    }

    /**
     * @param message   from switch or to switch - depends on statGroup
     * @param statGroup
     */
    void spyMessage(M message, STATISTIC_GROUP statGroup);
}
