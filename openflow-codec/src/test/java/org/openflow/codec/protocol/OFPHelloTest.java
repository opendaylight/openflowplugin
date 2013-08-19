package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.openflow.codec.io.DataBuffers;
import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.protocol.OFPHello;
import org.openflow.codec.protocol.OFPHelloElemHeader;
import org.openflow.codec.protocol.OFPHelloElemType;
import org.openflow.codec.protocol.OFPHelloElemVersionBitmap;

public class OFPHelloTest extends TestCase {

    private IDataBuffer buffer = DataBuffers.allocate(1024);

    protected void tearDown() throws Exception {
        super.tearDown();
        buffer.clear();
    }

    public void testHelloElemTypeMapping() throws Exception {
        TestCase.assertEquals(OFPHelloElemType.VERSIONBITMAP, OFPHelloElemType.valueOf((short) 1));
    }

    public void testReadWrite() {
        OFPHello helloWriter = new OFPHello();
        List<OFPHelloElemHeader> elements = new ArrayList<OFPHelloElemHeader>();
        elements.add(OFPHelloElemType.VERSIONBITMAP.newInstance());
        helloWriter.setHelloElements(elements);
        helloWriter.writeTo(buffer);
        buffer.flip();
        OFPHello helloReader = new OFPHello();
        helloReader.readFrom(buffer);
        TestCase.assertEquals(helloWriter, helloReader);
    }

    public void testReadWriteWithBitmap() {
        OFPHello helloWriter = new OFPHello();
        List<OFPHelloElemHeader> elements = new ArrayList<OFPHelloElemHeader>();
        OFPHelloElemVersionBitmap bitmap = (OFPHelloElemVersionBitmap) OFPHelloElemType.VERSIONBITMAP.newInstance();
        bitmap.setBitmaps(new int[] { 3, 1 });
        elements.add(bitmap);
        helloWriter.setHelloElements(elements);
        helloWriter.writeTo(buffer);
        buffer.flip();
        OFPHello helloReader = new OFPHello();
        helloReader.readFrom(buffer);
        TestCase.assertEquals(helloWriter, helloReader);
    }

    public void testEqualHashcode() {
        OFPHello helloWriter = new OFPHello();
        List<OFPHelloElemHeader> elements = new ArrayList<OFPHelloElemHeader>();
        elements.add(OFPHelloElemType.VERSIONBITMAP.newInstance());
        helloWriter.setHelloElements(elements);
        helloWriter.writeTo(buffer);
        buffer.flip();
        OFPHello helloReader = new OFPHello();
        helloReader.readFrom(buffer);
        TestCase.assertEquals(helloWriter, helloReader);
        TestCase.assertEquals(helloWriter.hashCode(), helloReader.hashCode());
    }

}
