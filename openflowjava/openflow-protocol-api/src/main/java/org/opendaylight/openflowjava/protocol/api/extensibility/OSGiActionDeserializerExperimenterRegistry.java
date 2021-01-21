/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.api.extensibility;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.List;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.ExperimenterProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.ExperimenterRegistry.Versioned;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Your basic OSGi whiteboard. We index {@link ExperimenterProvider}s based on exposed values and create a fixed lookup.
 */
@Component
public final class OSGiActionDeserializerExperimenterRegistry implements Versioned {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiActionDeserializerExperimenterRegistry.class);

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    List<ExperimenterProvider> providers;

    private Versioned delegate = null;

    @Override
    public ActionDeserializer lookupAction(final Uint8 version, final Uint32 experimenter) {
        return delegate().lookupAction(version, experimenter);
    }

    @Activate
    void activate() {
        delegate = new ImmutableActionDeserializerExperimenterRegistry(providers);
        LOG.info("Experimenter action deserializer registry activated with {} providers", providers.size());
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("Experimenter action deserializer registry deactivated");
    }

    private Versioned delegate() {
        return verifyNotNull(delegate);
    }
}
