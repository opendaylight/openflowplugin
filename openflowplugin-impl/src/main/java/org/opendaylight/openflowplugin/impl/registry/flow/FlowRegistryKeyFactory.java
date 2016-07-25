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
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.impl.util.MatchComparatorFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class FlowRegistryKeyFactory {


    public FlowRegistryKeyFactory() {
    }

    public static FlowRegistryKey create(final Flow flow) {
        return new FlowRegistryKeyDto(flow);
    }

    private static final class FlowRegistryKeyDto implements FlowRegistryKey {

        private final short tableId;
        private final int priority;
        private final BigInteger cookie;
        private final Match match;
        private static final Match EMPTY_MATCH = new MatchBuilder().build();

        public FlowRegistryKeyDto(final Flow flow) {
            //TODO: mandatory flow input values (or default values) should be specified via yang model
            tableId = Preconditions.checkNotNull(flow.getTableId(), "flow tableId must not be null");
            priority = MoreObjects.firstNonNull(flow.getPriority(), OFConstants.DEFAULT_FLOW_PRIORITY);
            match = flow.getMatch()==null ? EMPTY_MATCH : flow.getMatch();
            cookie = MoreObjects.firstNonNull(flow.getCookie(), OFConstants.DEFAULT_FLOW_COOKIE).getValue();
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

            return getPriority() == that.getPriority() &&
                    getTableId() == that.getTableId() &&
                    getCookie().equals(that.getCookie()) &&
                    MatchComparatorFactory.createMatch().areObjectsEqual(getMatch(), that.getMatch());
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
