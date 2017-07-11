/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.mastership;

/**
 * Factory for mastership change service manager.
 * @see MastershipChangeServiceManager
 * @see MastershipChangeService
 * @since 0.5.0 Nitrogen
 */
public interface MastershipChangeServiceManagerFactory {

    /**
     * Creates instance of mastership change service manager.
     * @return new instance
     */
    MastershipChangeServiceManager newInstance();

}
