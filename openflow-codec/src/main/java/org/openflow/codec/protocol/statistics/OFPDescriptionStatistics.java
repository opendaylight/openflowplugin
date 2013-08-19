package org.openflow.codec.protocol.statistics;

import java.io.Serializable;

import org.openflow.codec.io.IDataBuffer;
import org.openflow.codec.util.StringByteSerializer;

/**
 * Represents an ofp_desc structure
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFPDescriptionStatistics implements OFPStatistics, Serializable {
    public static int DESCRIPTION_STRING_LENGTH = 256;
    public static int SERIAL_NUMBER_LENGTH = 32;

    protected String manufacturerDescription;
    protected String hardwareDescription;
    protected String softwareDescription;
    protected String serialNumber;
    protected String datapathDescription;

    /**
     * @return the manufacturerDescription
     */
    public String getManufacturerDescription() {
        return manufacturerDescription;
    }

    /**
     * @param manufacturerDescription
     *            the manufacturerDescription to set
     */
    public void setManufacturerDescription(String manufacturerDescription) {
        this.manufacturerDescription = manufacturerDescription;
    }

    /**
     * @return the hardwareDescription
     */
    public String getHardwareDescription() {
        return hardwareDescription;
    }

    /**
     * @param hardwareDescription
     *            the hardwareDescription to set
     */
    public void setHardwareDescription(String hardwareDescription) {
        this.hardwareDescription = hardwareDescription;
    }

    /**
     * @return the softwareDescription
     */
    public String getSoftwareDescription() {
        return softwareDescription;
    }

    /**
     * @param softwareDescription
     *            the softwareDescription to set
     */
    public void setSoftwareDescription(String softwareDescription) {
        this.softwareDescription = softwareDescription;
    }

    /**
     * @return the serialNumber
     */
    public String getSerialNumber() {
        if (serialNumber.equals("None"))
            return "";
        return serialNumber;
    }

    /**
     * @param serialNumber
     *            the serialNumber to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return the datapathDescription
     */
    public String getDatapathDescription() {
        return datapathDescription;
    }

    /**
     * @param datapathDescription
     *            the datapathDescription to set
     */
    public void setDatapathDescription(String datapathDescription) {
        this.datapathDescription = datapathDescription;
    }

    @Override
    public int getLength() {
        return 1056;
    }

    @Override
    public void readFrom(IDataBuffer data) {
        this.manufacturerDescription = StringByteSerializer.readFrom(data, DESCRIPTION_STRING_LENGTH);
        this.hardwareDescription = StringByteSerializer.readFrom(data, DESCRIPTION_STRING_LENGTH);
        this.softwareDescription = StringByteSerializer.readFrom(data, DESCRIPTION_STRING_LENGTH);
        this.serialNumber = StringByteSerializer.readFrom(data, SERIAL_NUMBER_LENGTH);
        this.datapathDescription = StringByteSerializer.readFrom(data, DESCRIPTION_STRING_LENGTH);
    }

    @Override
    public void writeTo(IDataBuffer data) {
        StringByteSerializer.writeTo(data, DESCRIPTION_STRING_LENGTH, this.manufacturerDescription);
        StringByteSerializer.writeTo(data, DESCRIPTION_STRING_LENGTH, this.hardwareDescription);
        StringByteSerializer.writeTo(data, DESCRIPTION_STRING_LENGTH, this.softwareDescription);
        StringByteSerializer.writeTo(data, SERIAL_NUMBER_LENGTH, this.serialNumber);
        StringByteSerializer.writeTo(data, DESCRIPTION_STRING_LENGTH, this.datapathDescription);
    }

    @Override
    public int hashCode() {
        final int prime = 409;
        int result = 1;
        result = prime * result + ((datapathDescription == null) ? 0 : datapathDescription.hashCode());
        result = prime * result + ((hardwareDescription == null) ? 0 : hardwareDescription.hashCode());
        result = prime * result + ((manufacturerDescription == null) ? 0 : manufacturerDescription.hashCode());
        result = prime * result + ((serialNumber == null) ? 0 : serialNumber.hashCode());
        result = prime * result + ((softwareDescription == null) ? 0 : softwareDescription.hashCode());
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
        if (!(obj instanceof OFPDescriptionStatistics)) {
            return false;
        }
        OFPDescriptionStatistics other = (OFPDescriptionStatistics) obj;
        if (datapathDescription == null) {
            if (other.datapathDescription != null) {
                return false;
            }
        } else if (!datapathDescription.equals(other.datapathDescription)) {
            return false;
        }
        if (hardwareDescription == null) {
            if (other.hardwareDescription != null) {
                return false;
            }
        } else if (!hardwareDescription.equals(other.hardwareDescription)) {
            return false;
        }
        if (manufacturerDescription == null) {
            if (other.manufacturerDescription != null) {
                return false;
            }
        } else if (!manufacturerDescription.equals(other.manufacturerDescription)) {
            return false;
        }
        if (serialNumber == null) {
            if (other.serialNumber != null) {
                return false;
            }
        } else if (!serialNumber.equals(other.serialNumber)) {
            return false;
        }
        if (softwareDescription == null) {
            if (other.softwareDescription != null) {
                return false;
            }
        } else if (!softwareDescription.equals(other.softwareDescription)) {
            return false;
        }
        return true;
    }
}
