/*
 * Copyright IBM Corporation, 2013.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import java.math.BigInteger;


/**
 * Class holds Openflow enum contant.
 * @author avishnoi@in.ibm.com
 *
 */
public class OpenflowEnumConstant {
    
    public static final Short OFPTT_ALL = 0xff;
    public static final Long ANY = Long.parseLong("ffffffff", 16);
    public static final Long OFPP_ANY = ANY;
    public static final Long OFPG_ANY = ANY;
    public static final BigInteger DEFAULT_COOKIE = BigInteger.ZERO;
    public static final BigInteger DEFAULT_COOKIE_MASK = BigInteger.ZERO;


}
