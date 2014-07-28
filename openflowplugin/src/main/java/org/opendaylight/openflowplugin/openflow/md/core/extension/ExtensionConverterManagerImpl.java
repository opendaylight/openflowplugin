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
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.experimenter.ExperimenterActionSerializerKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserToOFJava;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * simple map-based registration engine implementation
 */
public class ExtensionConverterManagerImpl implements ExtensionConverterManager {

    private Map<MessageTypeKey<?>, ConvertorFromOFJava<?, ?>> registryFromOFJAva;
    private Map<ConverterExtensionKey<?>, ConvertorToOFJava<?>> registryToOFJAva;
    private Map<Class<? extends Action>, ConvertorToOFJava<?>> registryActionToOFJAva;

    /**
     * default ctor
     */
    public ExtensionConverterManagerImpl() {
        registryFromOFJAva = new ConcurrentHashMap<>();
        registryToOFJAva = new ConcurrentHashMap<>();
        registryActionToOFJAva = new ConcurrentHashMap<>();
    }

    /**
     * @param key
     * @param extConvertor
     * @return
     */
    private <FROM extends DataContainer, PATH extends AugmentationPath, KEY extends MessageTypeKey<?>> 
            RegistrationCloserFromOFJava<FROM, PATH> hireJanitor(
            KEY key, ConvertorFromOFJava<FROM, PATH> extConvertor) {
        RegistrationCloserFromOFJava<FROM, PATH> janitor = new RegistrationCloser.RegistrationCloserFromOFJava<>();
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
    private <TO extends DataContainer> RegistrationCloserToOFJava<TO> hireJanitor(
            ConverterExtensionKey<? extends ExtensionKey> key, ConvertorToOFJava<TO> extConvertor) {
        RegistrationCloserToOFJava<TO> janitor = new RegistrationCloser.RegistrationCloserToOFJava<>();
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
    private <TO extends DataContainer> RegistrationCloserToOFJava<TO> hireJanitor(
            Class<? extends Action> key, ConvertorToOFJava<TO> extConvertor) {
        RegistrationCloserToOFJava<TO> janitor = new RegistrationCloser.RegistrationCloserToOFJava<>();
        janitor.setConverter(extConvertor);
        //janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * cancel registration of given converter
     * 
     * @param key
     * @param converter
     */
    public void unregister(ConverterExtensionKey<?> key, ConvertorToOFJava<?> converter) {
        ConvertorToOFJava<?> registeredConverter = registryToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryToOFJAva.remove(key);
        }
    }
    
    /**
     * cancel registration of given converter
     * 
     * @param key
     * @param converter
     */
    public void unregister(Class<? extends Action> key, ConvertorToOFJava<?> converter) {
        ConvertorToOFJava<?> registeredConverter = registryToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryToOFJAva.remove(key);
        }
    }

    /**
     * cancel registration of given converter
     * 
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
    public <FROM extends DataContainer> ConvertorToOFJava<FROM> getConverter(
            ConverterExtensionKey<?> key) {
        return (ConvertorToOFJava<FROM>) registryToOFJAva.get(key);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends Action, TO extends DataContainer> ConvertorActionToOFJava<FROM, TO> getConverter(
            Class<FROM> key) {
        return (ConvertorActionToOFJava<FROM, TO>) registryActionToOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends DataContainer, PATH extends AugmentationPath> ConvertorFromOFJava<FROM, PATH> getConverter(
            MessageTypeKey<?> key) {
        return (ConvertorFromOFJava<FROM, PATH>) registryFromOFJAva.get(key);
    }

    @Override
    @Deprecated
    public ObjectRegistration<ConvertorToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action>> registerActionConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key,
            ConvertorToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> convertor) {
        registryToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }
    
    @Override
    public ObjectRegistration<ConvertorToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action>> registerActionConvertor(
            Class<? extends Action> key,
            ConvertorToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> convertor) {
        registryActionToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }
    
    @Override
    public ObjectRegistration<ConvertorFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, ActionPath>> registerActionConvertor(
            ExperimenterActionSerializerKey key,
            ConvertorFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, ActionPath> convertor) {
        registryFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorToOFJava<MatchEntries>> registerMatchConvertor(ConverterExtensionKey<? extends ExtensionKey> key,
            ConvertorToOFJava<MatchEntries> convertor) {
        registryToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorFromOFJava<MatchEntries, MatchPath>> registerMatchConvertor(
            MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            ConvertorFromOFJava<MatchEntries, MatchPath> convertor) {
        registryFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

}
