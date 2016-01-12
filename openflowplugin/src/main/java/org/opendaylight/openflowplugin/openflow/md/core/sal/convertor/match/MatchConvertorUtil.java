/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;

/**
 * match related tools
 */
public abstract class MatchConvertorUtil {

    private static final String PREFIX_SEPARATOR = "/";

    /**
     * @param pField ipv6 external header flag
     * @return integer containing lower 9 bits filled with corresponding flags
     */
    public static Integer ipv6ExthdrFlagsToInt(final Ipv6ExthdrFlags pField) {
        Integer bitmap = 0;
        bitmap |= pField.isNonext() ? (1 << 0) : 0;
        bitmap |= pField.isEsp() ? (1 << 1) : 0;
        bitmap |= pField.isAuth() ? (1 << 2) : 0;
        bitmap |= pField.isDest() ? (1 << 3) : 0;
        bitmap |= pField.isFrag() ? (1 << 4) : 0;
        bitmap |= pField.isRouter() ? (1 << 5) : 0;
        bitmap |= pField.isHop() ? (1 << 6) : 0;
        bitmap |= pField.isUnrep() ? (1 << 7) : 0;
        bitmap |= pField.isUnseq() ? (1 << 8) : 0;
        return bitmap;
    }

}
