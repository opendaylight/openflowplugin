/*
 * Copyright (c) 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.nbio.demo;

import javax.swing.*;
import java.awt.*;

/**
 * The options pane for a client window.
 *
 * @author Simon Hunt
 */
public class ClientPane extends JPanel {

    private static final Color BG = new Color(230, 240, 240);

    private final DemoClient parent;

    /**
     * Creates the client options pane.
     *
     * @param parent our parent container
     */
    ClientPane(DemoClient parent) {
        this.parent = parent;
        setBackground(BG);
    }
}
