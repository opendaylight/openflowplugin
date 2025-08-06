/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.mdsal;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowjava.protocol.impl.core.SwitchConnectionProviderImpl;
import org.opendaylight.openflowjava.protocol.spi.connection.SwitchConnectionProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow._switch.connection.config.rev160506.SwitchConnectionConfig;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.concepts.Registration;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A component exposing {@link SwitchConnectionProvider} into OSGi service registry based on MD-SAL's configuration
 * data store contents of {@link SwitchConnectionConfig}.
 */
@Component(service = { })
public final class OSGiSwitchConnectionProviders implements DataTreeChangeListener<SwitchConnectionConfig> {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiSwitchConnectionProviders.class);

    private final Map<String, ComponentInstance<SwitchConnectionProvider>> instances = new HashMap<>();
    private final ComponentFactory<SwitchConnectionProvider> providerFactory;
    private final Registration reg;

    @Activate
    public OSGiSwitchConnectionProviders(@Reference final DataBroker dataBroker,
            @Reference(target = "(component.factory=" + SwitchConnectionProviderImpl.FACTORY_NAME + ")")
            final ComponentFactory<SwitchConnectionProvider> providerFactory) {
        this.providerFactory = requireNonNull(providerFactory);
        reg = dataBroker.registerTreeChangeListener(LogicalDatastoreType.CONFIGURATION,
            DataObjectIdentifier.builder(SwitchConnectionConfig.class).build(), this);
        LOG.info("MD-SAL configuration-based SwitchConnectionProviders started");
    }

    @Deactivate
    synchronized void deactivate() {
        LOG.info("MD-SAL configuration-based SwitchConnectionProviders stopping");
        reg.close();
        instances.forEach((key, instance) -> instance.dispose());
        instances.clear();
        LOG.info("MD-SAL configuration-based SwitchConnectionProviders stopped");
    }

    @Override
    public synchronized void onDataTreeChanged(final List<DataTreeModification<SwitchConnectionConfig>> changes) {
        final var apply = new HashMap<String, SwitchConnectionConfig>();

        for (var change : changes) {
            final var root = change.getRootNode();
            switch (root.modificationType()) {
                case null -> throw new NullPointerException();
                case DELETE -> apply.put(root.dataBefore().getInstanceName(), null);
                case SUBTREE_MODIFIED, WRITE -> {
                    final var after = root.dataAfter();
                    apply.put(after.getInstanceName(), after);
                }
            }
        }

        LOG.debug("Applying {} changes", apply.size());
        apply.entrySet().stream()
            .sorted(Comparator.comparing(Entry::getKey))
            .forEach(entry -> {
                final var type = entry.getKey();
                final var prev = instances.remove(type);
                if (prev != null) {
                    LOG.info("Stopping instance of type '{}'", type);
                    prev.dispose();
                }

                final var config = entry.getValue();
                if (config != null) {
                    LOG.info("Starting instance of type '{}'", type);
                    instances.put(type, providerFactory.newInstance(FrameworkUtil.asDictionary(props(type, config))));
                }
            });
    }

    @Override
    public void onInitialData() {
        LOG.debug("No configuration is present");
    }

    private static Map<String, Object> props(final String type, final @Nullable SwitchConnectionConfig config) {
        return config != null ? Map.of("type", type, SwitchConnectionProviderImpl.PROP_CONFIG, config)
            : Map.of("type", type);
    }
}
