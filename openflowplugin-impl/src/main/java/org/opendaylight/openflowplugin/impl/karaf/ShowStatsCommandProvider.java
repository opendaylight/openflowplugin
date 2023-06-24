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
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;

@Service
@Command(scope = "ofp", name = "showStats", description = "Show openflow statistics.")
public class ShowStatsCommandProvider extends AbstractAction {
    @Override
    void execute(final PrintStream out) {
        final var sb = new StringBuilder();
        // FIXME: static wiring
        for (var line : OpenFlowPluginProviderImpl.getMessageIntelligenceAgency().provideIntelligence()) {
            sb.append(line).append('\n');
        }
        out.print(sb.toString());
    }
}
