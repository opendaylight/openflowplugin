/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.action;

import java.math.BigInteger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.SetNwDstActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.nw.dst.action._case.SetNwDstActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.Address;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.ActionBuilder;

/**
 * match conversion and injection test
 */
public class ActionSetNwDstReactorTest {

    private Address[] addresses;

    /**
     * prepare input match
     */
    @Before
    public void setUp() {
        addresses = new Address[]{
                new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.10.1/32")).build(),
                new Ipv4Builder().setIpv4Address(new Ipv4Prefix("10.0.10.1/16")).build(),
                new Ipv6Builder().setIpv6Address(new Ipv6Prefix("1234:5678:9abc:def1:2345:6789:abcd:ef12/128")).build(),
                new Ipv6Builder().setIpv6Address(new Ipv6Prefix("1234:5678:9abc:def1:2345:6789:abcd:ef12/42")).build(),
        };
    }

    /**
     * convert for OF-1.3, inject into {@link ActionBuilder}
     */
    @Test
    public void testMatchConvertorV13_flow() {
        final ActionBuilder target = new ActionBuilder();
        for (final Address address : addresses) {
            final SetNwDstActionCase action = prepareSetNwDstActionCase(address);
            ActionSetNwDstReactor.getInstance().convert(action,
                    OFConstants.OFP_VERSION_1_3, target, BigInteger.ONE);
            final Object mEntry = target.getActionChoice();
/*
            Assert.assertNotNull(mEntry);
            if (address instanceof Ipv4) {
                Ipv4DstCase ipv4DstCase = ((Ipv4DstCase) mEntry.getMatchEntryValue());
                Assert.assertNotNull(ipv4DstCase.getIpv4Dst());
            } else if (address instanceof Ipv6) {
                Ipv6DstCase ipv6DstCase = ((Ipv6DstCase) mEntry.getMatchEntryValue());
                Assert.assertNotNull(ipv6DstCase.getIpv6Dst());
            } else {
                Assert.fail("not tested yet: " + address.getClass().getName());
            }
*/
        }
    }

    /**
     * @param address
     * @return
     */
    private static SetNwDstActionCase prepareSetNwDstActionCase(final Address address) {
        return new SetNwDstActionCaseBuilder().setSetNwDstAction(
                new SetNwDstActionBuilder().setAddress(address).build()).build();
    }

    /**
     * convert for OF-1.0, inject into {@link ActionBuilder}
     */
    @Test
    public void testMatchConvertorV10_flow() {
        final ActionBuilder target = new ActionBuilder();
        for (final Address address : addresses) {
            final SetNwDstActionCase action = prepareSetNwDstActionCase(address);

            if (address instanceof Ipv4) {
                ActionSetNwDstReactor.getInstance().convert(action,
                        OFConstants.OFP_VERSION_1_0, target, BigInteger.ONE);
//                Assert.assertNotNull(target.getAugmentation(IpAddressAction.class).getIpAddress());
            } else {
                try {
                    ActionSetNwDstReactor.getInstance().convert(action,
                            OFConstants.OFP_VERSION_1_0, target, BigInteger.ONE);
                    Assert.fail("address of this type must not pass the reactor: " + address.getClass().getName());
                } catch (final Exception e) {
                    //expected
                    Assert.assertEquals(IllegalArgumentException.class, e.getClass());
                }
            }
        }
    }
}
