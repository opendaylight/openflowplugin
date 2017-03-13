/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.request.multipart.request.body.MultipartRequestDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.request.multipart.request.body.MultipartRequestFlowTableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.request.multipart.request.body.MultipartRequestPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowAggregateStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.multipart.request.multipart.request.body.MultipartRequestFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestGroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequestBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.experimenter.types.rev151020.multipart.request.multipart.request.body.MultipartRequestExperimenterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStatsBuilder;

/**
 * openflowplugin-impl
 * org.opendaylight.openflowplugin.impl.common
 * <p>
 * Factory class is designed for easy producing a MultipartRequestInput. Class should help
 * to understand a relationship between {@link MultipartType} and {@link MultipartRequestInput}
 * without touch OF specification 1.3.2  - a section 7.3.5. Multipart Messages
 * see also <a href="https://www.opennetworking.org/images/stories/downloads/sdn-resources/onf-specifications/openflow/openflow-spec-v1.3.2.pdf">OpenFlow 1.3.2</a>
 *
 * @author <a href="mailto:vdemcak@cisco.com">Vaclav Demcak</a>
 *         <p>
 *         Created: Mar 27, 2015
 */
public final class MultipartRequestInputFactory {

    private MultipartRequestInputFactory() {
        throw new UnsupportedOperationException("Factory class");
    }

