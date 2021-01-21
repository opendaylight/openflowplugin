/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

@Beta
public interface ActionDeserializer extends OFDeserializer<Action>, HeaderDeserializer<Action> {
    public interface OFRegistry extends Immutable {
        public interface Versioned extends Immutable {
            /**
             * Lookup an action serializer based on OpenFlow protocol version and action code.
             *
             * @param version OpenFlow protocol version
             * @param type Action type
             * @return {@link ActionDeserializer}, or null
             * @throws NullPointerException if any argument is null
             */
            @Nullable ActionDeserializer lookupAction(@NonNull Uint8 version, @NonNull Uint16 type);
        }

        /**
         * Lookup an action serializer based on action code.
         *
         * @param type Action type
         * @return {@link ActionDeserializer}, or null
         * @throws NullPointerException if {@code type} is null
         */
        @Nullable ActionDeserializer lookupAction(@NonNull Uint16 type);
    }

    public interface ExperimenterRegistry extends Immutable {
        public interface Versioned extends Immutable {
            /**
             * Lookup an action serializer based on OpenFlow protocol version and action code.
             *
             * @param version OpenFlow protocol version
             * @param experimenter Experimenter ID
             * @return {@link ActionDeserializer}, or null
             * @throws NullPointerException if any argument is null
             */
            @Nullable ActionDeserializer lookupAction(@NonNull Uint8 version, @NonNull Uint32 experimenter);
        }

        /**
         * Lookup an action serializer based on experimenter and action code.
         *
         * @param experimenter Experimenter ID
         * @return {@link ActionDeserializer}, or null
         * @throws NullPointerException if {@code experimenter} is null
         */
        @Nullable ActionDeserializer lookupAction(@NonNull Uint32 experimenter);
    }

    public interface Provider extends Immutable {

        @NonNull Uint8 version();

        @NonNull ActionDeserializer deserializer();
    }

    public interface OFProvider extends Provider {

        @NonNull Uint16 type();
    }

    public interface ExperimenterProvider extends Provider {

        @NonNull Uint32 experimenter();
    }
}
