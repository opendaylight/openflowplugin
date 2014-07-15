/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.openflowjava.nx.NiciraConstants;
import org.opendaylight.openflowjava.nx.codec.match.Reg0Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg1Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg2Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg3Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg4Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg5Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg6Codec;
import org.opendaylight.openflowjava.nx.codec.match.Reg7Codec;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.action.RegLoadConvertor;
import org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match.RegConvertor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.Experimenter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.action.rev140714.NxActionRegLoadKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxmNxRegKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class NiciraExtensionProvider implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(NiciraExtensionProvider.class);

    private ExtensionConverterRegistrator extensionConverterRegistrator;
    private Set<ObjectRegistration<?>> registrations;

    private final static RegConvertor REG_CONVERTOR = new RegConvertor();

    @Override
    public void close() {
        for (AutoCloseable janitor : registrations) {
            try {
                janitor.close();
            } catch (Exception e) {
                LOG.warn("closing of extension converter failed", e);
            }
        }
        extensionConverterRegistrator = null;
    }

    /**
     * @param extensionConverterRegistrator
     */
    public void setExtensionConverterRegistrator(
            ExtensionConverterRegistrator extensionConverterRegistrator) {
                this.extensionConverterRegistrator = extensionConverterRegistrator;
    }
    
    /**
     * register appropriate converters
     */
    public void registerConverters() {
        registrations = new HashSet<>();
        registrations.add(extensionConverterRegistrator.registerActionConvertor(new ConverterExtensionKey<>(NxActionRegLoadKey.class,  EncodeConstants.OF13_VERSION_ID), new RegLoadConvertor()));
        registrations.add(extensionConverterRegistrator.registerActionConvertor(
                new ExperimenterActionSerializerKey(EncodeConstants.OF13_VERSION_ID, NiciraConstants.NX_VENDOR_ID), 
                new RegLoadConvertor()));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(new ConverterExtensionKey<>(NxmNxRegKey.class, EncodeConstants.OF13_VERSION_ID), REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg0Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg1Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg2Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg3Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg4Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg5Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg6Codec.SERIALIZER_KEY, REG_CONVERTOR));
        registrations.add(extensionConverterRegistrator.registerMatchConvertor(Reg7Codec.SERIALIZER_KEY, REG_CONVERTOR));
    }

}
