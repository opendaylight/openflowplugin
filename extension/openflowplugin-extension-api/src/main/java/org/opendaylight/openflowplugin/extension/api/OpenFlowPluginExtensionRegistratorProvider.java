/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.api;

/**
 * Provides registrator for openflowplugin extension converters.
 * This method of getting the registrator is deprecated, rather use blueprint for getting following service:
 * @see org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager
 */
@Deprecated
public interface OpenFlowPluginExtensionRegistratorProvider {
    /**
     * @return the extensionConverterRegistry - here extension converters can be registered in order to support
     * vendor messages
     */
    ExtensionConverterRegistrator getExtensionConverterRegistrator();
}