package org.openflow.codec.protocol.instruction;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.instruction.OFPInstructionGoToTable;
import org.openflow.codec.protocol.instruction.OFPInstructionType;

/**
 * test class to verify instruction structure
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionGoToTableTest extends TestCase {

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testOFInstructionGoToTableCreation() {
        OFPInstructionGoToTable instruction = new OFPInstructionGoToTable();
        assertTrue(instruction.type.equals(OFPInstructionType.GOTO_TABLE));

    }

    public void testClone() throws CloneNotSupportedException {
        OFPInstructionGoToTable instruction = new OFPInstructionGoToTable();
        instruction.setTableId((byte) 2);
        OFPInstructionGoToTable instructionCloned = (OFPInstructionGoToTable) instruction.clone();
        TestCase.assertEquals(instruction, instructionCloned);

        instruction.setTableId((byte) 1);
        TestCase.assertNotSame(instruction, instructionCloned);

        instruction = (OFPInstructionGoToTable) instructionCloned.clone();
        TestCase.assertEquals(instruction, instructionCloned);
    }

    public void testReadWriteSuccess() {
        OFPInstructionGoToTable instruction = new OFPInstructionGoToTable();
        instruction.setTableId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionGoToTable instructionTemp = new OFPInstructionGoToTable();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction, instructionTemp);

    }

    public void testToString() {
        OFPInstructionGoToTable instruction = new OFPInstructionGoToTable();
        instruction.setTableId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionGoToTable instructionTemp = new OFPInstructionGoToTable();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction.toString(), instructionTemp.toString());
    }

    public void testEqualHashcode() {

        OFPInstructionGoToTable instruction = new OFPInstructionGoToTable();
        instruction.setTableId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionGoToTable instructionTemp = new OFPInstructionGoToTable();
        instructionTemp.readFrom(buffer);

        TestCase.assertTrue(instruction.equals(instructionTemp));
        TestCase.assertEquals(instruction.hashCode(), instructionTemp.hashCode());
    }

}
