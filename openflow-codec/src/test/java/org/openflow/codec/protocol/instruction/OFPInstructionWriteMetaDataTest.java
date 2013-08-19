package org.openflow.codec.protocol.instruction;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.instruction.OFPInstructionType;
import org.openflow.codec.protocol.instruction.OFPInstructionWriteMetaData;

/**
 * test class to verify instruction structure
 *
 * @author AnilGujele
 *
 */
public class OFPInstructionWriteMetaDataTest extends TestCase {

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        buffer.clear();
    }

    public void testOFInstructionWriteMetaDataCreation() {
        OFPInstructionWriteMetaData instruction = new OFPInstructionWriteMetaData();
        assertTrue(instruction.type.equals(OFPInstructionType.WRITE_METADATA));

    }

    public void testClone() throws CloneNotSupportedException {
        OFPInstructionWriteMetaData instruction = new OFPInstructionWriteMetaData();
        instruction.setMetadata(2);
        instruction.setMetadataMask(2);
        OFPInstructionWriteMetaData instructionCloned = (OFPInstructionWriteMetaData) instruction.clone();
        TestCase.assertEquals(instruction, instructionCloned);

        instruction.setMetadata(1);
        instruction.setMetadataMask(1);
        TestCase.assertNotSame(instruction, instructionCloned);

        instruction = (OFPInstructionWriteMetaData) instructionCloned.clone();
        TestCase.assertEquals(instruction, instructionCloned);
    }

    public void testReadWriteSuccess() {
        OFPInstructionWriteMetaData instruction = new OFPInstructionWriteMetaData();
        instruction.setMetadata(2);
        instruction.setMetadataMask(2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionWriteMetaData instructionTemp = new OFPInstructionWriteMetaData();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction, instructionTemp);

    }

    public void testToString() {
        OFPInstructionWriteMetaData instruction = new OFPInstructionWriteMetaData();
        instruction.setMetadata(2);
        instruction.setMetadataMask(2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionWriteMetaData instructionTemp = new OFPInstructionWriteMetaData();
        instructionTemp.readFrom(buffer);
        TestCase.assertEquals(instruction.toString(), instructionTemp.toString());
    }

    public void testEqualHashcode() {

        OFPInstructionWriteMetaData instruction = new OFPInstructionWriteMetaData();
        instruction.setMetadata(2);
        instruction.setMetadataMask(2);
        instruction.writeTo(buffer);
        buffer.flip();

        OFPInstructionWriteMetaData instructionTemp = new OFPInstructionWriteMetaData();
        instructionTemp.readFrom(buffer);

        TestCase.assertTrue(instruction.equals(instructionTemp));
        TestCase.assertEquals(instruction.hashCode(), instructionTemp.hashCode());
    }

}
