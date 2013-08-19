package org.openflow.codec.protocol;

import java.util.ArrayList;
import java.util.List;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_hello message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - Feb 8, 2010
 * @author AnilGujele
 */
public class OFPHello extends OFPMessage {
    public static int MINIMUM_LENGTH = 8;
    private List<OFPHelloElemHeader> elements;

    /**
     * Construct a ofp_hello message
     */
    public OFPHello() {
        super();
        this.type = OFPType.HELLO;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * get hello elements
     *
     * @return
     */
    public List<OFPHelloElemHeader> getHelloElements() {
        return elements;
    }

    /**
     * set hello elements
     *
     * @param elements
     */
    public void setHelloElements(List<OFPHelloElemHeader> elements) {
        this.elements = elements;
        updateLength();
    }

    /**
     * update length
     */
    private void updateLength() {
        int newLength = OFPHello.MINIMUM_LENGTH;
        if (null != elements) {
            for (OFPHelloElemHeader elem : elements) {
                newLength += elem.getLengthU();
            }
        }
        this.length = (short) newLength;
    }

    /**
     * read OFPHello from buffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        int elementsLength = this.getLengthU() - OFPHello.MINIMUM_LENGTH;
        this.elements = readHelloElements(data, elementsLength);

    }

    /**
     * read hello element
     *
     * @param data
     * @param length
     * @return
     */
    private List<OFPHelloElemHeader> readHelloElements(IDataBuffer data, int length) {
        List<OFPHelloElemHeader> results = null;
        OFPHelloElemHeader tempElem = new OFPHelloElemHeader();
        OFPHelloElemHeader ofHelloElem;
        int end = data.position() + length;

        while (data.position() <= end) {
            if (data.remaining() < OFPHelloElemHeader.MINIMUM_LENGTH
                    || (data.position() + OFPHelloElemHeader.MINIMUM_LENGTH) > end)
                return results;

            // to read element type
            data.mark();
            tempElem.readFrom(data);
            data.reset();

            if (tempElem.getLengthU() > data.remaining() || (data.position() + tempElem.getLengthU()) > end)
                return results;

            if (null == tempElem.getOFHelloElemType()) {
                // element is not supported, so forward the position
                data.position(data.position() + tempElem.getLengthU());
                continue;
            }
            // create instance of element type
            ofHelloElem = tempElem.getOFHelloElemType().newInstance();
            // read hello element from data
            ofHelloElem.readFrom(data);
            if (null == results) {
                results = new ArrayList<OFPHelloElemHeader>();
            }
            results.add(ofHelloElem);
        }
        return results;
    }

    /**
     * write OFPHello to buffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        if (null != elements) {
            for (OFPHelloElemHeader elem : elements) {
                elem.writeTo(data);
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 764;
        int result = super.hashCode();
        result = prime * result + ((null == elements) ? 0 : elements.hashCode());
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
        if (!(obj instanceof OFPHello)) {
            return false;
        }
        OFPHello other = (OFPHello) obj;
        if (elements == null) {
            if (other.elements != null) {
                return false;
            }
        } else if (!elements.equals(other.elements)) {
            return false;
        }

        return true;
    }

}
