/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.DataPathEvent;
import org.opendaylight.of.controller.DataPathListener;
import org.opendaylight.of.controller.QueueEvent;

/**
 * Adapter for DataPathListener.
 *
 * @author Simon Hunt
 */
public class DataPathListenerAdapter implements DataPathListener {
    @Override public void queueEvent(QueueEvent event) { }
    @Override public void event(DataPathEvent event) { }
}
