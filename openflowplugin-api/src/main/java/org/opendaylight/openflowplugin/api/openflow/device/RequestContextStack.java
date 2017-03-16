/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.device;

import javax.annotation.Nullable;

/**
 * Request context stack.
 */
public interface RequestContextStack {
    /**
     * Method returns new request context for current request.
     * @return A request context, or null if one cannot be created.
     */
    @Nullable <T> RequestContext<T> createRequestContext();
}
