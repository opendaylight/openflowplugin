/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira;

import java.util.Collection;
import java.util.HashSet;

import org.opendaylight.openflowplugin.extension.api.ExtensionConverterRegistrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class NiciraExtensionProvider implements AutoCloseable {
    
    private static final Logger LOG = LoggerFactory
            .getLogger(NiciraExtensionProvider.class);

    private ExtensionConverterRegistrator extensionConverterRegistrator;
    private Collection<AutoCloseable> registrations;

    @Override
    public void close() {
        for (AutoCloseable janitor : registrations) {
            try {
                janitor.close();
            } catch (Exception e) {
                LOG.warn("closing of extension converter failed", e);
            }
        }
        extensionConverterRegistrator = null;
    }

    /**
     * @param extensionConverterRegistrator
     */
    public void setExtensionConverterRegistrator(
            ExtensionConverterRegistrator extensionConverterRegistrator) {
                this.extensionConverterRegistrator = extensionConverterRegistrator;
    }
    
    /**
     * register appropriate converters
     */
    public void registerConverters() {
        registrations = new HashSet<>();
        //TODO: compute key
        //extensionConverterRegistrator.registerConvertor(key, extConvertor);
    }

}
