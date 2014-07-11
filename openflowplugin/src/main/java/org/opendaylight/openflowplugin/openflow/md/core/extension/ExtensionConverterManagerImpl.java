/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.openflowjava.protocol.api.extensibility.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserToOFJava;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * simple map-based registration engine implementation
 */
public class ExtensionConverterManagerImpl implements
        ExtensionConverterManager {
    
    private Map<MessageTypeKey<?>, ConvertorFromOFJava<?, ?>> registryFromOFJAva;
    private Map<ConverterExtensionKey<?>, ConvertorToOFJava<?, ?>> registryToOFJAva;
    
    /**
     * default ctor 
     */
    public ExtensionConverterManagerImpl() {
        registryFromOFJAva = new ConcurrentHashMap<>();
        registryToOFJAva = new ConcurrentHashMap<>();
    }

    @Override
    public AutoCloseable registerConvertor(
            ConverterExtensionKey<? extends DataContainer> key,
            ConvertorToOFJava<? extends DataContainer, ? extends DataContainer> extConvertor) {
        registryToOFJAva.put(key, extConvertor);
        return hireJanitor(key, extConvertor);
    }

    @Override
    public AutoCloseable registerConvertor(
            MessageTypeKey<? extends DataContainer> key,
            ConvertorFromOFJava<? extends DataContainer, ? extends DataContainer> extConvertor) {
        registryFromOFJAva.put(key, extConvertor);
        return hireJanitor(key, extConvertor);
    }
    
    /**
     * @param key
     * @param extConvertor
     * @return
     */
    private AutoCloseable hireJanitor(
            MessageTypeKey<?> key,
            ConvertorFromOFJava<?, ?> extConvertor) {
        
        RegistrationCloserFromOFJava janitor = new RegistrationCloser.RegistrationCloserFromOFJava();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }
    
    /**
     * @param key
     * @param extConvertor
     * @return
     */
    private AutoCloseable hireJanitor(
            ConverterExtensionKey<?> key,
            ConvertorToOFJava<?, ?> extConvertor) {
        
        RegistrationCloserToOFJava janitor = new RegistrationCloser.RegistrationCloserToOFJava();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * cancel registration of given converter
     * @param key
     * @param converter
     */
    public void unregister(ConverterExtensionKey<?> key, ConvertorToOFJava<?, ?> converter) {
        ConvertorToOFJava<?, ?> registeredConverter = registryToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryToOFJAva.remove(key);
        }
    }
    
    /**
     * cancel registration of given converter
     * @param key
     * @param converter
     */
    public void unregister(MessageTypeKey<?> key, ConvertorFromOFJava<?, ?> converter) {
        ConvertorFromOFJava<?, ?> registeredConverter = registryFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryFromOFJAva.remove(key);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends DataContainer, TO extends DataContainer> ConvertorToOFJava<FROM, TO> getConverter(
            ConverterExtensionKey<?> key) {
        return (ConvertorToOFJava<FROM, TO>) registryToOFJAva.get(key);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends DataContainer, TO extends DataContainer> ConvertorFromOFJava<FROM, TO> getConverter(
            MessageTypeKey<?> key) {
        return (ConvertorFromOFJava<FROM, TO>) registryFromOFJAva.get(key);
    }

}
