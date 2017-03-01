/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.TableFeaturesPropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.table.feature.prop.type.ApplyActions;

public class ApplyActionsTablePropertySerializer extends AbstractTablePropertySerializer<ApplyActions> implements SerializerRegistryInjector {

    private SerializerRegistry registry;

    @Override
    protected void serializeProperty(final ApplyActions property, final ByteBuf byteBuf) {
        property
            .getApplyActions()
            .getAction()
            .stream()
            .map(Action::getAction)
            .forEach(action -> ActionUtil
                .writeActionHeader(
                    action,
                    EncodeConstants.OF13_VERSION_ID,
                    registry,
                    byteBuf));
    }

    @Override
    protected TableFeaturesPropType getType() {
        return TableFeaturesPropType.OFPTFPTAPPLYACTIONS;
    }

    @Override
    protected Class<ApplyActions> getClazz() {
        return ApplyActions.class;
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }

}
