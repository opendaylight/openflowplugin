
package org.opendaylight.openflowjava.tools;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for configurationType complex type.
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "configurationType", propOrder = {
    "controllerIp",
    "devicesCount",
    "ssl",
    "threads",
    "port",
    "timeout",
    "freeze",
    "sleep"
})
public class ConfigurationType {

    @XmlElement(name = "controller-ip", required = true, defaultValue = "127.0.0.1")
    String controllerIp;
    @XmlElement(name = "devices-count", required = true, defaultValue = "1")
    @XmlSchemaType(name = "positiveInteger")
    BigInteger devicesCount;
    @XmlElement(defaultValue = "false")
    Boolean ssl;
    @XmlElement(defaultValue = "1")
    @XmlSchemaType(name = "positiveInteger")
    BigInteger threads;
    @XmlElement(required = true, defaultValue = "6653")
    @XmlSchemaType(name = "positiveInteger")
    BigInteger port;
    @XmlElement(defaultValue = "1000")
    @XmlSchemaType(name = "positiveInteger")
    BigInteger timeout;
    @XmlElement(defaultValue = "3")
    @XmlSchemaType(name = "positiveInteger")
    BigInteger freeze;
    @XmlElement(defaultValue = "100")
    Long sleep;
    @XmlAttribute(name = "name", required = true)
    String name;

    /**
     * Gets the value of the controllerIp property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getControllerIp() {
        return controllerIp;
    }

    /**
     * Gets the value of the devicesCount property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getDevicesCount() {
        return devicesCount;
    }

    /**
     * Gets the value of the ssl property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSsl() {
        return ssl;
    }

    /**
     * Gets the value of the threads property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getThreads() {
        return threads;
    }

    /**
     * Gets the value of the port property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPort() {
        return port;
    }

    /**
     * Gets the value of the timeout property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getTimeout() {
        return timeout;
    }

    /**
     * Gets the value of the freeze property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getFreeze() {
        return freeze;
    }

    /**
     * Gets the value of the sleep property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getSleep() {
        return sleep;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

}
