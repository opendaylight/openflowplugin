/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.of.controller;

import org.opendaylight.util.event.TypedEvent;

/**
 * An OpenFlow Controller event.
 * <p>
 * This is the super-interface to all events, defining "when it happened"
 * and "what happened".
 *
 * @author Simon Hunt
 */
public interface OpenflowEvent extends TypedEvent<OpenflowEventType> {
}
