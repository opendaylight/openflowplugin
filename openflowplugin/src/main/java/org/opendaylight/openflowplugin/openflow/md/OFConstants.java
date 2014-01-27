/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md;

import java.math.BigInteger;

/**
 * OFP related constants
 */
public class OFConstants {

    /** reserved port: process with normal L2/L3 switching  */
    public static final short OFPP_NORMAL = ((short)0xfffa);
    /** reserved port: all physical ports except input port  */
    public static final short OFPP_ALL  = ((short)0xfffc);
    /** reserved port: local openflow port  */
    public static final short OFPP_LOCAL = ((short)0xfffe);
    
    
    /** openflow protocol 1.0 - version identifier */
    public static final short OFP_VERSION_1_0 = 0x01;
    /** openflow protocol 1.3 - version identifier */
    public static final short OFP_VERSION_1_3 = 0x04;
    
    public static final Short OFPTT_ALL = 0xff;
    private static final Long ANY = 0xffffffffL;
    /** openflow protocol 1.3 - port numbering */
    public static final Long OFPP_ANY = ANY;
    /** openflow protocol 1.3 - group numbering */
    public static final Long OFPG_ANY = ANY;
    /** openflow protocol 1.3 - group numbering */
    public static final Long OFPG_ALL = 0xfffffffcL;
    /** openflow protocol 1.3 - queue numbering */
    public static final Long OFPQ_ANY = ANY;
    /** openflow protocol 1.3 - meter numbering */
    public static final Long OFPM_ALL = 0xffffffffL;
    public static final BigInteger DEFAULT_COOKIE = BigInteger.ZERO;
    public static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO;
    /** openflow protocol 1.3 - no buffer */
    public static final Long OFP_NO_BUFFER = 0xffffffffL;

    public static final int MAC_ADDRESS_LENGTH = 6;
    public static final int SIZE_OF_LONG_IN_BYTES = 8;
    public static final int SIGNUM_UNSIGNED = 1;
}
