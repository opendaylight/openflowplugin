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
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.MatchNormalizationUtil;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;

public class FlowRegistryKeyFactory {

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
        final Match match = MatchNormalizationUtil
                .normalizeMatch(MoreObjects.firstNonNull(flow.getMatch(), OFConstants.EMPTY_MATCH), version);
        return new FlowRegistryKeyDto(tableId, priority, cookie, match);
    }

    static int generateMatchHashCode(final Match match) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(match.getEthernetMatch());
        result = prime * result + Objects.hashCode(match.getIcmpv4Match());
        result = prime * result + Objects.hashCode(match.getIcmpv6Match());
        result = prime * result + Objects.hashCode(match.getInPhyPort());
        result = prime * result + Objects.hashCode(match.getInPort());
        result = prime * result + Objects.hashCode(match.getIpMatch());
        result = prime * result + Objects.hashCode(match.getLayer3Match());
        result = prime * result + Objects.hashCode(match.getLayer4Match());
        result = prime * result + Objects.hashCode(match.getMetadata());
        result = prime * result + Objects.hashCode(match.getProtocolMatchFields());
        result = prime * result + Objects.hashCode(match.getTcpFlagsMatch());
        result = prime * result + Objects.hashCode(match.getTunnel());
        result = prime * result + Objects.hashCode(match.getVlanMatch());
        result = prime * result + ExtensionResolvers
                .getMatchExtensionResolver()
                .getExtension(match)
                .flatMap(extensions -> Optional.ofNullable(extensions.getExtensionList()))
                .map(Objects::hashCode)
                .orElse(0);

        return result;
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
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }

            if (object == null || !(object instanceof FlowRegistryKey)) {
                return false;
            }

            final FlowRegistryKey that = (FlowRegistryKey) object;

            return getPriority() == that.getPriority()
                    && getTableId() == that.getTableId()
                    && getCookie().equals(that.getCookie())
                    && generateMatchHashCode(getMatch()) == generateMatchHashCode(that.getMatch());
        }

        @Override
        public int hashCode() {
            int result = tableId;
            result = 31 * result + priority;
            result = 31 * result + cookie.hashCode();
            result = 31 * result + generateMatchHashCode(match);
            return result;
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
