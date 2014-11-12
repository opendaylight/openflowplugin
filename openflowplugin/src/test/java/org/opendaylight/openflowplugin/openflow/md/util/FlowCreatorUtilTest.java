/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.aggregate._case.MultipartRequestAggregateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

public class FlowCreatorUtilTest {

    private static final MacAddress macAddress = new MacAddress("00:00:00:00:00:00");
    private static final Ipv4Address ipv4Address = new Ipv4Address("0.0.0.0");

    /**
     * Test method for {@link FlowCreatorUtil#setWildcardedFlowMatch(short version, MultipartRequestFlowBuilder flowBuilder)}.
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
     * Test method for {@link FlowCreatorUtil#setWildcardedFlowMatch(short version, MultipartRequestAggregateBuilder aggregateBuilder)}.
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
        Short of10 = Short.valueOf(OFConstants.OFP_VERSION_1_0);
        Short of13 = Short.valueOf(OFConstants.OFP_VERSION_1_3);
        Short[] versions = {null, of10, of13};
        Boolean[] bools = {null, Boolean.TRUE, Boolean.FALSE};

        Integer defPri = Integer.valueOf(0x8000);
        Integer defIdle = Integer.valueOf(300);
        Integer defHard = Integer.valueOf(600);
        FlowModFlags defFlags = FlowModFlags.getDefaultInstance("sENDFLOWREM");
        FlowModFlags flags = new FlowModFlags(false, true, false, true, false);
        FlowCookie defCookie = new FlowCookie(BigInteger.ZERO);
        FlowCookie cookie = new FlowCookie(BigInteger.valueOf(0x12345L));
        FlowCookie cookie1 = new FlowCookie(BigInteger.valueOf(0x67890L));
        FlowCookie cookieMask = new FlowCookie(BigInteger.valueOf(0xffff00L));

        for (Short ver: versions) {
            OriginalFlowBuilder originalBuilder = new OriginalFlowBuilder();
            UpdatedFlowBuilder updatedBuilder = new UpdatedFlowBuilder();
            canModifyFlowTest(true, originalBuilder, updatedBuilder, ver);

            // Default value tests.
            canModifyFlowTest(true,
                              new OriginalFlowBuilder().setPriority(defPri),
                              updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder,
                              new UpdatedFlowBuilder().setPriority(defPri),
                              ver);
            canModifyFlowTest(true,
                              new OriginalFlowBuilder().setIdleTimeout(defIdle),
                              updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder,
                              new UpdatedFlowBuilder().setIdleTimeout(defIdle),
                              ver);
            canModifyFlowTest(true,
                              new OriginalFlowBuilder().setHardTimeout(defHard),
                              updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder,
                              new UpdatedFlowBuilder().setHardTimeout(defHard),
                              ver);
            canModifyFlowTest(true,
                              new OriginalFlowBuilder().setFlags(defFlags),
                              updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder,
                              new UpdatedFlowBuilder().setFlags(defFlags),
                              ver);
            canModifyFlowTest(true,
                              new OriginalFlowBuilder().setCookie(defCookie),
                              updatedBuilder, ver);
            canModifyFlowTest(true, originalBuilder,
                              new UpdatedFlowBuilder().setCookie(defCookie),
                              ver);

            // Set non-default values.
            canModifyFlowTest(true,
                              originalBuilder.setMatch(createMatch(0x800L)),
                              updatedBuilder.setMatch(createMatch(0x800L)),
                              ver);
            canModifyFlowTest(true, originalBuilder.setIdleTimeout(600),
                              updatedBuilder.setIdleTimeout(600), ver);
            canModifyFlowTest(true, originalBuilder.setHardTimeout(1200),
                              updatedBuilder.setHardTimeout(1200), ver);
            canModifyFlowTest(true, originalBuilder.setPriority(100),
                              updatedBuilder.setPriority(100), ver);
            canModifyFlowTest(true, originalBuilder.setFlags(flags),
                              updatedBuilder.setFlags(flags), ver);
            canModifyFlowTest(true, originalBuilder.setCookie(cookie),
                              updatedBuilder.setCookie(cookie), ver);

            OriginalFlow org = originalBuilder.build();
            UpdatedFlow upd = updatedBuilder.build();

            // Set different match.
            org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match[] matches = {null, createMatch(0x86ddL)};
            for (org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match m: matches) {
                canModifyFlowTest(false, originalBuilder,
                                  new UpdatedFlowBuilder(upd).setMatch(m),
                                  ver);
                canModifyFlowTest(false,
                                  new OriginalFlowBuilder(org).setMatch(m),
                                  updatedBuilder, ver);
            }

            // Set different idle-timeout, hard-timeout, priority.
            Integer[] integers = {null, Integer.valueOf(3600)};
            for (Integer i: integers) {
                canModifyFlowTest(false, originalBuilder,
                                  new UpdatedFlowBuilder(upd).setIdleTimeout(i),
                                  ver);
                canModifyFlowTest(false,
                                  new OriginalFlowBuilder(org).setIdleTimeout(i),
                                  updatedBuilder, ver);

                canModifyFlowTest(false, originalBuilder,
                                  new UpdatedFlowBuilder(upd).setHardTimeout(i),
                                  ver);
                canModifyFlowTest(false,
                                  new OriginalFlowBuilder(org).setHardTimeout(i),
                                  updatedBuilder, ver);

                canModifyFlowTest(false, originalBuilder,
                                  new UpdatedFlowBuilder(upd).setPriority(i),
                                  ver);
                canModifyFlowTest(false,
                                  new OriginalFlowBuilder(org).setPriority(i),
                                  updatedBuilder, ver);
            }

            // Set different FLOW_MOD flags.
            FlowModFlags[] flowModFlags = {
                null,
                defFlags,
                new FlowModFlags(true, true, true, true, true),
            };
            for (FlowModFlags f: flowModFlags) {
                canModifyFlowTest(false, originalBuilder,
                                  new UpdatedFlowBuilder(upd).setFlags(f),
                                  ver);
                canModifyFlowTest(false,
                                  new OriginalFlowBuilder(org).setFlags(f),
                                  updatedBuilder, ver);
            }

            // Set different cookie.
            FlowCookie[] cookies = {
                null,
                defCookie,
                new FlowCookie(BigInteger.valueOf(0x123456L)),
            };
            for (FlowCookie c: cookies) {
                canModifyFlowTest(false, originalBuilder,
                                  new UpdatedFlowBuilder(upd).setCookie(c),
                                  ver);
                canModifyFlowTest(false,
                                  new OriginalFlowBuilder(org).setCookie(c),
                                  updatedBuilder, ver);
            }

            // Cookie mask test.
            // Cookie mask is used by OF13 non-strict MODIFY command.
            updatedBuilder.setCookie(cookie1);
            for (Boolean strict: bools) {
                updatedBuilder.setCookieMask(null).setStrict(strict);
                canModifyFlowTest(false, originalBuilder, updatedBuilder, ver);

                updatedBuilder.setCookieMask(defCookie);
                canModifyFlowTest(false, originalBuilder, updatedBuilder, ver);

                updatedBuilder.setCookieMask(cookieMask);
                boolean expected = (of13.equals(ver) &&
                                    !Boolean.TRUE.equals(strict));
                canModifyFlowTest(expected, originalBuilder, updatedBuilder,
                                  ver);
            }
        }
    }

    /**
     * Test method for
     * {@link FlowCreatorUtil#equalsFlowModFlags(FlowModFlags, FlowModFlags)}.
     */
    @Test
    public void testEqualsFlowModFlags() {
        FlowModFlags[] defaults = {
            null,
            FlowModFlags.getDefaultInstance("sENDFLOWREM"),
            new FlowModFlags(false, false, false, false, true),
            new FlowModFlags(false, null, false, null, Boolean.TRUE),
        };
        FlowModFlags all = new FlowModFlags(true, true, true, true, true);
        FlowModFlags none = new FlowModFlags(null, null, null, null, null);

        for (FlowModFlags f: defaults) {
            assertTrue(FlowCreatorUtil.
                       equalsFlowModFlags(f, (FlowModFlags)null));
            assertTrue(FlowCreatorUtil.
                       equalsFlowModFlags((FlowModFlags)null, f));
            assertFalse(FlowCreatorUtil.
                        equalsFlowModFlags((FlowModFlags)null, all));
            assertFalse(FlowCreatorUtil.
                        equalsFlowModFlags(all, (FlowModFlags)null));
            assertFalse(FlowCreatorUtil.
                        equalsFlowModFlags((FlowModFlags)null, none));
            assertFalse(FlowCreatorUtil.
                        equalsFlowModFlags(none, (FlowModFlags)null));
        }

        String[] bitNames = {
            "cHECKOVERLAP",
            "nOBYTCOUNTS",
            "nOPKTCOUNTS",
            "rESETCOUNTS",
            "sENDFLOWREM"
        };
        int bit = 0;
        for (String name: bitNames) {
            FlowModFlags flags = FlowModFlags.getDefaultInstance(name);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, all));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(all, flags));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, none));
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(none, flags));

            for (String nm: bitNames) {
                FlowModFlags f = FlowModFlags.getDefaultInstance(nm);
                boolean expected = nm.equals(name);
                assertEquals(expected,
                             FlowCreatorUtil.equalsFlowModFlags(flags, f));
                assertEquals(expected,
                             FlowCreatorUtil.equalsFlowModFlags(f, flags));
            }

            boolean overlap = (bit == 0);
            boolean noByte = (bit == 1);
            boolean noPacket = (bit == 2);
            boolean reset = (bit == 3);
            boolean flowRem = (bit == 4);
            FlowModFlags f =
                new FlowModFlags(overlap, noByte, noPacket, reset, flowRem);
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(flags, f));
            assertTrue(FlowCreatorUtil.equalsFlowModFlags(f, flags));
            assertTrue(FlowCreatorUtil.
                       equalsFlowModFlags(f, new FlowModFlags(f)));

            f = new FlowModFlags(!overlap, noByte, noPacket, reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, f));
            f = new FlowModFlags(overlap, !noByte, noPacket, reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, f));
            f = new FlowModFlags(overlap, noByte, !noPacket, reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, f));
            f = new FlowModFlags(overlap, noByte, noPacket, !reset, flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, f));
            f = new FlowModFlags(overlap, noByte, noPacket, reset, !flowRem);
            assertFalse(FlowCreatorUtil.equalsFlowModFlags(flags, f));

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
        for (Boolean def: new Boolean[]{Boolean.TRUE, Boolean.FALSE}) {
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(def, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, def, def));

            Boolean inv = Boolean.valueOf(!def.booleanValue());
            assertFalse(FlowCreatorUtil.equalsWithDefault(null, inv, def));
            assertFalse(FlowCreatorUtil.equalsWithDefault(inv, null, def));
        }

        // Integer
        Integer[] integers = {
            Integer.valueOf(-100),
            Integer.valueOf(0),
            Integer.valueOf(100),
        };
        for (Integer def: integers) {
            Integer same = new Integer(def.intValue());
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(same, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, same, def));

            Integer diff = new Integer(def.intValue() +1);
            assertFalse(FlowCreatorUtil.equalsWithDefault(null, diff, def));
            assertFalse(FlowCreatorUtil.equalsWithDefault(diff, null, def));
        }

        // String
        String[] strings = {
            "",
            "test string 1",
            "test string 2",
        };
        for (String def: strings) {
            String same = new String(def);
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(same, null, def));
            assertTrue(FlowCreatorUtil.equalsWithDefault(null, same, def));

            String diff = def + "-1";
            assertFalse(FlowCreatorUtil.equalsWithDefault(null, diff, def));
            assertFalse(FlowCreatorUtil.equalsWithDefault(diff, null, def));
        }
    }

    private void assertMatch(Match match) {
        assertTrue(match.getType().getClass().isInstance(OxmMatchType.class));
    }

    private void assertMatch(MatchV10 matchV10) {
        assertEquals(matchV10.getDlDst(), macAddress);
        assertEquals(matchV10.getDlSrc(), macAddress);

        assertTrue(matchV10.getNwSrcMask().shortValue() == 0);
        assertTrue(matchV10.getNwDstMask().shortValue() == 0);

        assertTrue(matchV10.getInPort().intValue() == 0);
        assertTrue(matchV10.getDlVlan().intValue() == 0);
        assertTrue(matchV10.getDlVlanPcp().shortValue() == 0);
        assertTrue(matchV10.getDlType().intValue() == 0);

        assertTrue(matchV10.getNwTos().shortValue() == 0);
        assertTrue(matchV10.getNwProto().shortValue() == 0);

        assertEquals(matchV10.getNwSrc(), ipv4Address);
        assertEquals(matchV10.getNwDst(), ipv4Address);

        assertTrue(matchV10.getTpSrc().intValue() == 0);
        assertTrue(matchV10.getTpDst().intValue() == 0);
    }

    /**
     * Verify that {@link FlowCreatorUtil#canModifyFlow(OriginalFlow, UpdatedFlow, Short)}
     * returns expected value.
     *
     * @param expected
     *     An expected return value.
     * @param org
     *     A original flow builder that contains original flow to be tested.
     * @param upd
     *     An updated flow builder that contains updated flow to be tested.
     * @param version
     *     OpenFlow protocol version.
     */
    private void canModifyFlowTest(boolean expected, OriginalFlowBuilder org,
                                   UpdatedFlowBuilder upd, Short version) {
        boolean result = FlowCreatorUtil.
            canModifyFlow(org.build(), upd.build(), version);
        assertEquals(expected, result);
    }

    /**
     * Create a flow match that specifies ethernet type.
     *
     * @param etherType  An ethernet type value.
     * @return  A flow match that specifies the given ethernet type.
     */
    private org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match createMatch(long etherType) {
        EthernetTypeBuilder ethType = new EthernetTypeBuilder().
            setType(new EtherType(etherType));
        EthernetMatchBuilder ether = new EthernetMatchBuilder().
            setEthernetType(ethType.build());
        return new MatchBuilder().setEthernetMatch(ether.build()).build();
    }
}
