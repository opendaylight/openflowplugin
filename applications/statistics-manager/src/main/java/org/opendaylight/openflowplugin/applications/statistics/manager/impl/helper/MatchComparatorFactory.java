/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
/**
 * Provides comparator for comparing according to various {@link Match} attributes
 *
 */
public final class MatchComparatorFactory {

    private MatchComparatorFactory() {
        // NOOP
    }

    public static SimpleComparator<Match> createNull() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by whole object
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
                return (statsMatch == null) == (storedMatch == null);
            }
        };
    }

    public static SimpleComparator<Match> createVlan() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by VLAN
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getVlanMatch() == null) {
                    if (statsMatch.getVlanMatch() != null) {
                        return false;
                    }
                } else if (!storedMatch.getVlanMatch().equals(statsMatch.getVlanMatch())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createTunnel() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by tunnel
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getTunnel() == null) {
                    if (statsMatch.getTunnel() != null) {
                        return false;
                    }
                } else if (!storedMatch.getTunnel().equals(statsMatch.getTunnel())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createProtocolMatchFields() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by protocol fields
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getProtocolMatchFields() == null) {
                    if (statsMatch.getProtocolMatchFields() != null) {
                        return false;
                    }
                } else if (!storedMatch.getProtocolMatchFields().equals(statsMatch.getProtocolMatchFields())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createMetadata() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by metadata
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getMetadata() == null) {
                    if (statsMatch.getMetadata() != null) {
                        return false;
                    }
                } else if (!storedMatch.getMetadata().equals(statsMatch.getMetadata())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createL4() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by layer4
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getLayer4Match() == null) {
                    if (statsMatch.getLayer4Match() != null) {
                        return false;
                    }
                } else if (!storedMatch.getLayer4Match().equals(statsMatch.getLayer4Match())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createL3() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by layer3
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getLayer3Match() == null) {
                    if (statsMatch.getLayer3Match() != null) {
                        return false;
                    }
                } else if (!MatchComparatorHelper.layer3MatchEquals(statsMatch.getLayer3Match(), storedMatch.getLayer3Match())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createIp() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by Ip
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getIpMatch() == null) {
                    if (statsMatch.getIpMatch() != null) {
                        return false;
                    }
                } else if (!storedMatch.getIpMatch().equals(statsMatch.getIpMatch())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createInPort() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by InPort
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getInPort() == null) {
                    if (statsMatch.getInPort() != null) {
                        return false;
                    }
                } else if (!storedMatch.getInPort().equals(statsMatch.getInPort())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createInPhyPort() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by InPhyPort
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getInPhyPort() == null) {
                    if (statsMatch.getInPhyPort() != null) {
                        return false;
                    }
                } else if (!storedMatch.getInPhyPort().equals(statsMatch.getInPhyPort())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createEthernet() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by Ethernet
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getEthernetMatch() == null) {
                    if (statsMatch.getEthernetMatch() != null) {
                        return false;
                    }
                } else if (!MatchComparatorHelper.ethernetMatchEquals(statsMatch.getEthernetMatch(), storedMatch.getEthernetMatch())) {
                    return false;
                }
                return true;
            }
        };
    }

    public static SimpleComparator<Match> createIcmpv4() {
        return new SimpleComparator<Match>() {
            /**
             * Comparation by Icmpv4
             */
            @Override
            public boolean areObjectsEqual(Match statsMatch, Match storedMatch) {
            	if (storedMatch == null) return false;
                if (storedMatch.getIcmpv4Match() == null) {
                    if (statsMatch.getIcmpv4Match() != null) {
                        return false;
                    }
                } else if (!storedMatch.getIcmpv4Match().equals(statsMatch.getIcmpv4Match())) {
                    return false;
                }
                return true;
            }
        };
    }

}
