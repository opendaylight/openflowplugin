package org.openflow.codec.protocol;

import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.U16;

/**
 * Represents an ofp_port_mod message
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPPortMod extends OFPMessage {
    public static int MINIMUM_LENGTH = 40;

    protected int portNumber;
    protected byte[] hardwareAddress;
    protected int config;
    protected int mask;
    protected int advertise;

    public OFPPortMod() {
        super();
        this.type = OFPType.PORT_MOD;
        this.length = U16.t(MINIMUM_LENGTH);
    }

    /**
     * @return the portNumber
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber
     *            the portNumber to set
     */
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * @return the hardwareAddress
     */
    public byte[] getHardwareAddress() {
        return hardwareAddress;
    }

    /**
     * @param hardwareAddress
     *            the hardwareAddress to set
     */
    public void setHardwareAddress(byte[] hardwareAddress) {
        if (hardwareAddress.length != OFPPort.OFP_ETH_ALEN)
            throw new RuntimeException("Hardware address must have length " + OFPPort.OFP_ETH_ALEN);
        this.hardwareAddress = hardwareAddress;
    }

    /**
     * @return the config
     */
    public int getConfig() {
        return config;
    }

    /**
     * @param config
     *            the config to set
     */
    public void setConfig(int config) {
        this.config = config;
    }

    /**
     * @return the mask
     */
    public int getMask() {
        return mask;
    }

    /**
     * @param mask
     *            the mask to set
     */
    public void setMask(int mask) {
        this.mask = mask;
    }

    /**
     * @return the advertise
     */
    public int getAdvertise() {
        return advertise;
    }

    /**
     * @param advertise
     *            the advertise to set
     */
    public void setAdvertise(int advertise) {
        this.advertise = advertise;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        super.readFrom(data);
        this.portNumber = data.getInt();
        data.getInt(); // pad
        if (this.hardwareAddress == null)
            this.hardwareAddress = new byte[OFPPort.OFP_ETH_ALEN];
        data.get(this.hardwareAddress);
        data.getShort(); // pad
        this.config = data.getInt();
        this.mask = data.getInt();
        this.advertise = data.getInt();
        data.getShort(); // pad
        data.get(); // pad
    }

    @Override
    public void writeTo(IDataBuffer data) {
        super.writeTo(data);
        data.putInt(this.portNumber);
        data.putInt(0); // pad
        data.put(this.hardwareAddress);
        data.putShort((short) 0); // pad
        data.putInt(this.config);
        data.putInt(this.mask);
        data.putInt(this.advertise);
        data.putShort((short) 0); // pad
        data.put((byte) 0); // pad
    }

    @Override
    public int hashCode() {
        final int prime = 311;
        int result = super.hashCode();
        result = prime * result + advertise;
        result = prime * result + config;
        result = prime * result + Arrays.hashCode(hardwareAddress);
        result = prime * result + mask;
        result = prime * result + portNumber;
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
        if (!(obj instanceof OFPPortMod)) {
            return false;
        }
        OFPPortMod other = (OFPPortMod) obj;
        if (advertise != other.advertise) {
            return false;
        }
        if (config != other.config) {
            return false;
        }
        if (!Arrays.equals(hardwareAddress, other.hardwareAddress)) {
            return false;
        }
        if (mask != other.mask) {
            return false;
        }
        if (portNumber != other.portNumber) {
            return false;
        }
        return true;
    }
}
