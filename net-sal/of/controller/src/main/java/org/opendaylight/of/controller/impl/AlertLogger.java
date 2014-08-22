/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.controller.Alert;
import org.opendaylight.of.controller.AlertSink;
import org.opendaylight.of.controller.flow.FlowTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides simple logging of alerts.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
class AlertLogger implements AlertSink {

    private final Logger log = LoggerFactory.getLogger(FlowTracker.class);
    private static final String ALERT = "Alert: {}";

    @Override
    public void postAlert(Alert alert) {
        switch (alert.severity()) {
            case CRITICAL:
                log.error(ALERT, alert);
                break;
            case WARNING:
                log.warn(ALERT, alert);
                break;
            case INFO:
                log.info(ALERT, alert);
                break;
        }
    }
}
