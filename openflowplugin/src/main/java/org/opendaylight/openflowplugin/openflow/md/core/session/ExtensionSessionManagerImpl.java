/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.core.session.ExtensionSessionManager;

public final class ExtensionSessionManagerImpl implements ExtensionSessionManager {
    private static final ExtensionSessionManagerImpl INSTANCE = new ExtensionSessionManagerImpl();

    private ExtensionConverterProvider extensionConverterProvider;

    private ExtensionSessionManagerImpl() {
        // Hidden on purpose
    }

    /**
     * Returns singleton instance.
     */
    public static ExtensionSessionManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void setExtensionConverterProvider(final ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    @Override
    public ExtensionConverterProvider getExtensionConverterProvider() {
        return extensionConverterProvider;
    }
}
