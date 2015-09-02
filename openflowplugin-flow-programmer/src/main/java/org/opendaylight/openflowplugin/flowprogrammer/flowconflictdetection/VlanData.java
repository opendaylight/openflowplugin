/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;

import java.lang.Integer;
import java.lang.Short;
import java.lang.Boolean;

/**
 * This class converts VlanMatch to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class VlanData {
    private Integer vlanId;
    private Boolean vlanIdPresent;
    private Short vlanPcp;

    public VlanData(Integer vlanId, Boolean vlanIdPresent, Short vlanPcp) {
        this.vlanId = vlanId;
        this.vlanIdPresent = vlanIdPresent;
        this.vlanPcp = vlanPcp;
    }

    public static VlanData toVlanData(VlanMatch match) {
        Integer vlanId = Integer.valueOf(-1);
        Boolean vlanIdPresent = Boolean.FALSE;
        Short vlanPcp = Short.valueOf((short)(-1));
        if (match != null) {
            if (match.getVlanId() != null) {
                vlanId = match.getVlanId().getVlanId().getValue();
            }
            vlanIdPresent = match.getVlanId().isVlanIdPresent();
            if (match.getVlanPcp() != null) {
                vlanPcp = match.getVlanPcp().getValue();
            }
        }
        return new VlanData(vlanId, vlanIdPresent, vlanPcp);
    }

    public boolean isSame(VlanMatch match) {
        VlanData vlanData = toVlanData(match);
        if (!vlanData.vlanId.equals(this.vlanId)
            && !vlanData.vlanId.equals(Integer.valueOf((short)(-1)))
            && !this.vlanId.equals(Integer.valueOf((short)(-1)))) {
            return false;
        }
        if (vlanData.vlanIdPresent != this.vlanIdPresent) {
            return false;
        }
        if (!vlanData.vlanPcp.equals(this.vlanPcp)
            && !vlanData.vlanPcp.equals(Short.valueOf((short)(-1)))
            && !this.vlanPcp.equals(Short.valueOf((short)(-1)))) {
            return false;
        }
        return true;
    }
}
