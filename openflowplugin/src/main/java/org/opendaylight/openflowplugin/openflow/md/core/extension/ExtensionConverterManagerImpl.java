/*
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
import org.opendaylight.openflowplugin.extension.api.ConverterMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorData;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
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
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Simple map-based registration engine implementation.
 */
public class ExtensionConverterManagerImpl implements ExtensionConverterManager {

    private final Map<MessageTypeKey<?>, ConvertorFromOFJava<?, ?>> registryFromOFJAva;
    private final Map<ConverterExtensionKey<?>, ConvertorToOFJava<?>> registryToOFJAva;
    private final Map<TypeVersionKey<? extends Action>, ConvertorActionToOFJava<? extends Action,
            ? extends DataContainer>> registryActionToOFJAva;
    private final Map<MessageTypeKey<?>, ConvertorActionFromOFJava<?, ?>> registryActionFromOFJAva;
    private final Map<TypeVersionKey<?>, ConverterMessageToOFJava<? extends ExperimenterMessageOfChoice,
            ? extends DataContainer, ? extends ConvertorData>> registryMessageToOFJAva;
    private final Map<MessageTypeKey<?>, ConvertorMessageFromOFJava<? extends ExperimenterDataOfChoice, MessagePath>>
            registryMessageFromOFJAva;

    /**
     * Default constructor.
     */
    public ExtensionConverterManagerImpl() {
        registryFromOFJAva = new ConcurrentHashMap<>();
        registryToOFJAva = new ConcurrentHashMap<>();
        registryActionToOFJAva = new ConcurrentHashMap<>();
        registryActionFromOFJAva = new ConcurrentHashMap<>();
        registryMessageToOFJAva = new ConcurrentHashMap<>();
        registryMessageFromOFJAva = new ConcurrentHashMap<>();
    }

    /**
     * Creates a registration closer.
     *
     * @param key message key
     * @param extConvertor extension convertor
     * @return registration closure
     */
    private <F extends DataContainer, P extends AugmentationPath, K extends MessageTypeKey<?>>
            RegistrationCloserFromOFJava<F, P> hireJanitor(final K key,
                    final ConvertorFromOFJava<F, P> extConvertor) {
        RegistrationCloserFromOFJava<F, P> janitor = new RegistrationCloser.RegistrationCloserFromOFJava<>();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * Creates a registration closer.
     *
     * @param key message type key
     * @param extConvertor extension convertor
     * @return registration closure
     */
    private <F extends DataContainer, P extends AugmentationPath, K extends MessageTypeKey<?>>
            RegistrationCloserActionFromOFJava<F, P> hireJanitor(
                final K key, final ConvertorActionFromOFJava<F, P> extConvertor) {
        RegistrationCloserActionFromOFJava<F, P> janitor =
                new RegistrationCloser.RegistrationCloserActionFromOFJava<>();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * Creates a registration closer.
     *
     * @param key message type key
     * @param extConvertor extension convertor
     * @return registration closure
     */
    private <T extends DataContainer> RegistrationCloserToOFJava<T> hireJanitor(
            final ConverterExtensionKey<? extends ExtensionKey> key, final ConvertorToOFJava<T> extConvertor) {
        RegistrationCloserToOFJava<T> janitor = new RegistrationCloser.RegistrationCloserToOFJava<>();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * Creates a registration closer.
     *
     * @param key message type key
     * @param extConvertor extension convertor
     * @return registration closure
     */
    private <T extends DataContainer> RegistrationCloserActionToOFJava<T> hireJanitor(
            final TypeVersionKey<? extends Action> key, final ConvertorActionToOFJava<Action, T> extConvertor) {
        RegistrationCloserActionToOFJava<T> janitor = new RegistrationCloser.RegistrationCloserActionToOFJava<>();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * Creates a registration closer.
     *
     * @param key message type key
     * @param extConvertor extension convertor
     * @return registration closure
     */
    private <T extends DataContainer, K extends ExperimenterMessageOfChoice,
        D extends ConvertorData> RegistrationCloserMessageToOFJava<T, K,
        D> hireMessageJanitor(final TypeVersionKey<K> key, final ConverterMessageToOFJava<K, T, D> extConvertor) {
        RegistrationCloserMessageToOFJava<T, K, D> janitor = new RegistrationCloserMessageToOFJava<>();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * Creates a registration closer.
     *
     * @param key message type key
     * @param extConvertor extension convertor
     * @return registration closure
     */
    private <F extends DataContainer, P extends AugmentationPath, K extends MessageTypeKey<?>>
            RegistrationCloserMessageFromOFJava<F, P> hireMessageJanitor(
                final K key, final ConvertorMessageFromOFJava<F, P> extConvertor) {
        RegistrationCloserMessageFromOFJava<F, P> janitor = new RegistrationCloserMessageFromOFJava<>();
        janitor.setConverter(extConvertor);
        janitor.setKey(key);
        janitor.setRegistrator(this);
        return janitor;
    }

    /**
     * Cancel registration of given converter.
     *
     * @param key message key
     * @param converter extension convertor
     */
    public void unregister(final ConverterExtensionKey<?> key, final ConvertorToOFJava<?> converter) {
        ConvertorToOFJava<?> registeredConverter = registryToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryToOFJAva.remove(key);
        }
    }

    /**
     * Cancel registration of given converter.
     *
     * @param key message key
     * @param converter extension convertor
     */
    public void unregister(final TypeVersionKey<? extends Action> key, final ConvertorActionToOFJava<?, ?> converter) {
        ConvertorActionToOFJava<?, ?> registeredConverter = registryActionToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryActionToOFJAva.remove(key);
        }
    }

    /**
     * Cancel registration of given converter.
     *
     * @param key message key
     * @param converter extension convertor
     */
    public void unregister(final MessageTypeKey<?> key, final ConvertorFromOFJava<?, ?> converter) {
        ConvertorFromOFJava<?, ?> registeredConverter = registryFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryFromOFJAva.remove(key);
        }
    }

    /**
     * Cancel registration of given converter.
     *
     * @param key message key
     * @param converter extension convertor
     */
    public void unregister(final MessageTypeKey<?> key, final ConvertorActionFromOFJava<?, ?> converter) {
        ConvertorActionFromOFJava<?, ?> registeredConverter = registryActionFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryActionFromOFJAva.remove(key);
        }
    }

    /**
     * Cancel registration of given converter.
     *
     * @param key message key
     * @param converter extension convertor
     */
    public void unregister(final MessageTypeKey<?> key, final ConvertorMessageFromOFJava<?, ?> converter) {
        ConvertorMessageFromOFJava<?, ?> registeredConverter = registryMessageFromOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryMessageFromOFJAva.remove(key);
        }
    }

