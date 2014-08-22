/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.CheckpointEvent;
import org.opendaylight.of.controller.OpenflowEventType;

/**
 * A checkpoint message event.
 * <p>
 * This event is used by the Management API to insert checkpoints into the
 * message TX/RX stream. The checkpoint includes a textual description.
 *
 * @author Simon Hunt
 */
class CheckpointEvt extends MessageEvt implements CheckpointEvent {
    private final Code code;
    private final String text;

    /** Constructs a checkpoint message event.
     *
     * @param code event code
     * @param text the textual description
     */
    CheckpointEvt(Code code, String text) {
        super(OpenflowEventType.MX_CHECKPOINT, null, null, 0, null);
        this.code = code;
        this.text = text;
    }

    @Override
    public Code code() {
        return code;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int i = sb.indexOf(PV_LABEL);
        int len = sb.length();
        sb.replace(i + 1, len, code().name())
                .append(",\"").append(text).append("\"}");
        return sb.toString();
    }
}
