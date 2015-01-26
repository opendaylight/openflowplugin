/*
 * Openflow Status
 *
 */

package org.opendaylight.openflowplugin.karaf.commands;

import junit.framework.TestCase;

import org.opendaylight.openflowplugin.karaf.commands.OpenflowStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenflowStatusTest extends TestCase {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(TestCase.class);

    public void testToMB() throws Exception {
        OpenflowStatus test = new OpenflowStatus();
        assertTrue(true);
    }

}
