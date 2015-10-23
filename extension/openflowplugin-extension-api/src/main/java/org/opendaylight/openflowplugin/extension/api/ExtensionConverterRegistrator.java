/**
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
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * registration place for message converters provided by vendor extension
 */
public interface ExtensionConverterRegistrator {


    ObjectRegistration<ConvertorToOFJava<MatchEntry>> registerMatchConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key, ConvertorToOFJava<MatchEntry> convertor);

    ObjectRegistration<ConvertorFromOFJava<MatchEntry, MatchPath>> registerMatchConvertor(
            MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            ConvertorFromOFJava<MatchEntry, MatchPath> convertor);

    /**
     * @param key       action case type + ofp-version
     * @param convertor
     * @return closable registration
     */
    ObjectRegistration<ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action>>
    registerActionConvertor(
            TypeVersionKey<? extends org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> key,
            ConvertorActionToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action, Action> convertor);

    /**
     * @param key       actionSubType, action type (Experimenter), experimenterId, version
     * @param convertor
     * @return closable registration
     */
    ObjectRegistration<ConvertorActionFromOFJava<Action, ActionPath>> registerActionConvertor(
            ActionSerializerKey<?> key, ConvertorActionFromOFJava<Action, ActionPath> convertor);

    /**
     * @param key       consists of: experimenter type, version
     * @param convertor TO OFJava (suitable for both: symmetric and multipart)
     * @return closeable registration
     */
    <I extends ExperimenterMessageOfChoice, O extends DataContainer> ObjectRegistration<ConvertorMessageToOFJava<I, O>> registerMessageConvertor(
            TypeVersionKey<I> key, ConvertorMessageToOFJava<I, O> convertor);

    /**
     * @param key       consists of: experimenter type, version
     * @param convertor FROM OFJava (suitable for both: symmetric and multipart)
     * @return closeable registration
     */
    <I extends ExperimenterDataOfChoice> ObjectRegistration<ConvertorMessageFromOFJava<I, MessagePath>> registerMessageConvertor(
            MessageTypeKey<?> key, ConvertorMessageFromOFJava<I, MessagePath> convertor);
}
