/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.api;

import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionSerializerKey;

/**
 * collection of factory/util methods
 */
public final class CiscoUtil {
    
    public static final ExperimenterActionSerializerKey createOfJavaKeyFrom(CiscoActionSerializerKey key) {
        return new ExperimenterActionSerializerKey(key.getVersion(), CiscoConstants.COF_VENDOR_ID, key.getSubtype());
    }

}
