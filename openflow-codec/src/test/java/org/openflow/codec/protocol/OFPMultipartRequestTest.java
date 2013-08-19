package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFBMatchFields;
import org.openflow.codec.protocol.OFPMatch;
import org.openflow.codec.protocol.OXMClass;
import org.openflow.codec.protocol.OXMField;
import org.openflow.codec.protocol.OFPPort.OFPortConfig;
import org.openflow.codec.protocol.OFPPort.OFPortFeatures;
import org.openflow.codec.protocol.OFPPort.OFPortState;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.action.OFPActionType;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.factory.OFPInstructionFactory;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.protocol.instruction.OFPInstructionType;
import org.openflow.codec.protocol.statistics.OFPAggregateStatisticsRequest;
import org.openflow.codec.protocol.statistics.OFPDescriptionStatistics;
import org.openflow.codec.protocol.statistics.OFPExperimenterMultipartHeader;
import org.openflow.codec.protocol.statistics.OFPFlowStatisticsRequest;
import org.openflow.codec.protocol.statistics.OFPPortDescriptionStatistics;
import org.openflow.codec.protocol.statistics.OFPPortStatisticsRequest;
import org.openflow.codec.protocol.statistics.OFPQueueStatisticsRequest;
import org.openflow.codec.protocol.statistics.OFPTableFeatures;
import org.openflow.codec.protocol.statistics.OFPTableStatistics;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropHeader;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropApplyActions;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropApplyActionsMiss;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropApplySetField;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropApplySetFieldMiss;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropExperimenter;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropExperimenterMiss;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropInstructions;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropInstructionsMiss;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropMatch;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropNextTables;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropNextTablesMiss;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropType;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropWildcards;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropWriteActions;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropWriteActionsMiss;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropWriteSetField;
import org.openflow.codec.protocol.statistics.table.OFPTableFeaturePropWriteSetFieldMiss;
import org.openflow.codec.util.OFTestCase;

public class OFPMultipartRequestTest extends OFTestCase {

    private IDataBuffer buffer = DataBuffers.allocate(2 * 1024);

    public void tearDown() throws Exception {
        super.tearDown();
        buffer.clear();
    }

