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
 * lower openflow controller.
 * 
 * @author Thomas Vachuska
 */
public class StandaloneOpenflowController {
    
    /**
     * Main entry point to launch the switch bank.
     * 
     * @param args command-line arguments; target user@host, number of
     *        switches and number of packets to be pumped by each switch
     */
    public static void main(String args[]) {
        out.println("Setting up the controller... ");
        OpenflowControllerDistributedSpeedTest oct = 
                new OpenflowControllerDistributedSpeedTest();

        processArgs(args);
        
        try {
            OpenflowControllerDistributedSpeedTest.classSetUp();
            out.println("Launching test...");
            oct.launch();
            
        } catch (Throwable e) {
            err.println("Unable to complete test");
            e.printStackTrace();
            System.exit(1);
            
        } finally {
            out.println("Tearing down the controller... ");
            OpenflowControllerDistributedSpeedTest.classTearDown();
            System.exit(0);
        }
    }
    
    // Process the command-line arguments
    protected static void processArgs(String args[]) {
        if (args.length > 0)
            OpenflowControllerDistributedSpeedTest.ctlAddress = args[0];
        if (args.length > 1)
            OpenflowControllerDistributedSpeedTest.target = args[1];
        if (args.length > 2)
            OpenflowControllerDistributedSpeedTest.switchCount = args[2];
        if (args.length > 3)
            OpenflowControllerDistributedSpeedTest.packetCount = args[3];
        if (args.length > 4)
            OpenflowControllerDistributedSpeedTest.batchPermits = args[4];
        if (args.length > 5)
            OpenflowControllerDistributedSpeedTest.batchSize = args[5];
        if (args.length > 6)
            OpenflowControllerDistributedSpeedTest.setMode(args[6]);
    }
    
}
