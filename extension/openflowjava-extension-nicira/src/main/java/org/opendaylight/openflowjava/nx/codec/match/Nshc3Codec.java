/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.codec.match;

import org.opendaylight.openflowjava.nx.api.NiciraConstants;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ExperimenterClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.match.rev140421.NxmNxNshc3;

public class Nshc3Codec extends AbstractNshcCodec {

    private static final int NXM_FIELD_CODE = 8;
    public static final MatchEntrySerializerKey<ExperimenterClass, NxmNxNshc3> SERIALIZER_KEY =
            createSerializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NxmNxNshc3.VALUE);
    public static final MatchEntryDeserializerKey DESERIALIZER_KEY =
            createDeserializerKey(
                    EncodeConstants.OF_VERSION_1_3,
                    NiciraConstants.NX_NSH_VENDOR_ID,
                    NXM_FIELD_CODE);

    @Override
    public int getNxmFieldCode() {
        return NXM_FIELD_CODE;
    }

    @Override
    public MatchField getNxmField() {
        return NxmNxNshc3.VALUE;
    }
}
