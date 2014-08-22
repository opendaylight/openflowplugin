/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.mp.MBodyMutableDesc;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.of.lib.msg.OfmMutableMultipartReply;
import org.opendaylight.of.lib.msg.OpenflowMessage;

import static org.opendaylight.of.lib.mp.MultipartType.DESC;
import static org.opendaylight.of.lib.msg.MessageType.MULTIPART_REPLY;
import static org.opendaylight.util.StringUtils.EMPTY;

/**
 * Encapsulates the description configuration of a mock-switch.
 *
 * @author Simon Hunt
 */
public class CfgDesc {

    private String mfrDesc = EMPTY;
    private String hwDesc = EMPTY;
    private String swDesc = EMPTY;
    private String serialNum = EMPTY;
    private String dpDesc = EMPTY;


    @Override
    public String toString() {
        return "{cfgDesc:mfr=\"" + mfrDesc + "\",hw=\"" + hwDesc +
                "\",mockswitch=\"" + swDesc + "\",ser#=\"" + serialNum +
                "\",dp=\"" + dpDesc + "\"}";
    }

    /** Returns the manufacture description.
     *
     * @return the manufacturer description
     */
    public String getMfrDesc() {
        return mfrDesc;
    }

    /** Returns the hardware description.
     *
     * @return the hardware description
     */
    public String getHwDesc() {
        return hwDesc;
    }

    /** Returns the software description.
     *
     * @return the software description
     */
    public String getSwDesc() {
        return swDesc;
    }

    /** Returns the serial number.
     *
     * @return the serial number
     */
    public String getSerialNum() {
        return serialNum;
    }

    /** Returns the datapath friendly description.
     *
     * @return the datapath friendly description
     */
    public String getDpDesc() {
        return dpDesc;
    }

    /** Sets a description string.
     *
     * @param key which string
     * @param text the text to set
     */
    public void setDesc(SwitchDefn.Keyword key, String text) {
        switch (key) {
            case DESC_MFR:
                mfrDesc = text;
                break;
            case DESC_HW:
                hwDesc = text;
                break;
            case DESC_SW:
                swDesc = text;
                break;
            case DESC_SERIAL:
                serialNum = text;
                break;
            case DESC_DP:
                dpDesc = text;
                break;
            default:
                throw new IllegalStateException("What?");
        }
    }

    /** Creates a multipart DESC reply from the configured data. The request
     * has to be passed in so we can replicate the transaction id.
     *
     * @param request the request
     * @return a multipart DESC reply
     */
    public OpenflowMessage createMpDescReply(OpenflowMessage request) {
        // TODO: use .create(req, type, subtype) signature when available
        OfmMutableMultipartReply rep = (OfmMutableMultipartReply)
                MessageFactory.create(request, MULTIPART_REPLY);
        MBodyMutableDesc desc = (MBodyMutableDesc)
                MpBodyFactory.createReplyBody(rep.getVersion(), DESC);
        desc.mfrDesc(mfrDesc).hwDesc(hwDesc).swDesc(swDesc)
                .serialNum(serialNum).dpDesc(dpDesc);
        rep.body((MultipartBody) desc.toImmutable());
        return rep.toImmutable();
    }
}
