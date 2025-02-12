/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.onf;

import static java.util.Objects.requireNonNull;

import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.onf.converter.BundleAddMessageConverter;
import org.opendaylight.openflowplugin.extension.onf.converter.BundleControlConverter;
import org.opendaylight.openflowplugin.extension.onf.deserializer.OnfExperimenterErrorFactory;
import org.opendaylight.openflowplugin.extension.onf.serializer.BundleAddMessageFactory;
import org.opendaylight.openflowplugin.extension.onf.serializer.BundleControlFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleAddMessageSal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.bundle.service.rev170124.send.experimenter.input.experimenter.message.of.choice.BundleControlSal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleAddMessageOnf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev170124.experimenter.input.experimenter.data.of.choice.BundleControlOnf;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main provider for ONF extensions.
 */
@Singleton
@Component(service = { })
public final class OnfExtensionProvider implements AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(OnfExtensionProvider.class);

    private static final BundleControlConverter BUNDLE_CONTROL_CONVERTER = new BundleControlConverter();
    private static final BundleAddMessageConverter BUNDLE_ADD_MESSAGE_CONVERTER = new BundleAddMessageConverter();

    private final List<Registration> registrations;

    @Inject
    @Activate
    public OnfExtensionProvider(@Reference final SwitchConnectionProvider switchConnectionProvider,
            @Reference final OpenFlowPluginExtensionRegistratorProvider converterRegistrator) {
        final var extensionConverterRegistrator = requireNonNull(
            converterRegistrator.getExtensionConverterRegistrator(), "ExtensionConverterRegistrator can not be null!");

        switchConnectionProvider.registerExperimenterMessageSerializer(
            new ExperimenterIdTypeSerializerKey<>(OFConstants.OFP_VERSION_1_3,
                OnfConstants.ONF_EXPERIMENTER, OnfConstants.ONF_ET_BUNDLE_CONTROL.toJava(),
                ExperimenterDataOfChoice.class), new BundleControlFactory());
        switchConnectionProvider.registerExperimenterMessageSerializer(
            new ExperimenterIdTypeSerializerKey<>(OFConstants.OFP_VERSION_1_3,
                OnfConstants.ONF_EXPERIMENTER, OnfConstants.ONF_ET_BUNDLE_ADD_MESSAGE.toJava(),
                ExperimenterDataOfChoice.class), new BundleAddMessageFactory());

        switchConnectionProvider.registerExperimenterMessageDeserializer(
            new ExperimenterIdTypeDeserializerKey(OFConstants.OFP_VERSION_1_3,
                OnfConstants.ONF_EXPERIMENTER, OnfConstants.ONF_ET_BUNDLE_CONTROL.toJava(),
                ExperimenterDataOfChoice.class),
            new org.opendaylight.openflowplugin.extension.onf.deserializer.BundleControlFactory());
        switchConnectionProvider.registerErrorDeserializer(
            new ExperimenterIdDeserializerKey(OFConstants.OFP_VERSION_1_3,
                OnfConstants.ONF_EXPERIMENTER, ErrorMessage.class),
            new OnfExperimenterErrorFactory());

        registrations = List.of(
            extensionConverterRegistrator.registerMessageConvertor(
                new TypeVersionKey<>(BundleControlSal.class, OFConstants.OFP_VERSION_1_3),
                BUNDLE_CONTROL_CONVERTER),
            extensionConverterRegistrator.registerMessageConvertor(
                new MessageTypeKey<>(OFConstants.OFP_VERSION_1_3, BundleControlOnf.class),
                BUNDLE_CONTROL_CONVERTER),
            extensionConverterRegistrator.registerMessageConvertor(
                new TypeVersionKey<>(BundleAddMessageSal.class, OFConstants.OFP_VERSION_1_3),
                BUNDLE_ADD_MESSAGE_CONVERTER),
            extensionConverterRegistrator.registerMessageConvertor(
                new MessageTypeKey<>(OFConstants.OFP_VERSION_1_3, BundleAddMessageOnf.class),
                BUNDLE_ADD_MESSAGE_CONVERTER));

        LOG.info("ONF Extension Provider started.");
    }

    @PreDestroy
    @Deactivate
    @Override
    public void close() {
        registrations.forEach(Registration::close);
        LOG.info("ONF Extension Provider stopped.");
    }
}
