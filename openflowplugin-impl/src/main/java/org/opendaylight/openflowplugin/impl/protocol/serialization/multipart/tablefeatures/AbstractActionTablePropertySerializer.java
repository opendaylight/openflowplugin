/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart.tablefeatures;

import io.netty.buffer.ByteBuf;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistryInjector;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.impl.protocol.serialization.util.ActionUtil;
import org.opendaylight.openflowplugin.protocol.converter.common.OrderComparator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.ActionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;

public abstract class AbstractActionTablePropertySerializer<T extends TableFeaturePropType>
        extends AbstractTablePropertySerializer<T> implements SerializerRegistryInjector {

    private final ExtensionConverterProvider extensionConverterProvider;
    private SerializerRegistry registry;

    public AbstractActionTablePropertySerializer(final ExtensionConverterProvider extensionConverterProvider) {
        this.extensionConverterProvider = extensionConverterProvider;
    }

    /**
     * Try to write list of OpenFlowPlugin actions to output buffer
     * @param actions List of OpenFlowPlugin actions
     * @param outBuffer output buffer
     */
    protected void writeActions(ActionList actions, short version, ByteBuf outBuffer) {
        Optional.ofNullable(actions).flatMap(as -> Optional.ofNullable(as.getAction())).ifPresent(as -> as
                .stream().sorted(OrderComparator.build()).forEach(a -> ActionUtil
                        .writeActionHeader(a.getAction(), version, registry, outBuffer, extensionConverterProvider)));
    }

    @Override
    public void injectSerializerRegistry(final SerializerRegistry serializerRegistry) {
        registry = serializerRegistry;
    }
}
