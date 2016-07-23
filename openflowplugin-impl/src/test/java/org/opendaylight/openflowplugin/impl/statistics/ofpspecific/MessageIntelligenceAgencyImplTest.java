/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.ofpspecific;

import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;


public class MessageIntelligenceAgencyImplTest {

    @Test
    public void testMessageIntelligenceAgency() {
        final MessageIntelligenceAgencyImpl messageIntelligenceAgency = new MessageIntelligenceAgencyImpl();
        messageIntelligenceAgency.spyMessage(String.class, MessageSpy.STATISTIC_GROUP.FROM_SWITCH);
        messageIntelligenceAgency.spyMessage(Integer.class, MessageSpy.STATISTIC_GROUP.TO_SWITCH_ENTERED);
        final List<String> intelligence = messageIntelligenceAgency.provideIntelligence();
        findExpectedStatistics(intelligence, "FROM_SWITCH: MSG[String] -> +1 | 1", "TO_SWITCH_ENTERED: MSG[Integer] -> +1 | 1");
    }

    private void findExpectedStatistics(final List<String> statisticsInfo, String ... expectedValues) {
        for (String expectedValue : expectedValues) {
            assertTrue("Expected value " + expectedValue + "wasn't found.", findValueInStatistics(statisticsInfo, expectedValue));
        }
    }

    private boolean findValueInStatistics(List<String> statisticsInfo, String expectedValue) {
        for (String pieceOfInfo : statisticsInfo) {
            if (pieceOfInfo.equals(expectedValue)) {
                return true;
            }
        }
        return false;
    }


}
