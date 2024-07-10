/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.extension.api.path.MessagePath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.experimenter.core.ExperimenterDataOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.experimenter.core.message.ExperimenterMessageOfChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * Registry for message converters provided by vendor extension.
 */
public interface ExtensionConverterRegistrator {


    ObjectRegistration<ConvertorToOFJava<MatchEntry>> registerMatchConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key, ConvertorToOFJava<MatchEntry> convertor);

    ObjectRegistration<ConvertorFromOFJava<MatchEntry, MatchPath>> registerMatchConvertor(
            MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            ConvertorFromOFJava<MatchEntry, MatchPath> convertor);

    /**
     * Registers an action converter.
     *
     * @param key action case type + ofp-version
     * @param converter the converter
     * @return closable registration
     */
    ObjectRegistration<ConvertorActionToOFJava<
        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>>
            registerActionConvertor(TypeVersionKey<? extends
                org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> key,
                ConvertorActionToOFJava<
                    org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action,
                    Action> converter);

    /**
     * Registers an action converter.
     *
     * @param key actionSubType, action type (Experimenter), experimenterId, version
     * @param converter the converter
     * @return closable registration
     */
    ObjectRegistration<ConvertorActionFromOFJava<Action, ActionPath>> registerActionConvertor(
            ActionSerializerKey<?> key, ConvertorActionFromOFJava<Action, ActionPath> converter);

    /**
     * Registers a message converter.
     *
     * @param key consists of: experimenter type, version
     * @param <I> experimenterMessageOfChoice
     * @param <O> DataContainer
     * @param <D> ConvertorData
     * @param converter TO OFJava (suitable for both: symmetric and multipart)
     * @return closeable registration
     */
    <I extends ExperimenterMessageOfChoice, O extends DataContainer, D extends ConvertorData>
        ObjectRegistration<ConverterMessageToOFJava<I, O, D>>
        registerMessageConvertor(TypeVersionKey<I> key, ConverterMessageToOFJava<I, O, D> converter);

    /**
     * Registers a message converter.
     *
     * @param key consists of: experimenter type, version
     * @param <I> experimenterMessageOfChoice
     * @param converter FROM OFJava (suitable for both: symmetric and multipart)
     * @return closeable registration
     */
    <I extends ExperimenterDataOfChoice> ObjectRegistration<ConvertorMessageFromOFJava<I, MessagePath>>
        registerMessageConvertor(MessageTypeKey<?> key, ConvertorMessageFromOFJava<I, MessagePath> converter);
}
