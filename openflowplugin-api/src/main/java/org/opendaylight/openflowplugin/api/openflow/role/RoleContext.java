/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.role;

import com.google.common.collect.ClassToInstanceMap;
import org.opendaylight.openflowplugin.api.openflow.OFPContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.yangtools.yang.binding.Rpc;

/**
 * Handles propagation of SLAVE and MASTER roles on connected devices.
 */
public interface RoleContext extends OFPContext, RequestContextStack {
    /**
     * Sets role service rpc implementation.
     *
     * @param salRoleServiceMap the sal role service map
     */
    void setRoleServiceMap(ClassToInstanceMap<Rpc<?, ?>> salRoleServiceMap);
}