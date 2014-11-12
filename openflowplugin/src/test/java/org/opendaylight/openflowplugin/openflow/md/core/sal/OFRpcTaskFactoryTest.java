/*
 * Copyright (c) 2014 NEC Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import org.junit.Test;

import org.opendaylight.openflowplugin.api.OFConstants;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.OriginalFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.flow.update.UpdatedFlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;

/**
 * Unit test for {@link OFRpcTaskFactory}.
 */
public class OFRpcTaskFactoryTest {
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
            assertTrue(OFRpcTaskFactory.canModifyFlow(
                           originalBuilder.build(), updatedBuilder.build(),
                           ver));

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
            Match[] matches = {null, createMatch(0x86ddL)};
            for (Match m: matches) {
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

    @Test
    public void testFlowModFlagsEquals() {
        FlowModFlags[] defaults = {
            null,
            FlowModFlags.getDefaultInstance("sENDFLOWREM"),
            new FlowModFlags(false, false, false, false, true),
            new FlowModFlags(false, null, false, null, Boolean.TRUE),
        };
        FlowModFlags all = new FlowModFlags(true, true, true, true, true);
        FlowModFlags none = new FlowModFlags(null, null, null, null, null);

        for (FlowModFlags f: defaults) {
            assertTrue(OFRpcTaskFactory.equals(f, (FlowModFlags)null));
            assertTrue(OFRpcTaskFactory.equals((FlowModFlags)null, f));
            assertFalse(OFRpcTaskFactory.equals((FlowModFlags)null, all));
            assertFalse(OFRpcTaskFactory.equals(all, (FlowModFlags)null));
            assertFalse(OFRpcTaskFactory.equals((FlowModFlags)null, none));
            assertFalse(OFRpcTaskFactory.equals(none, (FlowModFlags)null));
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
            assertFalse(OFRpcTaskFactory.equals(flags, all));
            assertFalse(OFRpcTaskFactory.equals(all, flags));
            assertFalse(OFRpcTaskFactory.equals(flags, none));
            assertFalse(OFRpcTaskFactory.equals(none, flags));

            for (String nm: bitNames) {
                FlowModFlags f = FlowModFlags.getDefaultInstance(nm);
                boolean expected = nm.equals(name);
                assertEquals(expected, OFRpcTaskFactory.equals(flags, f));
                assertEquals(expected, OFRpcTaskFactory.equals(f, flags));
            }

            boolean overlap = (bit == 0);
            boolean noByte = (bit == 1);
            boolean noPacket = (bit == 2);
            boolean reset = (bit == 3);
            boolean flowRem = (bit == 4);
            FlowModFlags f =
                new FlowModFlags(overlap, noByte, noPacket, reset, flowRem);
            assertTrue(OFRpcTaskFactory.equals(flags, f));
            assertTrue(OFRpcTaskFactory.equals(f, flags));
            assertTrue(OFRpcTaskFactory.equals(f, new FlowModFlags(f)));

            f = new FlowModFlags(!overlap, noByte, noPacket, reset, flowRem);
            assertFalse(OFRpcTaskFactory.equals(flags, f));
            f = new FlowModFlags(overlap, !noByte, noPacket, reset, flowRem);
            assertFalse(OFRpcTaskFactory.equals(flags, f));
            f = new FlowModFlags(overlap, noByte, !noPacket, reset, flowRem);
            assertFalse(OFRpcTaskFactory.equals(flags, f));
            f = new FlowModFlags(overlap, noByte, noPacket, !reset, flowRem);
            assertFalse(OFRpcTaskFactory.equals(flags, f));
            f = new FlowModFlags(overlap, noByte, noPacket, reset, !flowRem);
            assertFalse(OFRpcTaskFactory.equals(flags, f));

            bit++;
        }
    }

    private void canModifyFlowTest(boolean expected, OriginalFlowBuilder org,
                                   UpdatedFlowBuilder upd, Short version) {
        boolean result = OFRpcTaskFactory.
            canModifyFlow(org.build(), upd.build(), version);
        assertEquals(expected, result);
    }

    private Match createMatch(long etherType) {
        EthernetTypeBuilder ethType = new EthernetTypeBuilder().
            setType(new EtherType(etherType));
        EthernetMatchBuilder ether = new EthernetMatchBuilder().
            setEthernetType(ethType.build());
        return new MatchBuilder().setEthernetMatch(ether.build()).build();
    }
}
