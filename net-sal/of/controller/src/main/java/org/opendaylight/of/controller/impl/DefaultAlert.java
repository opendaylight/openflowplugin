/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.Alert;
import org.opendaylight.util.TimeUtils;

import java.util.Date;

/**
 * A default implementation of {@link Alert}.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
class DefaultAlert implements Alert {
    static TimeUtils TIME = TimeUtils.getInstance();

    private final long ts;
    private final Severity sev;
    private final String desc;

    /** Constructs an alert with the specified severity and description.
     * Note that the timestamp will be set to "currentTimeMillis()".
     *
     * @param sev the alert severity
     * @param desc the alert description
     */
    public DefaultAlert(Severity sev, String desc) {
        this.ts = TIME.currentTimeMillis();
        this.sev = sev;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "{" + new Date(ts) + "," + sev + ",\"" + desc + "\"}";
    }

    @Override
    public long ts() {
        return ts;
    }

    @Override
    public Severity severity() {
        return sev;
    }

    @Override
    public String description() {
        return desc;
    }
}
