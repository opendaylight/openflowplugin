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

/**
 * @author mirehak
 */
public class ExtensionSessionManagerImpl implements ExtensionSessionManager {

    protected static final Logger LOG = LoggerFactory.getLogger(ExtensionSessionManagerImpl.class);
    private static ExtensionSessionManagerImpl instance;
    private ExtensionConverterProvider extensionConverterProvider;


    /**
     * @return singleton instance
     */
    public static ExtensionSessionManager getInstance() {
        if (instance == null) {
            synchronized (ExtensionSessionManagerImpl.class) {
                if (instance == null) {
                    instance = new ExtensionSessionManagerImpl();
                }
            }
        }
        return instance;
    }

    private ExtensionSessionManagerImpl() {
        LOG.debug("singleton creating");
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
