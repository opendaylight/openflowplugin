/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;

import java.util.*;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/**
 * Encapsulates configuration information about the version(s) the mock
 * switch announces it supports, via its HELLO message to the controller.
 *
 * @author Simon Hunt
 */
public class CfgHello {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            CfgHello.class, "cfgHello");

    private static final String E_NO_PVS = RES.getString("e_no_pvs");

    private Behavior how;
    private final Set<ProtocolVersion> versions;
    private final ProtocolVersion pv;
    private boolean legacy = false;

    /** Configures the mock-switch outbound HELLO message. The sending
     * behavior determines whether the switch will send the HELLO message
     * immediately after the connection is established (eagerly), or whether
     * it will wait until it receives the HELLO message from the controller
     * (lazily).
     *
     * @param sendHow HELLO sending behavior
     * @param pvs protocol versions reported as supported
     */
    public CfgHello(Behavior sendHow, ProtocolVersion... pvs) {
        notNull(sendHow, pvs);
        if (pvs.length == 0)
            throw new IllegalArgumentException(E_NO_PVS);

        this.how = sendHow;
        this.versions = new HashSet<ProtocolVersion>(Arrays.asList(pvs));
        this.pv = ProtocolVersion.max(versions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{cfgHello: ");
        sb.append(how).append(",pv=").append(pv)
                .append(",suppPv=").append(versions)
                .append(",legacy=").append(legacy)
                .append("}");
        return sb.toString();
    }

    /** Latch HELLO to be the "legacy" format; that is, not the
     * 1.3.1 structure with versionBitmap.
     * @return self; for chaining
     * @throws IllegalStateException if more than one version was specified
     *          in the constructor
     */
    public CfgHello legacy() {
        legacy = true;
        return this;
    }

    /** Creates the HELLO message to send to the controller, based on the
     * configuration.
     *
     * @return the HELLO message to send
     */
    public OfmHello createHelloMsg() {
        OfmMutableHello hello =
                (OfmMutableHello) MessageFactory.create(pv, MessageType.HELLO);
        hello.clearXid();
        if (!legacy) {
            HelloElement vbitmap =
                    HelloElementFactory.createVersionBitmapElement(versions);
            hello.addElement(vbitmap);
        }
        return (OfmHello) hello.toImmutable();
    }

    /** Returns the highest protocol version configured.
     *
     * @return the max version
     */
    public ProtocolVersion getMaxVersion() {
        return pv;
    }

    /** Returns the required behavior of the switch when sending its HELLO.
     *
     * @return the HELLO send behavior
     */
    public Behavior getBehavior() {
        return how;
    }

    /** Returns the set of supported versions.
     *
      * @return the supported versions
     */
    public Set<ProtocolVersion> getVersions() {
        return Collections.unmodifiableSet(versions);
    }

    /** Returns true if the legacy flag is latched. That is, the generated
     * HELLO message will not contain the "version bitmap" hello element.
     *
     * @return true if legacy
     */
    public boolean isLegacy() {
        return legacy;
    }


    /** Mock switch's behavior in sending the HELLO message. */
    public static enum Behavior {
        /** Send eagerly; that is, send the HELLO as soon as the
         * connection is made.
         */
        EAGER,
        /** Send lazily; that is, wait for the HELLO from the controller
         * before sending ours to the controller.
         */
        LAZY,
    }
}