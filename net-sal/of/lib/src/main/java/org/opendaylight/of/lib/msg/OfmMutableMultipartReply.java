/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.MutableStructure;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.mp.MpBodyFactory;
import org.opendaylight.of.lib.mp.MultipartBody;

import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.sameVersion;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MultipartReplyFlag.REPLY_MORE;

/**
 * Mutable subclass of {@link OfmMultipartReply}.
 *
 * @author Simon Hunt
 */
public class OfmMutableMultipartReply extends OfmMultipartReply
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow MULTIPART_REPLY message.
     *
     * @param header the message header
     */
    OfmMutableMultipartReply(Header header) {
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
        OfmMultipartReply msg = new OfmMultipartReply(header);
        msg.multipartType = this.multipartType;
        msg.flags = this.flags;
        if (MutableStructure.class.isInstance(this.body))
            msg.body = (MultipartBody)
                    ((MutableStructure) this.body).toImmutable();
        else
            msg.body = this.body;
        // recalculate the total message length, now that body is immutable
        header.length = MessageCreator.getMpHeaderLength(header.version) +
                (body == null ? 0 : body.getTotalLength());
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
    // ==== SETTERS

    /* IMPLEMENTATION NOTE:
    *   For every MultipartBody (reply) concrete class there is a
    *   corresponding MultipartType constant.
    *
    *   Thus, the consumer should create a mutable body from the body
    *   factory, populate it, then call body(); we will derive and
    *   set the type from the class of the body.
    */

    /** Sets the body of this multipart reply; Since 1.0.
     *
     * @param body the multipart body for this reply
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if body is null
     * @throws VersionMismatchException if the body is not the same version
     *          as this instance
     */
    // IMPLEMENTATION NOTE: We allow mutable bodies to be added, as they are
    //  a work-in-progress. They'll be made immutable when the message is.
    //  Also, we don't check for completeness of the body now - that is done
    //  when the message is validated, just before encoding.
    public OfmMutableMultipartReply body(MultipartBody body) {
        mutt.checkWritable(this);
        // do NOT check for version. See OfmMultipartReply class description.
        notNull(body);
        sameVersion("MP-Reply / Body", header.version, body.getVersion());
        this.multipartType = MpBodyFactory.getType(body);
        this.body = body;
        int fixed = header.version == V_1_0 ? MessageFactory.LIB_MP_HEADER_10
                                            : MessageFactory.LIB_MP_HEADER;
        this.header.length = fixed + body.getTotalLength();
        return this;
    }


    /* IMPLEMENTATION NOTE:
     *   Although the more general approach would be to define the method
     *     public void setFlags(Set<MultipartReplyFlag> flags),
     *   there is, currently, only a single flag to set: REPLY_MORE.
     *   Therefore, in the interests of simplicity we will define the
     *   following method instead...
     */

    /** Sets the REPLY_MORE flag.
     * <p>
     * The presence of this flag indicates to the receiver that more (related)
     * multipart-reply messages are on their way; Only the last reply
     * in such a sequence should have this flag {@code false} (cleared).
     * <p>
     * The default setting for this flag is {@code false}; calling this
     * method will latch the flag to {@code true}.
     *
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableMultipartReply setMoreFlag() {
        mutt.checkWritable(this);
        // do NOT check for version. See OfmMultipartRequest class description.
        if (flags == null)
            flags = new TreeSet<MultipartReplyFlag>();
        flags.add(REPLY_MORE);
        return this;
    }
}
