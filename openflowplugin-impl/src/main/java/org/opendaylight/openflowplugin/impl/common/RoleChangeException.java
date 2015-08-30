package org.opendaylight.openflowplugin.impl.common;

/**
 * Created by kramesha on 8/21/15.
 */
public class RoleChangeException extends Exception {
    private static final long serialVersionUID = -615991366447313972L;

    /**
     * default ctor
     *
     * @param message
     */
    public RoleChangeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public RoleChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
