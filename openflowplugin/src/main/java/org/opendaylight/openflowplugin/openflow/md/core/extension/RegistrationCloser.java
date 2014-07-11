/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;

/**
 * @param <KEY> converter key
 * @param <CONVERTER> converter instance
 */
public abstract class RegistrationCloser<KEY, CONVERTER> implements AutoCloseable {
    
    private ExtensionConverterManagerImpl registrator;
    private KEY key;
    private CONVERTER converter;
    
    
    
    /**
     * @param registrator the registrator to set
     */
    public void setRegistrator(ExtensionConverterManagerImpl registrator) {
        this.registrator = registrator;
    }
    /**
     * @param key the key to set
     */
    public void setKey(KEY key) {
        this.key = key;
    }
    /**
     * @param converter the converter to set
     */
    public void setConverter(CONVERTER converter) {
        this.converter = converter;
    }
    /**
     * @return the registrator
     */
    public ExtensionConverterManagerImpl getRegistrator() {
        return registrator;
    }
    /**
     * @return the key
     */
    public KEY getKey() {
        return key;
    }
    /**
     * @return the converter
     */
    public CONVERTER getConverter() {
        return converter;
    }
    
    /**
     * standalone deregistrator
     */
    public static class RegistrationCloserToOFJava extends RegistrationCloser<ConverterExtensionKey<?>, ConvertorToOFJava<?, ?>> {
        
        @Override
        public void close() throws Exception {
            // TODO Auto-generated method stub
            getRegistrator().unregister(getKey(), getConverter());
        }
        
    }
    
    /**
     * standalone deregistrator
     */
    public static class RegistrationCloserFromOFJava extends RegistrationCloser<MessageTypeKey<?>, ConvertorFromOFJava<?, ?>> {
        
        @Override
        public void close() throws Exception {
            // TODO Auto-generated method stub
            getRegistrator().unregister(getKey(), getConverter());
        }
        
    }
}
