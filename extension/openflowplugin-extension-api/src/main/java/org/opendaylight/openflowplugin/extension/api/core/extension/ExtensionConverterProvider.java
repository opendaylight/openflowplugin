/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api.core.extension;

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
import org.opendaylight.yangtools.binding.DataContainer;

public interface ExtensionConverterProvider {

    /**
     * Lookup converter.
     *
     * @param <F> type of parameter for getConverter
     * @param <P> augmentationPath
     * @param key the message type key
     * @return found converter
     */
    <F extends DataContainer, P extends AugmentationPath> ConvertorFromOFJava<F, P> getConverter(MessageTypeKey<?> key);

    /**
     * Lookup converter.
     *
     * @param <T> type of parameter
     * @param key the converter extension key
     * @return found converter
     */
    <T extends DataContainer> ConvertorToOFJava<T> getConverter(ConverterExtensionKey<?> key);

    /**
     * Lookup converter.
     *
     * @param <F> Actiontype
     * @param <T> datacontainer
     * @param key the type version key
     * @return found converter
     */
    <F extends Action, T extends DataContainer> ConvertorActionToOFJava<F, T> getConverter(TypeVersionKey<F> key);

    /**
     * Lookup converter.
     * TODO: this method should be compatible with {@link #getConverter(MessageTypeKey)} after matches are migrated
     * to similar structure
     *  @param <F> DataContainer
     *  @param <P> AugmentationPath
     * @param key the message type key
     * @return found converter
     */
    <F extends DataContainer, P extends AugmentationPath> ConvertorActionFromOFJava<F, P> getActionConverter(
            MessageTypeKey<?> key);

    /**
     * Lookup converter for experimenter message.
     *
     * @param <F> ExperimenterMessageOfChoice
     * @param <T> DataContainer
     * @param <D> DataConvertor
     * @param key the type version key
     * @return found converter
     */
    <F extends ExperimenterMessageOfChoice, T extends DataContainer,
        D extends ConvertorData> ConverterMessageToOFJava<F, T, D> getMessageConverter(TypeVersionKey<F> key);

    /**
     * Lookup converter for experimenter message.
     *
     * @param <F> DataContainer
     * @param <P> AugmentationPath
     * @param key the message type key
     * @return found converter
     */
    <F extends DataContainer, P extends AugmentationPath> ConvertorMessageFromOFJava<F, P> getMessageConverter(
            MessageTypeKey<?> key);
}
