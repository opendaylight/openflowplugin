/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import java.util.Iterator;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

public abstract class AbstractIpv4PrefixEntrySerializer extends AbstractMatchEntrySerializer<Ipv4Prefix, Boolean> {
    protected AbstractIpv4PrefixEntrySerializer(int oxmFieldCode, int oxmClassCode) {
        super(oxmFieldCode, oxmClassCode, EncodeConstants.SIZE_OF_INT_IN_BYTES);
    }

    @Override
    protected final Boolean extractEntryMask(Ipv4Prefix entry) {
        // Split address to IP and mask
        final Iterator<String> addressParts = IpConversionUtil.splitToParts(entry);
        addressParts.next();

        // Check if we have mask
        return addressParts.hasNext() && Integer.parseInt(addressParts.next()) < 32;
    }

    @Override
    protected final void serializeEntry(Ipv4Prefix entry, Boolean mask, ByteBuf outBuffer) {
        writeIpv4Prefix(entry, outBuffer);
    }
}
