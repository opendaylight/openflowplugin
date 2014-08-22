/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.*;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.of.lib.msg.PortFactory;

import java.util.ArrayList;
import java.util.List;

import static org.opendaylight.of.lib.CommonUtils.*;

/**
 * Represents a port description; part of a reply to a port-description
 * request multipart message; since 1.3.
 *
 * @author Scott Simes
 * @author Simon Hunt
 */
public class MBodyPortDesc extends OpenflowStructure implements MultipartBody {

    Port port;

    /**
     * Constructs a multipart body PORT_DESC type.
     *
     * @param pv the protocol version
     */
    public MBodyPortDesc(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public String toString() {
        return port.toString();
    }

    @Override
    public String toDebugString() {
        return toDebugString(0);
    }

    /** Returns a multi-line string representation of this structure, with
     * the specified indentation.
     *
     * @param indent the number of spaces to indent
     * @return the multi-line string representation
     */
    public String toDebugString(int indent) {
        return port.toDebugString(indent);
    }

    @Override
    public int getTotalLength() {
        return PortFactory.getPortLength(version);
    }

    /** Returns the list of ports; Since 1.3.
     *
     * @return the list of ports
     */
    public Port getPort() {
        return port;
    }


    //=======================================================================
    // Exo-Skeletal Arrays

    private static final String LINE_PREFIX = EOLI + "-- PORT ";
    private static final String LINE = " ----------------" + EOLI;

    /** Represents an array of port descriptions. */
    public static class Array extends MBodyList<MBodyPortDesc> {
        Array(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public Class<MBodyPortDesc> getElementClass() {
            return MBodyPortDesc.class;
        }

        @Override
        public String toString() {
            return "{Array::Ports: count=" + cSize(list) + "}";
        }

        @Override
        public String toDebugString() {
            StringBuilder sb = new StringBuilder();
            int index = 0;
            for (MBodyPortDesc pd: list)
                sb.append(LINE_PREFIX).append(index++).append(LINE)
                        .append(pd.toDebugString(2));
            return sb.toString();
        }

        /** Returns the contents of this port description message body as
         * a list of ports.
         *
         * @return the list of ports contained within this message
         */
        public List<Port> getPorts() {
            List<Port> ports = new ArrayList<Port>(list.size());
            for (MBodyPortDesc pd: list)
                ports.add(pd.getPort());
            return ports;
        }
    }


    /** A mutable array of port descriptions. */
    public static class MutableArray extends Array implements MutableStructure {

        private final Mutable mutt = new Mutable();

        /** Constructor, initializing the internal list.
         *
         * @param pv protocol version
         */
        MutableArray(ProtocolVersion pv) {
            super(pv);
        }

        @Override
        public OpenflowStructure toImmutable() {
            // Can only do this once
            mutt.invalidate(this);
            // Transfer the payload to an immutable instance
            MBodyPortDesc.Array array = new Array(version);
            // copy elements across
            array.addAll(this.list);
            return array;
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
        // ==== ADDERS

        /** Adds a port to this mutable port description array.
         *
         * @param port the port to add
         * @return self, for chaining
         * @throws InvalidMutableException if this array is no longer writable
         * @throws NullPointerException if port is null
         * @throws IllegalArgumentException if port is mutable
         * @throws IncompleteStructureException if the port is incomplete
         */
        public MutableArray addPort(Port port)
                throws IncompleteStructureException {
            mutt.checkWritable(this);
            notNull(port);
            notMutable(port);
            port.validate();
            MBodyPortDesc pd = new MBodyPortDesc(version);
            pd.port = port;
            list.add(pd);
            return this;
        }
    }
}
