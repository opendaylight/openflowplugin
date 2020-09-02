/*
 * Copyright (c) 2019 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.match;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;

public abstract class AbstractIpv4PrefixEntrySerializer extends AbstractMatchEntrySerializer<Ipv4Prefix, Integer> {
    protected AbstractIpv4PrefixEntrySerializer(final int oxmClassCode, final int oxmFieldCode) {
        super(new ConstantHeaderWriter<>(oxmClassCode, oxmFieldCode, Integer.BYTES));
    }

    @Override
    protected final Integer extractEntryMask(final Ipv4Prefix entry) {
        return IpConversionUtil.hasIpv4Prefix(entry);
    }

    @Override
    protected final void serializeEntry(final Ipv4Prefix entry, final Integer mask, final ByteBuf outBuffer) {
        writeIpv4Prefix(entry, mask, outBuffer);
    }
}
