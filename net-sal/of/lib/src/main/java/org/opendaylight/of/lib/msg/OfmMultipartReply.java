/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.IncompleteMessageException;
import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.mp.MultipartBody;
import org.opendaylight.of.lib.mp.MultipartType;

import java.util.Collections;
import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.msg.MultipartReplyFlag.REPLY_MORE;

/**
 * Represents an OpenFlow MULTIPART_REPLY message; Since 1.3 (but see note).
 * <p>
 * <em>Important Note:</em> Although MULTIPART_REPLY messages were only added
 * to the protocol at 1.3, they are semantically equivalent to the
 * (now deprecated) STATS_REPLY messages in 1.0, 1.1 and 1.2.
 * <p>
 * For example, to create a "switch description reply" message
 * (Stats-Reply/DESC) to send back to the controller from a mock-switch
 * implementation emulating an OpenFlow 1.0 switch, one would do the following:
 * <pre>
 * OfmMultipartRequest request = ...;  // the message we are replying to
 *
 * OfmMutableMultipartReply reply = (OfmMutableMultipartReply)
 *      MessageFactory.create(MULTIPART_REPLY, request);
 *
 * MBodyMutableDesc desc = (MBodyMutableDesc)
 *      MpBodyFactory.createReplyBody(DESC);
 * desc.mfrDesc(...).hwDesc(...).swDesc(...)
 *     .serialNum(...).dpDesc(...);
 *
 * reply.body((MultipartBody) desc.toImmutable());
 * OpenflowMessage msgToSend = reply.toImmutable();
 * </pre>
 *
 * When the message is encoded, the {@link MessageFactory} will use the version
 * of the message to decide whether to encode it as a MULTIPART_REPLY or
 * a STATS_REPLY.
 *
 * @see OfmMultipartRequest
 * @author Simon Hunt
 */
public class OfmMultipartReply extends OpenflowMessage {
    MultipartType multipartType;
    Set<MultipartReplyFlag> flags;
    MultipartBody body;

    /**
     * Constructs an OpenFlow MULTIPART_REPLY message.
     *
     * @param header the message header
     */
    OfmMultipartReply(Header header) {
        super(header);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        int len = sb.length();
        sb.replace(len-1, len, ",").append(multipartType)
                .append(",flgs=").append(flags)
                .append(",body=").append(bodyString())
                .append("}");
        return sb.toString();
    }

    private String bodyString() {
        return body == null ? NO_BODY : body.toString();
    }

    @Override
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        sb.append(EOLI).append(body == null ? NO_BODY : body.toDebugString());
        return sb.toString();
    }

    @Override
    public void validate() throws IncompleteMessageException {
        notNullIncompleteMsg(multipartType, body);
        try {
            body.validate();
        } catch (IncompleteStructureException e) {
            throw new IncompleteMessageException(e);
        }
    }

    /** Returns the multipart message type; Since 1.3.
     *
     * @return the multipart message type.
     */
    public MultipartType getMultipartType() {
        return multipartType;
    }

    /** Returns the set of multipart message reply flags; Since 1.3.
     *
     * @return the set of flags
     */
    public Set<MultipartReplyFlag> getFlags() {
        return flags == null ? null : Collections.unmodifiableSet(flags);
    }

    /** Returns {@code true} if the {@link MultipartReplyFlag#REPLY_MORE}
     * flag is present.
     * <p>
     * The presence of this flag indicates to the receiver that more
     * (related) multipart-replies are on their way; Only the last reply
     * in such a sequence will have this flag {@code false} (cleared).
     *
     * @return true
     */
    public boolean hasMore() {
        return flags != null && flags.contains(REPLY_MORE);
    }

    /** Returns the body of the message; Since 1.3.
     *
     * @return the body of the message
     */
    public MultipartBody getBody() {
        return body;
    }
}
