/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;

import static org.opendaylight.of.lib.CommonUtils.EOLI;

/**
 * Represents the description of an openflow datapath;
 * Since 1.0.
 *
 * @author Simon Hunt
 */
public class MBodyDesc extends OpenflowStructure implements MultipartBody {

    private static final int BODY_LEN = 1056;

    String mfrDesc;
    String hwDesc;
    String swDesc;
    String serialNum;
    String dpDesc;

    /**
     * Constructs a multipart body DESC type.
     *
     * @param pv the protocol version
     */
    public MBodyDesc(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        return "{mfr:" + mfrDesc + ",hw=" + hwDesc + ",mockswitch=" + swDesc + ",...}";
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mfr : ").append(mfrDesc)
          .append(EOLI).append("H/W : ").append(hwDesc)
          .append(EOLI).append("S/W : ").append(swDesc)
          .append(EOLI).append("Ser#: ").append(serialNum)
          .append(EOLI).append("DP  : ").append(dpDesc);
        return sb.toString();
    }

    @Override
    public int getTotalLength() {
        return BODY_LEN;
    }

    /** Returns the manufacturer description; Since 1.0.
     *
     * @return the manufacturer description
     */
    public String getMfrDesc() {
        return mfrDesc;
    }

    /** Returns the hardware description; Since 1.0.
     *
     * @return the hardware description
     */
    public String getHwDesc() {
        return hwDesc;
    }

    /** Returns the software description; Since 1.0.
     *
     * @return the software description
     */
    public String getSwDesc() {
        return swDesc;
    }

    /** Returns the serial number; Since 1.0.
     *
     * @return the serial number
     */
    public String getSerialNum() {
        return serialNum;
    }

    /** Returns the data-path description; Since 1.0.
     *
     * @return the data-path description
     */
    public String getDpDesc() {
        return dpDesc;
    }
}
