/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.opendaylight.of.lib.instr.Action;


/**
 * An adapter for {@link PacketOut} that provides default implementations
 * of the event methods. This class can be extended as an alternative to
 * implementing <em>PacketOut</em> directly, allowing subclasses to rely on
 * the default behavior of methods they don't want to implement themselves.
 *
 * @author Shaun Wackerly
 */
public class PacketOutAdapter implements PacketOut {
    @Override public void addAction(Action action) { }
    @Override public void clearActions() { }
    @Override public void block() { }
    @Override public void send() { }
}
