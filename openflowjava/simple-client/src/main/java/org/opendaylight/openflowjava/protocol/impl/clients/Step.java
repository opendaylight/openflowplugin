
package org.opendaylight.openflowjava.protocol.impl.clients;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for stepType complex type.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "step", propOrder = {
    "order",
    "name",
    "event",
    "bytes"
})
public class Step {

    protected short order;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected EventType event;
    @XmlList
    @XmlElement(type = Short.class)
    @XmlSchemaType(name = "anySimpleType")
    protected List<Short> bytes;

    /**
     * Gets the value of the order property.
     */
    public short getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     */
    public void setOrder(short value) {
        this.order = value;
    }

    /**
     * Gets the value of the name property.
     * @return possible object is {@link String }
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * @param value allowed object is {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the event property.
     * @return possible object is {@link EventType }
     */
    public EventType getEvent() {
        return event;
    }

    /**
     * Sets the value of the event property.
     * @param value allowed object is {@link EventType }
     */
    public void setEvent(EventType value) {
        this.event = value;
    }

    /**
     * Gets the value of the bytes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bytes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBytes().add(newItem);
     * </pre>
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Short }
     */
    public List<Short> getBytes() {
        if (bytes == null) {
            bytes = new ArrayList<>();
        }
        return this.bytes;
    }

}
