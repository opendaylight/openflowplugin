/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

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
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Closes converter registrations.
 *
 * @param <K> converter key
 * @param <C> converter instance
 */
public abstract class RegistrationCloser<K, C> implements ObjectRegistration<C> {

    private ExtensionConverterManagerImpl registrator;
    private K key;
    private C converter;

    /**
     * Sets the registrator.
     *
     * @param registrator the registrator to set
     */
    public void setRegistrator(ExtensionConverterManagerImpl registrator) {
        this.registrator = registrator;
    }

    /**
     * Sets the key.
     *
     * @param key the key to set
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * Sets the converter.
     *
     * @param converter the converter to set
     */
    public void setConverter(C converter) {
        this.converter = converter;
    }

    /**
     * Returns the registrator.
     */
    public ExtensionConverterManagerImpl getRegistrator() {
        return registrator;
    }

    /**
     * Returns the key.
     */
    public K getKey() {
        return key;
    }

    /**
     * Returns the converter.
     */
    public C getConverter() {
        return converter;
    }

    @Override
    public C getInstance() {
        return getConverter();
    }

    /**
     * Standalone deregistrator.
     *
     * @param <T> target type of wrapped convertor
     */
    public static class RegistrationCloserToOFJava<T extends DataContainer> extends
            RegistrationCloser<ConverterExtensionKey<? extends ExtensionKey>, ConvertorToOFJava<T>> {

        @Override
        public void close() {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * Standalone deregistrator.
     *
     * @param <F> source type of wrapped convertor
     * @param <P> associated augmentation path
     */
    public static class RegistrationCloserFromOFJava<F extends DataContainer, P extends AugmentationPath>
            extends RegistrationCloser<MessageTypeKey<?>, ConvertorFromOFJava<F, P>> {
        @Override
        public void close() {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * Standalone deregistrator.
     *
     * @param <T> target type of wrapped convertor
     */
    public static class RegistrationCloserActionToOFJava<T extends DataContainer> extends
            RegistrationCloser<TypeVersionKey<? extends Action>, ConvertorActionToOFJava<Action, T>> {

        @Override
        public void close() {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * Standalone deregistrator.
     *
     * @param <F> source type of wrapped convertor
     * @param <P> associated augmentation path
     */
    public static class RegistrationCloserActionFromOFJava<F extends DataContainer, P extends AugmentationPath> extends
            RegistrationCloser<MessageTypeKey<?>, ConvertorActionFromOFJava<F, P>> {

        @Override
        public void close() {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * Standalone deregistrator.
     *
     * @param <T> target type of wrapped convertor
     */
    public static class RegistrationCloserMessageToOFJava<T extends DataContainer,
            K extends ExperimenterMessageOfChoice, D extends ConvertorData>
                extends RegistrationCloser<TypeVersionKey<K>, ConverterMessageToOFJava<K, T, D>> {

        @Override
        public void close() {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * Standalone deregistrator.
     *
     * @param <F> source type of wrapped convertor
     * @param <P> associated augmentation path
     */
    public static class RegistrationCloserMessageFromOFJava<F extends DataContainer, P extends AugmentationPath> extends
            RegistrationCloser<MessageTypeKey<?>, ConvertorMessageFromOFJava<F, P>> {

        @Override
        public void close() {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }
}
