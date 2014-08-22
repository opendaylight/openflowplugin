/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Control panel for client window.
 *
 * @author Simon Hunt
 */
public class ClientControls extends ButtonPanel {

    private static final String CONNECT = "Connect";

    private final DemoClient parent;

    /**
     * Creates the button panel for a client window.
     *
     * @param parent our parent container
     */
    public ClientControls(DemoClient parent) {
        this.parent = parent;
    }

    @Override
    protected List<Button> createButtons() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(new ConnectButton());
        return buttons;
    }

    // =====================================================================
    private class ConnectButton extends Button {
        ConnectButton() { super(CONNECT); }

        @Override
        protected void onClick() {
            parent.connect();
        }
    }
}
