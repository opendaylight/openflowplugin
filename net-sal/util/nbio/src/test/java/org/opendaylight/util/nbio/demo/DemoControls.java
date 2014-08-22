/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Main demo control panel.
 *
 * @author Simon Hunt
 */
public class DemoControls extends ButtonPanel {

    private static final String LOOP_START = "Start IO Loops";
    private static final String LOOP_STOP = "Stop IO Loops";
    private static final String NEW_CLIENT = "New Client";

    private final BufferModel bufferModel;
    private final Set<DemoClient> clients = new HashSet<>();


    DemoControls(BufferModel model) {
        this.bufferModel = model;
    }

    @Override
    protected List<Button> createButtons() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new LoopsButton());
        buttons.add(new ClientButton());
        return buttons;
    }

    void closeAllClients() {
        for (DemoClient dc: clients)
            dc.close();
    }

    // =================================================================

    private class LoopsButton extends Button {
        LoopsButton() { super(LOOP_START); }

        @Override
        protected void onClick() {
            String cmd = getActionCommand();
            if (cmd.equals(LOOP_START) && bufferModel.start())
                setText(LOOP_STOP);
            else if (cmd.equals(LOOP_STOP) && bufferModel.stop())
                setText(LOOP_START);
        }
    }

    private class ClientButton extends Button {
        ClientButton() { super(NEW_CLIENT); }

        @Override
        protected void onClick() {
            DemoClient dc = new DemoClient(clients.size() + 1);
            clients.add(dc);
        }
    }

}
