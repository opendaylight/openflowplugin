/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.ErrorEvent;
import org.opendaylight.of.controller.OpenflowEventType;

/**
 * An error event.
 *
 * @author Simon Hunt
 */
class ErrorEvt extends OpenflowEvt implements ErrorEvent {

    private final String text;
    private final Throwable cause;
    private final Object context;

    ErrorEvt(String text, Throwable cause, Object context) {
        super(OpenflowEventType.ERROR);
        this.text = text == null ? "" : text;
        this.cause = cause;
        this.context = context;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int n = sb.length();
        sb.replace(n-1, n, ",text=\"").append(text)
                .append("\",cause=").append(cause)
                .append("\",context=").append(context)
                .append("}");
        return sb.toString();
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    @Override
    public Object context() {
        return context;
    }
}
