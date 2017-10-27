/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.registry.flow;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.MatchNormalizationUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowRegistryKeyFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FlowRegistryKeyFactory.class);

    private FlowRegistryKeyFactory() {
        // Hide implicit constructor
    }

    @Nonnull
    public static FlowRegistryKey create(final short version, @Nonnull final Flow flow) {
        //TODO: mandatory flow input values (or default values) should be specified via yang model
        final short tableId = Preconditions.checkNotNull(flow.getTableId(), "flow tableId must not be null");
        final int priority = MoreObjects.firstNonNull(flow.getPriority(), OFConstants.DEFAULT_FLOW_PRIORITY);
        final BigInteger cookie =
                MoreObjects.firstNonNull(flow.getCookie(), OFConstants.DEFAULT_FLOW_COOKIE).getValue();
        Match match = MatchNormalizationUtil
                .normalizeMatch(MoreObjects.firstNonNull(flow.getMatch(), OFConstants.EMPTY_MATCH), version);
        return new FlowRegistryKeyDto(tableId, priority, cookie, match);
    }

    private static final class FlowRegistryKeyDto implements FlowRegistryKey {
        private final short tableId;
        private final int priority;
        private final BigInteger cookie;
        private final Match match;

        private FlowRegistryKeyDto(final short tableId,
                                   final int priority,
                                   @Nonnull final BigInteger cookie,
                                   @Nonnull final Match match) {
            this.tableId = tableId;
            this.priority = priority;
            this.cookie = cookie;
            this.match = match;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || !(o instanceof FlowRegistryKey)) {
                return false;
            }

            final FlowRegistryKey that = (FlowRegistryKey) o;

            return getPriority() == that.getPriority()
                    && getTableId() == that.getTableId()
                    && getCookie().equals(that.getCookie())
                    && equalMatch(that.getMatch());
        }

        private boolean equalMatch(final Match input) {
            GeneralAugMatchNodesNodeTableFlow thisAug = match.getAugmentation(GeneralAugMatchNodesNodeTableFlow.class);
            GeneralAugMatchNodesNodeTableFlow inputAug = input.getAugmentation(GeneralAugMatchNodesNodeTableFlow.class);
            if (thisAug != inputAug) {
                if (thisAug != null) {
                    if (inputAug == null) {
                        return false;
                    }
                    if (!Objects.equals(match.getEthernetMatch(), input.getEthernetMatch())) {
                        return false;
                    }
                    if (!Objects.equals(match.getIcmpv4Match(), input.getIcmpv4Match())) {
                        return false;
                    }
                    if (!Objects.equals(match.getIcmpv6Match(), input.getIcmpv6Match())) {
                        return false;
                    }
                    if (!Objects.equals(match.getInPhyPort(), input.getInPhyPort())) {
                        return false;
                    }
                    if (!Objects.equals(match.getInPort(), input.getInPort())) {
                        return false;
                    }
                    if (!Objects.equals(match.getIpMatch(), input.getIpMatch())) {
                        return false;
                    }
                    if (!Objects.equals(match.getLayer3Match(), input.getLayer3Match())) {
                        return false;
                    }
                    if (!Objects.equals(match.getLayer4Match(), input.getLayer4Match())) {
                        return false;
                    }
                    if (!Objects.equals(match.getMetadata(), input.getMetadata())) {
                        return false;
                    }
                    if (!Objects.equals(match.getProtocolMatchFields(), input.getProtocolMatchFields())) {
                        return false;
                    }
                    if (!Objects.equals(match.getTcpFlagsMatch(), input.getTcpFlagsMatch())) {
                        return false;
                    }
                    if (!Objects.equals(match.getTunnel(), input.getTunnel())) {
                        return false;
                    }
                    if (!Objects.equals(match.getVlanMatch(), input.getVlanMatch())) {
                        return false;
                    }
                    for (ExtensionList inputExtensionList : inputAug.getExtensionList()) {
                        if (!thisAug.getExtensionList().contains(inputExtensionList)) {
                            return false;
                        }
                    }
                }
            } else {
                return getMatch().equals(input);
            }
            return true;
        }

        @Override
        public int hashCode() {
            int result = tableId;
            result = 31 * result + priority;
            result = 31 * result + cookie.hashCode();
            result = 31 * result + match.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "FlowRegistryKeyDto{" +
                    "tableId=" + tableId +
                    ", priority=" + priority +
                    ", cookie=" + cookie +
                    ", match=" + match +
                    '}';
        }

        @Override
        public short getTableId() {
            return tableId;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public BigInteger getCookie() {
            return cookie;
        }

        @Override
        public Match getMatch() {
            return match;
        }
    }

}
