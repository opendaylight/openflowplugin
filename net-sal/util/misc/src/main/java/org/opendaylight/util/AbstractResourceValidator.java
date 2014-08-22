/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * This class extends the base functionality of {@link AbstractValidator} to provide useful
 * helper methods when validating implementations that must provide both a name and a description
 * for things from a resource bundle. The assumption made here is that each item tested will have
 * two entries in the resource bundle with keys of the form {@code [prefix]-name} and {@code [prefix]-desc}.
 * If either key is missing, or if either value (trimmed of whitespace) is an empty string, an error should
 * be added to the accumulated list of issues.
 *
 * @author Simon Hunt
 */
public abstract class AbstractResourceValidator extends AbstractValidator {

    // package private - JUnit tests have access
    static final String MISSING = "Missing";
    static final String EMPTY = "Empty";
    static final String NAME = "-name";
    static final String DESC = "-desc";
    static final String COLON = " : ";
    static final String RESOURCE = " resource: ";

    /** Adds an error to the issue list stating that [keyPrefix]-name is an empty resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     */
    protected void addEmptyNameError(String keyPrefix) {
        addResourceError(EMPTY, NAME, keyPrefix, null);
    }

    /** Adds an error to the issue list stating that [keyPrefix]-name is a missing resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     */
    protected void addMissingNameError(String keyPrefix) {
        addResourceError(MISSING, NAME, keyPrefix, null);
    }

    /** Adds an error to the issue list stating that [keyPrefix]-desc is an empty resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     */
    protected void addEmptyDescError(String keyPrefix) {
        addResourceError(EMPTY, DESC, keyPrefix, null);
    }

    /** Adds an error to the issue list stating that [keyPrefix]-desc is a missing resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     */
    protected void addMissingDescError(String keyPrefix) {
        addResourceError(MISSING, DESC, keyPrefix, null);
    }



    /** Adds an error to the issue list stating that [keyPrefix]-name is an empty resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     * @param footnote an optional note to tag onto the end of the error message
     */
    protected void addEmptyNameError(String keyPrefix, String footnote) {
        addResourceError(EMPTY, NAME, keyPrefix, footnote);
    }

    /** Adds an error to the issue list stating that [keyPrefix]-name is a missing resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     * @param footnote an optional note to tag onto the end of the error message
     */
    protected void addMissingNameError(String keyPrefix, String footnote) {
        addResourceError(MISSING, NAME, keyPrefix, footnote);
    }

    /** Adds an error to the issue list stating that [keyPrefix]-desc is an empty resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     * @param footnote an optional note to tag onto the end of the error message
     */
    protected void addEmptyDescError(String keyPrefix, String footnote) {
        addResourceError(EMPTY, DESC, keyPrefix, footnote);
    }

    /** Adds an error to the issue list stating that [keyPrefix]-desc is a missing resource.
     *
     * @param keyPrefix the prefix to the resource bundle key
     * @param footnote an optional note to tag onto the end of the error message
     */
    protected void addMissingDescError(String keyPrefix, String footnote) {
        addResourceError(MISSING, DESC, keyPrefix, footnote);
    }


    // put it altogether and post the error
    private void addResourceError(String type, String which, String keyPrefix, String footnote) {
        StringBuilder sb = new StringBuilder(type).append(RESOURCE).append(keyPrefix).append(which);
        if (footnote != null) {
            sb.append(COLON).append(footnote);
        }
        addError(sb.toString());
    }
}
