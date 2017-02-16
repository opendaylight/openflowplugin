/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;

/**
 * Provides comparator for comparing according to various {@link Match} attributes
 *
 */
public final class MatchComparatorFactory {

    private MatchComparatorFactory() {
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
        MATCH_COMPARATORS.add(MatchComparatorFactory.createExtension());
    }

    private static SimpleComparator<Match> createExtension() {
        return new SimpleComparator<Match>() {
            /**
             * Compare extension lists
             */
            @Override
            public boolean areObjectsEqual(final short version, final Match statsMatch, final Match storedMatch) {
                return ExtensionResolvers
                    .getMatchExtensionResolver()
                    .getExtension(statsMatch)
                    .flatMap(statExt -> Optional.ofNullable(statExt.getExtensionList()))
                    .map(statList -> ExtensionResolvers
                        .getMatchExtensionResolver()
                        .getExtension(storedMatch)
                        .flatMap(storedExt -> Optional.ofNullable(storedExt.getExtensionList()))
                        .filter(storedList -> statList.size() == storedList.size())
                        .map(storedList -> {
                            final Collection<ExtensionList> difference = new HashSet<>(statList);
                            difference.removeAll(storedList);
                            return difference.isEmpty();
                        })
                        .orElse(false))
                    .orElse(!ExtensionResolvers
                        .getMatchExtensionResolver()
                        .getExtension(storedMatch)
                        .flatMap(storedExt -> Optional.ofNullable(storedExt.getExtensionList()))
                        .isPresent());
            }
        };
    }

    private static SimpleComparator<Match> createNull() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by whole object
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                return (statsMatch == null) == (storedMatch == null);
            }
        };
    }

    private static SimpleComparator<Match> createVlan() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by VLAN
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createTunnel() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by tunnel
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createProtocolMatchFields() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by protocol fields
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createMetadata() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by metadata
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createL4() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by layer4
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createL3() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by layer3
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createIp() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by Ip
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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


    /**
     * Converts both ports in node connector id format to number format and compare them
     *
     * @param version openflow version
     * @param left first object to compare
     * @param right second object to compare
     * @return true if equal
     */
    private static boolean arePortNumbersEqual(short version, NodeConnectorId left, NodeConnectorId right) {
        final OpenflowVersion ofVersion = OpenflowVersion.get(version);

        final Long leftPort = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, left);
        final Long rightPort = InventoryDataServiceUtil.portNumberfromNodeConnectorId(ofVersion, right);

        if (leftPort == null) {
            if (rightPort != null) {
                return false;
            }
        } else if (!leftPort.equals(rightPort)) {
            return false;
        }

        return true;
    }

    private static SimpleComparator<Match> createInPort() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by InPort
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
                if (storedMatch.getInPort() == null) {
                    if (statsMatch.getInPort() != null) {
                        return false;
                    }
                } else if (!arePortNumbersEqual(version, storedMatch.getInPort(), statsMatch.getInPort())) {
                    return false;
                }
                return true;
            }
        };
    }

    private static SimpleComparator<Match> createInPhyPort() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by InPhyPort
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
                if (storedMatch.getInPhyPort() == null) {
                    if (statsMatch.getInPhyPort() != null) {
                        return false;
                    }
                } else if (!arePortNumbersEqual(version, storedMatch.getInPhyPort(), statsMatch.getInPhyPort())) {
                    return false;
                }
                return true;
            }
        };
    }

    private static SimpleComparator<Match> createEthernet() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by Ethernet
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    private static SimpleComparator<Match> createIcmpv4() {
        return new SimpleComparator<Match>() {
            /**
             * Compares by Icmpv4
             */
            @Override
            public boolean areObjectsEqual(short version, Match statsMatch, Match storedMatch) {
                if (storedMatch == null) {
                    return false;
                }
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

    public static SimpleComparator<Match> createMatch() {
        return new SimpleComparator<Match>() {
            /**
             * Compares flows by whole match
             */
            @Override
            public boolean areObjectsEqual(short version, final Match statsFlow, final Match storedFlow) {
                if (statsFlow == null) {
                    if (storedFlow != null) {
                        return false;
                    }
                } else if (!compareMatches(version, statsFlow, storedFlow)) {
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
     */
    private static boolean compareMatches(final short version, final Match statsMatch, final Match storedMatch) {
        if (statsMatch == storedMatch) {
            return true;
        }

        for (SimpleComparator<Match> matchComp : MATCH_COMPARATORS) {
            if (!matchComp.areObjectsEqual(version, statsMatch, storedMatch)) {
                return false;
            }
        }
        return true;
    }
}
