/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.cof.api;

import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;

/**
 * cisco extension related constants
 */
public final class CiscoConstants {

    /** vendor id of cisco */
    public static final Long COF_VENDOR_ID = 0x0000000CL;
    /** bulk vendor deserializer key for OF-1.3 */
    public static final ExperimenterActionDeserializerKey OF13_DESERIALIZER_KEY = new ExperimenterActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, COF_VENDOR_ID);
    /** bulk vendor deserializer key for OF-1.0 */
    public static final ExperimenterActionDeserializerKey OF10_DESERIALIZER_KEY = new ExperimenterActionDeserializerKey(
            EncodeConstants.OF10_VERSION_ID, COF_VENDOR_ID);

    private CiscoConstants() {
        //NOOP
    }

}
