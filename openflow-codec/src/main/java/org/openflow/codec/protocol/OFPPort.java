package org.openflow.codec.protocol;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.StringByteSerializer;

/**
 * Represents an ofp_port structure
 *
 * @author AnilGujele
 */
public class OFPPort implements Cloneable, Serializable {
    protected static int MINIMUM_LENGTH = 64;
    protected static int OFP_ETH_ALEN = 6;

    /**
     * represents ofp_port_config
     *
     */
    public enum OFPortConfig {
        OFPPC_PORT_DOWN(1 << 0), OFPPC_NO_RECV(1 << 2), OFPPC_NO_FWD(1 << 5), OFPPC_NO_PACKET_IN(1 << 6);

        protected int value;

        private OFPortConfig(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * represents ofp_port_state
     *
     */
    public enum OFPortState {
        OFPPS_LINK_DOWN(1 << 0), OFPPS_BLOCKED(1 << 1), OFPPS_LIVE(1 << 2);

        protected int value;

        private OFPortState(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * represents ofp_port_features
     *
     */
    public enum OFPortFeatures {
        OFPPF_10MB_HD(1 << 0), OFPPF_10MB_FD(1 << 1), OFPPF_100MB_HD(1 << 2), OFPPF_100MB_FD(1 << 3), OFPPF_1GB_HD(
                1 << 4), OFPPF_1GB_FD(1 << 5), OFPPF_10GB_FD(1 << 6), OFPPF_40GB_FD(1 << 7), OFPPF_100GB_FD(1 << 8), OFPPF_1TB_FD(
                1 << 9), OFPPF_OTHER(1 << 10), OFPPF_COPPER(1 << 11), OFPPF_FIBER(1 << 12), OFPPF_AUTONEG(1 << 13), OFPPF_PAUSE(
                1 << 14), OFPPF_PAUSE_ASYM(1 << 15);

        protected int value;

        private OFPortFeatures(int value) {
            this.value = value;
        }

        /**
         * @return the value
         */
        public int getValue() {
            return value;
        }
    }

    protected int portNumber;
    protected byte[] hardwareAddress;
    protected String name;
    protected int config;
    protected int state;
    protected int currentFeatures;
    protected int advertisedFeatures;
    protected int supportedFeatures;
    protected int peerFeatures;
    private int currentSpeed;
    private int maxSpeed;

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
    public void setPortNumber(short portNumber) {
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
        if (hardwareAddress.length != OFP_ETH_ALEN)
            throw new RuntimeException("Hardware address must have length " + OFP_ETH_ALEN);
        this.hardwareAddress = hardwareAddress;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * @param state
     *            the state to set
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * @return the currentFeatures
     */
    public int getCurrentFeatures() {
        return currentFeatures;
    }

    /**
     * @param currentFeatures
     *            the currentFeatures to set
     */
    public void setCurrentFeatures(int currentFeatures) {
        this.currentFeatures = currentFeatures;
    }

    /**
     * @return the advertisedFeatures
     */
    public int getAdvertisedFeatures() {
        return advertisedFeatures;
    }

    /**
     * @param advertisedFeatures
     *            the advertisedFeatures to set
     */
    public void setAdvertisedFeatures(int advertisedFeatures) {
        this.advertisedFeatures = advertisedFeatures;
    }

    /**
     * @return the supportedFeatures
     */
    public int getSupportedFeatures() {
        return supportedFeatures;
    }

    /**
     * @param supportedFeatures
     *            the supportedFeatures to set
     */
    public void setSupportedFeatures(int supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    /**
     * @return the peerFeatures
     */
    public int getPeerFeatures() {
        return peerFeatures;
    }

    /**
     * @param peerFeatures
     *            the peerFeatures to set
     */
    public void setPeerFeatures(int peerFeatures) {
        this.peerFeatures = peerFeatures;
    }

    /**
     * Read this message from the specified DataBuffer
     *
     * @param data
     */
    public void readFrom(IDataBuffer data) {
        this.portNumber = data.getInt();
        data.getInt(); // pad
        if (this.hardwareAddress == null)
            this.hardwareAddress = new byte[OFP_ETH_ALEN];
        data.get(this.hardwareAddress);
        data.getShort(); // pad
        byte[] name = new byte[16];
        data.get(name);
        // find the first index of 0
        int index = 0;
        for (byte b : name) {
            if (0 == b)
                break;
            ++index;
        }
        this.name = new String(Arrays.copyOf(name, index), Charset.forName("ascii"));
        this.config = data.getInt();
        this.state = data.getInt();
        this.currentFeatures = data.getInt();
        this.advertisedFeatures = data.getInt();
        this.supportedFeatures = data.getInt();
        this.peerFeatures = data.getInt();
        this.currentSpeed = data.getInt();
        this.maxSpeed = data.getInt();
    }

    /**
     * Write this message's binary format to the specified DataBuffer
     *
     * @param data
     */
    public void writeTo(IDataBuffer data) {
        data.putInt(this.portNumber);
        data.putInt(0); // pad
        data.put(hardwareAddress);
        data.putShort((short) 0); // pad
        StringByteSerializer.writeTo(data, 16, name);
        data.putInt(this.config);
        data.putInt(this.state);
        data.putInt(this.currentFeatures);
        data.putInt(this.advertisedFeatures);
        data.putInt(this.supportedFeatures);
        data.putInt(this.peerFeatures);
        data.putInt(this.currentSpeed);
        data.putInt(this.maxSpeed);
    }

    @Override
    public int hashCode() {
        final int prime = 307;
        int result = 1;
        result = prime * result + advertisedFeatures;
        result = prime * result + config;
        result = prime * result + currentFeatures;
        result = prime * result + Arrays.hashCode(hardwareAddress);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + peerFeatures;
        result = prime * result + portNumber;
        result = prime * result + state;
        result = prime * result + supportedFeatures;
        result = prime * result + currentSpeed;
        result = prime * result + maxSpeed;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OFPPort)) {
            return false;
        }
        OFPPort other = (OFPPort) obj;
        if (advertisedFeatures != other.advertisedFeatures) {
            return false;
        }
        if (config != other.config) {
            return false;
        }
        if (currentFeatures != other.currentFeatures) {
            return false;
        }
        if (!Arrays.equals(hardwareAddress, other.hardwareAddress)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (peerFeatures != other.peerFeatures) {
            return false;
        }
        if (portNumber != other.portNumber) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (supportedFeatures != other.supportedFeatures) {
            return false;
        }
        if (currentSpeed != other.currentSpeed) {
            return false;
        }
        if (maxSpeed != other.maxSpeed) {
            return false;
        }
        return true;
    }

    public OFPPort cloneOFPort() {
        OFPPort p;
        try {
            p = (OFPPort) this.clone();

        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
        return p;
    }

    /**
     * get current speed in kbps
     *
     * @return
     */
    public int getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * set current speed in kbps ex: 10 Gb Ethernet port should have this field
     * set to 10000000 (instead of 10312500)
     *
     * @param currentSpeed
     */
    public void setCurrentSpeed(int currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    /**
     * get max speed in kbps
     *
     * @return
     */
    public int getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * set current speed in kbps ex: 10 Gb Ethernet port should have this field
     * set to 10000000 (instead of 10312500)
     *
     * @param maxSpeed
     *            -
     */
    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * get the length of structure
     *
     * @return
     */
    public int getLength() {
        return MINIMUM_LENGTH;
    }

}
