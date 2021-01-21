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
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.OFProvider;
import org.opendaylight.openflowjava.protocol.api.extensibility.ActionDeserializer.OFRegistry.Versioned;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Your basic OSGi whiteboard. We index {@link OFProvider}s based on exposed values and create a fixed lookup.
 */
@Component
public final class OSGiActionDeserializerOFRegistry implements Versioned {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiActionDeserializerOFRegistry.class);

    @Reference(policyOption = ReferencePolicyOption.GREEDY)
    List<OFProvider> providers;

    private ImmutableActionDeserializerOFRegistry delegate = null;

    @Override
    public ActionDeserializer lookupAction(final Uint8 version, final Uint16 type) {
        return delegate().lookupAction(version, type);
    }

    @Activate
    void activate() {
        delegate = new ImmutableActionDeserializerOFRegistry(providers);
        LOG.info("OpenFlow action deserializer registry activated with {} providers", providers.size());
    }

    @Deactivate
    void deactivate() {
        delegate = null;
        LOG.info("OpenFlow action deserializer registry deactivated");
    }

    private Versioned delegate() {
        return verifyNotNull(delegate);
    }
}
