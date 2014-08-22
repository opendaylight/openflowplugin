/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.of.lib.dt.TableId;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_2;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;

/**
 * Mutable subclass of {@link OfmTableMod}.
 *
 * @author Simon Hunt
 */
public class OfmMutableTableMod extends OfmTableMod
        implements MutableMessage {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow TABLE_MOD message.
     *
     * @param header the message header
     */
    OfmMutableTableMod(Header header) {
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
        OfmTableMod msg = new OfmTableMod(header);
        msg.tableId = this.tableId;
        msg.config = this.config;
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

    /** Sets the table id; Since 1.1.
     * <p>
     * Note the special values:
     * <ul>
     *     <li>
     *         {@link TableId#MAX} : the maximum allowable value
     *     </li>
     *     <li>
     *         {@link TableId#ALL} : represents <em>all</em> tables
     *     </li>
     * </ul>
     *
     * @param tableId the table id
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version &lt; 1.1
     * @throws NullPointerException if tableId is null
     */
    public OfmMutableTableMod tableId(TableId tableId) {
        mutt.checkWritable(this);
        verMin11(header.version);
        notNull(tableId);
        this.tableId = tableId;
        return this;
    }

    /** Sets the table configuration flags; Since 1.1; Deprecated in 1.3.
     * <p>
     * Since 1.3, the table config flags are reserved for future use.
     *
     * @param flags the table configuration flags to set
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if version is not 1.1 or 1.2
     */
    public OfmMutableTableMod config(Set<TableConfig> flags) {
        mutt.checkWritable(this);
        ProtocolVersion pv = header.version;
        verMin11(pv);
        if (pv.gt(V_1_2))
            throw new VersionMismatchException(pv + E_DEPRECATED + V_1_3);

        if (flags == null) {
            this.config = null;
        } else {
            this.config = new TreeSet<TableConfig>();
            this.config.addAll(flags);
        }
        return this;
    }
}
