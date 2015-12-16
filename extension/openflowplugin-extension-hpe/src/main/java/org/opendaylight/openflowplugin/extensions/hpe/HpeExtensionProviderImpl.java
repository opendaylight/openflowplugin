/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extensions.hpe;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeTcpDstRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeTcpFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeTcpSrcRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeUdpDstRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extensions.hpe.rev151112.HpeUdpSrcRange;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.DataObject;

import java.util.LinkedList;
import java.util.List;

public class HpeExtensionProviderImpl {
    public static final Long HP_EXP_ID = 0x00002481L;

    private final List<SwitchConnectionProvider> openflowSwitchConnectionProvider;
    private final ExtensionConverterRegistrator extensionConverterRegistrator;
    private final List<HpeExtension> extensions = new LinkedList<>();
    private final List<Registration> convertorRegistrations = new LinkedList<>();

    public HpeExtensionProviderImpl(List<SwitchConnectionProvider> openflowSwitchConnectionProvider,
                                    OpenFlowPluginExtensionRegistratorProvider openflowPluginExtensionRegistry) {
        Preconditions.checkNotNull(openflowPluginExtensionRegistry);
        this.extensionConverterRegistrator = openflowPluginExtensionRegistry.getExtensionConverterRegistrator();
        this.openflowSwitchConnectionProvider = openflowSwitchConnectionProvider;
        Preconditions.checkNotNull(this.extensionConverterRegistrator);
        Preconditions.checkNotNull(this.openflowSwitchConnectionProvider);

        this.extensions.add(new HpeExtension(HpeUdpSrcRange.class, new UdpSrcRangeCodec(), new UdpSrcRangeConvertor()));
        this.extensions.add(new HpeExtension(HpeUdpDstRange.class, new UdpDstRangeCodec(), new UdpDstRangeConvertor()));
        this.extensions.add(new HpeExtension(HpeTcpSrcRange.class, new TcpSrcRangeCodec(), new TcpSrcRangeConvertor()));
        this.extensions.add(new HpeExtension(HpeTcpDstRange.class, new TcpDstRangeCodec(), new TcpDstRangeConvertor()));
        this.extensions.add(new HpeExtension(HpeTcpFlags.class, new TcpFlagsCodec(), new TcpFlagsConvertor()));
    }

    public void start() {
        for (HpeExtension extension : this.extensions) {
            HpeAbstractCodec codec = extension.getCodec();
            for (SwitchConnectionProvider switchConnectionProvider : this.openflowSwitchConnectionProvider) {
                switchConnectionProvider.registerMatchEntrySerializer(codec.getMatchEntrySerializerKey(), codec);
                switchConnectionProvider.registerMatchEntryDeserializer(codec.getMatchEntryDeserializerKey(), codec);
            }
            Class<? extends MatchField> matchField = extension.getMatchField();
            HpeAbstractConvertor<? extends DataObject> convertor = extension.getConvertor();
            this.extensionConverterRegistrator.registerMatchTypeToOFJava(convertor.getOxmMatchField(), matchField);
            this.convertorRegistrations.add(
                    this.extensionConverterRegistrator.registerMatchConvertor(convertor.getConverterExtensionKey(),
                            convertor));
            this.convertorRegistrations.add(
                    extensionConverterRegistrator.registerMatchConvertor(convertor.getMatchEntrySerializerKey(),
                            convertor));
        }
    }

    public void stop() throws Exception {
        for (Registration registration : this.convertorRegistrations) {
            registration.close();
        }
        for (HpeExtension extension : this.extensions) {
            HpeAbstractConvertor<? extends DataObject> convertor = extension.getConvertor();
            HpeAbstractCodec codec = extension.getCodec();
            for (SwitchConnectionProvider switchConnectionProvider : this.openflowSwitchConnectionProvider) {
                switchConnectionProvider.unregisterDeserializer(codec.getMatchEntryDeserializerKey());
                switchConnectionProvider.unregisterSerializer(codec.getMatchEntrySerializerKey());
            }
            this.extensionConverterRegistrator.unregisterMatchTypeToOFJava(convertor.getOxmMatchField());
        }
    }
}
