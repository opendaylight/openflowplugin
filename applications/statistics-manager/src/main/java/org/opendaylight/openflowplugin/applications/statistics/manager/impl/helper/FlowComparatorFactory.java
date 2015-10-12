/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import java.util.ArrayList;
import java.util.Collection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;

public final class FlowComparatorFactory {

    private FlowComparatorFactory() {
        // NOOP
    }

    private static final Collection<SimpleComparator<Match>> MATCH_COMPARATORS = new ArrayList<>();
    static {
        MATCH_COMPARATORS.add(MatchComparatorFactory.createEthernet());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createIcmpv4());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createInPhyPort());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createInPort());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createIp());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createL3());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createL4());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createProtocolMatchFields());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createMetadata());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createNull());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createTunnel());
        MATCH_COMPARATORS.add(MatchComparatorFactory.createVlan());
    }

    public static SimpleComparator<Flow> createContainerName() {
        return new SimpleComparator<Flow>() {
            /**
             * Compares flows by container name
             */
            @Override
            public boolean areObjectsEqual(Flow statsFlow, Flow storedFlow) {
                if (statsFlow.getContainerName() == null) {
                    if (storedFlow.getContainerName() != null) {
                        return false;
                    }
                } else if (!statsFlow.getContainerName().equals(storedFlow.getContainerName())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Flow> createPriority() {
        return new SimpleComparator<Flow>() {
            /**
             * Compares flows by priority
             */
            @Override
            public boolean areObjectsEqual(final Flow statsFlow, final Flow storedFlow) {
                if (storedFlow.getPriority() == null) {
                    if (statsFlow.getPriority() != null && statsFlow.getPriority() != 0x8000) {
                        return false;
                    }
                } else if (!statsFlow.getPriority().equals(storedFlow.getPriority())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Flow> createTableId() {
        return new SimpleComparator<Flow>() {
            /**
             * Compares flows by table ID
             */
            @Override
            public boolean areObjectsEqual(final Flow statsFlow, final Flow storedFlow) {
                if (statsFlow.getTableId() == null) {
                    if (storedFlow.getTableId() != null) {
                        return false;
                    }
                } else if (!statsFlow.getTableId().equals(storedFlow.getTableId())) {
                    return false;
                }
                return true;
            }
        };
    }

    /*
     * TODO:Cookie is used in flow comparison for the applications using match extensions
     * in their flow body. As of now openflowplugin don't use match extensions
     * in flow comparison, that can create a scenario where more then one stored flow
     * can match to any stats flow, if stored flows differ only by match extension.
     * Once match extensions are part of flow comparison, we should remove cookie
     * from flow comparison.
     */
    public static SimpleComparator<Flow> createCookie() {
        return new SimpleComparator<Flow>() {
            /**
             * Compares flows by cookie value
             */
            @Override
            public boolean areObjectsEqual(final Flow statsFlow, final Flow storedFlow) {
                /*
                 * Cookie is an optional field, so user might not set it, but if switch
                 * get flow without cookie value , it will use 0 as a default cookie value
                 * and return cookie=0 when openflowplugin fetch the flow stats from switch.
                 * In this scenario flow comparison will fail. Below check make sure that
                 * if user didn't set cookie value while flow installation, skip the comparison.
                 */
                if(storedFlow.getCookie() == null){
                    return true;
                }
                if (statsFlow.getCookie() == null) {
                    if (storedFlow.getCookie() != null) {
                        return false;
                    }
                } else if (!statsFlow.getCookie().equals(storedFlow.getCookie())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Flow> createMatch() {
        return new SimpleComparator<Flow>() {
            /**
             * Compares flows by whole match
             */
            @Override
            public boolean areObjectsEqual(final Flow statsFlow, final Flow storedFlow) {
                if (statsFlow.getMatch() == null) {
                    if (storedFlow.getMatch() != null) {
                        return false;
                    }
                } else if (!compareMatches(statsFlow.getMatch(), storedFlow.getMatch())) {
                    return false;
                }
                return true;
            }
        };
    }


    /**
     * Explicit equals method to compare the 'match' for flows stored in the data-stores and flow fetched from the switch.
     * Flow installation process has three steps
     * 1) Store flow in config data store
     * 2) and send it to plugin for installation
     * 3) Flow gets installed in switch
     *
     * The flow user wants to install and what finally gets installed in switch can be slightly different.
     * E.g, If user installs flow with src/dst ip=10.0.0.1/24, when it get installed in the switch
     * src/dst ip will be changes to 10.0.0.0/24 because of netmask of 24. When statistics manager fetch
     * stats it gets 10.0.0.0/24 rather then 10.0.0.1/24. Custom match takes care of by using masked ip
     * while comparing two ip addresses.
     *
     * Sometimes when user don't provide few values that is required by flow installation request, like
     * priority,hard timeout, idle timeout, cookies etc, plugin usages default values before sending
     * request to the switch. So when statistics manager gets flow statistics, it gets the default value.
     * But the flow stored in config data store don't have those defaults value. I included those checks
     * in the customer flow/match equal function.
     *
     *
     * @param statsMatch
     * @param storedMatch
     * @return
     */
    private static boolean compareMatches(final Match statsMatch, final Match storedMatch) {
        if (statsMatch == storedMatch) {
            return true;
        }

        for (SimpleComparator<Match> matchComp : MATCH_COMPARATORS) {
            if (!matchComp.areObjectsEqual(statsMatch, storedMatch)) {
                return false;
            }
        }
        return true;
    }
}
