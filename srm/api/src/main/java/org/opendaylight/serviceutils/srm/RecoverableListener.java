/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.serviceutils.srm;

public interface RecoverableListener {
    /**
     * register a recoverable listener.
     */
    void registerListener();

    /**
     * Deregister a recoverable listener.
     */
    void deregisterListener();
}