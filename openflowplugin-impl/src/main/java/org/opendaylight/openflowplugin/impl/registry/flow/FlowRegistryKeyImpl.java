/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2024 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.registry.flow;

import static java.util.Objects.requireNonNull;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yangtools.yang.common.Uint64;

record FlowRegistryKeyImpl(
        short tableId,
        int priority,
        @NonNull Uint64 cookie,
        @NonNull Match match) implements FlowRegistryKey {

    FlowRegistryKeyImpl {
        requireNonNull(cookie);
        requireNonNull(match);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || obj instanceof FlowRegistryKey that
            && priority == that.getPriority() && tableId == that.getTableId() && cookie.equals(that.getCookie())
            && equalMatch(that.getMatch());
    }

    private boolean equalMatch(final Match input) {
        GeneralAugMatchNodesNodeTableFlow thisAug = match.augmentation(GeneralAugMatchNodesNodeTableFlow.class);
        GeneralAugMatchNodesNodeTableFlow inputAug = input.augmentation(GeneralAugMatchNodesNodeTableFlow.class);
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
                for (var inputExtensionList : inputAug.nonnullExtensionList().values()) {
                    if (!thisAug.nonnullExtensionList().containsValue(inputExtensionList)) {
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
        return "FlowRegistryKeyDto{"
            + "tableId=" + tableId
            + ", priority=" + priority
            + ", cookie=" + cookie
            + ", match=" + match
            + '}';
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
    public Uint64 getCookie() {
        return cookie;
    }

    @Override
    public Match getMatch() {
        return match;
    }
}
