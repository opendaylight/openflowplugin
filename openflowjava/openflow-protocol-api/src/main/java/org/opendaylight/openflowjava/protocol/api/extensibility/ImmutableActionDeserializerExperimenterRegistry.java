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
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.ExperimenterProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.ExperimenterRegistry;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.ExperimenterRegistry.Versioned;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

@Beta
@MetaInfServices(value = Versioned.class)
public final class ImmutableActionDeserializerExperimenterRegistry
        extends AbstractVersionedRegistry<ExperimenterRegistry> implements Versioned {
    @Beta
    private static final class ImmutableExperimenterRegistry implements ExperimenterRegistry {
        private final ImmutableMap<Uint32, ActionDeserializer> expToDeser;

        ImmutableExperimenterRegistry(final Collection<ExperimenterProvider> providers) {
            expToDeser = providers.stream().collect(
                ImmutableMap.toImmutableMap(ExperimenterProvider::experimenter, ExperimenterProvider::deserializer));
        }

        @Override
        public ActionDeserializer lookupAction(final Uint32 experimenter) {
            return expToDeser.get(experimenter);
        }
    }

    public ImmutableActionDeserializerExperimenterRegistry() {
        this(ServiceLoader.load(ExperimenterProvider.class).stream().map(Supplier::get).collect(Collectors.toList()));
    }

    public ImmutableActionDeserializerExperimenterRegistry(final Collection<ExperimenterProvider> providers) {
        super(Maps.transformValues(Multimaps.index(providers, ExperimenterProvider::version).asMap(),
            ImmutableExperimenterRegistry::new));
    }

    @Override
    public ActionDeserializer lookupAction(final Uint8 version, final Uint32 experimenter) {
        final ExperimenterRegistry registry = lookupItem(version);
        return registry == null ? null : registry.lookupAction(experimenter);
    }
}
