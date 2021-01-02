/*
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.Convertor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.port.mod.port.Port;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortState;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.Ports;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.desc._case.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Converts port mod, port status and port description MD-SAL messages to OF library data.
 *
 * <p>
 * Example usage:
 * <pre>
 * {@code
 * VersionConvertorData data = new VersionConvertorData(version);
 * Optional<PortModInput> ofPort = convertorManager.convert(salPort, data);
 * }
 * </pre>
 */
public class PortConvertor extends Convertor<Port, PortModInput, VersionConvertorData> {

    private static final Set<Class<?>> TYPES = Collections.singleton(Port.class);

    /**
     * Create default empty port mod input
     * Use this method, if result from convertor is empty.
     *
     * @param version Openflow version
     * @return default empty port mod input
     */
    public static PortModInput defaultResult(final Uint8 version) {
        return new PortModInputBuilder().setVersion(version).build();
    }

    private static PortConfig maskPortConfigFields(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {
        if (configData == null) {
            return null;
        }
        return new PortConfig(configData.getNOFWD(), configData.getNOPACKETIN(), configData.getNORECV(),
            configData.getPORTDOWN());
    }

    private static PortConfigV10 maskPortConfigV10Fields(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortConfig configData) {
        if (configData == null) {
            return null;
        }
        return new PortConfigV10(false, configData.getNOFWD(), configData.getNOPACKETIN(), configData.getNORECV(), true,
            true, configData.getPORTDOWN());

    }

    private static PortFeatures getPortFeatures(final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port
            .rev130925.PortFeatures salPortFeatures) {
        return new PortFeatures(salPortFeatures.getHundredGbFd(), salPortFeatures.getHundredMbFd(),
                salPortFeatures.getHundredMbHd(), salPortFeatures.getTenGbFd(), salPortFeatures.getTenMbFd(),
                salPortFeatures.getTenMbHd(), salPortFeatures.getOneGbFd(), salPortFeatures.getOneGbHd(),
                salPortFeatures.getOneTbFd(), salPortFeatures.getFortyGbFd(), salPortFeatures.getAutoeng(),
                salPortFeatures.getCopper(), salPortFeatures.getFiber(), salPortFeatures.getOther(),
                salPortFeatures.getPause(), salPortFeatures.getPauseAsym());
    }

    private static PortFeaturesV10 getPortFeaturesV10(final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types
            .port.rev130925.PortFeatures salPortFeatures) {
        return new PortFeaturesV10(salPortFeatures.getHundredMbFd(), salPortFeatures.getHundredMbHd(),
                salPortFeatures.getTenGbFd(), salPortFeatures.getTenMbFd(), salPortFeatures.getTenMbHd(),
                salPortFeatures.getOneGbFd(), salPortFeatures.getOneGbHd(), salPortFeatures.getAutoeng(),
                salPortFeatures.getCopper(), salPortFeatures.getFiber(), salPortFeatures.getPause(),
                salPortFeatures.getPauseAsym());
    }

    /**
     * This method is called as a reply to OFPMP_PORT_DESCRIPTION message(OF1.3.1).
     *
     * @param source  FlowCapablePort
     * @param version openflow version
     * @return OF:Ports
     */
    @VisibleForTesting
    static Ports toPortDesc(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.FlowCapablePort source,
            final short version) {

        PortsBuilder ofPortDescDataBuilder = new PortsBuilder();

        ofPortDescDataBuilder.setPortNo(OpenflowPortsUtil.getProtocolPortNumber(OpenflowVersion.get(version),
                source.getPortNumber())); // portNO

        ofPortDescDataBuilder.setHwAddr(source.getHardwareAddress());
        ofPortDescDataBuilder.setName(source.getName());

        PortConfig config = maskPortConfigFields(source.getConfiguration());

        ofPortDescDataBuilder.setConfig(config);

        PortState portState = getPortState(source.getState());

        ofPortDescDataBuilder.setState(portState);
        ofPortDescDataBuilder.setCurrentFeatures(getPortFeatures(source.getCurrentFeature()));
        ofPortDescDataBuilder.setAdvertisedFeatures(getPortFeatures(source.getAdvertisedFeatures()));
        ofPortDescDataBuilder.setSupportedFeatures(getPortFeatures(source.getSupported()));
        ofPortDescDataBuilder.setPeerFeatures(getPortFeatures(source.getPeerFeatures()));
        ofPortDescDataBuilder.setCurrSpeed(source.getCurrentSpeed());
        ofPortDescDataBuilder.setMaxSpeed(source.getMaximumSpeed());

        return ofPortDescDataBuilder.build();

    }

    private static PortState getPortState(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortState state) {
        return new PortState(state.getLinkDown(), state.getBlocked(), state.getLive());
    }

    @Override
    public Collection<Class<?>> getTypes() {
        return TYPES;
    }

    @Override
    public PortModInput convert(final Port source, final VersionConvertorData data) {
        PortConfig config = maskPortConfigFields(source.getConfiguration());
        PortConfigV10 configV10 = maskPortConfigV10Fields(source.getConfiguration());

        PortModInputBuilder portModInputBuilder = new PortModInputBuilder();
        portModInputBuilder.setAdvertise(getPortFeatures(source.getAdvertisedFeatures()));
        portModInputBuilder.setPortNo(new PortNumber(OpenflowPortsUtil.getProtocolPortNumber(
                OpenflowVersion.get(data.getVersion()), source.getPortNumber())));

        portModInputBuilder.setConfig(config);
        portModInputBuilder.setMask(MoreObjects.firstNonNull(maskPortConfigFields(source.getMask()),
                new PortConfig(true, true, true, true)));

        portModInputBuilder.setHwAddress(new MacAddress(source.getHardwareAddress()));

        portModInputBuilder.setVersion(data.getVersion());

        portModInputBuilder.setConfigV10(configV10);
        portModInputBuilder.setMaskV10(MoreObjects.firstNonNull(maskPortConfigV10Fields(source.getMask()),
                new PortConfigV10(true, true, true, true, true, true, true)));
        portModInputBuilder.setAdvertiseV10(getPortFeaturesV10(source.getAdvertisedFeatures()));
        return portModInputBuilder.build();
    }
}
