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
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterManager;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserActionFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserActionToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserMessageFromOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserMessageToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.RegistrationCloser.RegistrationCloserToOFJava;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
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
    private final Map<TypeVersionKey<?>, ConvertorMessageToOFJava<? extends ExperimenterMessageOfChoice, ? extends DataContainer>> registryMessageToOFJAva;
    private final Map<MessageTypeKey<?>, ConvertorMessageFromOFJava<? extends ExperimenterDataOfChoice, MessagePath>> registryMessageFromOFJAva;
    private final Map<Class<? extends MatchField>, Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField>> registryMatchTypeToOFJava;

    /**
     * default ctor
     */
    public ExtensionConverterManagerImpl() {
        registryFromOFJAva = new ConcurrentHashMap<>();
        registryToOFJAva = new ConcurrentHashMap<>();
        registryActionToOFJAva = new ConcurrentHashMap<>();
        registryActionFromOFJAva = new ConcurrentHashMap<>();
        registryMessageToOFJAva = new ConcurrentHashMap<>();
        registryMessageFromOFJAva = new ConcurrentHashMap<>();
        registryMatchTypeToOFJava = new ConcurrentHashMap<>();
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
     * @param key
     * @param extConvertor
     * @return
     */
    private <TO extends DataContainer, K extends ExperimenterMessageOfChoice> RegistrationCloserMessageToOFJava<TO, K> hireMessageJanitor(
            final TypeVersionKey<K> key,
            final ConvertorMessageToOFJava<K, TO> extConvertor) {
        RegistrationCloserMessageToOFJava<TO, K> janitor = new RegistrationCloserMessageToOFJava<>();
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
    RegistrationCloserMessageFromOFJava<FROM, PATH> hireMessageJanitor(
            final KEY key, final ConvertorMessageFromOFJava<FROM, PATH> extConvertor) {
        RegistrationCloserMessageFromOFJava<FROM, PATH> janitor = new RegistrationCloserMessageFromOFJava<>();
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

    /**
     * cancel registration of given converter
     *
     * @param key
     * @param converter
     */
    public void unregister(final MessageTypeKey<?> key, final ConvertorMessageFromOFJava<?, ?> converter) {
        ConvertorMessageFromOFJava<?, ?> registeredConverter = registryMessageFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryMessageFromOFJAva.remove(key);
        }
    }

    /**
     * cancel registration of given converter
     *
     * @param key
     * @param converter
     */
    public void unregister(final TypeVersionKey<?> key, final ConvertorMessageToOFJava<?, ?> converter) {
        ConvertorMessageToOFJava<?, ?> registeredConverter = registryMessageToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryMessageToOFJAva.remove(key);
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

    @Override
    public <I extends ExperimenterMessageOfChoice, O extends DataContainer> ObjectRegistration<ConvertorMessageToOFJava<I, O>> registerMessageConvertor(
            TypeVersionKey<I> key, ConvertorMessageToOFJava<I, O> convertor) {
        registryMessageToOFJAva.put(key, convertor);
        return hireMessageJanitor(key, convertor);
    }

    @Override
    public <I extends ExperimenterDataOfChoice> ObjectRegistration<ConvertorMessageFromOFJava<I, MessagePath>> registerMessageConvertor(
            MessageTypeKey<?> key, ConvertorMessageFromOFJava<I, MessagePath> convertor) {
        registryMessageFromOFJAva.put(key, convertor);
        return hireMessageJanitor(key, convertor);
    }

    @Override
    public <F extends ExperimenterMessageOfChoice, T extends DataContainer> ConvertorMessageToOFJava<F, T> getMessageConverter(TypeVersionKey<F> key) {
        return (ConvertorMessageToOFJava<F, T>) registryMessageToOFJAva.get(key);
    }

    @Override
    public <F extends DataContainer, P extends AugmentationPath> ConvertorMessageFromOFJava<F, P> getMessageConverter(MessageTypeKey<?> key) {
        return (ConvertorMessageFromOFJava<F, P>) registryMessageFromOFJAva.get(key);
    }

    @Override
    public void registerMatchTypeToOFJava(Class<? extends MatchField> ofJavaMatchFieldType,
                                          Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> ofPluginMatchFieldType) {
        registryMatchTypeToOFJava.put(ofJavaMatchFieldType, ofPluginMatchFieldType);
    }

    @Override
    public void unregisterMatchTypeToOFJava(Class<? extends MatchField> ofJavaMatchFieldType) {
        registryMatchTypeToOFJava.remove(ofJavaMatchFieldType);
    }

    @Override
    public Class<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.MatchField> getMatchHeaderType(Class<? extends MatchField> matchField) {
        return registryMatchTypeToOFJava.get(matchField);
    }
}
