/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * This exception allows for a message key to be associated with the
 * exception, such that a localized error message can be presented to the user
 * in the UI where appropriate.
 * 
 * @author Simon Hunt
 */
public class LocalizableException extends Exception {

    private static final long serialVersionUID = 6003273947524741191L;

    private String msgKey;

    /**
     * Constructs an exception with no associated message key.
     */
    public LocalizableException() {
    }

    /**
     * Constructs an exception with the given message key. The message key is
     * intended for retrieving a localized message to present to the user in
     * the UI.
     * 
     * @param uiMessageKey the message key for an external UI message
     */
    public LocalizableException(String uiMessageKey) {
        msgKey = uiMessageKey;
    }

    /**
     * Constructs an exception with the given message key, and the given log
     * message (internal). The message key is intended for retrieving a
     * localized message to present to the user in the UI. The log message is
     * for internal logging.
     * 
     * @param uiMessageKey the message key for an external UI message
     * @param logMessage the internal log message
     */
    public LocalizableException(String uiMessageKey, String logMessage) {
        super(logMessage);
        msgKey = uiMessageKey;
    }

    /**
     * Constructs an exception with the given message key, given log message
     * (internal), and cause. The message key is intended for retrieving a
     * localized message to present to the user in the UI. The log message is
     * for internal logging.
     * 
     * @param uiMessageKey the message key for an external UI message
     * @param logMessage the internal log message
     * @param cause the underlying exception that caused this exception
     */
    public LocalizableException(String uiMessageKey, String logMessage, 
                                Throwable cause) {
        super(logMessage, cause);
        msgKey = uiMessageKey;
    }

    /**
     * Constructs an exception with the given message key and cause. The
     * message key is intended for retrieving a localized message to present
     * to the user in the UI.
     * 
     * @param uiMessageKey the message key for an external UI message
     * @param cause the underlying exception that caused this exception
     */
    public LocalizableException(String uiMessageKey, Throwable cause) {
        super(cause);
        msgKey = uiMessageKey;
    }

    /**
     * Returns the message key associated with this exception (may be null).
     * 
     * @return the message key
     */
    public String getMessageKey() {
        return msgKey;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String key = msgKey == null ? "(null)" : "\"" + msgKey + "\"";
        String msg = getMessage();

        StringBuilder sb = new StringBuilder(s);
        sb.append(" msgKey=").append(key);
        if (msg != null)
            sb.append(": ").append(msg);
        return sb.toString();
    }

}
