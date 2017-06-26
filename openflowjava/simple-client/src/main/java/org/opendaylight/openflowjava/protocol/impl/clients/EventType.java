
package org.opendaylight.openflowjava.protocol.impl.clients;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eventType.
 */
@XmlType(name = "eventType")
@XmlEnum
public enum EventType {

    @XmlEnumValue("sleepEvent")
    SLEEP_EVENT("sleepEvent"),
    @XmlEnumValue("waitForMessageEvent")
    WAIT_FOR_MESSAGE_EVENT("waitForMessageEvent"),
    @XmlEnumValue("sendEvent")
    SEND_EVENT("sendEvent");
    private final String value;

    EventType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventType fromValue(String v) {
        for (EventType c: EventType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
