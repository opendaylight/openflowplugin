/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.msg;

import org.opendaylight.of.lib.*;
import org.opendaylight.util.net.BigPortNumber;
import org.opendaylight.util.net.MacAddress;

import java.util.Set;
import java.util.TreeSet;

import static org.opendaylight.of.lib.CommonUtils.*;
import static org.opendaylight.util.PrimitiveUtils.verifyU32;

/**
 * Mutable subclass of {@link Port}.
 *
 * @author Simon Hunt
 */
public class MutablePort extends Port implements MutableStructure {

    private final Mutable mutt = new Mutable();

    /**
     * Constructs a mutable OpenFlow port structure.
     *
     * @param pv the protocol version
     */
    public MutablePort(ProtocolVersion pv) {
        super(pv);
    }

    @Override
    public OpenflowStructure toImmutable() {
        // Can only do this once
        mutt.invalidate(this);
        // Transfer the payload to an immutable instance
        Port port = new Port(this.version);
        port.portNumber = this.portNumber;
        port.hwAddress = this.hwAddress;
        port.name = this.name;
        port.config = this.config;
        port.state = this.state;
        port.current = this.current;
        port.advertised = this.advertised;
        port.supported = this.supported;
        port.peer = this.peer;
        port.currentSpeed = this.currentSpeed;
        port.maxSpeed = this.maxSpeed;
        return port;
    }

