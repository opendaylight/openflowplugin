/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.openflowjava.protocol.api.extensibility.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.extensibility.keys.MatchEntrySerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ActionBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Clazz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;

/**
 * registration place for message converters provided by vendor extensions
 */
public interface ExtensionConverterRegistrator {

    public AutoCloseable registerActionConvertor(
            ConverterExtensionKey<? extends ExtensionKey> key,
            ConvertorToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action> convertor);

    public AutoCloseable registerActionConvertor(
            ActionSerializerKey<? extends ActionBase> key,
            ConvertorFromOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action, Action> convertor);

    public AutoCloseable registerMatchConvertor(ConverterExtensionKey<? extends ExtensionKey> key,
            ConvertorToOFJava<Match, MatchEntries> convertor);

    public AutoCloseable registerMatchConvertor(MatchEntrySerializerKey<? extends Clazz, ? extends MatchField> key,
            ConvertorFromOFJava<MatchEntries, Match> convertor);

}
