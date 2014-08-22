/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.ListenerEvent;
import org.opendaylight.of.controller.OpenflowEventType;
import org.opendaylight.of.controller.OpenflowListener;

/**
 * A listener event.
 *
 * @author Simon Hunt
 */
class ListenerEvt extends OpenflowEvt implements ListenerEvent {

    private final OpenflowListener<?> listener;

    ListenerEvt(OpenflowEventType type, OpenflowListener<?> listener) {
        super(type);
        this.listener = listener;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        sb.replace(n-1,n,",lstr=").append(listener.getClass().getSimpleName())
                .append("}");
        return sb.toString();
    }

    @Override
    public OpenflowListener<?> listener() {
        return listener;
    }
}
