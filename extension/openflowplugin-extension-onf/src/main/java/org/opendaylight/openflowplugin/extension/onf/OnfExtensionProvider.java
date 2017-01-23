/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import com.google.common.base.Preconditions;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.opendaylight.openflowplugin.extension.api.OpenFlowPluginExtensionRegistratorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main provider for ONF extensions.
 */
public class OnfExtensionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OnfExtensionProvider.class);

    private SwitchConnectionProvider switchConnectionProvider;
    private ExtensionConverterRegistrator converterRegistrator;

    public OnfExtensionProvider(final SwitchConnectionProvider switchConnectionProvider,
                                final OpenFlowPluginExtensionRegistratorProvider converterRegistrator) {
        this.switchConnectionProvider = Preconditions.checkNotNull(switchConnectionProvider,
                "SwitchConnectionProvider can not be null!");
        this.converterRegistrator = Preconditions.checkNotNull(converterRegistrator.getExtensionConverterRegistrator(),
                "ExtensionConverterRegistrator can not be null!");
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
        // TODO
    }


    private void registerDeserializers() {
        // TODO
    }

    private void registerConverters() {
        // TODO
    }

}
