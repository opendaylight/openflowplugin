/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.apache.felix.scr.annotations.*;
import org.opendaylight.of.controller.*;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.util.event.EventDispatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenFlow controller ignition component.
 *
 * @author Thomas Vachuska
 */
@Component(immediate = true)
@Service
public class ControllerManagerComponent extends ControllerManager implements ControllerService {

    private final Logger log = LoggerFactory.getLogger(OpenflowController.class);

    private static final String MSG_ALERT = "ALERT: {} {}";
    private static final String MSG_STARTED = "OpenFlow controller started";
    private static final String MSG_STOPPED = "OpenFlow controller stopped";

    // FIXME: inject ControllerConfig later

    // TODO: inject these later, but for now these are placeholders
    private volatile RoleAdvisor roleAdvisor = new SimpleRoleAdvisor();
    private volatile AlertSink alertSink = new LoggingAlertSink();

    // External dependencies
    @Reference(name = "EventDispatchService", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected volatile EventDispatchService eds;

    @Reference(name = "PostHandshakeSink", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected volatile PostHandshakeSink postHandshakeSink;

    @Reference(name = "FlowModAdvisor", policy = ReferencePolicy.DYNAMIC,
               cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected volatile FlowModAdvisor flowModAdvisor;

    private ControllerConfig cfg = new ControllerConfig.Builder().build();

    @Activate
    public void activate() {
        log.info(MSG_STARTED);
        init(cfg, alertSink, postHandshakeSink, flowModAdvisor, roleAdvisor, eds);
        startIOProcessing();
    }

    @Deactivate
    public void deactivate() {
        log.info(MSG_STOPPED);
    }

    // Role advisor that always claims mastership over a datapath.
    private class SimpleRoleAdvisor implements RoleAdvisor {
        @Override
        public boolean isMasterFor(DataPathId dpid) {
            return true;
        }
    }

    // Alert sink that sends alerts to support log.
    private class LoggingAlertSink implements AlertSink {
        @Override
        public void postAlert(Alert alert) {
            log.info(MSG_ALERT, alert.severity(), alert.description());
        }
    }

}
