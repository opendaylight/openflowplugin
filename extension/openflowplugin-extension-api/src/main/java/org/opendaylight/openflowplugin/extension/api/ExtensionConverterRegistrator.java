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
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yangtools.concepts.ObjectRegistration;

/**
 * registration place for message converters provided by vendor extensions
 */
public interface ExtensionConverterRegistrator {

    ObjectRegistration<ConvertorToOFJava<Action>> registerActionConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key, ConvertorToOFJava<Action> convertor);

    ObjectRegistration<ConvertorFromOFJava<Action, ActionPath>> registerActionConvertor(
            ActionSerializerKey<? extends ActionBase> key, ConvertorFromOFJava<Action, ActionPath> convertor);

    ObjectRegistration<ConvertorToOFJava<MatchEntries>> registerMatchConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key, ConvertorToOFJava<MatchEntries> convertor);

    ObjectRegistration<ConvertorFromOFJava<MatchEntries, MatchPath>> registerMatchConvertor(
            MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            ConvertorFromOFJava<MatchEntries, MatchPath> convertor);

}
