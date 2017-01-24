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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundleFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.BundlePropertyType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.add.message.grouping.bundle.inner.message.BundlePortModCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundleProperty;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.common.grouping.BundlePropertyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.BundlePropertyExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.bundle.property.grouping.bundle.property.entry.bundle.property.experimenter.BundlePropertyExperimenterData;

/**
 * Test for {@link org.opendaylight.openflowplugin.extension.onf.serializer.AbstractBundleMessageFactory}
 * and util methods.
 */
public class AbstractBundleMessageFactoryTest {

    @Test
    public void writeBundleFlags() throws Exception {
        ByteBuf out = UnpooledByteBufAllocator.DEFAULT.buffer();
        AbstractBundleMessageFactory.writeBundleFlags(new BundleFlags(true, true), out);
        Assert.assertEquals("Wrong flags", 3, out.readUnsignedShort());
    }

    public static List<BundleProperty> createListWithBundleExperimenterProperty(BundlePropertyExperimenterData data) {
        BundlePropertyBuilder propertyBuilder = new BundlePropertyBuilder();
        propertyBuilder.setType(BundlePropertyType.ONFETBPTEXPERIMENTER);
        BundlePropertyExperimenterBuilder experimenterPropertyBuilder = new BundlePropertyExperimenterBuilder();
        experimenterPropertyBuilder.setExperimenter(new ExperimenterId(1L));
        experimenterPropertyBuilder.setExpType(2L);

        experimenterPropertyBuilder.setBundlePropertyExperimenterData(data);
        propertyBuilder.setBundlePropertyEntry(experimenterPropertyBuilder.build());
        return new ArrayList<>(Collections.singleton(propertyBuilder.build()));
    }

    public static BundlePropertyExperimenterData createBundleExperimenterPropertyData() {
        return () -> null;
    }

    public static BundlePortModCase createBundlePortModCase() {
        BundlePortModCaseBuilder caseBuilder = new BundlePortModCaseBuilder();
        caseBuilder.setVersion((short) EncodeConstants.OF13_VERSION_ID);
        caseBuilder.setXid(3L);
        caseBuilder.setPortNo(new PortNumber(9L));
        caseBuilder.setHwAddress(new MacAddress("08:00:27:00:B0:EB"));
        caseBuilder.setConfig(new PortConfig(true, false, true, false));
        caseBuilder.setMask(new PortConfig(false, true, false, true));
        caseBuilder.setAdvertise(new PortFeatures(true, false, false, false,
                                                  false, false, false, true,
                                                  false, false, false, false,
                                                  false, false, false, false));
        return caseBuilder.build();
    }
}