    /** Produces a copy of the given port.
     *
     * @param original the original port
     * @param mutable whether the copy should be mutable
     * @return the copy
     */
    static Port makeCopy(Port original, boolean mutable) {
        ProtocolVersion pv = original.getVersion();
        Port copy = mutable ? new MutablePort(pv) : new Port(pv);
        copy.portNumber = original.portNumber;
        copy.hwAddress = original.hwAddress;
        copy.name = original.name;
        if (original.config != null)
            copy.config = new TreeSet<PortConfig>(original.config);
        if (original.state != null)
            copy.state = new TreeSet<PortState>(original.state);
        if (original.current != null)
            copy.current = new TreeSet<PortFeature>(original.current);
        if (original.advertised != null)
            copy.advertised = new TreeSet<PortFeature>(original.advertised);
        if (original.supported != null)
            copy.supported = new TreeSet<PortFeature>(original.supported);
        if (original.peer != null)
            copy.peer = new TreeSet<PortFeature>(original.peer);
        copy.currentSpeed = original.currentSpeed;
        copy.maxSpeed = original.maxSpeed;
        return copy;
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

    /** Sets the port number for this port; Since 1.0.
     * <p>
     * Note that in 1.0, port numbers are u16.
     *
     * @param portNumber the port number
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if the argument is null
     * @throws IllegalArgumentException if the port number is invalid
     */
    public MutablePort portNumber(BigPortNumber portNumber) {
        mutt.checkWritable(this);
        notNull(portNumber);
        Port.validatePortValue(portNumber, version);
        this.portNumber = portNumber;
        return this;
    }

    /** Sets the hardware address for this port; Since 1.0.
     *
     * @param hwAddress the hardware address
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if the argument is null
     */
    public MutablePort hwAddress(MacAddress hwAddress) {
        mutt.checkWritable(this);
        notNull(hwAddress);
        this.hwAddress = hwAddress;
        return this;
    }

    /** Sets the friendly name for this port; Since 1.0.
     * Note that the maximum allowed length of the name is 15 characters;
     * an exception is thrown if the argument exceeds this length.
     *
     * @param name the friendly name
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name length is &gt;15
     */
    public MutablePort name(String name) {
        mutt.checkWritable(this);
        notNull(name);
        stringField(name, PortFactory.NAME_FIELD_LEN);
        this.name = name;
        return this;
    }

    /** Sets the port configuration flags, (may be null); Since 1.0.
     * The specified flags are validated against the protocol version
     * of this port structure. An exception is thrown if there is a
     * flag present that is not supported by the protocol version.
     *
     * @param config the port configuration flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if an unsupported flag is present
     */
    public MutablePort config(Set<PortConfig> config) {
        mutt.checkWritable(this);
        // TODO : PortConfig.verifyFlags(config, version);
        this.config = config == null ? null : new TreeSet<PortConfig>(config);
        return this;
    }

    /** Sets the port state flags, (may be null); Since 1.0.
     * The specified flags are validated against the protocol version
     * of this port structure. An exception is thrown if there is a
     * flag present that is not supported by the protocol version.
     *
     * @param state the port state flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if an unsupported flag is present
     */
    public MutablePort state(Set<PortState> state) {
        mutt.checkWritable(this);
        // TODO : PortState.verifyFlags(state, version);
        this.state = state == null ? null : new TreeSet<PortState>(state);
        return this;
    }

    /** Sets the port current features flags, (may be null); Since 1.0.
     * The specified flags are validated against the protocol version
     * of this port structure. An exception is thrown if there is a
     * flag present that is not supported by the protocol version.
     *
     * @param current the port current features flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if an unsupported flag is present
     */
    public MutablePort current(Set<PortFeature> current) {
        mutt.checkWritable(this);
        // TODO : PortFeature.verifyFlags(state, version);
        this.current = current == null ? null :
                new TreeSet<PortFeature>(current);
        return this;
    }

    /** Sets the port advertised features flags, (may be null); Since 1.0.
     * The specified flags are validated against the protocol version
     * of this port structure. An exception is thrown if there is a
     * flag present that is not supported by the protocol version.
     *
     * @param advertised the port advertised features flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if an unsupported flag is present
     */
    public MutablePort advertised(Set<PortFeature> advertised) {
        mutt.checkWritable(this);
        // TODO : PortFeature.verifyFlags(state, version);
        this.advertised = advertised == null ? null :
                new TreeSet<PortFeature>(advertised);
        return this;
    }

    /** Sets the port supported features flags, (may be null); Since 1.0.
     * The specified flags are validated against the protocol version
     * of this port structure. An exception is thrown if there is a
     * flag present that is not supported by the protocol version.
     *
     * @param supported the port supported features flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if an unsupported flag is present
     */
    public MutablePort supported(Set<PortFeature> supported) {
        mutt.checkWritable(this);
        // TODO : PortFeature.verifyFlags(state, version);
        this.supported = supported == null ? null :
                new TreeSet<PortFeature>(supported);
        return this;
    }

    /** Sets the port peer features flags, (may be null); Since 1.0.
     * The specified flags are validated against the protocol version
     * of this port structure. An exception is thrown if there is a
     * flag present that is not supported by the protocol version.
     *
     * @param peer the port peer features flags
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if an unsupported flag is present
     */
    public MutablePort peer(Set<PortFeature> peer) {
        mutt.checkWritable(this);
        // TODO : PortFeature.verifyFlags(state, version);
        this.peer = peer == null ? null :
                new TreeSet<PortFeature>(peer);
        return this;
    }

    /** Sets the port current speed (bitrate) in kbps; Since 1.1.
     * <p>
     * The number should be rounded to match common usage. For example
     * an optical 10 Gb Ethernet port should have this field set to
     * {@code 10000000} (instead of {@code 10312500}), and an
     * OC-192 port should have this field set to {@code 10000000}
     * (instead of {@code 9953280}).
     *
     * @param currentSpeed the current speed
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if protocol version is &lt; 1.1
     * @throws IllegalArgumentException if value is not u32
     */
    public MutablePort currentSpeed(long currentSpeed) {
        mutt.checkWritable(this);
        verMin11(version, "port: current speed");
        verifyU32(currentSpeed);
        this.currentSpeed = currentSpeed;
        return this;
    }

    /** Sets the port maximum speed (bitrate) in kbps; Since 1.1.
     * <p>
     * The number should be rounded to match common usage. For example
     * an optical 10 Gb Ethernet port should have this field set to
     * {@code 10000000} (instead of {@code 10312500}), and an
     * OC-192 port should have this field set to {@code 10000000}
     * (instead of {@code 9953280}).
     *
     * @param maxSpeed the maximum speed
     * @return self, for chaining
     * @throws InvalidMutableException if this instance is no longer writable
     * @throws VersionMismatchException if protocol version is &lt; 1.1
     * @throws IllegalArgumentException if value is not u32
     */
    public MutablePort maxSpeed(long maxSpeed) {
        mutt.checkWritable(this);
        verMin11(version, "port: max speed");
        verifyU32(maxSpeed);
        this.maxSpeed = maxSpeed;
        return this;
    }
}
