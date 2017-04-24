/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.api.openflow.lifecycle;

/**
 * Holds information about context chain state
 */
public interface ContextChainStateHolder {

    /**
     * Device need to be in state SLAVE or MASTER.
     * @return false if in undefined state
     */
    boolean hasState();

}
