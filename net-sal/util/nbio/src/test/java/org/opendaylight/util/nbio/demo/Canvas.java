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
 * Our UI canvas, where we will illustrate the buffers / IO Loops.
 *
 * @author Simon Hunt
 */
public class Canvas extends JPanel {
    private static final Color BG = new Color(204, 206, 230);

    private final BufferModel model;

    Canvas(BufferModel model) {
        this.model = model;
        setBackground(BG);
    }
}
