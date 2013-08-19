package org.openflow.codec.protocol.instruction;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.instruction.OFPInstructionMeter;
import org.openflow.codec.protocol.instruction.OFPInstructionType;

/**
 * test class to verify instruction structure
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionMeterTest extends TestCase {

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testOFInstructionMeterCreation() {
        OFPInstructionMeter instruction = new OFPInstructionMeter();
        assertTrue(instruction.type.equals(OFPInstructionType.METER));

    }

    public void testClone() throws CloneNotSupportedException {
        OFPInstructionMeter instruction = new OFPInstructionMeter();
        instruction.setMeterId((byte) 2);
        OFPInstructionMeter instructionCloned = (OFPInstructionMeter) instruction.clone();
        TestCase.assertEquals(instruction, instructionCloned);

        instruction.setMeterId((byte) 1);
        TestCase.assertNotSame(instruction, instructionCloned);

        instruction = (OFPInstructionMeter) instructionCloned.clone();
        TestCase.assertEquals(instruction, instructionCloned);
    }

    public void testReadWriteSuccess() {
        OFPInstructionMeter instruction = new OFPInstructionMeter();
        instruction.setMeterId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionMeter instructionTemp = new OFPInstructionMeter();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction, instructionTemp);

    }

    public void testToString() {
        OFPInstructionMeter instruction = new OFPInstructionMeter();
        instruction.setMeterId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionMeter instructionTemp = new OFPInstructionMeter();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction.toString(), instructionTemp.toString());
    }

    public void testEqualHashcode() {

        OFPInstructionMeter instruction = new OFPInstructionMeter();
        instruction.setMeterId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionMeter instructionTemp = new OFPInstructionMeter();
        instructionTemp.readFrom(buffer);

        TestCase.assertTrue(instruction.equals(instructionTemp));
        TestCase.assertEquals(instruction.hashCode(), instructionTemp.hashCode());
    }

}
