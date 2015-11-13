/*
 * Copyright (c) 2015 Intel Corp. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.jbench;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the Runnable Interface and exposes methods that creates a group of fakeswitches.
 * @author Raksha Madhava Bangera
 *
 */
public class FakeSwitchGroup extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger("Jbench");
    private Jbench jbench;
    private int fakeSwitchStartIndex;
    private int fakeSwitchEndIndex;

    /**
     * Constructor of FakeSwitchGroup
     * @param threadName - ThreadName of this FakeSwitchGroup
     * @param jbenchObj - Reference to Jbench driver class object
     * @param startIndex - startIndex of fakeSwitch
     * @param endIndex - endIndex of fakeSwitch
     */
    FakeSwitchGroup(String threadName, Jbench jbenchObj, int startIndex, int endIndex) {
        jbench = jbenchObj;
        fakeSwitchStartIndex = startIndex;
        fakeSwitchEndIndex = endIndex;
    }
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while ( System.currentTimeMillis() < jbench.getEndTime()) {
            for (int switchCount = fakeSwitchStartIndex; switchCount < fakeSwitchEndIndex; switchCount++) {
                FakeSwitch fakeSwitch = jbench.getFakeSwitch(switchCount);
                fakeSwitch.makePacketIns();
                fakeSwitch.getResponseFromController();
            }
        }
    }
}
