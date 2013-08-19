package org.openflow.codec.protocol.instruction;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.instruction.OFPInstructionExperimenter;
import org.openflow.codec.protocol.instruction.OFPInstructionType;

/**
 * test class to verify instruction structure
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionExperimenterTest extends TestCase {

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testOFInstructionExperimenterCreation() {
        OFPInstructionExperimenter instruction = new OFPInstructionExperimenter();
        assertTrue(instruction.type.equals(OFPInstructionType.EXPERIMENTER));

    }

    public void testClone() throws CloneNotSupportedException {
        OFPInstructionExperimenter instruction = new OFPInstructionExperimenter();
        instruction.setExperimenterId((byte) 2);
        OFPInstructionExperimenter instructionCloned = (OFPInstructionExperimenter) instruction.clone();
        TestCase.assertEquals(instruction, instructionCloned);

        instruction.setExperimenterId((byte) 1);
        TestCase.assertNotSame(instruction, instructionCloned);

        instruction = (OFPInstructionExperimenter) instructionCloned.clone();
        TestCase.assertEquals(instruction, instructionCloned);
    }

    public void testReadWriteSuccess() {
        OFPInstructionExperimenter instruction = new OFPInstructionExperimenter();
        instruction.setExperimenterId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionExperimenter instructionTemp = new OFPInstructionExperimenter();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction, instructionTemp);

    }

    public void testToString() {
        OFPInstructionExperimenter instruction = new OFPInstructionExperimenter();
        instruction.setExperimenterId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionExperimenter instructionTemp = new OFPInstructionExperimenter();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction.toString(), instructionTemp.toString());
    }

    public void testEqualHashcode() {

        OFPInstructionExperimenter instruction = new OFPInstructionExperimenter();
        instruction.setExperimenterId((byte) 2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionExperimenter instructionTemp = new OFPInstructionExperimenter();
        instructionTemp.readFrom(buffer);

        TestCase.assertTrue(instruction.equals(instructionTemp));
        TestCase.assertEquals(instruction.hashCode(), instructionTemp.hashCode());
    }

}