    /**
     * Method validate input and makes {@link org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader}
     * from input values. Method set a moreRequest marker to false and it creates default empty multipart request body
     * by {@link MultipartType}
     *
     * @param xid xid
     * @param version OpenFlow version
     * @param type multipart type
     * @param canUseSingleLayer can use single layer serialization
     * @return multipart request
     */
    public static OfHeader makeMultipartRequest(final long xid,
                                                final short version,
                                                @Nonnull final MultipartType type,
                                                final boolean canUseSingleLayer) {
        return canUseSingleLayer ?
            new MultipartRequestBuilder()
                .setRequestMore(false)
                .setVersion(version)
                .setXid(xid)
                .setMultipartRequestBody(makeDefaultSingleLayerBody(type))
                .build() :
            new MultipartRequestInputBuilder()
                .setFlags(new MultipartRequestFlags(false))
                .setMultipartRequestBody(makeDefaultMultiLayerBody(type, version))
                .setVersion(version)
                .setType(type)
                .setXid(xid)
                .build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request
        .MultipartRequestBody makeDefaultSingleLayerBody(final MultipartType type) {
        switch (type) {
            case OFPMPDESC: return new MultipartRequestDescBuilder().build();
            case OFPMPFLOW: return new MultipartRequestFlowStatsBuilder()
                .setMatch(new org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                    .MatchBuilder().build())
                .build();
            case OFPMPAGGREGATE: return new MultipartRequestFlowAggregateStatsBuilder().build();
            case OFPMPTABLE: return new MultipartRequestFlowTableStatsBuilder().build();
            case OFPMPPORTSTATS: return new org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body
                .MultipartRequestPortStatsBuilder().build();
            case OFPMPQUEUE: return new MultipartRequestQueueStatsBuilder().build();
            case OFPMPGROUP: return new MultipartRequestGroupStatsBuilder().build();
            case OFPMPGROUPDESC: return new MultipartRequestGroupDescBuilder().build();
            case OFPMPGROUPFEATURES: return new MultipartRequestGroupFeaturesBuilder().build();
            case OFPMPMETER: return new MultipartRequestMeterStatsBuilder().build();
            case OFPMPMETERCONFIG: return new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body
                .MultipartRequestMeterConfigBuilder().build();
            case OFPMPMETERFEATURES: return new MultipartRequestMeterFeaturesBuilder().build();
            case OFPMPTABLEFEATURES: return new org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.request.multipart.request.body
                .MultipartRequestTableFeaturesBuilder().build();
            case OFPMPPORTDESC: return new MultipartRequestPortDescBuilder().build();
            case OFPMPEXPERIMENTER: return new MultipartRequestExperimenterBuilder().build();
            default:throw new IllegalArgumentException("Unknown MultipartType " + type);
        }
    }

    private static MultipartRequestBody makeDefaultMultiLayerBody(@Nonnull final MultipartType type,
                                                                  final short version) {
        switch (type) {
            case OFPMPDESC:
                return new MultipartRequestDescCaseBuilder().build();
            case OFPMPFLOW:
                MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
                MultipartRequestFlowBuilder multipartRequestFlowBuilder = new MultipartRequestFlowBuilder();
                multipartRequestFlowBuilder.setTableId(OFConstants.OFPTT_ALL);
                multipartRequestFlowBuilder.setOutPort(OFConstants.OFPP_ANY);
                multipartRequestFlowBuilder.setOutGroup(OFConstants.OFPG_ANY);
                multipartRequestFlowBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
                multipartRequestFlowBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);

                switch (version) {
                    case OFConstants.OFP_VERSION_1_0:
                        MatchV10Builder matchV10Builder = MatchUtil.createEmptyV10Match();
                        multipartRequestFlowBuilder.setMatchV10(matchV10Builder.build());
                        break;
                    case OFConstants.OFP_VERSION_1_3:
                        multipartRequestFlowBuilder.setMatch(new MatchBuilder().setType(OxmMatchType.class).build());
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown version " + version);
                }

                multipartRequestFlowCaseBuilder.setMultipartRequestFlow(multipartRequestFlowBuilder.build());
                return multipartRequestFlowCaseBuilder.build();
            case OFPMPAGGREGATE:
                return new MultipartRequestAggregateCaseBuilder().build();
            case OFPMPTABLE:
                return new MultipartRequestTableCaseBuilder().build();
            case OFPMPPORTSTATS:
                MultipartRequestPortStatsCaseBuilder multipartRequestPortStatsCaseBuilder = new MultipartRequestPortStatsCaseBuilder();
                MultipartRequestPortStatsBuilder multipartRequestPortStatsBuilder = new MultipartRequestPortStatsBuilder();
                multipartRequestPortStatsBuilder.setPortNo(OFConstants.OFPP_ANY);
                multipartRequestPortStatsCaseBuilder.setMultipartRequestPortStats(multipartRequestPortStatsBuilder.build());
                return multipartRequestPortStatsCaseBuilder.build();
            case OFPMPQUEUE:
                MultipartRequestQueueCaseBuilder multipartRequestQueueCaseBuilder = new MultipartRequestQueueCaseBuilder();
                MultipartRequestQueueBuilder multipartRequestQueueBuilder = new MultipartRequestQueueBuilder();
                multipartRequestQueueBuilder.setPortNo(OFConstants.OFPP_ANY);
                multipartRequestQueueBuilder.setQueueId(OFConstants.OFPQ_ALL);
                multipartRequestQueueCaseBuilder.setMultipartRequestQueue(multipartRequestQueueBuilder.build());
                return multipartRequestQueueCaseBuilder.build();
            case OFPMPGROUP:
                MultipartRequestGroupCaseBuilder multipartRequestGroupCaseBuilder = new MultipartRequestGroupCaseBuilder();
                MultipartRequestGroupBuilder multipartRequestGroupBuilder = new MultipartRequestGroupBuilder();
                GroupId groupId = new GroupId(OFConstants.OFPG_ALL);
                multipartRequestGroupBuilder.setGroupId(groupId);
                multipartRequestGroupCaseBuilder.setMultipartRequestGroup(multipartRequestGroupBuilder.build());
                return multipartRequestGroupCaseBuilder.build();
            case OFPMPGROUPDESC:
                return new MultipartRequestGroupDescCaseBuilder().build();
            case OFPMPGROUPFEATURES:
                return new MultipartRequestGroupFeaturesCaseBuilder().build();
            case OFPMPMETER:
                MultipartRequestMeterCaseBuilder multipartRequestMeterCaseBuilder = new MultipartRequestMeterCaseBuilder();
                MultipartRequestMeterBuilder multipartRequestMeterBuilder = new MultipartRequestMeterBuilder();
                MeterId meterId = new MeterId(OFConstants.OFPM_ALL);
                multipartRequestMeterBuilder.setMeterId(meterId);
                multipartRequestMeterCaseBuilder.setMultipartRequestMeter(multipartRequestMeterBuilder.build());
                return multipartRequestMeterCaseBuilder.build();
            case OFPMPMETERCONFIG:
                MultipartRequestMeterConfigCaseBuilder multipartRequestMeterConfigCaseBuilder = new MultipartRequestMeterConfigCaseBuilder();
                MultipartRequestMeterConfigBuilder multipartRequestMeterConfigBuilder = new MultipartRequestMeterConfigBuilder();
                MeterId configMeterId = new MeterId(OFConstants.OFPM_ALL);
                multipartRequestMeterConfigBuilder.setMeterId(configMeterId);
                multipartRequestMeterConfigCaseBuilder.setMultipartRequestMeterConfig(multipartRequestMeterConfigBuilder.build());
                return multipartRequestMeterConfigCaseBuilder.build();
            case OFPMPMETERFEATURES:
                return new MultipartRequestMeterFeaturesCaseBuilder().build();
            case OFPMPTABLEFEATURES:
                MultipartRequestTableFeaturesCaseBuilder tableFeaturesCaseBuilder = new MultipartRequestTableFeaturesCaseBuilder();
                tableFeaturesCaseBuilder.setMultipartRequestTableFeatures(new MultipartRequestTableFeaturesBuilder().build());
                return tableFeaturesCaseBuilder.build();
            case OFPMPPORTDESC:
                return new MultipartRequestPortDescCaseBuilder().build();
            case OFPMPEXPERIMENTER:
                return new MultipartRequestExperimenterCaseBuilder().build();
            default:
                throw new IllegalArgumentException("Unknown MultipartType " + type);
        }
    }
}
