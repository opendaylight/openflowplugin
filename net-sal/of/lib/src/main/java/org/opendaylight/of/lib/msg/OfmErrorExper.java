/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.err.ErrorType;

import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteMsg;

/**
 * Represents an OpenFlow EXPERIMENTER ERROR message; Since 1.2.
 *
 * @author Pramod Shanbhag
 */
public class OfmErrorExper extends OfmError {

    /* Experimenter defined. */
    int expType;
    /* Experimenter ID which takes the same form as in structure
     * ofp experimenter header. 
     */
    int id;

    /**
     * Constructs an OpenFlow EXPERIMENTER ERROR message.
     * The error type is set to EXPERIMENTER.
     *
     * @param header the message header
     */
    OfmErrorExper(Header header) {
        super(header);
        this.type = ErrorType.EXPERIMENTER;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ofm:").append(header)
                .append(",").append(type).append("/").append(expType)
                .append(",").append(ExperimenterId.idToString(id))
                .append(",#dataBytes=").append(data == null ? 0 : data.length)
                .append("}");
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteMessageException {
        notNullIncompleteMsg(type);
    }

    /** Returns the experimenter-defined type; Since 1.2.
     *
     * @return the experimenter-defined type
     */
    public int getExpType() {
        return expType;
    }

    /** Returns the experimenter ID encoded as an int; Since 1.2.
     *
     * @return the experimenter ID (encoded)
     */
    public int getId() {
        return id;
    }

    /** Returns the experimenter ID (if we know it); null otherwise; Since 1.2.
     *
     * @return the experimenter ID
     */
    public ExperimenterId getExpId() {
        return ExperimenterId.decode(id);
    }
}
