/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.extension.api;

/**
 * Created by mirehak on 4/25/15.
 */
public interface OpenFlowPluginExtensionRegistratorProvider {
    /**
     * @return the extensionConverterRegistry - here extension converters can be registered in order to support
     * vendor messages
     */
    public ExtensionConverterRegistrator getExtensionConverterRegistrator();
}
