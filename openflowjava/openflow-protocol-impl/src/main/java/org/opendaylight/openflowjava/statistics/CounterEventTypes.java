/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.statistics;

/**
 * Enumeration of events to be counted with StatisticsCounters
 * @author madamjak
 *
 */
public enum CounterEventTypes {
    /**
     * enter message to OFJ and pass to downstream
     */
    DS_ENTERED_OFJAVA,
    /**
     * flow-mod is entered
     */
    DS_FLOW_MODS_ENTERED,
    /**
     * encode message successfully
     */
    DS_ENCODE_SUCCESS,
    /**
     * fail encode message
     */
    DS_ENCODE_FAIL,
    /**
     * flow-mod encoded and sent to downstream
     */
    DS_FLOW_MODS_SENT,
    /**
     * packetIn message got dropped -filtering is active
     */
    US_DROPPED_PACKET_IN,
    /**
     * receive message and pass to upstream
     */
    US_RECEIVED_IN_OFJAVA,
    /**
     * decode message successfully
     */
    US_DECODE_SUCCESS,
    /**
     * fail decode message
     */
    US_DECODE_FAIL,
    /**
     * pass message to consumer (end of upstream)
     */
    US_MESSAGE_PASS;
}