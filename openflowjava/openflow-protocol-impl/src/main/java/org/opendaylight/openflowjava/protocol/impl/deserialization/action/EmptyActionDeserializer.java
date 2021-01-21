/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.deserialization.action;

import io.netty.buffer.ByteBuf;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.jdt.annotation.NonNull;
import org.kohsuke.MetaInfServices;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.util.ActionConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.ActionChoice;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlInCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.CopyTtlOutCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecMplsTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.DecNwTtlCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopPbbCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.PopVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.StripVlanCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Component;

/**
 * Common class for AbstractActionDeserializers which do not carry any data beyond the header.
 */
public final class EmptyActionDeserializer<T extends ActionChoice> extends AbstractActionDeserializer<T> {
    public EmptyActionDeserializer(final @NonNull T emptyChoice) {
        super(emptyChoice);
    }

    @Override
    public Action deserialize(final ByteBuf input) {
        input.skipBytes(ActionConstants.PADDING_IN_ACTION_HEADER);
        return deserializeHeader(input);
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class StripVlanCode implements OFProvider {
        @Inject
        public StripVlanCode() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_0;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.STRIP_VLAN_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new StripVlanCaseBuilder().build());
        }
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class CopyTtlIn implements OFProvider {
        @Inject
        public CopyTtlIn() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.COPY_TTL_IN_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new CopyTtlInCaseBuilder().build());
        }
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class CopyTtlOut implements OFProvider {
        @Inject
        public CopyTtlOut() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.COPY_TTL_OUT_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new CopyTtlOutCaseBuilder().build());
        }
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class DecMplsTtl implements OFProvider {
        @Inject
        public DecMplsTtl() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.DEC_MPLS_TTL_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new DecMplsTtlCaseBuilder().build());
        }
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class PopVlan implements OFProvider {
        @Inject
        public PopVlan() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.POP_VLAN_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new PopVlanCaseBuilder().build());
        }
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class DecNwTtl implements OFProvider {
        @Inject
        public DecNwTtl() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.DEC_NW_TTL_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new DecNwTtlCaseBuilder().build());
        }
    }

    @MetaInfServices
    @Singleton
    @Component
    public static final class PopPbb implements OFProvider {
        @Inject
        public PopPbb() {
            // Exposed for DI
        }

        @Override
        public Uint8 version() {
            return EncodeConstants.OF_VERSION_1_3;
        }

        @Override
        public Uint16 type() {
            return Uint16.valueOf(ActionConstants.POP_PBB_CODE);
        }

        @Override
        public ActionDeserializer deserializer() {
            return new EmptyActionDeserializer<>(new PopPbbCaseBuilder().build());
        }
    }
}
