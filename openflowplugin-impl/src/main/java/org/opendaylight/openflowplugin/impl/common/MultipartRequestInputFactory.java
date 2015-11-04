/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import com.google.common.base.Preconditions;
import java.math.BigInteger;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.impl.util.MatchUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestExperimenterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortDescCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestPortStatsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestQueueCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestTableFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter.config._case.MultipartRequestMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.port.stats._case.MultipartRequestPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.queue._case.MultipartRequestQueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.table.features._case.MultipartRequestTableFeaturesBuilder;

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
     * Method validate input and makes {@link MultipartRequestInput} from input values. Method set
     * a moreRequest marker to false and it creates default empty {@link MultipartRequestBody}
     * by {@link MultipartType}
     *
     * @param xid
     * @param type
     * @param ofVersion
     * @return
     */
    public static MultipartRequestInput makeMultipartRequestInput(final long xid, final short ofVersion,
                                                                  @Nonnull final MultipartType type) {
        return makeMultipartRequestInput(xid, ofVersion, type, makeDefaultEmptyRequestBody(type, ofVersion));
    }


    /**
     * Method validate input and makes {@link MultipartRequestInput} from input values. Method set
     * a moreRequest marker to false and it creates default empty {@link MultipartRequestBody}
     * by {@link MultipartType}
     *
     * @param xid
     * @param type
     * @param ofVersion
     * @param  body
     * @return
     */
    public static MultipartRequestInput makeMultipartRequestInput(final long xid, final short ofVersion,
                                                                  @Nonnull final MultipartType type,
                                                                  @Nonnull final MultipartRequestBody body) {
        return maker(xid, type, ofVersion, false, body);
    }

    /**
     * Method build {@link MultipartRequestInput} from input values. It is private because we would like
     * to validate only what is really need to be validate.
     *
     * @param xid
     * @param type
     * @param ofVersion
     * @param moreRequests
     * @param body
     * @return
     */
    private static MultipartRequestInput maker(final long xid, final MultipartType type,
                                               final short ofVersion, final boolean moreRequests, final MultipartRequestBody body) {
        final MultipartRequestInputBuilder builder = new MultipartRequestInputBuilder();
        builder.setFlags(new MultipartRequestFlags(moreRequests));
        builder.setMultipartRequestBody(body);
        builder.setVersion(ofVersion);
        builder.setType(type);
        builder.setXid(xid);
        return builder.build();
    }

    private static MultipartRequestBody makeDefaultEmptyRequestBody(@CheckForNull final MultipartType type, @CheckForNull final short version) {
        Preconditions.checkArgument(type != null, "Multipart Request can not by build without type!");
        switch (type) {
            case OFPMPDESC:
                return new MultipartRequestDescCaseBuilder().build();
            case OFPMPFLOW:
                return makeDefaultMultipartRequestFlowCase(version, BigInteger.ZERO, BigInteger.ZERO);
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

    public static MultipartRequestBody makeDefaultMultipartRequestFlowCase(@CheckForNull short version,
                                                                           @CheckForNull BigInteger cookie,
                                                                           @CheckForNull BigInteger cookieMask) {
        MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
        MultipartRequestFlowBuilder multipartRequestFlowBuilder = new MultipartRequestFlowBuilder();
        multipartRequestFlowBuilder.setTableId(OFConstants.OFPTT_ALL);
        multipartRequestFlowBuilder.setOutPort(OFConstants.OFPP_ANY);
        multipartRequestFlowBuilder.setOutGroup(OFConstants.OFPG_ANY);
        multipartRequestFlowBuilder.setCookie(cookie);
        multipartRequestFlowBuilder.setCookieMask(cookieMask);
        switch (version) {
            case OFConstants.OFP_VERSION_1_0:
                MatchV10Builder matchV10Builder = MatchUtil.createEmptyV10Match();
                multipartRequestFlowBuilder.setMatchV10(matchV10Builder.build());
                break;
            case OFConstants.OFP_VERSION_1_3:
                multipartRequestFlowBuilder.setMatch(new MatchBuilder().setType(OxmMatchType.class).build());
                break;
        }
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(multipartRequestFlowBuilder.build());
        return multipartRequestFlowCaseBuilder.build();
    }

    private static boolean validationOfMultipartTypeAndRequestBody(@CheckForNull final MultipartType type,
                                                                   @CheckForNull final MultipartRequestBody body) {
        Preconditions.checkArgument(type != null, "Multipart Request can not by build without type!");
        Preconditions.checkArgument(body != null, "Multipart Request can not by build without body!");
        switch (type) {
            case OFPMPDESC:
                return body instanceof MultipartRequestDescCase;
            case OFPMPFLOW:
                return body instanceof MultipartRequestFlowCase;
            case OFPMPAGGREGATE:
                return body instanceof MultipartRequestAggregateCase;
            case OFPMPTABLE:
                return body instanceof MultipartRequestTableCase;
            case OFPMPPORTSTATS:
                return body instanceof MultipartRequestPortStatsCase;
            case OFPMPQUEUE:
                return body instanceof MultipartRequestQueueCase;
            case OFPMPGROUP:
                return body instanceof MultipartRequestGroupCase;
            case OFPMPGROUPDESC:
                return body instanceof MultipartRequestGroupDescCase;
            case OFPMPGROUPFEATURES:
                return body instanceof MultipartRequestGroupFeaturesCase;
            case OFPMPMETER:
                return body instanceof MultipartRequestMeterCase;
            case OFPMPMETERCONFIG:
                return body instanceof MultipartRequestMeterConfigCase;
            case OFPMPMETERFEATURES:
                return body instanceof MultipartRequestMeterFeaturesCase;
            case OFPMPTABLEFEATURES:
                return body instanceof MultipartRequestTableFeaturesCase;
            case OFPMPPORTDESC:
                return body instanceof MultipartRequestPortDescCase;
            case OFPMPEXPERIMENTER:
                return body instanceof MultipartRequestExperimenterCase;
            default:
                throw new IllegalArgumentException("Unknown MultipartType " + type);
        }
    }
}
