/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.karaf;

import java.io.PrintStream;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageIntelligenceAgency;

@Command(scope = "ofp", name = "clearStats", description = "Clear openflow statistics.")
public class ClearStatsCommandProvider extends OsgiCommandSupport {
    @Reference
    MessageIntelligenceAgency messageIntelligenceAgency;

    @Override
    protected Object doExecute() {
        messageIntelligenceAgency.resetStatistics();
        PrintStream out = session.getConsole();
        out.print("Openflow plugin statistics cleaned.\n");
        return null;
    }
}
