/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.nx.api;

import org.opendaylight.openflowjava.protocol.api.extensibility.OFDeserializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntryDeserializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.MatchEntrySerializerKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;

/**
 * @author msunal
 *
 */
public interface NiciraExtensionCodecRegistrator {

    void registerActionDeserializer(NiciraActionDeserializerKey key, OFDeserializer<Action> deserializer);

    void unregisterActionDeserializer(NiciraActionDeserializerKey key);

    void registerActionSerializer(NiciraActionSerializerKey key, OFSerializer<Action> serializer);

    void unregisterActionSerializer(NiciraActionSerializerKey key);

    void registerMatchEntryDeserializer(MatchEntryDeserializerKey key, OFDeserializer<MatchEntries> deserializer);

    void unregisterMatchEntryDeserializer(MatchEntryDeserializerKey key);

    void registerMatchEntrySerializer(MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key,
            OFSerializer<MatchEntries> serializer);

    void unregisterMatchEntrySerializer(MatchEntrySerializerKey<? extends OxmClassBase, ? extends MatchField> key);

}
