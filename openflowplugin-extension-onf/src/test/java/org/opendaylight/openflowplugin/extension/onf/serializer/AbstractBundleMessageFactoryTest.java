/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.serializer.AbstractBundleMessageFactory}
 * and util methods.
 */
public abstract class AbstractBundleMessageFactoryTest {

    @Mock
    protected SerializerRegistry registry;
    @Mock
    protected OFSerializer<BundlePropertyExperimenterData> propertySerializer;
    @Mock
    protected BundlePropertyExperimenterData propertyExperimenterData;

    @Test
    public void writeBundleFlags() {
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        AbstractBundleMessageFactory.writeBundleFlags(new BundleFlags(true, true), out);
        Assert.assertEquals("Wrong flags", 3, out.readUnsignedShort());
    }

}