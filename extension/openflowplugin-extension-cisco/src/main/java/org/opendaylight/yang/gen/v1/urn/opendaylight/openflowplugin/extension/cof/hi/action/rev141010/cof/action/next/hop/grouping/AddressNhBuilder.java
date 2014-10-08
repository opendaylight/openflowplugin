package org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.next.hop.grouping;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.cof.action.next.hop.grouping.ActionOutputNhHi.AddressNh;

import com.google.common.base.Preconditions;


/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 */
public class AddressNhBuilder {

    public static AddressNh getDefaultInstance(java.lang.String defaultValue) {
        Preconditions.checkArgument(defaultValue != null);
        
        //ipv4:
        for (String pattern : Ipv4Address.PATTERN_CONSTANTS) {
            if (defaultValue.matches(pattern)) {
                return new AddressNh(new Ipv4Address(defaultValue));
            }
        }
        //mac48:
        for (String pattern : MacAddress.PATTERN_CONSTANTS) {
            if (defaultValue.matches(pattern)) {
                return new AddressNh(new MacAddress(defaultValue));
            }
        }
        //ipv6:
        for (String pattern : Ipv6Address.PATTERN_CONSTANTS) {
            if (defaultValue.matches(pattern)) {
                return new AddressNh(new Ipv6Address(defaultValue));
            }
        }
        
        throw new IllegalArgumentException("net-hop-address not understood:"+defaultValue);
    }

}
