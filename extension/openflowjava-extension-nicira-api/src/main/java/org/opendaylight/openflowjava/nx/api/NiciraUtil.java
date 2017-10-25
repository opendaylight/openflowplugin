/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;

/**
 * @author msunal
 */
public final class NiciraUtil {
    private NiciraUtil() { }

    public static final ActionSerializerKey<?> createOfJavaKeyFrom(NiciraActionSerializerKey key) {
        return new ActionSerializerKey<>(key.getVersion(), key.getSubtype(), NiciraConstants.NX_VENDOR_ID);
    }

}
