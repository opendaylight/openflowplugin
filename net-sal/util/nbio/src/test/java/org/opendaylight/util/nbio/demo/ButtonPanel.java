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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Base class for a panel of push buttons.
 *
 * @author Simon Hunt
 */
public abstract class ButtonPanel extends JPanel {
    private static final Dimension DIM = new Dimension(40, 40);
    private static final Color BG = new Color(225, 222, 244);

    /**
     * Creates the button panel, using {@link #createButtons()} to generate
     * the buttons.
     */
    protected ButtonPanel() {
        setBackground(BG);
        for (JButton button: createButtons())
            add(button);
    }

    @Override public Dimension getMinimumSize() { return DIM; }
    @Override public Dimension getMaximumSize() { return DIM; }
    @Override public Dimension getPreferredSize() { return DIM; }


    /**
     * Subclasses must create and return a list of {@link Button buttons}.
     *
     * @return the list of buttons
     */
    protected abstract java.util.List<Button> createButtons();


    /**
     * Base class for buttons for this panel.
     */
    protected abstract class Button extends JButton {

        /**
         * Constructs the button, setting the givel label as its text.
         *
         * @param label the button label
         */
        protected Button(String label) {
            super(label);
            addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    onClick();
                }
            });
        }

        /**
         * Callback invoked when the button is clicked.
         * <p>
         * This default implementation does nothing.
         */
        protected void onClick() { }
    }

}
