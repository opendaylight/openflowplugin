/**
  Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

  This program and the accompanying materials are made available under the
  terms of the Eclipse Public License v1.0 which accompanies this distribution,
  and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterIdTypeSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.onf.converter.BundleAddMessageConverter;
import org.opendaylight.openflowplugin.extension.onf.converter.BundleControlConverter;
import org.opendaylight.openflowplugin.extension.onf.deserializer.OnfExperimenterErrorFactory;
import org.opendaylight.openflowplugin.extension.onf.serializer.BundleAddMessageFactory;
import org.opendaylight.openflowplugin.extension.onf.serializer.BundleControlFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ErrorMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleAddMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.onf.rev161130.experimenter.input.experimenter.data.of.choice.BundleControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main provider for ONF extensions.
 */
public class OnfExtensionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OnfExtensionProvider.class);

    private SwitchConnectionProvider switchConnectionProvider;
    private ExtensionConverterRegistrator converterRegistrator;

    private static final BundleControlConverter bundleControlConverter = new BundleControlConverter();
    private static final BundleAddMessageConverter bundleAddMessageConverter = new BundleAddMessageConverter();

    public OnfExtensionProvider(final SwitchConnectionProvider switchConnectionProvider,
                                final OpenFlowPluginExtensionRegistratorProvider converterRegistrator) {
        this.switchConnectionProvider = switchConnectionProvider;
        this.converterRegistrator = converterRegistrator.getExtensionConverterRegistrator();
        LOG.info("ONF Extension Provider created.");
    }

    /**
     * Init method for registration of converters called by blueprint.
     */
    public void init() {
        registerSerializers();
        registerDeserializers();
        registerConverters();
        LOG.info("ONF Extension Provider started.");
    }

    private void registerSerializers() {
        switchConnectionProvider.registerExperimenterMessageSerializer(
                new ExperimenterIdTypeSerializerKey<>(OFConstants.OFP_VERSION_1_3,
                                                      OnfConstants.ONF_EXPERIMENTER_ID,
                                                      OnfConstants.ONF_ET_BUNDLE_CONTROL,
                                                      ExperimenterDataOfChoice.class),
                new BundleControlFactory());
        switchConnectionProvider.registerExperimenterMessageSerializer(
                new ExperimenterIdTypeSerializerKey<>(OFConstants.OFP_VERSION_1_3,
                                                      OnfConstants.ONF_EXPERIMENTER_ID,
                                                      OnfConstants.ONF_ET_BUNDLE_ADD_MESSAGE,
                                                      ExperimenterDataOfChoice.class),
                new BundleAddMessageFactory());
    }


    private void registerDeserializers() {
        switchConnectionProvider.registerExperimenterMessageDeserializer(
                new ExperimenterIdTypeDeserializerKey(OFConstants.OFP_VERSION_1_3,
                                                      OnfConstants.ONF_EXPERIMENTER_ID,
                                                      OnfConstants.ONF_ET_BUNDLE_CONTROL,
                                                      ExperimenterDataOfChoice.class),
                new org.opendaylight.openflowplugin.extension.onf.deserializer.BundleControlFactory());
        switchConnectionProvider.registerErrorDeserializer(
                new ExperimenterIdDeserializerKey(OFConstants.OFP_VERSION_1_3,
                                                  OnfConstants.ONF_EXPERIMENTER_ID,
                                                  ErrorMessage.class),
                new OnfExperimenterErrorFactory());
    }

    private void registerConverters() {
        converterRegistrator.registerMessageConvertor(new TypeVersionKey<>(BundleControl.class, OFConstants.OFP_VERSION_1_3), bundleControlConverter);
        converterRegistrator.registerMessageConvertor(new MessageTypeKey<>(OFConstants.OFP_VERSION_1_3, BundleControl.class), bundleControlConverter);
        converterRegistrator.registerMessageConvertor(new TypeVersionKey<>(BundleAddMessage.class, OFConstants.OFP_VERSION_1_3), bundleAddMessageConverter);
    }

}
