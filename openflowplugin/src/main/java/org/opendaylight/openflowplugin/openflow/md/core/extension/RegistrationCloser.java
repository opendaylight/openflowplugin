/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorMessageToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.path.AugmentationPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * @param <KEY> converter key
 * @param <CONVERTER> converter instance
 */
public abstract class RegistrationCloser<KEY, CONVERTER> implements ObjectRegistration<CONVERTER> {

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

    @Override
    public CONVERTER getInstance() {
        return getConverter();
    }

    /**
     * standalone deregistrator
     * @param <TO> target type of wrapped convertor
     */
    public static class RegistrationCloserToOFJava<TO extends DataContainer> extends
            RegistrationCloser<ConverterExtensionKey<? extends ExtensionKey>, ConvertorToOFJava<TO>> {

        @Override
        public void close() throws Exception {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * standalone deregistrator
     * @param <FROM> source type of wrapped convertor
     * @param <PATH> associated augmentation path
     */
    public static class RegistrationCloserFromOFJava<FROM extends DataContainer, PATH extends AugmentationPath> extends RegistrationCloser<MessageTypeKey<?>, ConvertorFromOFJava<FROM, PATH>> {

        @Override
        public void close() throws Exception {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * standalone deregistrator
     * @param <TO> target type of wrapped convertor
     */
    public static class RegistrationCloserActionToOFJava<TO extends DataContainer> extends
            RegistrationCloser<TypeVersionKey<? extends Action>, ConvertorActionToOFJava<Action, TO>> {

        @Override
        public void close() throws Exception {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * standalone deregistrator
     * @param <FROM> source type of wrapped convertor
     * @param <PATH> associated augmentation path
     */
    public static class RegistrationCloserActionFromOFJava<FROM extends DataContainer, PATH extends AugmentationPath> extends
            RegistrationCloser<MessageTypeKey<?>, ConvertorActionFromOFJava<FROM, PATH>> {

        @Override
        public void close() throws Exception {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * standalone deregistrator
     *
     * @param <TO> target type of wrapped convertor
     */
    public static class RegistrationCloserMessageToOFJava<TO extends DataContainer, K extends ExperimenterMessageOfChoice> extends
            RegistrationCloser<TypeVersionKey<K>, ConvertorMessageToOFJava<K, TO>> {

        @Override
        public void close() throws Exception {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }

    /**
     * standalone deregistrator
     *
     * @param <FROM> source type of wrapped convertor
     * @param <PATH> associated augmentation path
     */
    public static class RegistrationCloserMessageFromOFJava<FROM extends DataContainer, PATH extends AugmentationPath> extends
            RegistrationCloser<MessageTypeKey<?>, ConvertorMessageFromOFJava<FROM, PATH>> {

        @Override
        public void close() throws Exception {
            getRegistrator().unregister(getKey(), getConverter());
        }
    }


}
