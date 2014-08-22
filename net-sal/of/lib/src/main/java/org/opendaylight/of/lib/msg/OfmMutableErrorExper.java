/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.ExperimenterId;
import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;

/**
 * Mutable subclass of {@link OfmErrorExper}.
 *
 * @author Pramod Shanbhag
 */
public class OfmMutableErrorExper extends OfmErrorExper 
        implements MutableMessage {

    private final Mutable mutt = new Mutable();
    
    private static final int FIELDS_LEN = 8;
    private static final int FIXED_HEADER_LEN = 
            OpenflowMessage.OFM_HEADER_LEN + FIELDS_LEN;

    /**
     * Constructs a mutable OpenFlow EXPERIMENTER ERROR message.
     *
     * @param header the message header
     */
    OfmMutableErrorExper(Header header) {
        super(header);
    }
    
    @Override
    public void clearXid() {
        mutt.checkWritable(this);
        header.xid = 0;
    }

    @Override
    public OpenflowMessage toImmutable() {
        // Can only do this once
        mutt.invalidate(this);

        // Copy over to read-only instance
        OfmErrorExper msg = new OfmErrorExper(header);
        msg.type = this.type;
        msg.expType = this.expType;
        msg.id = this.id;
        msg.data = this.data;
        return msg;
    }

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }
    
    // =====================================================================
    // SETTERS

    /** Sets the experimenter-defined type; Since 1.2.
     *
     * @param expType the experimenter-defined type
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public void setExpType(int expType) {
        mutt.checkWritable(this);
        this.expType = expType;
    }
    
    /** Sets the experimenter ID; Since 1.2.
    *
    * @param expId the experimenter ID
    * @throws InvalidMutableException if this instance is no longer writable
    */
   public void setExpId(int expId) {
       mutt.checkWritable(this);
       this.id = expId;
   }
   
   /**
    * Sets the experimenter ID; Since 1.2.
    *
    * @param id the experimenter identifier
    * @throws InvalidMutableException if this instance is no longer writable
    */
   public void setExpId(ExperimenterId id) {
       setExpId(id.encodedId());
   }
   
   /**
    * Sets the data; Since 1.2.
    *
    * @param data the data
    * @throws InvalidMutableException if this instance is no longer writable
    */
   public void setData(byte[] data) {
       mutt.checkWritable(this);
       this.data = data.clone();
       header.length = FIXED_HEADER_LEN + data.length;   
   }
}
