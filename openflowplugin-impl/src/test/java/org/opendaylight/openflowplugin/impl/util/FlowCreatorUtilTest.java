/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint64;

public class FlowCreatorUtilTest {

    private static final MacAddress MAC_ADDRESS = new MacAddress("00:00:00:00:00:00");
    private static final Ipv4Address IPV_4_ADDRESS = new Ipv4Address("0.0.0.0");

    /**
     * Test method for
     * {@link FlowCreatorUtil#setWildcardedFlowMatch(short version, MultipartRequestFlowBuilder flowBuilder)}.
     */
    @Test
    public void testSetWildcardedFlowMatch_1_0() {
        MultipartRequestFlowBuilder multipartRequestFlowBuilder = new MultipartRequestFlowBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_0, multipartRequestFlowBuilder);
        MultipartRequestFlow multipartRequestFlow = multipartRequestFlowBuilder.build();
        assertMatch(multipartRequestFlow.getMatchV10());

        multipartRequestFlowBuilder = new MultipartRequestFlowBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_3, multipartRequestFlowBuilder);
        multipartRequestFlow = multipartRequestFlowBuilder.build();
        assertMatch(multipartRequestFlow.getMatch());
    }

    /**
     * Test method for
     * {@link FlowCreatorUtil#setWildcardedFlowMatch(short version, MultipartRequestAggregateBuilder aggregateBuilder)}.
     */
    @Test
    public void testSetWildcardedFlowMatch_() {
        MultipartRequestAggregateBuilder multipartRequestAggregateBuilder = new MultipartRequestAggregateBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_0, multipartRequestAggregateBuilder);
        MultipartRequestAggregate multipartRequestAggregate = multipartRequestAggregateBuilder.build();
        assertMatch(multipartRequestAggregate.getMatchV10());

        multipartRequestAggregateBuilder = new MultipartRequestAggregateBuilder();
        FlowCreatorUtil.setWildcardedFlowMatch(OFConstants.OFP_VERSION_1_3, multipartRequestAggregateBuilder);
        multipartRequestAggregate = multipartRequestAggregateBuilder.build();
        assertMatch(multipartRequestAggregate.getMatch());


    }

    /**
     * Test method for
     * {@link FlowCreatorUtil#canModifyFlow(OriginalFlow, UpdatedFlow, Short)}.
     */
    @Test
    public void testCanModifyFlow() {
        final Short of10 = Short.valueOf(OFConstants.OFP_VERSION_1_0);
        final Short of13 = Short.valueOf(OFConstants.OFP_VERSION_1_3);
        final Short[] versions = {null, of10, of13};
        final Boolean[] bools = {null, Boolean.TRUE, Boolean.FALSE};

        final Uint16 defPri = Uint16.valueOf(0x8000);
        final Uint16 defIdle = Uint16.ZERO;
        final Uint16 defHard = Uint16.ZERO;
        final FlowModFlags defFlags = FlowModFlags.getDefaultInstance("sENDFLOWREM");
        final FlowModFlags flags = new FlowModFlags(false, true, false, true, false);
        final FlowCookie defCookie = new FlowCookie(Uint64.ZERO);
        final FlowCookie cookie = new FlowCookie(Uint64.valueOf(0x12345));
        final FlowCookie cookie1 = new FlowCookie(Uint64.valueOf(0x67890));
        final FlowCookie cookieMask = new FlowCookie(Uint64.valueOf(0xffff00));

        for (final Short ver : versions) {
            final OriginalFlowBuilder originalBuilder = new OriginalFlowBuilder();
            final UpdatedFlowBuilder updatedBuilder = new UpdatedFlowBuilder();
            canModifyFlowTest(true, originalBuilder, updatedBuilder, ver);

            // Default value tests.
            canModifyFlowTest(true, new OriginalFlowBuilder().setPriority(defPri), updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder, new UpdatedFlowBuilder().setPriority(defPri), ver);
            canModifyFlowTest(true, new OriginalFlowBuilder().setIdleTimeout(defIdle), updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder, new UpdatedFlowBuilder().setIdleTimeout(defIdle), ver);
            canModifyFlowTest(true, new OriginalFlowBuilder().setHardTimeout(defHard), updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder, new UpdatedFlowBuilder().setHardTimeout(defHard), ver);
            canModifyFlowTest(false, new OriginalFlowBuilder().setFlags(defFlags), updatedBuilder, ver);
            canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder().setFlags(defFlags), ver);
            canModifyFlowTest(true, new OriginalFlowBuilder().setCookie(defCookie), updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder, new UpdatedFlowBuilder().setCookie(defCookie), ver);

            // Set non-default values.
            canModifyFlowTest(true, originalBuilder.setMatch(createMatch(0x800L)),
                              updatedBuilder.setMatch(createMatch(0x800L)), ver);
            canModifyFlowTest(true, originalBuilder.setIdleTimeout(Uint16.valueOf(600)),
                updatedBuilder.setIdleTimeout(Uint16.valueOf(600)), ver);
            canModifyFlowTest(true, originalBuilder.setHardTimeout(Uint16.valueOf(1200)),
                updatedBuilder.setHardTimeout(Uint16.valueOf(1200)), ver);
            canModifyFlowTest(true, originalBuilder.setPriority(Uint16.valueOf(100)),
                updatedBuilder.setPriority(Uint16.valueOf(100)), ver);
            canModifyFlowTest(true, originalBuilder.setFlags(flags), updatedBuilder.setFlags(flags), ver);
            canModifyFlowTest(true, originalBuilder.setCookie(cookie), updatedBuilder.setCookie(cookie), ver);

            final OriginalFlow org = originalBuilder.build();
            final UpdatedFlow upd = updatedBuilder.build();

            // Set different match.
            final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match[] matches
                    = {null, createMatch(0x86ddL)};
            for (final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match m : matches) {
                canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder(upd).setMatch(m), ver);
                canModifyFlowTest(false, new OriginalFlowBuilder(org).setMatch(m), updatedBuilder, ver);
            }

            // Set different idle-timeout, hard-timeout, priority.
            final Integer[] integers = {null, Integer.valueOf(3600)};
            for (final Integer i : integers) {
                canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder(upd).setIdleTimeout(i), ver);
                canModifyFlowTest(false, new OriginalFlowBuilder(org).setIdleTimeout(i), updatedBuilder, ver);

                canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder(upd).setHardTimeout(i), ver);
                canModifyFlowTest(false, new OriginalFlowBuilder(org).setHardTimeout(i), updatedBuilder, ver);

                canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder(upd).setPriority(i), ver);
                canModifyFlowTest(false, new OriginalFlowBuilder(org).setPriority(i), updatedBuilder, ver);
            }

            // Set different FLOW_MOD flags.
            final FlowModFlags[] flowModFlags = {null, defFlags, new FlowModFlags(true, true, true, true, true),};
            for (final FlowModFlags f : flowModFlags) {
                canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder(upd).setFlags(f), ver);
                canModifyFlowTest(false, new OriginalFlowBuilder(org).setFlags(f), updatedBuilder, ver);
            }

            // Set different cookie.
            final FlowCookie[] cookies = {null, defCookie, new FlowCookie(Uint64.valueOf(0x123456L)),};
            for (final FlowCookie c : cookies) {
                canModifyFlowTest(false, originalBuilder, new UpdatedFlowBuilder(upd).setCookie(c), ver);
                canModifyFlowTest(false, new OriginalFlowBuilder(org).setCookie(c), updatedBuilder, ver);
            }

            // Cookie mask test.
            // Cookie mask is used by OF13 non-strict MODIFY command.
            updatedBuilder.setCookie(cookie1);
            for (final Boolean strict : bools) {
                updatedBuilder.setCookieMask(null).setStrict(strict);
                canModifyFlowTest(false, originalBuilder, updatedBuilder, ver);

                updatedBuilder.setCookieMask(defCookie);
                canModifyFlowTest(false, originalBuilder, updatedBuilder, ver);

                updatedBuilder.setCookieMask(cookieMask);
                final boolean expected = of13.equals(ver) && !Boolean.TRUE.equals(strict);
                canModifyFlowTest(expected, originalBuilder, updatedBuilder, ver);
            }
        }
    }

    /**
     * Test method for
     * {@link FlowCreatorUtil#equalsFlowModFlags(FlowModFlags, FlowModFlags)}.
     */
    @Test
    public void testEqualsFlowModFlags() {
        final FlowModFlags[] defaults = {null, new FlowModFlags(false, false, false, false, false), new FlowModFlags(
                false, null, false, null, Boolean.FALSE),};
        final FlowModFlags all = new FlowModFlags(true, true, true, true, true);
        final FlowModFlags none = new FlowModFlags(null, null, null, null, null);

        for (final FlowModFlags f : defaults) {
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(f, null));
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(null, f));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(null, all));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(all, null));
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(null, none));
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(none, null));
        }

        final String[] bitNames = {"cHECKOVERLAP", "nOBYTCOUNTS", "nOPKTCOUNTS", "rESETCOUNTS", "sENDFLOWREM"};
        int bit = 0;
        for (final String name : bitNames) {
            final FlowModFlags flags = FlowModFlags.getDefaultInstance(name);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, all));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(all, flags));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, none));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(none, flags));

            for (final String nm : bitNames) {
                final FlowModFlags f = FlowModFlags.getDefaultInstance(nm);
                final boolean expected = nm.equals(name);
                assertEquals(expected, FlowCreatorUtil.equalsFlowModFlags(flags, f));
                assertEquals(expected, FlowCreatorUtil.equalsFlowModFlags(f, flags));
            }

            final boolean overlap = bit == 0;
            final boolean noByte = bit == 1;
            final boolean noPacket = bit == 2;
            final boolean reset = bit == 3;
            final boolean flowRem = bit == 4;
            FlowModFlags flowModFlags = new FlowModFlags(overlap, noByte, noPacket, reset, flowRem);
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(flags, flowModFlags));
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(flowModFlags, flags));
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(flowModFlags, new FlowModFlags(flowModFlags)));

            flowModFlags = new FlowModFlags(!overlap, noByte, noPacket, reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, flowModFlags));
            flowModFlags = new FlowModFlags(overlap, !noByte, noPacket, reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, flowModFlags));
            flowModFlags = new FlowModFlags(overlap, noByte, !noPacket, reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, flowModFlags));
            flowModFlags = new FlowModFlags(overlap, noByte, noPacket, !reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, flowModFlags));
            flowModFlags = new FlowModFlags(overlap, noByte, noPacket, reset, !flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, flowModFlags));

            bit++;
        }
    }

    /**
     * Test method for
     * {@link FlowCreatorUtil#equalsWithDefault(Object, Object, Object)}.
     */
    @Test
    public void testEqualsWithDefault() {
        // Boolean
        for (final Boolean def : new Boolean[]{Boolean.TRUE, Boolean.FALSE}) {
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(def, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, def, def));

            final Boolean inv = Boolean.valueOf(!def.booleanValue());
            assertFalse(FlowCreatorUtil.equalsWithDefault(null, inv, def));
            assertFalse(FlowCreatorUtil.equalsWithDefault(inv, null, def));
        }

        // Integer
        final Integer[] integers = {Integer.valueOf(-100), Integer.valueOf(0), Integer.valueOf(100),};
        for (final Integer def : integers) {
            final Integer same = new Integer(def.intValue());
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(same, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, same, def));

            final Integer diff = new Integer(def.intValue() + 1);
            assertFalse(FlowCreatorUtil.equalsWithDefault(null, diff, def));
            assertFalse(FlowCreatorUtil.equalsWithDefault(diff, null, def));
        }

        // String
        final String[] strings = {"", "test string 1", "test string 2",};
        for (final String def : strings) {
            final String same = new String(def);
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(same, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, same, def));

            final String diff = def + "-1";
            assertFalse(FlowCreatorUtil.equalsWithDefault(null, diff, def));
            assertFalse(FlowCreatorUtil.equalsWithDefault(diff, null, def));
        }
    }

    private static void assertMatch(final Match match) {
        assertTrue(match.getType().getClass().isInstance(OxmMatchType.class));
    }

    private static void assertMatch(final MatchV10 matchV10) {
        assertEquals(matchV10.getDlDst(), MAC_ADDRESS);
        assertEquals(matchV10.getDlSrc(), MAC_ADDRESS);

        assertTrue(matchV10.getNwSrcMask().shortValue() == 0);
        assertTrue(matchV10.getNwDstMask().shortValue() == 0);

        assertTrue(matchV10.getInPort().intValue() == 0);
        assertTrue(matchV10.getDlVlan().intValue() == 0);
        assertTrue(matchV10.getDlVlanPcp().shortValue() == 0);
        assertTrue(matchV10.getDlType().intValue() == 0);

        assertTrue(matchV10.getNwTos().shortValue() == 0);
        assertTrue(matchV10.getNwProto().shortValue() == 0);

        assertEquals(matchV10.getNwSrc(), IPV_4_ADDRESS);
        assertEquals(matchV10.getNwDst(), IPV_4_ADDRESS);

        assertTrue(matchV10.getTpSrc().intValue() == 0);
        assertTrue(matchV10.getTpDst().intValue() == 0);
    }

    /**
     * Verify that {@link FlowCreatorUtil#canModifyFlow(OriginalFlow, UpdatedFlow, Short)}
     * returns expected value.
     *
     * @param expected An expected return value.
     * @param org      A original flow builder that contains original flow to be tested.
     * @param upd      An updated flow builder that contains updated flow to be tested.
     * @param version  OpenFlow protocol version.
     */
    private static void canModifyFlowTest(final boolean expected, final OriginalFlowBuilder org,
                                          final UpdatedFlowBuilder upd, final Short version) {
        final boolean result = FlowCreatorUtil.canModifyFlow(org.build(), upd.build(), version);
        assertEquals(expected, result);
    }

    /**
     * Create a flow match that specifies ethernet type.
     *
     * @param etherType An ethernet type value.
     * @return A flow match that specifies the given ethernet type.
     */
    private static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match createMatch(
            final long etherType) {
        final EthernetTypeBuilder ethType = new EthernetTypeBuilder().setType(new EtherType(etherType));
        final EthernetMatchBuilder ether = new EthernetMatchBuilder().setEthernetType(ethType.build());
        return new MatchBuilder().setEthernetMatch(ether.build()).build();
    }
}
