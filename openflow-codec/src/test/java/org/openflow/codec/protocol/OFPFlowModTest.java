package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFBMatchFields;
import org.openflow.codec.protocol.OFPFlowMod;
import org.openflow.codec.protocol.OFPFlowModCommand;
import org.openflow.codec.protocol.OFPMatch;
import org.openflow.codec.protocol.OFPType;
import org.openflow.codec.protocol.OXMClass;
import org.openflow.codec.protocol.OXMField;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.action.OFPActionOutput;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.factory.OFPInstructionFactory;
import org.openflow.codec.protocol.instruction.OFPInstruction;
import org.openflow.codec.protocol.instruction.OFPInstructionApplyActions;
import org.openflow.codec.protocol.instruction.OFPInstructionGoToTable;

/**
 * test class to verify OFPFlowMod message
 *
 * @author AnilGujele
 *
 */
public class OFPFlowModTest extends TestCase {

    private OFPInstructionFactory instrFactory = new OFPBasicFactoryImpl();

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testFlowModCreation() {
        OFPFlowMod flowMod = this.getDefaultFlowMod();
        assertTrue(flowMod.type.equals(OFPType.FLOW_MOD));

    }

    private OFPFlowMod getDefaultFlowMod() {
        OFPFlowMod flowMod = new OFPFlowMod();
        flowMod.setCookie(25L);
        flowMod.setCookieMask(25L);
        flowMod.setTableId((byte) 0);
        flowMod.setBufferId(1);
        flowMod.setIdleTimeout((short) 2000);
        flowMod.setHardTimeout((short) 25000);
        flowMod.setPriority((short) 1);
        flowMod.setCommand(OFPFlowModCommand.OFPFC_ADD);
        flowMod.setOutPort(1000);
        flowMod.setOutGroup(1);
        flowMod.setFlags((short) 1);
        OFPMatch match = new OFPMatch();
        flowMod.setMatch(match);
        OFPInstruction instr = new OFPInstructionGoToTable();
        List<OFPInstruction> instrList = new ArrayList<OFPInstruction>();
        instrList.add(instr);
        flowMod.setInstructions(instrList);
        flowMod.setInstructionFactory(instrFactory);
        return flowMod;
    }

    public void testClone() {
        OFPFlowMod flowMod = this.getDefaultFlowMod();
        OFPFlowMod flowModCloned = flowMod.clone();
        TestCase.assertEquals(flowMod, flowModCloned);

        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        flowMod.getMatch().addMatchField(matchField);
        TestCase.assertNotSame(flowMod, flowModCloned);

        flowMod = flowModCloned.clone();
        TestCase.assertEquals(flowMod, flowModCloned);
    }

    public void testReadWriteSuccess() {
        OFPFlowMod flowModWriter = this.getDefaultFlowMod();
        OFPFlowMod flowModReader = new OFPFlowMod();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        flowModWriter.getMatch().addMatchField(matchField);
        flowModWriter.writeTo(buffer);
        buffer.flip();
        flowModReader.setInstructionFactory(instrFactory);
        flowModReader.readFrom(buffer);
        TestCase.assertEquals(flowModWriter, flowModReader);

    }

    public void testReadWriteFailed() {
        OFPFlowMod flowModWriter = this.getDefaultFlowMod();
        OFPFlowMod flowModReader = new OFPFlowMod();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        flowModWriter.getMatch().addMatchField(matchField);
        flowModWriter.writeTo(buffer);
        buffer.flip();
        boolean result = false;
        try {
            flowModReader.readFrom(buffer);
        } catch (RuntimeException ex) {
            result = true;
        }
        TestCase.assertTrue(result);
    }

    public void testLength() {
        OFPFlowMod flowModWriter = this.getDefaultFlowMod();
        OFPFlowMod flowModReader = new OFPFlowMod();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        flowModWriter.getMatch().addMatchField(matchField);
        flowModWriter.writeTo(buffer);
        buffer.flip();
        flowModReader.setInstructionFactory(instrFactory);
        flowModReader.readFrom(buffer);
        TestCase.assertEquals(flowModWriter.getLength(), flowModReader.getLength());

    }

    public void testToString() {
        OFPFlowMod flowModWriter = this.getDefaultFlowMod();
        OFPFlowMod flowModReader = new OFPFlowMod();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        flowModWriter.getMatch().addMatchField(matchField);
        flowModWriter.writeTo(buffer);
        buffer.flip();
        flowModReader.setInstructionFactory(instrFactory);
        flowModReader.readFrom(buffer);
        TestCase.assertEquals(flowModWriter.toString(), flowModReader.toString());
    }

    public void testEqualHashcode() {

        OFPFlowMod flowModWriter = this.getDefaultFlowMod();
        OFPFlowMod flowModReader = new OFPFlowMod();
        OXMField matchField = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_SRC, false, new byte[] { 1, 2,
                3, 4 });
        flowModWriter.getMatch().addMatchField(matchField);
        OXMField matchField1 = new OXMField(OXMClass.OPENFLOW_BASIC, OFBMatchFields.IPV4_DST, false, new byte[] { 1, 2,
                3, 4 });
        flowModWriter.getMatch().addMatchField(matchField1);
        flowModWriter.writeTo(buffer);
        buffer.flip();
        flowModReader.setInstructionFactory(instrFactory);
        flowModReader.readFrom(buffer);
        TestCase.assertTrue(flowModWriter.equals(flowModReader));
        TestCase.assertEquals(flowModWriter.hashCode(), flowModReader.hashCode());
    }

    public void testFlowModInstrActions() {
        OFPFlowMod flowModWriter = this.getDefaultFlowMod();
        OFPInstructionApplyActions instr = new OFPInstructionApplyActions();
        List<OFPInstruction> instrList = new ArrayList<OFPInstruction>();
        instrList.add(instr);
        OFPAction action = new OFPActionOutput();
        List<OFPAction> actionList = new ArrayList<OFPAction>();
        actionList.add(action);
        instr.setActions(actionList);
        instr.setActionFactory(new OFPBasicFactoryImpl());
        flowModWriter.setInstructions(instrList);
        flowModWriter.writeTo(buffer);
        buffer.flip();
        OFPFlowMod flowModReader = new OFPFlowMod();
        flowModReader.setInstructionFactory(instrFactory);
        flowModReader.readFrom(buffer);
        assertEquals(flowModWriter, flowModReader);

    }

}
