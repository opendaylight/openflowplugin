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
import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserActionFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserActionToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserToOFJava;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * simple map-based registration engine implementation
 */
public class ExtensionConverterManagerImpl implements ExtensionConverterManager {

    private final Map<MessageTypeKey<?>, ConvertorFromOFJava<?, ?>> registryFromOFJAva;
    private final Map<ConverterExtensionKey<?>, ConvertorToOFJava<?>> registryToOFJAva;
    private final Map<TypeVersionKey<? extends Action>, ConvertorActionToOFJava<? extends Action, ? extends DataContainer>> registryActionToOFJAva;
    private final Map<MessageTypeKey<?>, ConvertorActionFromOFJava<?, ?>> registryActionFromOFJAva;

    /**
     * default ctor
     */
    public ExtensionConverterManagerImpl() {
        registryFromOFJAva = new ConcurrentHashMap<>();
        registryToOFJAva = new ConcurrentHashMap<>();
        registryActionToOFJAva = new ConcurrentHashMap<>();
        registryActionFromOFJAva = new ConcurrentHashMap<>();
    }

    /**
     * @param key
     * @param extConvertor
     * @return
     */
    private <FROM extends DataContainer, PATH extends AugmentationPath, KEY extends MessageTypeKey<?>>
    RegistrationCloserFromOFJava<FROM, PATH> hireJanitor(
            final KEY key, final ConvertorFromOFJava<FROM, PATH> extConvertor) {
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
    private <FROM extends DataContainer, PATH extends AugmentationPath, KEY extends MessageTypeKey<?>>
    RegistrationCloserActionFromOFJava<FROM, PATH> hireJanitor(
            final KEY key, final ConvertorActionFromOFJava<FROM, PATH> extConvertor) {
        RegistrationCloserActionFromOFJava<FROM, PATH> janitor = new RegistrationCloser.RegistrationCloserActionFromOFJava<>();
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
            final ConverterExtensionKey<? extends ExtensionKey> key, final ConvertorToOFJava<TO> extConvertor) {
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
    private <TO extends DataContainer> RegistrationCloserActionToOFJava<TO> hireJanitor(
            final TypeVersionKey<? extends Action> key, final ConvertorActionToOFJava<Action, TO> extConvertor) {
        RegistrationCloserActionToOFJava<TO> janitor = new RegistrationCloser.RegistrationCloserActionToOFJava<>();
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
    public void unregister(final ConverterExtensionKey<?> key, final ConvertorToOFJava<?> converter) {
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
    public void unregister(final TypeVersionKey<? extends Action> key, final ConvertorActionToOFJava<?, ?> converter) {
        ConvertorActionToOFJava<?, ?> registeredConverter = registryActionToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryActionToOFJAva.remove(key);
        }
    }

    /**
     * cancel registration of given converter
     *
     * @param key
     * @param converter
     */
    public void unregister(final MessageTypeKey<?> key, final ConvertorFromOFJava<?, ?> converter) {
        ConvertorFromOFJava<?, ?> registeredConverter = registryFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryFromOFJAva.remove(key);
        }
    }

    /**
     * cancel registration of given converter
     *
     * @param key
     * @param converter
     */
    public void unregister(final MessageTypeKey<?> key, final ConvertorActionFromOFJava<?, ?> converter) {
        ConvertorActionFromOFJava<?, ?> registeredConverter = registryActionFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryActionFromOFJAva.remove(key);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends DataContainer> ConvertorToOFJava<FROM> getConverter(
            final ConverterExtensionKey<?> key) {
        return (ConvertorToOFJava<FROM>) registryToOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends Action, TO extends DataContainer> ConvertorActionToOFJava<FROM, TO> getConverter(
            final TypeVersionKey<FROM> key) {
        return (ConvertorActionToOFJava<FROM, TO>) registryActionToOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends DataContainer, PATH extends AugmentationPath> ConvertorFromOFJava<FROM, PATH> getConverter(
            final MessageTypeKey<?> key) {
        return (ConvertorFromOFJava<FROM, PATH>) registryFromOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <FROM extends DataContainer, PATH extends AugmentationPath> ConvertorActionFromOFJava<FROM, PATH> getActionConverter(
            final MessageTypeKey<?> key) {
        return (ConvertorActionFromOFJava<FROM, PATH>) registryActionFromOFJAva.get(key);
    }

    @Override
    public ObjectRegistration<ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action>>
    registerActionConvertor(
            final TypeVersionKey<? extends Action> key,
            final ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> convertor) {
        registryActionToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorActionFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, ActionPath>>
    registerActionConvertor(
            final ActionSerializerKey<?> key,
            final ConvertorActionFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action, ActionPath> convertor) {
        registryActionFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorToOFJava<MatchEntry>> registerMatchConvertor(final ConverterExtensionKey<? extends ExtensionKey> key,
                                                                                      final ConvertorToOFJava<MatchEntry> convertor) {
        registryToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorFromOFJava<MatchEntry, MatchPath>> registerMatchConvertor(
            final MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            final ConvertorFromOFJava<MatchEntry, MatchPath> convertor) {
        registryFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

}
