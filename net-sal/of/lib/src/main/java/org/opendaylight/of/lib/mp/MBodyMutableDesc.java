/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.stringField;

/**
 * Mutable subclass of {@link MBodyDesc}.
 *
 * @author Simon Hunt
 */
public class MBodyMutableDesc extends MBodyDesc implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable multipart body DESC type.
     *
     * @param pv the protocol version
     */
    public MBodyMutableDesc(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        MBodyDesc desc = new MBodyDesc(version);
        desc.mfrDesc = this.mfrDesc;
        desc.hwDesc = this.hwDesc;
        desc.swDesc = this.swDesc;
        desc.serialNum = this.serialNum;
        desc.dpDesc = this.dpDesc;
        return desc;
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
    // ==== SETTERS

    /** Sets the manufacturer description; Since 1.0.
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#DESC_STR_LEN} - 1.
     *
     * @param mfrDesc the manufacturer description
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if mfrDesc is null
     * @throws IllegalArgumentException if string length is &gt;255
     */
    public MBodyMutableDesc mfrDesc(String mfrDesc) {
        mutt.checkWritable(this);
        notNull(mfrDesc);
        stringField(mfrDesc, MpBodyFactory.DESC_STR_LEN);
        this.mfrDesc = mfrDesc;
        return this;
    }

    /** Sets the hardware description; Since 1.0.
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#DESC_STR_LEN} - 1.
     *
     * @param hwDesc the hardware description
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if hwDesc is null
     * @throws IllegalArgumentException if string length is &gt;255
     */
    public MBodyMutableDesc hwDesc(String hwDesc) {
        mutt.checkWritable(this);
        notNull(hwDesc);
        stringField(hwDesc, MpBodyFactory.DESC_STR_LEN);
        this.hwDesc = hwDesc;
        return this;
    }

    /** Sets the software description; Since 1.0.
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#DESC_STR_LEN} - 1.
     *
     * @param swDesc the software description
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if swDesc is null
     * @throws IllegalArgumentException if string length is &gt;255
     */
    public MBodyMutableDesc swDesc(String swDesc) {
        mutt.checkWritable(this);
        notNull(swDesc);
        stringField(swDesc, MpBodyFactory.DESC_STR_LEN);
        this.swDesc = swDesc;
        return this;
    }

    /** Sets the serial number; Since 1.0.
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#SERIAL_NUM_LEN} - 1.
     *
     * @param serialNum the serial number
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if serialNum is null
     * @throws IllegalArgumentException if string length is &gt;31
     */
    public MBodyMutableDesc serialNum(String serialNum) {
        mutt.checkWritable(this);
        notNull(serialNum);
        stringField(serialNum, MpBodyFactory.SERIAL_NUM_LEN);
        this.serialNum = serialNum;
        return this;
    }

    /** Sets the human readable description of the datapath; Since 1.0.
     * Note that the maximum allowed length of the string is
     * {@link MpBodyFactory#DESC_STR_LEN} - 1.
     *
     * @param dpDesc the datapath description
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if dpDesc is null
     * @throws IllegalArgumentException if string length is &gt;255
     */
    public MBodyMutableDesc dpDesc(String dpDesc) {
        mutt.checkWritable(this);
        notNull(dpDesc);
        stringField(dpDesc, MpBodyFactory.DESC_STR_LEN);
        this.dpDesc = dpDesc;
        return this;
    }
}
