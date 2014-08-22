/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.util.NotYetImplementedException;

import static org.opendaylight.of.lib.msg.MessageCopier.CopyType.*;

/**
 * Copies OpenFlow messages to create read-only or mutable versions.
 *
 * @author Simon Hunt
 */
class MessageCopier {
    static enum CopyType {
        IMMUTABLE(false),
        IMMUTABLE_EXACT(true),
        MUTABLE(false),
        MUTABLE_EXACT(true);

        private final boolean exact;

        CopyType(boolean exact) { this.exact = exact; }
        public boolean exact() { return exact; }
    }

    /**
     * Creates an immutable copy of the supplied OpenFlow message.
     * Note, however, that a new XID is assigned to the copy.
     *
     * @param msg the message to copy
     * @return the immutable copy
     */
    static OpenflowMessage copy(OpenflowMessage msg) {
        switch (msg.getType()) {
            case FLOW_MOD:
                return OfmMutableFlowMod.makeCopy((OfmFlowMod) msg, IMMUTABLE);
            default:
                throw new NotYetImplementedException();
        }
    }

    /**
     * Creates an immutable copy of the supplied OpenFlow message.
     * Note that the XID of the original is retained in the copy.
     *
     * @param msg the message to copy
     * @return the immutable copy
     */
    static OpenflowMessage exactCopy(OpenflowMessage msg) {
        switch (msg.getType()) {
            case FLOW_MOD:
                return OfmMutableFlowMod.makeCopy((OfmFlowMod) msg,
                        IMMUTABLE_EXACT);
            default:
                throw new NotYetImplementedException();
        }
    }

    /**
     * Creates a mutable copy of the supplied OpenFlow message.
     * Note, however, that a new XID is assigned to the copy.
     *
     * @param msg the message to copy
     * @return the mutable copy
     */
    static MutableMessage mutableCopy(OpenflowMessage msg) {
        switch (msg.getType()) {
            case FLOW_MOD:
                return (MutableMessage)
                        OfmMutableFlowMod.makeCopy((OfmFlowMod) msg, MUTABLE);
            default:
                throw new NotYetImplementedException();
        }
    }

    /**
     * Creates a mutable copy of the supplied OpenFlow message.
     * Note that the XID of the original is retained in the copy.
     *
     * @param msg the message to copy
     * @return the mutable copy
     */
    static MutableMessage exactMutableCopy(OpenflowMessage msg) {
        switch (msg.getType()) {
            case FLOW_MOD:
                return (MutableMessage)
                        OfmMutableFlowMod.makeCopy((OfmFlowMod) msg,
                                MUTABLE_EXACT);
            default:
                throw new NotYetImplementedException();
        }
    }
}
