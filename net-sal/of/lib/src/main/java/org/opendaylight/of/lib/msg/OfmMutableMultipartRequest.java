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
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.util.ResourceUtils;

import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.notNull;
import static org.opendaylight.of.lib.CommonUtils.sameVersion;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.msg.MultipartRequestFlag.REQUEST_MORE;

/**
 * Mutable subclass of {@link OfmMultipartRequest}.
 *
 * @author Simon Hunt
 */
public class OfmMutableMultipartRequest extends OfmMultipartRequest
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow MULTIPART_REQUEST message.
     *
     * @param header the message header
     */
    OfmMutableMultipartRequest(Header header) {
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
        OfmMultipartRequest msg = new OfmMultipartRequest(header);
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
    *   For every MultipartBody (request) concrete class there is
    *   a corresponding MultipartType constant. However, some type constants
    *   do not have a corresponding (request) body - the message is fully
    *   implied in the header.
    *
    *   Thus, in the former case, the consumer should create a mutable
    *   body from the body factory, populate it, then call body(); we will
    *   derive and set the type from the class of the body.
    *   In the latter case, the consumer should call type(); the body
    *   will remain null.
    *   In both cases, we will make sure the appropriate method is called.
    */

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            OfmMutableMultipartRequest.class, "ofmMutableMultipartRequest");

    private static final String E_USE_SET_BODY = RES
            .getString("e_use_set_body");

    /** These types have no body for their request messages. */
    private static final MultipartType[] REQUEST_NO_BODY = {
            MultipartType.DESC,
            // FLOW has a body
            // AGGREGATE has a body
            MultipartType.TABLE,
            // PORT_STATS has a body
            // QUEUE has a body
            // GROUP has a body
            MultipartType.GROUP_DESC,
            MultipartType.GROUP_FEATURES,
            // METER has a body
            // METER_CONFIG has a body
            MultipartType.METER_FEATURES,
            MultipartType.TABLE_FEATURES, // NOTE: body OPTIONAL!
            MultipartType.PORT_DESC,
            // EXPERIMENTER has a body
    };
    private static final Set<MultipartType> REQUEST_NO_BODY_SET =
            new TreeSet<MultipartType>(Arrays.asList(REQUEST_NO_BODY));

    private static void validateNoBody(MultipartType type) {
        if (!REQUEST_NO_BODY_SET.contains(type))
            throw new IllegalArgumentException(E_USE_SET_BODY + type);
    }

    /** Sets the type of this multipart request; Since 1.0.
     * <p>
     *  This method should only be used for request types that have
     *  no corresponding body.
     *  For those requests that have a body, use {@link #body(MultipartBody)}
     *  instead.
     *
     * @param type the multipart type for this request
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if type is null
     * @throws IllegalArgumentException if the type requires a body
     * @see #body(MultipartBody)
     */
    public OfmMutableMultipartRequest type(MultipartType type) {
        mutt.checkWritable(this);
        // do NOT check for version. See OfmMultipartRequest class description.
        notNull(type);
        validateNoBody(type);
        this.multipartType = type;
        return this;
    }

    /** Sets the body of this multipart request; Since 1.0.
     * <p>
     *  This method should only be used for request types that have a body.
     *  For those requests that do not have a body, use
     *  {@link #type(MultipartType)} instead.
     *
     * @see #type(MultipartType)
     * @param body the multipart body for this request
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
    public OfmMutableMultipartRequest body(MultipartBody body) {
        mutt.checkWritable(this);
        // do NOT check for version. See OfmMultipartRequest class description.
        notNull(body);
        sameVersion("MP-Request / Body", header.version, body.getVersion());
        this.multipartType = MpBodyFactory.getType(body);
        this.body = body;
        int fixed = header.version == V_1_0 ? MessageFactory.LIB_MP_HEADER_10
                                            : MessageFactory.LIB_MP_HEADER;
        this.header.length = fixed + body.getTotalLength();
        return this;
    }


    /* IMPLEMENTATION NOTE:
     *   Although the more general approach would be to define the method
     *     public void setFlags(Set<MultipartRequestFlag> flags),
     *   there is, currently, only a single flag to set: REQUEST_MORE.
     *   Therefore, in the interests of simplicity we will define the
     *   following method instead...
     */

    /** Sets the REQUEST_MORE flag.
     * <p>
     * The presence of this flag indicates to the receiver that more (related)
     * multipart-request messages are on their way; Only the last request
     * in such a sequence should have this flag {@code false} (cleared).
     * <p>
     * The default setting for this flag is {@code false}; calling this
     * method will latch the flag to {@code true}.
     *
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     */
    public OfmMutableMultipartRequest setMoreFlag() {
        mutt.checkWritable(this);
        // do NOT check for version. See OfmMultipartRequest class description.
        if (flags == null)
            flags = new TreeSet<MultipartRequestFlag>();
        flags.add(REQUEST_MORE);
        return this;
    }
}