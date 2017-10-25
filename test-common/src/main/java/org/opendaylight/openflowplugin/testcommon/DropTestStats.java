/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.testcommon;

public class DropTestStats {
    private final int rcvd;
    private final int sent;
    private final int excs;
    private volatile int ftrSuccess;
    protected volatile int ftrFailed;
    private final int runablesExecuted;
    private final int runablesRejected;

    private final String message;

    public DropTestStats(int sent, int rcvd) {
        this.sent = sent;
        this.rcvd = rcvd;
        this.excs = 0;
        this.runablesExecuted = 0;
        this.message = null;
        runablesRejected = 0;
    }

    public DropTestStats(int sent, int rcvd, int excs) {
        this.sent = sent;
        this.rcvd = rcvd;
        this.excs = excs;
        this.message = null;
        this.runablesExecuted = 0;
        runablesRejected = 0;
    }

    public DropTestStats(int sent, int rcvd, int excs, int ftrFailed, int ftrSuccess, int runablesExecuted, int runablesRejected) {
        this.sent = sent;
        this.rcvd = rcvd;
        this.excs = excs;
        this.ftrFailed = ftrFailed;
        this.ftrSuccess = ftrSuccess;
        this.message = null;
        this.runablesExecuted = runablesExecuted;
        this.runablesRejected = runablesRejected;
    }

    public DropTestStats(String message) {
        this.sent = -1;
        this.rcvd = -1;
        this.excs = -1;
        this.message = message;
        this.runablesExecuted = -1;
        runablesRejected = 0;
    }

    public int getSent() {
        return this.sent;
    }

    public int getRcvd() {
        return this.rcvd;
    }

    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (this.message == null) {
            result.append("\n Rcvd:");
            result.append(this.rcvd);
            result.append("\n Sent: ");
            result.append(this.sent);
            result.append("\n Exceptions: ");
            result.append(this.excs);

            result.append("\n future success :");
            result.append(this.ftrSuccess);
            result.append("\n future failed :");
            result.append(this.ftrFailed);
            result.append("\n run() executions :");
            result.append(this.runablesExecuted);
            result.append("\n run() rejected :");
            result.append(this.runablesRejected);

        } else {
            result.append(this.message);
        }

        return result.toString();
    }

}
