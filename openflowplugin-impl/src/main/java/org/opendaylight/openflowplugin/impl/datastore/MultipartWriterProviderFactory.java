/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.datastore;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.impl.datastore.multipart.DescMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.FlowStatsMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.GroupDescMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.GroupFeaturesMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.GroupStatsMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.MeterConfigMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.MeterFeaturesMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.MeterStatsMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.PortDescMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.PortStatsMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.QueueStatsMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.TableFeaturesMultipartWriter;
import org.opendaylight.openflowplugin.impl.datastore.multipart.TableStatsMultipartWriter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Multipart writer provider factory.
 */
public final class MultipartWriterProviderFactory {

    private MultipartWriterProviderFactory() {
    }

    /**
     * Create default #{@link MultipartWriterProvider}.
     *
     * @param deviceContext device context
     * @return the statistics writer provider
     */
    public static MultipartWriterProvider createDefaultProvider(final DeviceContext deviceContext) {
        final InstanceIdentifier<Node> instanceIdentifier = deviceContext.getDeviceInfo().getNodeInstanceIdentifier();
        final MultipartWriterProvider provider = new MultipartWriterProvider();

        //  Periodic/direct statistics writers
        provider.register(MultipartType.OFPMPTABLE, new TableStatsMultipartWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPGROUP, new GroupStatsMultipartWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPMETER, new MeterStatsMultipartWriter(deviceContext, instanceIdentifier));
        provider.register(MultipartType.OFPMPPORTSTATS, new PortStatsMultipartWriter(deviceContext,
                instanceIdentifier, deviceContext.getPrimaryConnectionContext().getFeatures()));
        provider.register(MultipartType.OFPMPQUEUE, new QueueStatsMultipartWriter(deviceContext,
                instanceIdentifier, deviceContext.getPrimaryConnectionContext().getFeatures()));
        provider.register(MultipartType.OFPMPFLOW, new FlowStatsMultipartWriter(deviceContext, instanceIdentifier,
                deviceContext, deviceContext.getDeviceInfo().getVersion()));
        provider.register(MultipartType.OFPMPGROUPDESC, new GroupDescMultipartWriter(deviceContext,
                instanceIdentifier, deviceContext));
        provider.register(MultipartType.OFPMPMETERCONFIG, new MeterConfigMultipartWriter(deviceContext,
                instanceIdentifier, deviceContext));

        // Device initialization writers
        provider.register(MultipartType.OFPMPDESC, new DescMultipartWriter(deviceContext, instanceIdentifier,
                deviceContext.getPrimaryConnectionContext()));
        provider.register(MultipartType.OFPMPGROUPFEATURES, new GroupFeaturesMultipartWriter(deviceContext,
                instanceIdentifier));
        provider.register(MultipartType.OFPMPMETERFEATURES, new MeterFeaturesMultipartWriter(deviceContext,
                instanceIdentifier));
        provider.register(MultipartType.OFPMPTABLEFEATURES, new TableFeaturesMultipartWriter(deviceContext,
                instanceIdentifier));
        provider.register(MultipartType.OFPMPPORTDESC, new PortDescMultipartWriter(deviceContext, instanceIdentifier,
                deviceContext.getPrimaryConnectionContext().getFeatures()));

        return provider;
    }

}
