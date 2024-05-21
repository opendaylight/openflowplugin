/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import java.io.PrintStream;
import java.util.List;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;

@Command(scope = "ofp", name = "showStats", description = "Show openflow statistics.")
public class ShowStatsCommandProvider extends OsgiCommandSupport {
    @Reference
    MessageIntelligenceAgency messageIntelligenceAgency;

    @Override
    protected Object doExecute() {
        PrintStream out = session.getConsole();
        final List<String> statistics = messageIntelligenceAgency.provideIntelligence();
        final StringBuilder result = new StringBuilder();
        for (String line : statistics) {
            result.append(line);
            result.append("\n");
        }
        out.print(result.toString());
        return null;
    }
}
