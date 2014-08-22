/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import static java.lang.System.err;
import static java.lang.System.out;


/**
 * Auxiliary test fixture intended to be deployable and executable as a
 * standalone VM as an vehicle for analyzing and verifying performance of the
 * controller manager assembly.
 * 
 * @author Thomas Vachuska
 */
public class StandaloneControllerManager extends StandaloneOpenflowController {
    
    /**
     * Main entry point to launch the switch bank.
     * 
     * @param args command-line arguments; target user@host, number of
     *        switches and number of packets to be pumped by each switch
     */
    public static void main(String args[]) {
        out.println("Setting up the controller... ");
        ControllerManagerDistributedSpeedTest cmt = 
                new ControllerManagerDistributedSpeedTest();

        processArgs(args);
        
        try {
            ControllerManagerDistributedSpeedTest.classSetUp();
            out.println("Launching test...");
            cmt.launch();
            
        } catch (Throwable e) {
            err.println("Unable to complete test");
            e.printStackTrace();
            System.exit(1);
            
        } finally {
            out.println("Tearing down the controller... ");
            ControllerManagerDistributedSpeedTest.classTearDown();
            System.exit(0);
        }
    }
    
}
