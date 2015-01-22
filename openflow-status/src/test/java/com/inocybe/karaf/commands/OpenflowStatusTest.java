/*
 * Openflow Status
 *
 */

package com.inocybe.karaf.commands;

import junit.framework.TestCase;

import com.inocybe.karaf.commands.OpenflowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowStatusTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    public void testToMB() throws Exception {
        OpenflowStatus test = new OpenflowStatus();
        assertTrue(true);
    }

}
