/*
 * Copyright (c) 2024 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Session;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 28.5.2015.
 */
@Command(scope = "ofp", name = "reset-session-stats", description = "Resets session statistics counters.")
@Service
public class ResetSessionStatsCommand implements Action {

    @Reference
    Session session;

    @Override
    public Object execute() {
        SessionStatistics.resetAllCounters();
        session.getConsole().println("Session statistics counters reset.");
        return null;
    }
}
