/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataObjectModification;
import org.opendaylight.controller.md.sal.binding.api.DataTreeIdentifier;
import org.opendaylight.controller.md.sal.binding.api.DataTreeModification;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * Test for {@link SnapshotElicitRegistry}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SnapshotElicitRegistryTest {

    public static final NodeId NODE_ID = new NodeId("testNode");
    private SnapshotElicitRegistry snapshotElicitRegistry;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(SnapshotElicitRegistry.DATE_AND_TIME_FORMAT);

    @Mock
    private DataTreeModification<Node> modification;
    @Mock
    private FlowCapableStatisticsGatheringStatus statisticsGatheringStatus;
    @Mock
    private Map<NodeId, Date> registration;

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(NODE_ID));
        final DataTreeIdentifier<Node> dataTreeIdentifier =
                new DataTreeIdentifier<>(LogicalDatastoreType.OPERATIONAL, nodePath);
        final DataObjectModification<Node> operationalModification = Mockito.mock(DataObjectModification.class);
        final Node operationalNode = Mockito.mock(Node.class);
        final SnapshotGatheringStatusEnd snapshotGatheringStatusEnd = Mockito.mock(SnapshotGatheringStatusEnd.class);
        final DateAndTime timestamp = Mockito.mock(DateAndTime.class);

        snapshotElicitRegistry = new SnapshotElicitRegistry();

        Mockito.when(modification.getRootNode()).thenReturn(operationalModification);
        Mockito.when(modification.getRootPath()).thenReturn(dataTreeIdentifier);
        Mockito.when(operationalModification.getDataAfter()).thenReturn(operationalNode);
        Mockito.when(operationalNode.getAugmentation(FlowCapableStatisticsGatheringStatus.class)).thenReturn(statisticsGatheringStatus);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd()).thenReturn(snapshotGatheringStatusEnd);
        Mockito.when(snapshotGatheringStatusEnd.getEnd()).thenReturn(timestamp);
}

    @Test
    public void testRegistrationForOperationalSnapshot() {
        snapshotElicitRegistry.registerForNextConsistentOperationalSnapshot(NODE_ID);
        Assert.assertEquals(true, snapshotElicitRegistry.isRegistered(NODE_ID));
        snapshotElicitRegistry.unregisterForNextConsistentOperationalSnapshot(NODE_ID);
        Assert.assertEquals(false, snapshotElicitRegistry.isRegistered(NODE_ID));
    }

    @Test
    public void testIsConsistentUnsuccessful() {
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd().isSucceeded()).thenReturn(false);
        Assert.assertEquals(false, snapshotElicitRegistry.isConsistent(modification));
    }

    @Test
    public void testIsConsistentOperationalPresent() throws ParseException {
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd().isSucceeded()).thenReturn(true);
        Date date1 = simpleDateFormat.parse("0000-12-12T01:01:01.000-07:00");
        String date2 = "9999-12-12T01:01:01.000-07:00";

        snapshotElicitRegistry.getRegistration().put(NODE_ID, date1);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd().getEnd().getValue()).thenReturn(date2);
        Assert.assertEquals(true, snapshotElicitRegistry.isConsistent(modification));
    }

    @Test
    public void testIsConsistentOperationalNotPresent() throws ParseException {
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd().isSucceeded()).thenReturn(true);
        Date date2 = simpleDateFormat.parse("9999-12-12T01:01:01.000-07:00");
        String date1 = "0000-12-12T01:01:01.000-07:00";

        snapshotElicitRegistry.getRegistration().put(NODE_ID, date2);
        Mockito.when(statisticsGatheringStatus.getSnapshotGatheringStatusEnd().getEnd().getValue()).thenReturn(date1);
        Assert.assertEquals(false, snapshotElicitRegistry.isConsistent(modification));
    }

}
