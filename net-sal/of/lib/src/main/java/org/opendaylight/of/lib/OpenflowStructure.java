/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib;


/**
 * Base class for all OpenFlow structures.
 *
 * @author Simon Hunt
 * @author Scott Simes
 */
public abstract class OpenflowStructure implements Structure {
    private static final String OX = "0x";

    /** Every structure knows what version it is representing. */
    protected final ProtocolVersion version;

    protected Throwable parseErrorCause = null;

    /**
     * Constructs an OpenFlow structure.
     *
     * @param pv the protocol version
     */
    public OpenflowStructure(ProtocolVersion pv) {
        if (pv == null)
            throw new NullPointerException("version cannot be null");
        version = pv;
    }

    @Override
    public ProtocolVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "{ofs:" + version.name() + "}";
    }

    /**
     * Returns a string representation useful for debugging.
     * This default implementation delegates to {@link #toString()}, but
     * subclasses are free to override this behavior.
     *
     * @return a (possibly multi-line) string representation of this message
     */
    @Override
    public String toDebugString() {
        return toString();
    }

    /**
     * Validates this structure for completeness and throws an exception
     * if the structure is considered "not complete".
     * <p>
     * This default implementation does nothing, i.e. default behavior is
     * that structures are considered complete.
     * <p>
     * Subclasses should override this method to check that mandatory
     * fields or other internal state is present, throwing an exception
     * if it is not.
     *
     * @throws IncompleteStructureException if the structure is not complete
     */
    public void validate() throws IncompleteStructureException { }

    /**
     * Indicates if an exception was encountered during parsing, resulting
     * in an incomplete message structure. If this is true, the exception
     * thrown and caught during parsing is available via
     * {@link #parseErrorCause()}.
     *
     * @return true if structure was not parsed without error
     */
    public boolean incomplete() {
        return parseErrorCause != null;
    }

    /**
     * The exception encountered while parsing this structure, if any.
     * This will be null if the structure parsed without error.
     *
     * @return the parse exception
     */
    public Throwable parseErrorCause() {
        return parseErrorCause;
    }

    /**
     * Returns the given long as a string in hex form.
     *
     * @param value the value
     * @return the value in hex form
     */
    protected static String hex(long value) {
        return OX + Long.toHexString(value);
    }

    /**
     * Returns the given int as a string in hex form.
     *
     * @param value the value
     * @return the value in hex form
     */
    protected static String hex(int value) {
        return OX + Integer.toHexString(value);
    }
}
