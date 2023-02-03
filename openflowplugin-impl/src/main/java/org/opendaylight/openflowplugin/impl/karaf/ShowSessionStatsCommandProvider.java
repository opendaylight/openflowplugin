/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.karaf;

import java.io.PrintStream;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.SessionStatistics;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 21.5.2015.
 */
@Service
@Command(scope = "ofp", name = "show-session-stats", description = "Show session statistics.")
public class ShowSessionStatsCommandProvider extends AbstractAction {
    @Override
    void execute(final PrintStream out) {
        final var sb = new StringBuilder();
        // FIXME: static wiring
        for (var line : SessionStatistics.provideStatistics()) {
            sb.append(line).append('\n');
        }
        out.print(sb.toString());
    }
}
