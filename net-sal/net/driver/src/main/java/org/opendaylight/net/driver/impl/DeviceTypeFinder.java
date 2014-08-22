/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.driver.impl;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opendaylight.util.driver.DefaultDeviceInfo;
import org.opendaylight.util.driver.DefaultDeviceType;
import org.opendaylight.util.driver.DeviceDriverFactory;

/**
 * Scan through the registered Device Types and find the best match for a given
 * Vendor, Product, Firmware combination.
 * 
 * If an exact match is not found, find the best possible match.
 *
 * @author Sean Humphress
 */
public class DeviceTypeFinder {

    private DeviceDriverFactory ddp;
    private String mfr;
    private String hw;
    private String fw;

    private Map<String, DefaultDeviceType> mfgMap;

    private enum MatchField { MFG, HW, FW};
    private EnumSet<MatchField> matchFields;

    public DeviceTypeFinder(DeviceDriverFactory ddp) {
        this.ddp = ddp;
    }

    /**
     * Set the tuple used for the Device Type lookup.
     *
     * @param mfg Vendor
     * @param hw  Product
     * @param fw  Firmware version
     */
    public void setSearchTerms(String mfg, String hw, String fw) {
        this.mfr = mfg;
        this.hw  = hw;
        this.fw  = fw;
    }

    /**
     * Find the best match Device Type
     *
     * @return Device Type Name, or null if no match found
     */
    public String findTypeName() {
        String typeName;

        createMfgMatchMap(ddp.getDeviceTypeNames());

        // Find exact match
        matchFields = EnumSet.of(MatchField.MFG, MatchField.HW, MatchField.FW);
        typeName = findTypeNameLoop();
        if (typeName != null)
            return typeName;

        // Remove FW requirement and try again
        matchFields.remove(MatchField.FW);
        typeName = findTypeNameLoop();
        if (typeName != null)
            return typeName;

        // Still not a match, look for a Vendor generic driver
        matchFields.remove(MatchField.HW);
        typeName = findTypeNameLoop();
        if (typeName != null)
            return typeName;

        // No match
        return null;
    }

    /**
     * Find the Device Types that match the requested Vendor and store in a map.
     * 
     * @param names Device Type Name
     */
    private void createMfgMatchMap(Set<String> names) {
        mfgMap = new HashMap<String, DefaultDeviceType>();
        if (names != null) {
            for (String name : names) {
                DefaultDeviceInfo ddi = (DefaultDeviceInfo) ddp.create(name);
                DefaultDeviceType ddt = ddi.getType();

                if (ddt.getVendor() != null && doesMatch(ddt.getVendor(), mfr))
                    mfgMap.put(name, ddt);
            }
        }
    }
    /**
     * Loop through the map, remove entries that won't match later, and return
     * an exact match if found.
     * 
     * @return match if found, else null
     */
    private String findTypeNameLoop() {
        Iterator<Entry<String, DefaultDeviceType>> itr =
                mfgMap.entrySet().iterator();
        Entry<String, DefaultDeviceType> pair;

        while (itr.hasNext()) {
            pair = itr.next();
            if (isNameOid(pair))
                itr.remove();
            else if (!isPartialMatch(pair.getValue()))
                itr.remove();
            else if (isExactMatch(pair.getValue()))
                return pair.getKey();
        }
        return null;
    }

    /**
     * Device Types are duplicated for their oids.  Remove the oid matches to
     * reduce the number of comparisons.
     *
     * @param pair Device type name and device type
     * @return true if the device type name is an oid
     */
    private boolean isNameOid(Entry<String, DefaultDeviceType> pair) {
        DefaultDeviceType dt = pair.getValue();
        Set<String> oids = dt.getOids();
        return (oids.contains(pair.getKey()));

    }
    /**
     * Can this device type be a possible match in subsequent loops?
     * 
     * Loop1 - Exact match of all three values
     * Loop2 - Exact match of MFG / HW, with null FW
     * Loop3 - Exact match of MFG (provided by mfgMap), with null Product
     * 
     * @param ddt default device type to test
     * @return false to remove from future tests
     */
    private boolean isPartialMatch(DefaultDeviceType ddt) {
        if (matchFields.contains(MatchField.FW)) {
            if (ddt.getProduct() == null || doesMatch(ddt.getProduct(), hw))
                return true;
        }
        else if (matchFields.contains(MatchField.HW)) {
            if (ddt.getFw() == null)
                return true;
        } else {
            if (ddt.getProduct() == null)
                return true;
        }
        return false;
    }
    /**
     * Is the Device Type a match for the current search field
     *
     * @param ddt default device type
     * @return true if exact match
     */
    private boolean isExactMatch(DefaultDeviceType ddt) {
        if (matchFields.contains(MatchField.FW)) {
            return doesMatch(ddt.getFw(), fw);
        }
        if (matchFields.contains(MatchField.HW)) {
            return doesMatch(ddt.getProduct(), hw);
        }
        return doesMatch(ddt.getVendor(), mfr);
    }
    private boolean doesMatch(String a, String b) {
        if (a == null)
            return (b == null);
        return (a.equals(b));
    }

}
