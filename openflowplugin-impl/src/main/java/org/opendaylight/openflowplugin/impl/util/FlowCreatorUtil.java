/*
 * Copyright (c) 2013, 2015 IBM Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import java.util.Objects;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow.FlowConvertor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class FlowCreatorUtil {
    /**
     * Default FLOW_MOD flags.
     */
    public static final FlowModFlags DEFAULT_FLOW_MOD_FLAGS = new FlowModFlags(
            FlowConvertor.DEFAULT_OFPFF_CHECK_OVERLAP, FlowConvertor.DEFAULT_OFPFF_NO_BYT_COUNTS,
            FlowConvertor.DEFAULT_OFPFF_NO_PKT_COUNTS, FlowConvertor.DEFAULT_OFPFF_RESET_COUNTS,
            FlowConvertor.DEFAULT_OFPFF_FLOW_REM);

    private FlowCreatorUtil() {
        throw new AssertionError("FlowCreatorUtil is not expected to be instantiated.");
    }

    public static void setWildcardedFlowMatch(final short version, final MultipartRequestFlowBuilder flowBuilder) {
        if (version == OFConstants.OFP_VERSION_1_0) {
            flowBuilder.setMatchV10(createWildcardedMatchV10());
        }
        if (version == OFConstants.OFP_VERSION_1_3) {
            flowBuilder.setMatch(createWildcardedMatch());
        }
    }

    public static void setWildcardedFlowMatch(final short version,
            final MultipartRequestAggregateBuilder aggregateBuilder) {
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
    // FIXME: make this a constant
    public static MatchV10 createWildcardedMatchV10() {
        return new MatchV10Builder()
            .setWildcards(new FlowWildcardsV10(true, true, true, true, true, true, true, true, true, true))
            .setNwSrcMask(Uint8.ZERO)
            .setNwDstMask(Uint8.ZERO)
            .setInPort(Uint16.ZERO)
            .setDlSrc(new MacAddress("00:00:00:00:00:00"))
            .setDlDst(new MacAddress("00:00:00:00:00:00"))
            .setDlVlan(Uint16.ZERO)
            .setDlVlanPcp(Uint8.ZERO)
            .setDlType(Uint16.ZERO)
            .setNwTos(Uint8.ZERO)
            .setNwProto(Uint8.ZERO)
            .setNwSrc(new Ipv4Address("0.0.0.0"))
            .setNwDst(new Ipv4Address("0.0.0.0"))
            .setTpSrc(Uint16.ZERO)
            .setTpDst(Uint16.ZERO)
            .build();
    }

    public static Match createWildcardedMatch() {
        return new MatchBuilder().setType(OxmMatchType.class).build();
    }

    /**
     * Determine whether a flow entry can be modified or not.
     *
     * @param original An original flow entry.
     * @param updated  An updated flow entry.
     * @param version  Protocol version.
     * @return {@code true} only if a flow entry can be modified.
     */
    public static boolean canModifyFlow(final OriginalFlow original, final UpdatedFlow updated, final Short version) {
        // FLOW_MOD does not change match, priority, idle_timeout, hard_timeout,
        // flags, and cookie.
        if (!Objects.equals(original.getMatch(), updated.getMatch()) || !equalsWithDefault(original.getPriority(),
                                                                                           updated.getPriority(),
                                                                                           FlowConvertor
                                                                                                   .DEFAULT_PRIORITY)
                || !equalsWithDefault(original.getIdleTimeout(), updated.getIdleTimeout(),
                                      FlowConvertor.DEFAULT_IDLE_TIMEOUT) || !equalsWithDefault(
                original.getHardTimeout(), updated.getHardTimeout(), FlowConvertor.DEFAULT_HARD_TIMEOUT)
                || !equalsFlowModFlags(original.getFlags(), updated.getFlags())) {
            return false;
        }

        if (!Boolean.TRUE.equals(updated.getStrict()) && version != null
                && version.shortValue() != OFConstants.OFP_VERSION_1_0) {
            FlowCookie cookieMask = updated.getCookieMask();
            if (cookieMask != null) {
                Uint64 mask = cookieMask.getValue();
                if (mask != null && mask.longValue() != 0) {
                    // Allow FLOW_MOD with filtering by cookie.
                    return true;
                }
            }
        }

        FlowCookie oc = original.getCookie();
        FlowCookie uc = updated.getCookie();
        Uint64 orgCookie;
        Uint64 updCookie;
        if (oc == null) {
            if (uc == null) {
                return true;
            }

            orgCookie = OFConstants.DEFAULT_COOKIE;
            updCookie = uc.getValue();
        } else {
            orgCookie = oc.getValue();
            updCookie = uc == null ? OFConstants.DEFAULT_COOKIE : uc.getValue();
        }

        return equalsWithDefault(orgCookie, updCookie, OFConstants.DEFAULT_COOKIE);
    }

    /**
     * Return {@code true} only if given two FLOW_MOD flags are identical.
     *
     * @param flags1 A value to be compared.
     * @param flags2 A value to be compared.
     * @return {@code true} only if {@code flags1} and {@code flags2} are identical.
     */
    public static boolean equalsFlowModFlags(final FlowModFlags flags1, final FlowModFlags flags2) {
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
            f2 = flags2 == null ? DEFAULT_FLOW_MOD_FLAGS : flags2;
        }

        return equalsWithDefault(f1.getCHECKOVERLAP(), f2.getCHECKOVERLAP(), Boolean.FALSE)
            && equalsWithDefault(f1.getNOBYTCOUNTS(), f2.getNOBYTCOUNTS(), Boolean.FALSE)
            && equalsWithDefault(f1.getNOPKTCOUNTS(), f2.getNOPKTCOUNTS(), Boolean.FALSE)
            && equalsWithDefault(f1.getRESETCOUNTS(), f2.getRESETCOUNTS(), Boolean.FALSE)
            && equalsWithDefault(f1.getSENDFLOWREM(), f2.getSENDFLOWREM(), Boolean.FALSE);
    }

    /**
     * Return {@code true} only if given two values are identical.
     *
     * @param value1 A value to be compared.
     * @param value2 A value to be compared.
     * @param def    Default value. This value is used if {@code null} is passed to
     *               {@code value1} or {@code value2}.
     * @param <T>    Type of values.
     * @return {@code true} only if {@code value1} and {@code value2} are identical.
     */
    public static <T> boolean equalsWithDefault(final T value1, final T value2, final T def) {
        if (value1 == null) {
            return value2 == null || value2.equals(def);
        } else if (value2 == null) {
            return value1.equals(def);
        }

        return value1.equals(value2);
    }
}
