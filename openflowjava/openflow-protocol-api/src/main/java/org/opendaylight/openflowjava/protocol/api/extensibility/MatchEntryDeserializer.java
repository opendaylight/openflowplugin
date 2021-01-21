/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public interface MatchEntryDeserializer extends HeaderDeserializer<MatchEntry>, OFDeserializer<MatchEntry> {
    public interface OFRegistry extends Immutable {
        public interface Versioned extends Immutable {
            /**
             * Lookup a match deserializer based on OpenFlow protocol version and class/field code.
             *
             * @param version OpenFlow protocol version
             * @return {@link MatchEntryDeserializer}, or null
             * @throws NullPointerException if any argument is null
             */
            // FIXME: use something else than int?
            @Nullable MatchEntryDeserializer lookupMatch(@NonNull Uint8 version, int oxmClass, int oxmField);

            @Nullable OFRegistry lookupMatchRegistry(@NonNull Uint8 version);
        }

        /**
         * Lookup an match deserializer based on class/field code.
         *
         * @param type Action type
         * @return {@link MatchEntryDeserializer}, or null
         * @throws NullPointerException if {@code type} is null
         */
        @Nullable MatchEntryDeserializer lookupMatch(int oxmClass, int oxmField);
    }

    public interface ExperimenterRegistry extends Immutable {
        public interface Versioned extends Immutable {
            /**
             * Lookup an match deserializer based on OpenFlow protocol version and action code.
             *
             * @param version OpenFlow protocol version
             * @param experimenter Experimenter ID
             * @return {@link ActionDeserializer}, or null
             * @throws NullPointerException if any argument is null
             */
            @Nullable MatchEntryDeserializer lookupMatch(@NonNull Uint8 version, int oxmField,
                @NonNull Uint32 experimenter);


            @Nullable ExperimenterRegistry lookupMatchRegistry(@NonNull Uint8 version);
        }

        /**
         * Lookup an action serializer based on experimenter and action code.
         *
         * @param experimenter Experimenter ID
         * @return {@link ActionDeserializer}, or null
         * @throws NullPointerException if {@code experimenter} is null
         */
        @Nullable MatchEntryDeserializer lookupMatch(int oxmField, @NonNull Uint32 experimenter);
    }

    public interface Provider extends Immutable {

        @NonNull Uint8 version();

        @NonNull MatchEntryDeserializer deserializer();

        int oxmField();
    }

    public interface OFProvider extends Provider {

        int oxmClass();
    }

    public interface ExperimenterProvider extends Provider {

        @NonNull Uint32 experimenter();
    }
}
