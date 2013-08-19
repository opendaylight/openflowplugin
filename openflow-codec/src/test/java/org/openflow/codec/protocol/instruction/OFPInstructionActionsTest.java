package org.openflow.codec.protocol.instruction;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.action.OFPAction;
import org.openflow.codec.protocol.action.OFPActionCopyTimeToLiveIn;
import org.openflow.codec.protocol.action.OFPActionOutput;
import org.openflow.codec.protocol.factory.OFPBasicFactoryImpl;
import org.openflow.codec.protocol.factory.OFPActionFactory;
import org.openflow.codec.protocol.instruction.OFPInstructionApplyActions;
import org.openflow.codec.protocol.instruction.OFPInstructionClearActions;
import org.openflow.codec.protocol.instruction.OFPInstructionType;
import org.openflow.codec.protocol.instruction.OFPInstructionWriteActions;

/**
 * test class to verify instruction structure
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionActionsTest extends TestCase {

    private OFPActionFactory factory = new OFPBasicFactoryImpl();

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testOFInstructionApplyActions() {
        OFPInstructionApplyActions instruction = new OFPInstructionApplyActions();
        assertTrue(instruction.type.equals(OFPInstructionType.APPLY_ACTIONS));

    }

    public void testOFInstructionWriteActions() {
        OFPInstructionWriteActions instruction = new OFPInstructionWriteActions();
        assertTrue(instruction.type.equals(OFPInstructionType.WRITE_ACTIONS));

    }

    public void testOFInstructionClearActions() {
        OFPInstructionClearActions instruction = new OFPInstructionClearActions();
        assertTrue(instruction.type.equals(OFPInstructionType.CLEAR_ACTIONS));

    }

    public void testClone() throws CloneNotSupportedException {
        OFPInstructionApplyActions instruction = getOFInstructionApplyActions();
        OFPInstructionApplyActions instructionCloned = (OFPInstructionApplyActions) instruction.clone();
        TestCase.assertEquals(instruction, instructionCloned);

        OFPAction action = new OFPActionCopyTimeToLiveIn();
        List<OFPAction> actionList = new ArrayList<OFPAction>();
        actionList.add(action);
        instructionCloned.setActions(actionList);
        instructionCloned.setActionFactory(factory);
        TestCase.assertNotSame(instruction, instructionCloned);

        instruction = (OFPInstructionApplyActions) instructionCloned.clone();
        TestCase.assertEquals(instruction, instructionCloned);
    }

    public void testReadWriteSuccess() {
        OFPInstructionApplyActions instruction = getOFInstructionApplyActions();

        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionApplyActions instructionTemp = new OFPInstructionApplyActions();
        instructionTemp.setActionFactory(factory);
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction, instructionTemp);

    }

    public void testReadWriteFailed() {
        OFPInstructionApplyActions instruction = getOFInstructionApplyActions();

        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionApplyActions instructionTemp = new OFPInstructionApplyActions();
        boolean result = false;
        try {
            instructionTemp.readFrom(buffer);
        } catch (RuntimeException ex) {
            result = true;
        }
        TestCase.assertTrue(result);

    }

    public void testToString() {
        OFPInstructionApplyActions instruction = getOFInstructionApplyActions();

        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionApplyActions instructionTemp = new OFPInstructionApplyActions();
        instructionTemp.setActionFactory(factory);
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction.toString(), instructionTemp.toString());
    }

    public void testEqualHashcode() {

        OFPInstructionApplyActions instruction = getOFInstructionApplyActions();

        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionApplyActions instructionTemp = new OFPInstructionApplyActions();
        instructionTemp.setActionFactory(factory);
        instructionTemp.readFrom(buffer);

        TestCase.assertTrue(instruction.equals(instructionTemp));
        TestCase.assertEquals(instruction.hashCode(), instructionTemp.hashCode());
    }

    private OFPInstructionApplyActions getOFInstructionApplyActions() {
        OFPInstructionApplyActions instruction = new OFPInstructionApplyActions();
        OFPAction action = new OFPActionOutput();
        List<OFPAction> actionList = new ArrayList<OFPAction>();
        actionList.add(action);
        instruction.setActions(actionList);
        instruction.setActionFactory(factory);
        return instruction;
    }

}