    /**
     * Cancel registration of given converter.
     *
     * @param key message key
     * @param converter extension convertor
     */
    public void unregister(final TypeVersionKey<?> key, final ConverterMessageToOFJava<?, ?, ?> converter) {
        ConverterMessageToOFJava<?, ?, ?> registeredConverter = registryMessageToOFJAva.get(key);
        if (registeredConverter != null && registeredConverter == converter) {
            registryMessageToOFJAva.remove(key);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends DataContainer> ConvertorToOFJava<F> getConverter(
            final ConverterExtensionKey<?> key) {
        return (ConvertorToOFJava<F>) registryToOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends Action, T extends DataContainer> ConvertorActionToOFJava<F, T> getConverter(
            final TypeVersionKey<F> key) {
        return (ConvertorActionToOFJava<F, T>) registryActionToOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends DataContainer, P extends AugmentationPath> ConvertorFromOFJava<F, P> getConverter(
            final MessageTypeKey<?> key) {
        return (ConvertorFromOFJava<F, P>) registryFromOFJAva.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <F extends DataContainer, P extends AugmentationPath> ConvertorActionFromOFJava<F, P> getActionConverter(
            final MessageTypeKey<?> key) {
        return (ConvertorActionFromOFJava<F, P>) registryActionFromOFJAva.get(key);
    }

    @Override
    public ObjectRegistration<ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow
            .common.action.rev150203.actions.grouping.Action>> registerActionConvertor(
                final TypeVersionKey<? extends Action> key,final ConvertorActionToOFJava<Action, org.opendaylight
                    .yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action> convertor) {
        registryActionToOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorActionFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
            .action.rev150203.actions.grouping.Action, ActionPath>> registerActionConvertor(
                final ActionSerializerKey<?> key,
                final ConvertorActionFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action
                    .rev150203.actions.grouping.Action, ActionPath> convertor) {
        registryActionFromOFJAva.put(key, convertor);
        return hireJanitor(key, convertor);
    }

    @Override
    public ObjectRegistration<ConvertorToOFJava<MatchEntry>> registerMatchConvertor(
            final ConverterExtensionKey<? extends ExtensionKey> key, final ConvertorToOFJava<MatchEntry> convertor) {
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
    public <I extends ExperimenterMessageOfChoice, O extends DataContainer, D extends ConvertorData>
            ObjectRegistration<ConverterMessageToOFJava<I, O, D>> registerMessageConvertor(
                TypeVersionKey<I> key, ConverterMessageToOFJava<I, O, D> convertor) {
        registryMessageToOFJAva.put(key, convertor);
        return hireMessageJanitor(key, convertor);
    }

    @Override
    public <I extends ExperimenterDataOfChoice> ObjectRegistration<ConvertorMessageFromOFJava<I, MessagePath>>
            registerMessageConvertor(MessageTypeKey<?> key, ConvertorMessageFromOFJava<I, MessagePath> convertor) {
        registryMessageFromOFJAva.put(key, convertor);
        return hireMessageJanitor(key, convertor);
    }

    @Override
    public <F extends ExperimenterMessageOfChoice, T extends DataContainer,
        D extends ConvertorData> ConverterMessageToOFJava<F, T, D> getMessageConverter(TypeVersionKey<F> key) {
        return (ConverterMessageToOFJava<F, T, D>) registryMessageToOFJAva.get(key);
    }

    @Override
    public <F extends DataContainer, P extends AugmentationPath> ConvertorMessageFromOFJava<F, P>
           getMessageConverter(MessageTypeKey<?> key) {
        return (ConvertorMessageFromOFJava<F, P>) registryMessageFromOFJAva.get(key);
    }
}
