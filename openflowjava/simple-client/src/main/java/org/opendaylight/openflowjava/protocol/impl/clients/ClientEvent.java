/*
 * Copyright (c) 2013 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.clients;

/**
 * Uniting interface used for scenario support
 * @author michal.polkorab
 *
 */
public interface ClientEvent {

    /**
     * Common method for triggering events
     * @return true if event executed successfully
     */
    boolean eventExecuted();
}
