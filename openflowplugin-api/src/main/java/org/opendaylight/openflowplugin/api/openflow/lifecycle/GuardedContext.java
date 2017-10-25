/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.lifecycle;

import com.google.common.util.concurrent.Service;
import java.util.function.Function;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;

/**
 * Stateful OpenFlow context wrapper.
 */
public interface GuardedContext extends OFPContext {
    /**
     * Returns the lifecycle state of the service.
     *
     * @return the service state
     */
    Service.State state();

    /**
     * Maps delegate inside guarded context to T.
     *
     * @param <T>         the type parameter
     * @param transformer the transformer
     * @return the t
     */
    <T> T map(Function<OFPContext, T> transformer);
}