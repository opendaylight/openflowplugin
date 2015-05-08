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
         * message from MD-SAL entered service - first point of encounter
         */
        TO_SWITCH_ENTERED,
        /**
         * message from MD-SAL was disregarded (e.g. outstanding requests limit reached).
         */
        TO_SWITCH_DISREGARDED,
        /**
         * message from MD-SAL to switch - sent to OFJava successfully
         */
        TO_SWITCH_SUBMIT_SUCCESS,
        /**
         * message from MD-SAL to switch - ready to sent to OFJava (might be one-to-multiple ration between entered and sent)
         */
        TO_SWITCH_READY_FOR_SUBMIT,
        /**
         * message from MD-SAL to switch - sent to OFJava but failed
         */
        TO_SWITCH_SUBMIT_FAILURE
    }

    /**
     * @param message   from switch or to switch - depends on statGroup
     * @param statGroup
     */
    void spyMessage(M message, STATISTIC_GROUP statGroup);
}
