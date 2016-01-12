/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.util;

import java.math.BigInteger;
import java.util.Objects;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public final class FlowCreatorUtil {
    /**
     * Default FLOW_MOD flags.
     */
    public static final FlowModFlags  DEFAULT_FLOW_MOD_FLAGS =
        new FlowModFlags(FlowConvertor.DEFAULT_OFPFF_CHECK_OVERLAP,
                         FlowConvertor.DEFAULT_OFPFF_NO_BYT_COUNTS,
                         FlowConvertor.DEFAULT_OFPFF_NO_PKT_COUNTS,
                         FlowConvertor.DEFAULT_OFPFF_RESET_COUNTS,
                         FlowConvertor.DEFAULT_OFPFF_FLOW_REM);

    private FlowCreatorUtil() {
        throw new AssertionError("FlowCreatorUtil is not expected to be instantiated.");
    }

    public static void setWildcardedFlowMatch(short version, MultipartRequestFlowBuilder flowBuilder) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            flowBuilder.setMatchV10(createWildcardedMatchV10());
        }
        if (version == OFConstants.OFP_VERSION_1_3) {
            flowBuilder.setMatch(createWildcardedMatch());
        }
    }

    public static void setWildcardedFlowMatch(short version, MultipartRequestAggregateBuilder aggregateBuilder) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            aggregateBuilder.setMatchV10(createWildcardedMatchV10());
        }
        if (version == OFConstants.OFP_VERSION_1_3) {
            aggregateBuilder.setMatch(createWildcardedMatch());
        }
    }

    /**
     * Method creates openflow 1.0 format match, that can match all the flow entries.
     *
     * @return V10 Match object
     */
    public static MatchV10 createWildcardedMatchV10() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        builder.setNwSrcMask((short) 0);
        builder.setNwDstMask((short) 0);
        builder.setInPort(0);
        builder.setDlSrc(new MacAddress("00:00:00:00:00:00"));
        builder.setDlDst(new MacAddress("00:00:00:00:00:00"));
        builder.setDlVlan(0);
        builder.setDlVlanPcp((short) 0);
        builder.setDlType(0);
        builder.setNwTos((short) 0);
        builder.setNwProto((short) 0);
        builder.setNwSrc(new Ipv4Address("0.0.0.0"));
        builder.setNwDst(new Ipv4Address("0.0.0.0"));
        builder.setTpSrc(0);
        builder.setTpDst(0);
        return builder.build();
    }

    public static Match createWildcardedMatch() {
        return new MatchBuilder().setType(OxmMatchType.class).build();
    }

    /**
     * Determine whether a flow entry can be modified or not.
     *
     * @param original  An original flow entry.
     * @param updated   An updated flow entry.
     * @param version   Protocol version.
     * @return  {@code true} only if a flow entry can be modified.
     */
    public static boolean canModifyFlow(OriginalFlow original,
                                        UpdatedFlow updated, Short version) {
        // FLOW_MOD does not change match, priority, idle_timeout, hard_timeout,
        // flags, and cookie.
        if (!Objects.equals(original.getMatch(), updated.getMatch()) ||
            !equalsWithDefault(original.getPriority(), updated.getPriority(),
                               FlowConvertor.DEFAULT_PRIORITY) ||
            !equalsWithDefault(original.getIdleTimeout(),
                               updated.getIdleTimeout(),
                               FlowConvertor.DEFAULT_IDLE_TIMEOUT) ||
            !equalsWithDefault(original.getHardTimeout(),
                               updated.getHardTimeout(),
                               FlowConvertor.DEFAULT_HARD_TIMEOUT) ||
            !equalsFlowModFlags(original.getFlags(), updated.getFlags())) {
            return false;
        }

        if (!Boolean.TRUE.equals(updated.isStrict()) &&
            version != null &&
            version.shortValue() != OFConstants.OFP_VERSION_1_0) {
            FlowCookie cookieMask = updated.getCookieMask();
            if (cookieMask != null) {
                BigInteger mask = cookieMask.getValue();
                if (mask != null && !mask.equals(BigInteger.ZERO)) {
                    // Allow FLOW_MOD with filtering by cookie.
                    return true;
                }
            }
        }

        FlowCookie oc = original.getCookie();
        FlowCookie uc = updated.getCookie();
        BigInteger orgCookie;
        BigInteger updCookie;
        if (oc == null) {
            if (uc == null) {
                return true;
            }

            orgCookie = OFConstants.DEFAULT_COOKIE;
            updCookie = uc.getValue();
        } else {
            orgCookie = oc.getValue();
            updCookie = (uc == null)
                ? OFConstants.DEFAULT_COOKIE : uc.getValue();
        }

        return equalsWithDefault(orgCookie, updCookie,
                                 OFConstants.DEFAULT_COOKIE);
    }

    /**
     * Return {@code true} only if given two FLOW_MOD flags are identical.
     *
     * @param flags1 A value to be compared.
     * @param flags2 A value to be compared.
     * @return
     *   {@code true} only if {@code flags1} and {@code flags2} are identical.
     */
    public static boolean equalsFlowModFlags(FlowModFlags flags1,
                                             FlowModFlags flags2) {
        FlowModFlags f1;
        FlowModFlags f2;
        if (flags1 == null) {
            if (flags2 == null) {
                return true;
            }

            f1 = DEFAULT_FLOW_MOD_FLAGS;
            f2 = flags2;
        } else {
            f1 = flags1;
            f2 = (flags2 == null) ? DEFAULT_FLOW_MOD_FLAGS : flags2;
        }

        return equalsWithDefault(f1.isCHECKOVERLAP(), f2.isCHECKOVERLAP(),
                                 Boolean.FALSE) &&
            equalsWithDefault(f1.isNOBYTCOUNTS(), f2.isNOBYTCOUNTS(),
                              Boolean.FALSE) &&
            equalsWithDefault(f1.isNOPKTCOUNTS(), f2.isNOPKTCOUNTS(),
                              Boolean.FALSE) &&
            equalsWithDefault(f1.isRESETCOUNTS(), f2.isRESETCOUNTS(),
                              Boolean.FALSE) &&
            equalsWithDefault(f1.isSENDFLOWREM(), f2.isSENDFLOWREM(),
                              Boolean.FALSE);
    }

    /**
     * Return {@code true} only if given two values are identical.
     *
     * @param value1 A value to be compared.
     * @param value2 A value to be compared.
     * @param def
     *    Default value. This value is used if {@code null} is passed to
     *    {@code value1} or {@code value2}.
     * @param <T> Type of values.
     * @return
     *   {@code true} only if {@code value1} and {@code value2} are identical.
     */
    public static <T> boolean equalsWithDefault(T value1, T value2, T def) {
        if (value1 == null) {
            return value2 == null || value2.equals(def);
        } else if (value2 == null) {
            return value1.equals(def);
        }

        return value1.equals(value2);
    }
}
