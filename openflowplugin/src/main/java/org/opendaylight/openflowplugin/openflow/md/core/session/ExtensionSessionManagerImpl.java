/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.core.session.ExtensionSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExtensionSessionManagerImpl implements ExtensionSessionManager {
    private static final Logger LOG = LoggerFactory.getLogger(ExtensionSessionManagerImpl.class);

    private static ExtensionSessionManagerImpl INSTANCE = new ExtensionSessionManagerImpl();

    private ExtensionConverterProvider extensionConverterProvider;

    /**
     * Returns singleton instance.
     */
    public static ExtensionSessionManager getInstance() {
        return INSTANCE;
    }

    private ExtensionSessionManagerImpl() {
    }

    @Override
    public void setExtensionConverterProvider(
            ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }
}
