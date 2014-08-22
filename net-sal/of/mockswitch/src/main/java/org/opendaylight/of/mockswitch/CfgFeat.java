/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.of.lib.msg.PortConfig;
import org.opendaylight.of.lib.msg.PortFeature;
import org.opendaylight.of.lib.msg.SupportedAction;
import org.opendaylight.util.net.MacPrefix;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Encapsulates the feature configuration of a mock-switch.
 *
 * @author Simon Hunt
 */
public class CfgFeat {
    private Set<SupportedAction> suppActs;
    private int portCount;
    private MacPrefix portMac;
    private Set<PortConfig> portConfig;
    private Set<PortFeature> portFeatures;
    private Map<Integer, Set<PortConfig>> portConfigOverride;
    private Map<Integer, Set<PortFeature>> portFeaturesOverride;

    /** No-args constructor to allow incremental creation. */
    public CfgFeat() {
        portConfigOverride = new HashMap<Integer, Set<PortConfig>>();
        portFeaturesOverride = new HashMap<Integer, Set<PortFeature>>();
    }

    @Override
    public String toString() {
        return "{cfgFeat:#ports=" + portCount + ",MAC=" + portMac + ":*,...}";
    }

    /** Returns the set of supported actions. Should be null, unless the
     * switch being modeled is 1.0.
     *
     * @return the supported actions, or null
     */
    public Set<SupportedAction> getSuppActs() {
        return suppActs;
    }

    /** Returns the port count.
     *
     * @return the port count
     */
    public int getPortCount() {
        return portCount;
    }

    /** Returns the port MAC prefix used to configure the port MACs.
     *
     * @return the port MAC prefix
     */
    public MacPrefix getPortMac() {
        return portMac;
    }

    /** Returns the default port config flags.
     *
     * @return the port configuration flags
     */
    public Set<PortConfig> getPortConfig() {
        return portConfig;
    }

    /** Returns the port config flags for the given port index. The indices
     * run from 0 to {@link #getPortCount() portCount} - 1.
     *
     * @param index the port index
     * @return the port configuration
     * @throws IllegalArgumentException if the index is out of range
     *
     */
    public Set<PortConfig> getPortConfig(int index) {
        verifyPortIndex(index);
        Set<PortConfig> cfg = portConfigOverride.get(index);
        return cfg != null ? cfg : portConfig;
    }

    /** Returns the port feature flags. Note that this structure is
     * duplicated across all reported ports.
     *
     * @return the port feature flags
     */
    public Set<PortFeature> getPortFeatures() {
        return portFeatures;
    }

    /** Returns the port feature flags for the given port index. The indices
     * run from 0 to {@link #getPortCount() portCount} - 1.
     *
     * @param index the port index
     * @return the port features
     * @throws IllegalArgumentException if the index is out of range
     *
     */
    public Set<PortFeature> getPortFeatures(int index) {
        verifyPortIndex(index);
        Set<PortFeature> feat = portFeaturesOverride.get(index);
        return feat != null ? feat : portFeatures;
    }

    /** Verifies that the given index is within range. Returns silently if
     * all is well; throws an exception if not.
     *
     * @param index the port index
     * @throws IllegalArgumentException if the index is out of range
     */
    private void verifyPortIndex(int index) {
        if (index < 0 || index > portCount-1)
            throw new IllegalArgumentException("index not [0.." +
                    (portCount - 1) + "]: " + index);
    }

    //=======================================================================

    /** Sets the supported actions. Only applies to 1.0 switches.
     *
     * @param suppActs the supported actions
     */
    public void setSuppActs(Set<SupportedAction> suppActs) {
        this.suppActs = suppActs;
    }

    /** Sets the port count. Valid values are from 1 to 255.
     *
     * @param portCount the number of ports
     */
    public void setPortCount(int portCount) {
        this.portCount = portCount;
    }

    /** Sets the port MAC prefix. This should be of size 5.
     *
     * @param pfx the port mac prefix
     */
    public void setPortMac(MacPrefix pfx) {
        if (pfx.size() != 5)
            throw new IllegalArgumentException("Bad MAC prefix - " +
                    "not of size 5: \"" + pfx + "\"");
        this.portMac = pfx;
    }

    /** Sets the default port config.
     *
     * @param portConfig the default port config
     */
    public void setPortConfig(Set<PortConfig> portConfig) {
        this.portConfig = new TreeSet<PortConfig>(portConfig);
    }

    /** Overrides the default port config for the given port index.
     *
     * @param index the port index
     * @param portConfig the port config for that port
     */
    public void overridePortConfig(int index, Set<PortConfig> portConfig) {
        verifyPortIndex(index);
        portConfigOverride.put(index, new TreeSet<PortConfig>(portConfig));
    }

    /** Sets the default port features.
     *
     * @param portFeatures the default port features
     */
    public void setPortFeatures(Set<PortFeature> portFeatures) {
        this.portFeatures = new TreeSet<PortFeature>(portFeatures);
    }

    /** Overrides the default port features for the given port index.
     *
     * @param index the port index
     * @param features the port features for that port
     */
    public void overridePortFeatures(int index, Set<PortFeature> features) {
        verifyPortIndex(index);
        portFeaturesOverride.put(index, new TreeSet<PortFeature>(features));
    }

    //=======================================================================
    // Utility methods

    /** Picks the first Rate flag and returns it.
     *
     * @param portFeatures the port features to examine
     * @return the first rate constant found
     */
    public static PortFeature pickRate(Set<PortFeature> portFeatures) {
        for (PortFeature pf: portFeatures) {
            switch (pf) {
                case RATE_10MB_HD:
                case RATE_10MB_FD:
                case RATE_100MB_HD:
                case RATE_100MB_FD:
                case RATE_1GB_HD:
                case RATE_1GB_FD:
                case RATE_10GB_FD:
                case RATE_40GB_FD:
                case RATE_100GB_FD:
                case RATE_1TB_FD:
                    return pf;
            }
        }
        return null;
    }

    /** Using the specified rate, divides by 2 to provide a fake
     * current port speed value.
     *
     * @param rate port rate
     * @return fake current port speed
     */
    public static long pickCurrentSpeed(PortFeature rate) {
        return pickSpeed(rate, 2);
    }

    /** Uses the specified rate to return a fake max port speed value.
     *
     * @param rate port rate
     * @return fake max port speed
     */
    public static  long pickMaxSpeed(PortFeature rate) {
        return pickSpeed(rate, 1);
    }

    /** Calculates a fake speed.
     *
     * @param rate the rate constant
     * @param divisor the base value divisor
     * @return a fake speed
     */
    private static  long pickSpeed(PortFeature rate, int divisor) {
        long base;
        switch (rate) {
            case RATE_10MB_HD:
            case RATE_10MB_FD:
                base = 10000;
                break;
            case RATE_100MB_HD:
            case RATE_100MB_FD:
                base = 100000;
                break;
            case RATE_1GB_HD:
            case RATE_1GB_FD:
                base = 1000000;
                break;
            case RATE_10GB_FD:
                base = 10000000;
                break;
            case RATE_40GB_FD:
                base = 40000000;
                break;
            case RATE_100GB_FD:
                base = 100000000;
                break;
            case RATE_1TB_FD:
                base = 1000000000;
                break;
            default:
                base = 0;
                break;
        }
        return base / divisor;
    }

}
