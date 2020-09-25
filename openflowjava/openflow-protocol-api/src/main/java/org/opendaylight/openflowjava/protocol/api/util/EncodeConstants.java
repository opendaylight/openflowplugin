/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.util;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Stores common constants.
 *
 * @author michal.polkorab
 */
public interface EncodeConstants {

    /** Default OF padding (in bytes). */
    byte PADDING = 8;

    /** OpenFlow v1.0 wire protocol number. */
    byte OF10_VERSION_ID = 0x01;

    /** OpenFlow v1.0 wire protocol number as Uint8. */
    @NonNull Uint8 OF_VERSION_1_0 = Uint8.valueOf(OF10_VERSION_ID);

    /** OpenFlow v1.3 wire protocol number. */
    byte OF13_VERSION_ID = 0x04;

    /** OpenFlow v1.3 wire protocol number as Uint8. */
    @NonNull Uint8 OF_VERSION_1_3 = Uint8.valueOf(OF13_VERSION_ID);

    /** OpenFlow v1.4 wire protocol number. */
    byte OF14_VERSION_ID = 0x05;

    /** OpenFlow v1.4 wire protocol number as Uint8. */
    @NonNull Uint8 OF_VERSION_1_4 = Uint8.valueOf(OF14_VERSION_ID);

    /** OpenFlow v1.5 wire protocol number. */
    byte OF15_VERSION_ID = 0x06;

    /** OpenFlow v1.5 wire protocol number as Uint8. */
    @NonNull Uint8 OF_VERSION_1_5 = Uint8.valueOf(OF15_VERSION_ID);

    /** OpenFlow Hello message type value. */
    byte OF_HELLO_MESSAGE_TYPE_VALUE = 0;

    /** OpenFlow PacketIn message type value. */
    byte OF_PACKETIN_MESSAGE_TYPE_VALUE = 10;

    /** Index of length in Openflow header. */
    int OFHEADER_LENGTH_INDEX = 2;

    /** Size of Openflow header. */
    int OFHEADER_SIZE = 8;

    /** Zero length - used when the length is updated later. */
    int EMPTY_LENGTH = 0;

    /** Length of mac address. */
    byte MAC_ADDRESS_LENGTH = 6;

    /** Number of groups in ipv4 address. */
    byte GROUPS_IN_IPV4_ADDRESS = 4;

    /** Length of ipv6 address in bytes. */
    byte SIZE_OF_IPV6_ADDRESS_IN_BYTES = 8 * Short.BYTES;

    /** Length of 3 bytes. */
    byte SIZE_OF_3_BYTES = 3;

    /** Empty (zero) int value. */
    int EMPTY_VALUE = 0;

    /** Common experimenter value. */
    int EXPERIMENTER_VALUE = 0xFFFF;

    /** OF v1.0 maximal port name length. */
    byte MAX_PORT_NAME_LENGTH = 16;

    // ONF Approved Extensions Constants.

    /** Experimenter ID of ONF approved extensions. */
    Uint32 ONF_EXPERIMENTER_ID = Uint32.valueOf(0x4F4E4600).intern();

    /** ONFOXM_ET_TCP_FLAGS value. */
    int ONFOXM_ET_TCP_FLAGS = 42;
}
