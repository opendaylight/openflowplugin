/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

/**
 * @author michal.polkorab
 *
 */
public final class InstructionConstants {

    /** Openflow v1.3 OFPIT_GOTO_TABLE code */
    public static final byte GOTO_TABLE_TYPE = 1;
    /** Openflow v1.3 OFPIT_WRITE_METADATA code */
    public static final byte WRITE_METADATA_TYPE = 2;
    /** Openflow v1.3 OFPIT_WRITE_ACTIONS code */
    public static final byte WRITE_ACTIONS_TYPE = 3;
    /** Openflow v1.3 OFPIT_APPLY_ACTIONS code */
    public static final byte APPLY_ACTIONS_TYPE = 4;
    /** Openflow v1.3 OFPIT_CLEAR_ACTIONS code */
    public static final byte CLEAR_ACTIONS_TYPE = 5;
    /** Openflow v1.3 OFPIT_METER code */
    public static final byte METER_TYPE = 6;

    /** PADDING in OFPIT_GOTO_TABLE */
    public static final byte PADDING_IN_GOTO_TABLE = 3;
    /** PADDING in OFPIT_WRITE_METADATA */
    public static final byte PADDING_IN_WRITE_METADATA = 4;
    /** PADDING in OFPIT_WRITE_ACTIONS, OFPIT_APPLY_ACTIONS
     *  and OFPIT_CLEAR_ACTIONS */
    public static final byte PADDING_IN_ACTIONS_INSTRUCTION = 4;

    /** Openflow v1.3 header length (padded) */
    public static final byte STANDARD_INSTRUCTION_LENGTH = 8;
    /** Openflow v1.3 OFPIT_WRITE_METADATA length */
    public static final byte WRITE_METADATA_LENGTH = 24;
    /** Openflow v1.3 header length (only type and length fields) */
    public static final byte INSTRUCTION_IDS_LENGTH = 4;

    private InstructionConstants() {
        throw new UnsupportedOperationException("Utility class shouldn't be instantiated");
    }
}
