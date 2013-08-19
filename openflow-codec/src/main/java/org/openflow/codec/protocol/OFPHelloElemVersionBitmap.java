package org.openflow.codec.protocol;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;

/**
 * Represents struct ofp_hello_elem_versionbitmap
 *
 *
 * @author AnilGujele
 *
 */
public class OFPHelloElemVersionBitmap extends OFPHelloElemHeader {
    private static final short MINIMUM_LENGTH = 4;
    private static final int INT_SIZE = 4;

    private int[] bitmaps;

    /**
     * constructor
     */
    public OFPHelloElemVersionBitmap() {
        super.setOFHelloElemType(OFPHelloElemType.VERSIONBITMAP);
        super.setLength(MINIMUM_LENGTH);
    }

    /**
     * get bitmaps
     *
     * @return
     */
    public int[] getBitmaps() {
        return bitmaps;
    }

    /**
     * set bitmaps
     *
     * @param bitmaps
     */
    public void setBitmaps(int[] bitmaps) {
        this.bitmaps = bitmaps;
        updateLength();
    }

    private void updateLength() {
        int newLength = ((null == bitmaps) ? 0 : bitmaps.length * INT_SIZE);
        this.length = (short) (OFPHelloElemVersionBitmap.MINIMUM_LENGTH + newLength);
    }

    /**
     * read OFPHelloElemVersionBitmap from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        int bitmapLength = this.getLengthU() - OFPHelloElemVersionBitmap.MINIMUM_LENGTH;
        int end = data.position() + bitmapLength;
        bitmaps = new int[bitmapLength / INT_SIZE];
        for (int i = 0; ((data.position() + INT_SIZE) <= end); i++) {
            this.bitmaps[i] = data.getInt();
        }

    }

    /**
     * write OFPHelloElemVersionBitmap to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        if (null == bitmaps) {
            bitmaps = new int[0];
        }
        for (int bitmap : bitmaps) {
            data.putInt(bitmap);
        }

    }

    @Override
    public int hashCode() {
        final int prime = 762;
        int result = super.hashCode();
        result = prime * result + Arrays.hashCode(bitmaps);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof OFPHelloElemVersionBitmap)) {
            return false;
        }
        OFPHelloElemVersionBitmap other = (OFPHelloElemVersionBitmap) obj;
        if (!Arrays.equals(bitmaps, other.bitmaps)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a string representation of the hello element
     */
    public String toString() {
        return "OFPHelloElemVersionBitmap[" + "type=" + this.getOFHelloElemType() + ", length=" + this.getLength()
                + ", bitmaps=" + Arrays.toString(bitmaps) + "]";
    }

}
