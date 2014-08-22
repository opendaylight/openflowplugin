/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.of.lib.dt.DataPathId;

/**
 * Provides an abstraction of the "Role Service".
 *
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public interface RoleAdvisor {

    /**
     * Returns {@code true} if the local controller instance has the
     * <em>Master</em> role for the specified datapath; {@code false} otherwise.
     *
     * @param dpid the target datapath
     * @return {@code true} if this controller is <em>Master</em>;
     *          {@code false} otherwise
     */
    boolean isMasterFor(DataPathId dpid);
}
