/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.extension.hpe.rev151112;

import org.opendaylight.openflowplugin.extensions.hpe.HpeExtensionProviderImpl;

public class HpeExtensionProviderModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.extension.hpe.rev151112.AbstractHpeExtensionProviderModule {
    public HpeExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public HpeExtensionProviderModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.extension.hpe.rev151112.HpeExtensionProviderModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        final HpeExtensionProviderImpl hpeExtensionProvider = new HpeExtensionProviderImpl(
                getOpenflowSwitchConnectionProviderDependency(),
                getOpenflowPluginExtensionRegistryDependency());
        hpeExtensionProvider.start();
        return new AutoCloseable() {
            @Override
            public void close() throws Exception {
                hpeExtensionProvider.stop();
            }
        };
    }

}
