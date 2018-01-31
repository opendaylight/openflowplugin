/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;

/**
 * Simple {@link ExtensionConverterProvider} place holder.
 */
public interface ExtensionConverterProviderKeeper {

    /**
     * Sets the ExtensionConverterProvider. Extension converters will be kept in order to be accessible by inner
     * infrastructure.
     *
     * @param extensionConverterProvider the ExtensionConverterProvider
     */
    void setExtensionConverterProvider(ExtensionConverterProvider extensionConverterProvider);

    /**
     * Returns the extension converters registry access point for by inner infrastructure.
     */
    ExtensionConverterProvider getExtensionConverterProvider();
}
