package org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.PortNumberUni;


/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 * 
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 * 
 */
public class PortNumberUniBuilder {

    public static PortNumberUni getDefaultInstance(java.lang.String defaultValue) {
        try {
            long uint32 = Long.parseLong(defaultValue);
            return new PortNumberUni(uint32);
        } catch(NumberFormatException e){
            return new PortNumberUni(defaultValue);
        }
    }

}
