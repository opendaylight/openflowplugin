/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.MessageEvent;
import org.opendaylight.of.controller.MessageListener;
import org.opendaylight.of.controller.QueueEvent;

/**
 * Adapter for MessageListener.
 *
 * @author Simon Hunt
 */
public class MessageListenerAdapter implements MessageListener {
    @Override public void queueEvent(QueueEvent event) { }
    @Override public void event(MessageEvent event) { }
}
