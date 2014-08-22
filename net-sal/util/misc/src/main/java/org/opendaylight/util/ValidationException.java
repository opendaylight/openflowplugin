/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.List;

/**
 * This exception is designed to be used with implementations of {@link AbstractValidator}.
 * It extends {@link RuntimeException} and holds a list of strings payload, allowing the
 * validator to attach its list of issues to the exception.
 *
 * @author Simon Hunt
 */
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = -5469604981809641079L;

    private final List<String> issues;

    /** Constructs a validation exception.
     *
     * @param message the exception message
     * @param validator the validator throwing the exception
     */
    public ValidationException(String message, AbstractValidator validator) {
        this(message, validator.getMessages());
    }

    /** Constructs a validation exception.
     *
     * @param message the exception message
     * @param validator the validator throwing the exception
     * @param cause an underlying cause
     */
    public ValidationException(String message, AbstractValidator validator, Throwable cause) {
        this(message, validator.getMessages(), cause);
    }

    /** Constructs a validation exception.
     *
     * @param message the exception message
     * @param issues the list of validation issues
     */
    public ValidationException(String message, List<String> issues) {
        super(message);
        this.issues = issues;
    }

    /** Constructs a validation exception.
     *
     * @param message the exception message
     * @param issues the list of validation issues
     * @param cause an underlying cause
     */
    public ValidationException(String message, List<String> issues, Throwable cause) {
        super(message, cause);
        this.issues = issues;
    }

    /** Returns the list of issues associated with this exception.
     *
     * @return the list of issues.
     */
    public List<String> getIssues() { return issues; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString()).append(StringUtils.EOL);
        sb.append(getMessagesAsString("  "));
        return sb.toString();
    }

    private String getMessagesAsString(String indent) {
        StringBuilder sb = new StringBuilder();
        if (issues == null) {
            sb.append(indent).append("(no issues listed)").append(StringUtils.EOL);
        } else {
            for (String s: issues) {
                sb.append(indent).append(s).append(StringUtils.EOL);
            }
        }
        return sb.toString();
    }

}
