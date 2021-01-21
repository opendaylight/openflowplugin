/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.kohsuke.MetaInfServices;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.OFProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.OFRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.OFRegistry.Versioned;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;

@Beta
@MetaInfServices(value = Versioned.class)
public final class ImmutableActionDeserializerOFRegistry extends AbstractVersionedRegistry<OFRegistry>
        implements Versioned {
    private static final class ImmutableOFRegistry implements OFRegistry {
        private final ImmutableMap<Uint16, ActionDeserializer> typeToDeser;

        ImmutableOFRegistry(final Collection<OFProvider> providers) {
            typeToDeser = providers.stream().collect(
                ImmutableMap.toImmutableMap(OFProvider::type, OFProvider::deserializer));
        }

        @Override
        public ActionDeserializer lookupAction(final Uint16 code) {
            return typeToDeser.get(code);
        }
    }

    public ImmutableActionDeserializerOFRegistry() {
        this(ServiceLoader.load(OFProvider.class).stream().map(Supplier::get).collect(Collectors.toList()));
    }

    public ImmutableActionDeserializerOFRegistry(final Collection<OFProvider> providers) {
        super(Maps.transformValues(Multimaps.index(providers, OFProvider::version).asMap(), ImmutableOFRegistry::new));
    }

    @Override
    public ActionDeserializer lookupAction(final Uint8 version, final Uint16 type) {
        final OFRegistry registry = lookupItem(version);
        return registry == null ? null : registry.lookupAction(type);
    }
}
