/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import java.io.PrintStream;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 28.5.2015.
 */
@Command(scope = "ofp", name = "reset-session-stats", description = "Resets session statistics counters.")
public class ResetSessionStatsComandProvider extends OsgiCommandSupport {

    @Override
    protected Object doExecute() throws Exception {
        PrintStream out = session.getConsole();
        SessionStatistics.resetAllCounters();
        out.print("Session statistics counters reset.\n");
        return null;
    }
}