    public void testOFFlowStatisticsRequest() throws Exception {
        OFPFlowStatisticsRequest request = new OFPFlowStatisticsRequest();
        request.setCookie(2L);
        request.setCookieMask(2L);
        request.setMatch(new OFPMatch());
        request.setOutGroup(3);
        request.setOutPort(400);
        request.setTableId((byte) 2);

        request.writeTo(buffer);
        buffer.flip();
        OFPFlowStatisticsRequest tempReq = new OFPFlowStatisticsRequest();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFAggregateStatisticsRequest() throws Exception {
        OFPAggregateStatisticsRequest request = new OFPAggregateStatisticsRequest();
        request.setCookie(2L);
        request.setCookieMask(2L);
        request.setMatch(new OFPMatch());
        request.setOutGroup(3);
        request.setOutPort(400);
        request.setTableId((byte) 2);

        request.writeTo(buffer);
        buffer.flip();
        OFPAggregateStatisticsRequest tempReq = new OFPAggregateStatisticsRequest();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFDescriptionStatistics() throws Exception {
        OFPDescriptionStatistics request = new OFPDescriptionStatistics();
        request.setDatapathDescription("dataPathDescription");
        request.setHardwareDescription("hardwareDescription");
        request.setManufacturerDescription("manufacturerDescription");
        request.setSerialNumber("serialNumber");
        request.setSoftwareDescription("softwareDescription");
        buffer.clear();
        request.writeTo(buffer);
        buffer.flip();
        OFPDescriptionStatistics tempReq = new OFPDescriptionStatistics();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFPortDescriptionStatistics() throws Exception {
        OFPPortDescriptionStatistics request = new OFPPortDescriptionStatistics();
        request.setAdvertisedFeatures(OFPortFeatures.OFPPF_1GB_FD.getValue());
        request.setConfig(OFPortConfig.OFPPC_NO_PACKET_IN.getValue());
        request.setCurrentFeatures(OFPortFeatures.OFPPF_1GB_HD.getValue());
        request.setCurrentSpeed(100000);
        request.setHardwareAddress(new byte[] { 1, 2, 3, 4, 5, 6 });
        request.setMaxSpeed(1000000);
        request.setName("open flow port");
        request.setPeerFeatures(OFPortFeatures.OFPPF_10GB_FD.getValue());
        request.setPortNumber((short) 200);
        request.setState(OFPortState.OFPPS_LIVE.getValue());
        request.setSupportedFeatures(OFPortFeatures.OFPPF_FIBER.getValue());

        request.writeTo(buffer);
        buffer.flip();
        OFPPortDescriptionStatistics tempReq = new OFPPortDescriptionStatistics();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFPortStatisticsRequest() throws Exception {
        OFPPortStatisticsRequest request = new OFPPortStatisticsRequest();
        request.setPortNumber(5342);
        request.writeTo(buffer);
        buffer.flip();
        OFPPortStatisticsRequest tempReq = new OFPPortStatisticsRequest();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFQueueStatisticsRequest() throws Exception {
        OFPQueueStatisticsRequest request = new OFPQueueStatisticsRequest();
        request.setPortNumber(5342);
        request.setQueueId(2);
        request.writeTo(buffer);
        buffer.flip();
        OFPQueueStatisticsRequest tempReq = new OFPQueueStatisticsRequest();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFTableStatistics() throws Exception {
        OFPTableStatistics request = new OFPTableStatistics();
        request.setActiveCount(10);
        request.setLookupCount(10);
        request.setMatchedCount(2);
        request.setTableId((byte) 1);
        request.writeTo(buffer);
        buffer.flip();
        OFPTableStatistics tempReq = new OFPTableStatistics();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFTableFeaturesNoProp() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();

        executeTest(request);
    }

    private OFPTableFeatures getOFTableFeatures() {
        OFPTableFeatures request = new OFPTableFeatures();
        request.setConfig(1);
        request.setMaxEntries(100);
        request.setMetadataMatch(10L);
        request.setMetadataWrite(5L);
        request.setName("table1");
        request.setTableId((byte) 1);
        return request;
    }

    public void testOFTableFeaturesNextTables() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();
        OFPTableFeaturePropNextTables prop = (OFPTableFeaturePropNextTables) OFPTableFeaturePropType.NEXT_TABLES
                .newInstance();
        prop.setNextTableIds(new byte[] { 4, 6, 9 });
        List<OFPTableFeaturePropHeader> list = new ArrayList<OFPTableFeaturePropHeader>();
        list.add(prop);
        request.setProperties(list);

        executeTest(request);
    }

    private void executeTest(OFPTableFeatures request) {
        request.writeTo(buffer);
        buffer.flip();
        OFPTableFeatures tempReq = new OFPTableFeatures();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFTableFeaturesNextTablesMiss() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();
        OFPTableFeaturePropNextTablesMiss prop = (OFPTableFeaturePropNextTablesMiss) OFPTableFeaturePropType.NEXT_TABLES_MISS
                .newInstance();
        prop.setNextTableIds(new byte[] { 4, 6, 9 });
        List<OFPTableFeaturePropHeader> list = new ArrayList<OFPTableFeaturePropHeader>();
        list.add(prop);
        request.setProperties(list);

        executeTest(request);
    }

    public void testOFTableFeaturesInstructionsSuccess() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();
        OFPInstructionFactory instructionFactory = new OFPBasicFactoryImpl();
        request.setInstructionFactory(instructionFactory);

        // Instructions property
        OFPTableFeaturePropInstructions prop = (OFPTableFeaturePropInstructions) OFPTableFeaturePropType.INSTRUCTIONS
                .newInstance();
        OFPInstruction instrId = OFPInstructionType.GOTO_TABLE.newInstance();
        List<OFPInstruction> list = new ArrayList<OFPInstruction>();
        list.add(instrId);
        prop.setInstructionIds(list);

        // Instruction miss property
        OFPTableFeaturePropInstructionsMiss propMiss = (OFPTableFeaturePropInstructionsMiss) OFPTableFeaturePropType.INSTRUCTIONS_MISS
                .newInstance();
        propMiss.setInstructionIds(list);

        List<OFPTableFeaturePropHeader> listProp = new ArrayList<OFPTableFeaturePropHeader>();
        listProp.add(prop);
        listProp.add(propMiss);
        request.setProperties(listProp);

        request.writeTo(buffer);
        buffer.flip();
        OFPTableFeatures tempReq = new OFPTableFeatures();
        tempReq.setInstructionFactory(instructionFactory);
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFTableFeaturesInstructionsFailed() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();

        // Instructions property
        OFPTableFeaturePropInstructions prop = (OFPTableFeaturePropInstructions) OFPTableFeaturePropType.INSTRUCTIONS
                .newInstance();
        OFPInstruction instrId = OFPInstructionType.GOTO_TABLE.newInstance();
        List<OFPInstruction> list = new ArrayList<OFPInstruction>();
        list.add(instrId);
        prop.setInstructionIds(list);

        List<OFPTableFeaturePropHeader> listProp = new ArrayList<OFPTableFeaturePropHeader>();
        listProp.add(prop);
        request.setProperties(listProp);

        request.writeTo(buffer);
        buffer.flip();
        OFPTableFeatures tempReq = new OFPTableFeatures();
        boolean result = false;
        try {
            tempReq.readFrom(buffer);
        } catch (RuntimeException ex) {
            result = true;
        }
        TestCase.assertTrue(result);
    }

    public void testOFTableFeaturesActions() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();
        OFPActionFactory actionFactory = new OFPBasicFactoryImpl();
        request.setActionFactory(actionFactory);
        // Apply action property
        OFPTableFeaturePropApplyActions prop = (OFPTableFeaturePropApplyActions) OFPTableFeaturePropType.APPLY_ACTIONS
                .newInstance();
        OFPAction action = OFPActionType.OUTPUT.newInstance();
        List<OFPAction> list = new ArrayList<OFPAction>();
        list.add(action);
        prop.setActionIds(list);

        OFPTableFeaturePropApplyActionsMiss propMiss = (OFPTableFeaturePropApplyActionsMiss) OFPTableFeaturePropType.APPLY_ACTIONS_MISS
                .newInstance();
        propMiss.setActionIds(list);

        OFPTableFeaturePropWriteActions prop1 = (OFPTableFeaturePropWriteActions) OFPTableFeaturePropType.WRITE_ACTIONS
                .newInstance();
        prop1.setActionIds(list);

        OFPTableFeaturePropWriteActionsMiss propMiss1 = (OFPTableFeaturePropWriteActionsMiss) OFPTableFeaturePropType.WRITE_ACTIONS_MISS
                .newInstance();
        propMiss1.setActionIds(list);

        List<OFPTableFeaturePropHeader> listProp = new ArrayList<OFPTableFeaturePropHeader>();
        listProp.add(prop);
        listProp.add(propMiss);
        listProp.add(prop1);
        listProp.add(propMiss1);

        request.setProperties(listProp);

        request.writeTo(buffer);
        buffer.flip();
        OFPTableFeatures tempReq = new OFPTableFeatures();
        tempReq.setActionFactory(actionFactory);
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFTableFeaturesMatch() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();

        OFPTableFeaturePropMatch prop = (OFPTableFeaturePropMatch) OFPTableFeaturePropType.MATCH.newInstance();
        OXMField field = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, null);
        List<OXMField> list = new ArrayList<OXMField>();
        list.add(field);
        prop.setOXMIds(list);

        OFPTableFeaturePropWildcards wildcard = (OFPTableFeaturePropWildcards) OFPTableFeaturePropType.WILDCARDS
                .newInstance();
        wildcard.setOXMIds(list);

        OFPTableFeaturePropWriteSetField write = (OFPTableFeaturePropWriteSetField) OFPTableFeaturePropType.WRITE_SETFIELD
                .newInstance();
        write.setOXMIds(list);

        OFPTableFeaturePropWriteSetFieldMiss writeMiss = (OFPTableFeaturePropWriteSetFieldMiss) OFPTableFeaturePropType.WRITE_SETFIELD_MISS
                .newInstance();
        writeMiss.setOXMIds(list);

        OFPTableFeaturePropApplySetField apply = (OFPTableFeaturePropApplySetField) OFPTableFeaturePropType.APPLY_SETFIELD
                .newInstance();
        apply.setOXMIds(list);

        OFPTableFeaturePropApplySetFieldMiss applyMiss = (OFPTableFeaturePropApplySetFieldMiss) OFPTableFeaturePropType.APPLY_SETFIELD_MISS
                .newInstance();
        applyMiss.setOXMIds(list);

        List<OFPTableFeaturePropHeader> listProp = new ArrayList<OFPTableFeaturePropHeader>();
        listProp.add(prop);
        listProp.add(wildcard);
        listProp.add(write);
        listProp.add(writeMiss);
        listProp.add(apply);
        listProp.add(applyMiss);

        request.setProperties(listProp);

        executeTest(request);
    }

    public void testOFTableFeaturesExperimenter() throws Exception {
        OFPTableFeatures request = getOFTableFeatures();

        OFPTableFeaturePropExperimenter prop = (OFPTableFeaturePropExperimenter) OFPTableFeaturePropType.EXPERIMENTER
                .newInstance();
        prop.setExpId(2);
        prop.setExpType(1);
        prop.setExpData(new int[] { 1, 2, 3 });

        OFPTableFeaturePropExperimenterMiss propMiss = (OFPTableFeaturePropExperimenterMiss) OFPTableFeaturePropType.EXPERIMENTER_MISS
                .newInstance();
        propMiss.setExpId(22);
        propMiss.setExpType(12);
        propMiss.setExpData(new int[] { 12, 22, 32 });

        List<OFPTableFeaturePropHeader> listProp = new ArrayList<OFPTableFeaturePropHeader>();
        listProp.add(prop);
        listProp.add(propMiss);

        request.setProperties(listProp);

        executeTest(request);
    }

    public void testOFPExperimenterMultipartHeaderReadWrite() throws Exception {
        OFPExperimenterMultipartHeader request = new OFPExperimenterMultipartHeader();
        request.setExperimenter(1);
        request.setExpType(2);
        request.setData(new byte[] { 1, 2, 3, 4 });

        request.writeTo(buffer);
        buffer.flip();
        OFPExperimenterMultipartHeader tempReq = new OFPExperimenterMultipartHeader();
        tempReq.setLength(12);
        tempReq.readFrom(buffer);

        TestCase.assertEquals(request, tempReq);
        TestCase.assertEquals(request.hashCode(), tempReq.hashCode());
    }

    public void testOFPExperimenterMultipartHeaderGetters() throws Exception {
        OFPExperimenterMultipartHeader request = new OFPExperimenterMultipartHeader();
        request.setExperimenter(1);
        request.setExpType(2);
        request.setData(new byte[] { 1, 2, 3, 4 });

        request.writeTo(buffer);
        buffer.flip();
        OFPExperimenterMultipartHeader tempReq = new OFPExperimenterMultipartHeader();
        tempReq.setLength(12);
        tempReq.readFrom(buffer);

        TestCase.assertEquals(1, tempReq.getExperimenter());
        TestCase.assertEquals(2, tempReq.getExpType());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    public void testOFPExperimenterMultipartHeaderNoData() throws Exception {
        OFPExperimenterMultipartHeader request = new OFPExperimenterMultipartHeader();
        request.setExperimenter(1);
        request.setExpType(2);

        request.writeTo(buffer);
        buffer.flip();
        OFPExperimenterMultipartHeader tempReq = new OFPExperimenterMultipartHeader();
        tempReq.readFrom(buffer);

        TestCase.assertEquals(1, tempReq.getExperimenter());
        TestCase.assertEquals(2, tempReq.getExpType());
        TestCase.assertEquals(request.getLength(), tempReq.getLength());
    }

    // public void testOFFlowStatisticsRequest() throws Exception {
    // byte[] packet = new byte[] { 0x04, 0x10, 0x00, 0x38, 0x00, 0x00, 0x00,
    // 0x16, 0x00, 0x01, 0x00, 0x00, (byte) 0xff, (byte) 0xff,
    // (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // (byte) 0xff, 0x00, (byte) 0xff, (byte) 0xff };
    //
    // OFPMessageFactory factory = new OFPBasicFactoryImpl();
    // IDataBuffer packetBuf = DataBuffers.createBuffer(8).wrap(packet);
    // List<OFPMessage> msgs = factory.parseMessages(packetBuf, packet.length);
    // TestCase.assertEquals(1, msgs.size());
    // TestCase.assertTrue(msgs.get(0) instanceof OFPMultipartRequest);
    // OFPMultipartRequest sr = (OFPMultipartRequest) msgs.get(0);
    // TestCase.assertEquals(OFPMultipartTypes.FLOW, sr.getMultipartType());
    // TestCase.assertEquals(1, sr.getStatistics().size());
    // TestCase.assertTrue(sr.getStatistics().get(0) instanceof
    // OFPFlowStatisticsRequest);
    // }
    //
    // public void testOFStatisticsRequestVendor() throws Exception {
    // byte[] packet = new byte[] { 0x01, 0x10, 0x00, 0x50, 0x00, 0x00, 0x00,
    // 0x63, (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x00, 0x00, 0x00,
    // 0x4c, 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    // 0x01, 0x00, 0x38, 0x00, 0x00, 0x00, 0x00, 0x00, 0x30, 0x20,
    // (byte) 0xe0, 0x00, 0x11, 0x00, 0x0c, 0x29, (byte) 0xc5,
    // (byte) 0x95, 0x57, 0x02, 0x25, 0x5c, (byte) 0xca, 0x00, 0x02,
    // (byte) 0xff, (byte) 0xff, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00,
    // 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x2d, 0x00, 0x50, 0x04,
    // 0x00, 0x00, 0x00, 0x00, (byte) 0xff, 0x00, 0x00, 0x00,
    // (byte) 0xff, (byte) 0xff, 0x4e, 0x20 };
    //
    // OFPMessageFactory factory = new OFPBasicFactoryImpl();
    // IDataBuffer packetBuf = DataBuffers.createBuffer(8).wrap(packet);
    // List<OFPMessage> msgs = factory.parseMessages(packetBuf, packet.length);
    // TestCase.assertEquals(1, msgs.size());
    // TestCase.assertTrue(msgs.get(0) instanceof OFPMultipartRequest);
    // OFPMultipartRequest sr = (OFPMultipartRequest) msgs.get(0);
    // TestCase.assertEquals(OFPMultipartTypes.VENDOR, sr.getMultipartType());
    // TestCase.assertEquals(1, sr.getStatistics().size());
    // TestCase.assertTrue(sr.getStatistics().get(0) instanceof
    // OFPVendorStatistics);
    // TestCase.assertEquals(68,
    // ((OFPVendorStatistics)sr.getStatistics().get(0)).getLength());
    // }
}
