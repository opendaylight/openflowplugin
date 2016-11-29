/*
  Copyright (c) 2016 Pantheon Technologies s.r.o. and others. All rights reserved.

  This program and the accompanying materials are made available under the
  terms of the Eclipse Public License v1.0 which accompanies this distribution,
  and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.onf;

import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;

/**
 * Main provider for ONF extensions registration.
 */
public class OnfExtensionProvider {

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
        // TODO
    }

}
