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
import org.opendaylight.openflowplugin.impl.OpenFlowPluginProviderImpl;

@Command(scope = "ofp", name = "showStats", description = "Show openflow statistics.")
@Service
public class ShowStatsCommand implements Action {

    @Reference
    Session session;

    @Override
    public Object execute() {
        final var console = session.getConsole();
        OpenFlowPluginProviderImpl.getMessageIntelligenceAgency().provideIntelligence().forEach(console::println);
        return null;
    }
}
