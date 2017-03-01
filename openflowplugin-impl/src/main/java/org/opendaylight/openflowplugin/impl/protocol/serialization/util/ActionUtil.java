/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.util;

import com.google.common.collect.Ordering;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import org.opendaylight.openflowjava.protocol.api.extensibility.HeaderSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowjava.protocol.api.extensibility.SerializerRegistry;
import org.opendaylight.openflowjava.protocol.api.keys.MessageTypeKey;
import org.opendaylight.openflowjava.protocol.impl.util.TypeKeyMakerFactory;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionToOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.TypeVersionKey;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.OrderComparator;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for action serialization
 */
public class ActionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(ActionUtil.class);

    private static final Ordering<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> ACTION_ORDERING =
            Ordering.from(OrderComparator.<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action>build());

    /**
     * Sort actions based on order
     * @param actions actions
     * @return sorted copy of actions
     */
    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> sortActions(final Iterable<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action> actions) {
        return ACTION_ORDERING.sortedCopy(actions);
    }

    /**
     * Serialize OpenFlow action, using extension converter if available
     * TODO: Remove also extension converters
     *
     * @param action    OpenFlowPlugin action
     * @param version   OpenFlow version
     * @param registry  serializer registry
     * @param outBuffer output buffer
     */
    @SuppressWarnings("unchecked")
    public static void writeAction(Action action, short version, SerializerRegistry registry, ByteBuf outBuffer) {
        try {
            Optional.ofNullable(OFSessionUtil.getExtensionConvertorProvider())
                .flatMap(provider ->
                    (GeneralExtensionGrouping.class.isInstance(action)
                        ? convertExtensionGrouping(provider, action, version)
                        : convertGenericAction(provider, action, version))
                        .map(ofjAction -> {
                            final OFSerializer<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                .action.rev150203.actions.grouping.Action> serializer = registry
                                .getSerializer(TypeKeyMakerFactory.createActionKeyMaker(version)
                                    .make(ofjAction));

                            serializer.serialize(ofjAction, outBuffer);
                            return action;
                        })
                ).orElseGet(() -> {
                final OFSerializer<Action> serializer = registry.getSerializer(
                    new MessageTypeKey<>(
                        version, (Class<? extends Action>) action.getImplementedInterface()));

                serializer.serialize(action, outBuffer);
                return action;
            });
        } catch (final IllegalStateException | ClassCastException e) {
            LOG.warn("Serializer for action {} for version {} not found.", action.getImplementedInterface(), version);
        }
    }

    /**
     * Serialize OpenFlow action header, using extension converter if available
     * TODO: Remove also extension converters
     *
     * @param action    OpenFlowPlugin action
     * @param version   OpenFlow version
     * @param registry  serializer registry
     * @param outBuffer output buffer
     */
    @SuppressWarnings("unchecked")
    public static void writeActionHeader(Action action, short version, SerializerRegistry registry, ByteBuf outBuffer) {
        try {
            Optional.ofNullable(OFSessionUtil.getExtensionConvertorProvider())
                .flatMap(provider ->
                    (GeneralExtensionGrouping.class.isInstance(action)
                        ? convertExtensionGrouping(provider, action, version)
                        : convertGenericAction(provider, action, version))
                        .map(ofjAction -> {
                            final HeaderSerializer<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common
                                .action.rev150203.actions.grouping.Action> serializer = registry
                                .getSerializer(TypeKeyMakerFactory.createActionKeyMaker(version)
                                    .make(ofjAction));

                            serializer.serializeHeader(ofjAction, outBuffer);
                            return action;
                        })
                ).orElseGet(() -> {
                final HeaderSerializer<Action> serializer = registry.getSerializer(
                    new MessageTypeKey<>(
                        version, (Class<? extends Action>) action.getImplementedInterface()));

                serializer.serializeHeader(action, outBuffer);
                return action;
            });
        } catch (final IllegalStateException | ClassCastException e) {
            LOG.warn("Header Serializer for action {} for version {} not found.", action.getImplementedInterface(), version);
        }
    }

    /**
     * Try to convert action that implements #{@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionGrouping}
     * to OpenFlowJava action
     *
     * @param provider extension converter provider
     * @param action OpenFlowPlugin action
     * @param version OpenFlow version
     * @return optional OpenFlowJava action
     */
    private static Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions
            .grouping.Action> convertExtensionGrouping(final ExtensionConverterProvider provider,
                                                       final Action action,
                                                       final short version) {
        final ConverterExtensionKey<? extends ExtensionKey> key =
                new ConverterExtensionKey<>(GeneralExtensionGrouping.class.cast(action).getExtensionKey(), version);

        final ConvertorToOFJava<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions
                .grouping.Action> converter = provider.getConverter(key);

        return Optional.ofNullable(converter).map(c -> c.convert(((GeneralExtensionGrouping) action).getExtension()));
    }

    /**
     * Try to convert generic OpenFlowPlugin action to OpenFlowJava action
     *
     * @param provider extension converter provider
     * @param action OpenFlowPlugin action
     * @param version OpenFlow version
     * @return optional OpenFlowJava action
     */
    @SuppressWarnings("unchecked")
    private static Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions
            .grouping.Action> convertGenericAction(final ExtensionConverterProvider provider,
                                                   final Action action,
                                                   final short version) {

        final TypeVersionKey<Action> key =
                new TypeVersionKey<>((Class<? extends Action>) action.getImplementedInterface(), version);

        final ConvertorActionToOFJava<Action, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action
                .rev150203.actions.grouping.Action> converter = provider.getConverter(key);


        return Optional.ofNullable(converter).map(c -> c.convert(action));

    }
}
