/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.instr;

import org.opendaylight.of.lib.InvalidMutableException;
import org.opendaylight.of.lib.Mutable;
import org.opendaylight.of.lib.MutableStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ValidationException;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Mutable subclass of {@link InstrAction}.
 *
 * @author Simon Hunt
 */
public abstract class InstrMutableAction extends InstrAction
        implements MutableStructure {

    final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable instruction.
     *
     * @param pv the protocol version
     * @param header the instruction header
     */
    InstrMutableAction(ProtocolVersion pv, Header header) {
        super(pv, header);
        this.header.length = InstructionFactory.INSTR_HEADER_LEN;
    }

    // leave concrete subclasses to implement toImmutable()

    @Override
    public boolean writable() {
        return mutt.writable();
    }

    @Override
    public String toString() {
        return mutt.tagString(super.toString());
    }

    /** Adds the specified action to this instruction.
     *
     * @param action the action to add
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if action is null
     */
    public InstrMutableAction addAction(Action action) {
        mutt.checkWritable(this);
        notNull(action);
        actions.add(action);
        validateActions();
        // if we are still going, the action validated okay
        header.length += action.header.length;
        return this;
    }

    /** Subclasses should validate the actions, and silently return
     * if all is well.
     * If a problem is detected, a ValidationException should be thrown.
     * @throws ValidationException if a problem with the actions
     */
    protected abstract void validateActions();

}
