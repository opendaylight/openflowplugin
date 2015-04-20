/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.statistics;


/**
 * ticket spy - aimed on collecting intel about tickets 
 * @param <M> type of watched message
 */
@Deprecated
public interface MessageSpy<M> extends Runnable {

    /**
     * @param message content of ticket
     */
    void spyIn(M message);

    /**
     * @param message content of ticket
     */
    void spyOut(M message);
    
    
    // TODO: temporary solution, should be refactored and moved to managed bean
    
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
    
    /**
     * @param message from switch or to switch - depends on statGroup
     * @param statGroup 
     */
    void spyMessage(M message, STATISTIC_GROUP statGroup);
    }
