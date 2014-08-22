/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.msg.Capability;
import org.opendaylight.util.net.IpAddress;

import java.util.Set;

import static org.opendaylight.of.lib.CommonUtils.notNull;

/** Encapsulates the base configuration of a mock-switch.
 *
 *  @author Simon Hunt
 */
public class CfgBase {
    private IpAddress controllerAddress;
    private int openflowPort;
    private int openflowTlsPort;
    private int openflowUdpPort;
    private int bufferCount;
    private int tableCount;
    private Set<Capability> capabilities;

    /** No-args constructor to allow incremental creation. */
    public CfgBase() { }

    @Override
    public String toString() {
        return "{cfgBase:ctlr=" + controllerAddress + ":" + openflowPort +
                ":" + openflowTlsPort + ":" + openflowUdpPort +
                ",#buf=" + bufferCount + ",#tab=" + tableCount + ",...}";
    }

    /** Returns the configured controller address.
     *
     * @return the controller address
     */
    public IpAddress getControllerAddress() {
        return controllerAddress;
    }

    /** Returns the configured OpenFlow port.
     *
     * @return the openflow port
     */
    public int getOpenflowPort() {
        return openflowPort;
    }
    
    /** Returns the configured OpenFlow TLS port.
     *
     * @return the openflow TLS port
     */
    public int getOpenflowTlsPort() {
        return openflowTlsPort;
    }
    
    /** Returns the configured OpenFlow UDP port.
     *
     * @return the openflow UDP port
     */
    public int getOpenflowUdpPort() {
        return openflowUdpPort;
    }

    /** Returns the configured number of buffers.
     *
     * @return the number of buffers
     */
    public int getBufferCount() {
        return bufferCount;
    }

    /** Returns the configured number of tables.
     *
     * @return the number of tables
     */
    public int getTableCount() {
        return tableCount;
    }

    /** Returns the configured capabilities.
     *
     * @return the capabilities
     */
    public Set<Capability> getCapabilities() {
        return capabilities;
    }

    //=======================================================================

    /** Sets the openflow controller address.
     *
     * @param address the controller address
     */
    public void setControllerAddress(IpAddress address) {
        notNull(address);
        this.controllerAddress = address;
    }

    /** Sets the OpenFlow listen port.
     *
     * @param port the OpenFlow port
     */
    public void setOpenflowPort(int port) {
        this.openflowPort = port;
    }
    
    /** Sets the OpenFlow TLS listen port.
     *
     * @param port the OpenFlow TLS port
     */
    public void setOpenflowTlsPort(int port) {
        this.openflowTlsPort = port;
    }
    
    /** Sets the OpenFlow UDP listen port.
     *
     * @param port the OpenFlow UDP port
     */
    public void setOpenflowUdpPort(int port) {
        this.openflowUdpPort = port;
    }

    /** Sets the switch's reported buffer count.
     *
     * @param bufferCount the buffer count
     */
    public void setBufferCount(int bufferCount) {
        this.bufferCount = bufferCount;
    }

    /** Sets the switch's reported table count.
     *
     * @param tableCount the table count
     */
    public void setTableCount(int tableCount) {
        this.tableCount = tableCount;
    }

    /** Sets the switch's reported OpenFlow capabilities.
     *
     * @param capabilities the capabilities
     */
    public void setCapabilities(Set<Capability> capabilities) {
        notNull(capabilities);
        this.capabilities = capabilities;
    }

}
