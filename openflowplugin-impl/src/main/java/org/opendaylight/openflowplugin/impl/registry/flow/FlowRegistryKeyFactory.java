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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.AllMatchesGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 8.4.2015.
 */
public class FlowRegistryKeyFactory {
    private static final Logger LOG = LoggerFactory.getLogger(FlowRegistryKey.class);

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

        public FlowRegistryKeyDto(final Flow flow) {
            //TODO: mandatory flow input values (or default values) should be specified via yang model
            tableId = Preconditions.checkNotNull(flow.getTableId(), "flow tableId must not be null");
            priority = MoreObjects.firstNonNull(flow.getPriority(), OFConstants.DEFAULT_FLOW_PRIORITY);
            match = Preconditions.checkNotNull(flow.getMatch(), "Match value must not be null");
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

            MatchAugmentationIterator thisIt = new MatchAugmentationIterator(this.match);
            MatchAugmentationIterator thatIt = new MatchAugmentationIterator(that.getMatch());

            boolean augmentationsMatch = true;
            Augmentation augThis = thisIt.next();
            Augmentation augThat = thatIt.next();

            while(true) {
                LOG.warn("JOSH comparing {} {}", augThis, augThat);
                if(!allMatchesGroupingsEqual((AllMatchesGrouping)augThis, (AllMatchesGrouping)augThat)) {
                    augmentationsMatch = false;
                    break;
                }
                if(augThis == null && augThat == null) {
                    break;
                }
                augThis = thisIt.next();
                augThat = thatIt.next();
            }


            boolean b =  getPriority() == that.getPriority() &&
                    getTableId() == that.getTableId() &&
                    augmentationsMatch;
            LOG.warn("JOSH equals {}", b);
            return b;
        }

        private boolean allMatchesGroupingsEqual(AllMatchesGrouping obj1, AllMatchesGrouping obj2) {
            Method[] methods = AllMatchesGrouping.class.getMethods();

            if(obj1 == null && obj2 == null) {
                return true;
            }

            if (obj1 == null) {
                return false;
            }

            if (obj2 == null) {
                return false;
            }

            for(Method method : methods) {
                if (!method.getName().startsWith("get")) {
                    continue;
                }

                if (method.getParameterCount() != 0) {
                    continue;
                }

                if (!DataObject.class.isAssignableFrom(method.getReturnType())) {
                    continue;
                }

                try {
                    if(!Objects.equals(method.invoke(obj1), method.invoke(obj2))){
                        return false;
                    }
                } catch (Exception e) {
                    LOG.error("Exception while comparing objects, assuming not equal", e);
                    return false;
                }
            }

            return true;
        }

        class MatchAugmentationIterator {

            Match myMatch;

            public MatchAugmentationIterator(Match match) {
                this.myMatch = match;
            }

            //TODO: add other GeneralAugMatch's
            //TODO: make static
            //TODO: The naming here sucks...pretty much all members need to be renamed
            private final Class[] generalExtensionListGroupings =
                    {GeneralAugMatchNodesNodeTableFlow.class,
                            GeneralAugMatchNotifUpdateFlowStats.class};
            private int matchIdx = 0;

            private List<ExtensionList> extListList;
            private int extListListIdx = 0;

            //TODO: add others
            //TODO: make static
            private final Class[] allMatchesGroupings =
                    {NxAugMatchNodesNodeTableFlow.class,
                            NxAugMatchNotifUpdateFlowStats.class};
            private int extListIdx = 0;

            public Augmentation next() {

                while (matchIdx < generalExtensionListGroupings.length) {
                    GeneralExtensionListGrouping extensionListGrouping = (GeneralExtensionListGrouping)
                                            myMatch.getAugmentation(generalExtensionListGroupings[matchIdx]);

                    if (null != extensionListGrouping) {
                        extListList = extensionListGrouping.getExtensionList();

                        while (extListListIdx < extListList.size()) {

                            ExtensionList extList = extListList.get(extListListIdx);

                            while(extListIdx < allMatchesGroupings.length) {
                                Augmentation res = extList.getExtension().getAugmentation(allMatchesGroupings[extListIdx]);
                                ++extListIdx;
                                if (res != null) {
                                    return res;
                                }
                            }
                            extListIdx = 0;
                            ++extListListIdx;
                        }
                        extListListIdx = 0;
                    }
                    ++matchIdx;

                }

                return null;
            }

        }

        private int matchHashCode() {
            int result = 0;
            result = 31 * result + Objects.hashCode(match.getEthernetMatch());
            result = 31 * result + Objects.hashCode(match.getIcmpv4Match());
            result = 31 * result + Objects.hashCode(match.getIcmpv6Match());
            result = 31 * result + Objects.hashCode(match.getInPhyPort());
            result = 31 * result + Objects.hashCode(match.getInPort());
            result = 31 * result + Objects.hashCode(match.getIpMatch());
            result = 31 * result + Objects.hashCode(match.getLayer3Match());
            result = 31 * result + Objects.hashCode(match.getLayer4Match());
            result = 31 * result + Objects.hashCode(match.getMetadata());
            result = 31 * result + Objects.hashCode(match.getProtocolMatchFields());
            result = 31 * result + Objects.hashCode(match.getTcpFlagMatch());
            result = 31 * result + Objects.hashCode(match.getTunnel());
            result = 31 * result + Objects.hashCode(match.getVlanMatch());

            MatchAugmentationIterator it = new MatchAugmentationIterator(match);
            Augmentation aug = it.next();
            while(aug != null) {
                result = 31 * result + aug.hashCode();
                aug = it.next();
            }

            return result;

        }

        @Override
        public int hashCode() {
            LOG.warn("JOSH hashCode for {}", this.getMatch());
            int result = tableId;
            result = 31 * result + priority;
            result = 31 * result + matchHashCode();
            LOG.warn("JOSH hash {} for {}", result, match);
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
