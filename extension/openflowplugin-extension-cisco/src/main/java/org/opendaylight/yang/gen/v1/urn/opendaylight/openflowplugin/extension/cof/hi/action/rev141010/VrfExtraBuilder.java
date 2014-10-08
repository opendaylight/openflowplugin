package org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.cof.hi.action.rev141010.VrfExtra;

import com.google.common.base.Preconditions;


/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string representation.
 * In some cases it is very difficult to automate it since there can be unions such as (uint32 - uint16), or (string - uint32).
 * The reason behind putting it under src/main/java is:
 * This class is generated in form of a stub and needs to be finished by the user. This class is generated only once to prevent
 * loss of user code.
 */
public class VrfExtraBuilder {

    public static VrfExtra getDefaultInstance(java.lang.String defaultValue) {
        Preconditions.checkArgument(defaultValue != null);
        if (defaultValue.startsWith("0x")) {
            return new VrfExtra(new VrfVpnId(ByteBufUtils.hexStringToBytes(defaultValue)));
        } else {
            return new VrfExtra(new VrfName(defaultValue));
        }
    }

}
