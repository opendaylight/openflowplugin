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
import org.opendaylight.openflowjava.protocol.api.extensibility.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserToOFJava;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Clazz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * simple map-based registration engine implementation
 */
public class ExtensionConverterManagerImpl implements ExtensionConverterManager {

    private Map<MessageTypeKey<?>, ConvertorFromOFJava<?, ?>> registryFromOFJAva;
    private Map<ConverterExtensionKey<?>, ConvertorToOFJava<?, ?>> registryToOFJAva;

    /**
     * default ctor
     */
    public ExtensionConverterManagerImpl() {
        registryFromOFJAva = new ConcurrentHashMap<>();
        registryToOFJAva = new ConcurrentHashMap<>();
    }

    /**
     * @param key
     * @param extConvertor
     * @return
     */
    private AutoCloseable hireJanitor(MessageTypeKey<?> key, ConvertorFromOFJava<?, ?> extConvertor) {

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
    private AutoCloseable hireJanitor(ConverterExtensionKey<?> key, ConvertorToOFJava<?, ?> extConvertor) {

        RegistrationCloserToOFJava janitor = new RegistrationCloser.RegistrationCloserToOFJava();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * cancel registration of given converter
     * 
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

    @Override
    public AutoCloseable registerActionConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key,
            ConvertorToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> convertor) {
        registryToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public AutoCloseable registerActionConvertor(
            ActionSerializerKey<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, ? extends ActionBase> key,
            ConvertorFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, Action> convertor) {
        registryFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public AutoCloseable registerMatchConvertor(ConverterExtensionKey<? extends ExtensionKey> key,
            ConvertorToOFJava<Match, MatchEntries> convertor) {
        registryToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public AutoCloseable registerMatchConvertor(
            MatchEntrySerializerKey<MatchEntries, ? extends Clazz, ? extends MatchField> key,
            ConvertorFromOFJava<MatchEntries, Match> convertor) {
        registryFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

}
