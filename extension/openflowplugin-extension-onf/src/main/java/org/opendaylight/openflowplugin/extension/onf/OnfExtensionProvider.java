/*
  Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

  This program and the accompanying materials are made available under the
  terms of the Eclipse Public License v1.0 which accompanies this distribution,
  and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.onf.converter.BundleControlConverter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.onf.approved.extensions.rev161128.send.experimenter.input.experimenter.message.of.choice.BundleControl;

/**
 * Main provider for ONF extensions registration.
 */
public class OnfExtensionProvider {

    private static final BundleControlConverter bundleControlConverter = new BundleControlConverter();

    private final ExtensionConverterRegistrator converterRegistrator;

    public OnfExtensionProvider(final ExtensionConverterRegistrator converterRegistrator) {
        this.converterRegistrator = converterRegistrator;
    }

    /**
     * Init method for registration of converters called by blueprint.
     */
    public void init() {
        registerConverters();
    }

    private void registerConverters() {
        converterRegistrator.registerMessageConvertor(new TypeVersionKey<>(BundleControl.class, OFConstants.OFP_VERSION_1_3), bundleControlConverter);
        converterRegistrator.registerMessageConvertor(new MessageTypeKey<>(OFConstants.OFP_VERSION_1_3,
                        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.approved.extensions.rev160802.experimenter.input.experimenter.data.of.choice.BundleControl.class),
                bundleControlConverter);
    }

}
