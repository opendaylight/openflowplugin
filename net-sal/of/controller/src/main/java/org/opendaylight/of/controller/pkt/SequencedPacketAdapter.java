/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pkt;

import org.opendaylight.of.controller.ErrorEvent;

/**
 * An adapter for {@link SequencedPacketListener} that provides default
 * implementations of the event methods. This class can be extended as an
 * alternative to implementing <em>SequencedPacketListener</em> directly,
 * allowing subclasses to rely on the default behavior of methods they don't
 * want to implement themselves.
 *
 * @author Simon Hunt
 */
public class SequencedPacketAdapter implements SequencedPacketListener {
    @Override public void event(MessageContext context) { }
    @Override public void errorEvent(ErrorEvent event) { }
}
