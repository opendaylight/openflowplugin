/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller;

/**
 * An adapter for the {@link ControllerStats} API, provided specifically for
 * unit tests to use, to insulate themselves from changes in the API.
 *
 * @author Simon Hunt
 */
public class ControllerStatsAdapter implements ControllerStats {
    @Override public long duration() { return 0; }
    @Override public long packetInCount() { return 0; }
    @Override public long packetInBytes() { return 0; }
    @Override public long packetOutCount() { return 0; }
    @Override public long packetOutBytes() { return 0; }
    @Override public long packetDropCount() { return 0; }
    @Override public long packetDropBytes() { return 0; }
    @Override public long msgRxCount() { return 0; }
    @Override public long msgTxCount() { return 0; }
}
