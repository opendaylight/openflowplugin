/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.statistics.manager.impl.helper;

import static org.junit.Assert.assertEquals;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.junit.Ignore;
import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.junit.BeforeClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;

public class MatchComparatorFactoryTest {

    private static Match nullMatch;
    private static Match storedMatch;
    private static Match statsMatch;

    @BeforeClass
    public static void initialization() {
        statsMatch = prepareMatch(1, (long)1, (short)1, (long)1);
        // copyStatsMatch = prepareMatch(1);
        storedMatch = prepareMatch(2, (long) 2, (short) 2, (long)2);
        nullMatch = new MatchBuilder().build();
    }

    private static Match prepareMatch(int vlanId, long tunelId, short mplsBos, long metadata) {
        final MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setVlanMatch(new VlanMatchBuilder().setVlanId(
                new VlanIdBuilder().setVlanId(new VlanId(vlanId)).build()).build());
        matchBuilder.setTunnel(new TunnelBuilder().setTunnelId(BigInteger.valueOf(tunelId)).build());
        matchBuilder.setProtocolMatchFields(new ProtocolMatchFieldsBuilder().setMplsBos(mplsBos).build());
        matchBuilder.setMetadata(new MetadataBuilder().setMetadata(BigInteger.valueOf(metadata)).build());
        return matchBuilder.build();
    }

    @Ignore
    @Test
    public void ethernetComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createEthernet();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void icmpv4ComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createIcmpv4();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void inPhyPortComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createInPhyPort();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void inPortComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createInPort();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void ipComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createIp();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void l3ComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createL3();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void l4ComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createL4();
        compareViaComparator(simpleComparator);
    }

    @Ignore
    @Test
    public void nullComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createNull();
        compareViaComparator(simpleComparator);
    }

    @Test
    public void metadataComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createMetadata();
        compareViaComparator(simpleComparator);
    }

    @Test
    public void protocolMatchFieldsComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createProtocolMatchFields();
        compareViaComparator(simpleComparator);
    }

    @Test
    public void tunnelComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createTunnel();
        compareViaComparator(simpleComparator);
    }

    @Test
    public void vlanComparationTest() {
        final SimpleComparator<Match> simpleComparator = MatchComparatorFactory.createVlan();
        compareViaComparator(simpleComparator);
    }

    private void compareViaComparator(SimpleComparator<Match> simpleComparator) {
        assertEquals(true, simpleComparator.areObjectsEqual(nullMatch, nullMatch));
        assertEquals(false, simpleComparator.areObjectsEqual(statsMatch, nullMatch));
        assertEquals(false, simpleComparator.areObjectsEqual(statsMatch, storedMatch));
        assertEquals(true, simpleComparator.areObjectsEqual(statsMatch, statsMatch));
    }

}
