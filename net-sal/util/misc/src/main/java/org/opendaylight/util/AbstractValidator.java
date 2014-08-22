/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.ArrayList;
import java.util.List;

/**
 * This class forms the basis for validators that want to report multiple warnings and errors.
 * <p>
 * Here's a pattern we want to avoid:
 * <ul><li>There are multiple issues with an implementation</li>
 *     <li>The first issue detected (#1) is reported</li>
 *     <li>The developer fixes that issue</li>
 *     <li>The first issue detected (#2) is reported</li>
 *     <li>The developer fixes that issue</li>
 *     <li>...</li>
 * </ul>
 * We want to discover and report <em>all</em> the issues at one time, allowing the
 * developer to fix them all in one go.
 * <p>
 * A typical pattern for concrete subclasses is:
 * <pre>
 * public class MyValidator extends AbstractValidator {
 *     // no instantiation outside the class
 *     private MyValidator() { }
 *
 *     private void validate(SomeInterface someImplementation) {
 *
 *         if ( <em>... some problem ...</em> ) {
 *             addError(" detail message ");
 *         } else if ( <em>... some other problem ...</em> ) {
 *             addError(" other detail message ");
 *         }
 *         throwExceptionIfMessages();
 *     }
 *
 *     &#47;** Validates an implementation of ...
 *      * If all is well, this method silently returns. If issues are found, a
 *      * ValidationException is thrown, with details of the discovered issues.
 *      *
 *      * &#64;throws ValidationException if issues were found
 *      * &#64;throws NullPointerException if parameter is null
 *      *&#47;
 *     public static void validateImplementation(SomeInterface someImplementation) {
 *         new MyValidator().validate(someImplementation);
 *     }
 * }
 * </pre>
 * Production code would then use the public API:
 * <pre>
 * SomeInterface myImplementation = ...
 * MyValidator.validateImplementation(myImplementation);
 * </pre>
 *
 * @author Simon Hunt
 */
public abstract class AbstractValidator {

    static String INFO = "Info: ";
    static String WARNING = "Warning: ";
    static String ERROR = "Error: ";

    static String DEFAULT_EX_MSG = "Validation Failed";


    private List<String> msgs = new ArrayList<String>();

    /** Returns the list of messages.
     *
     * @return the list of messages
     */
    public List<String> getMessages() {
        return new ArrayList<String>(msgs); // defensive copy
    }

    /** Returns the messages as a string (newlines after each individual message).
     *
     * @return the messages as a string
     */
    public String getMessagesAsString() {
        StringBuilder sb = new StringBuilder();
        for (String s: msgs) {
            sb.append(s).append(StringUtils.EOL);
        }
        return sb.toString();
    }


    /** This predicate returns true if the supplied string is null, or if (once trimmed of whitespace)
     * its length is zero.
     *
     * @param s the string
     * @return true if the string is null or just whitespace
     */
    protected boolean nullOrEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    /** Returns the number of messages stored.
     *
     * @return the number of messages
     */
    protected int numberOfMessages() {
        return msgs.size();
    }

    /** Add a message prefixed with "Info: "
     *
     * @param message the message
     */
    protected void addInfo(String message) {
        addMsg(INFO, message);
    }

    /** Add a message prefixed with "Warning: "
     *
     * @param message the message
     */
    protected void addWarning(String message) {
        addMsg(WARNING, message);
    }

    /** Add a message prefixed with "Error: "
     *
     * @param message the message
     */
    protected void addError(String message) {
        addMsg(ERROR, message);
    }

    /** Add a message verbatim.
     *
     * @param message the message
     */
    protected void addVerbatim(String message) {
        addMsg("", message);
    }

    /** This method will throw a default exception if the message buffer is not empty.
     *
     * @throws ValidationException thrown if any messages are pending
     */
    protected void throwExceptionIfMessages() {
        if (msgs.size() > 0) throwException();
    }

    /** This method throws an exception to terminate the validation. It uses a default
     * exception message of "Validation Failed".
     *
     * @throws ValidationException thrown using the default message
     */
    protected void throwException() {
        throw new ValidationException(DEFAULT_EX_MSG, this);
    }

    /** This method can be used when the validator finds an unrecoverable error and wants
     * to terminate the validation.
     *
     * @param message the exception message
     * @throws ValidationException thrown using the specified message
     */
    protected void throwException(String message) {
        throw new ValidationException(message, this);
    }

    /** This method can be used when the validator finds an unrecoverable error and wants
     * to terminate the validation.
     *
     * @param message the exception message
     * @param cause an underlying cause
     * @throws ValidationException thrown using the specified message and cause
     */
    protected void throwException(String message, Throwable cause) {
        throw new ValidationException(message, this, cause);
    }


    /** Add a message to the buffer.
     *
     * @param msgType message type
     * @param message message
     */
    private void addMsg(String msgType, String message) {
        msgs.add(msgType + message);
    }

}
