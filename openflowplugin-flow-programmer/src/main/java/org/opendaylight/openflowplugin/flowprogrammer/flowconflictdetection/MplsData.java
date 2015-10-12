/*
 * Copyright (c) 2015 Intel, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer.flowconflictdetection;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;

import java.lang.Long;
import java.lang.Short;

/**
 * This class converts ProtocolMatchFields to simple data type.
 *
 * <p>
 *
 * @author Yi Yang (yi.y.yang@intel.com)
 *
 * <p>
 * @since 2015-08-25
 */

public class MplsData {
    private Short mplsBos;
    private Long mplsLabel;
    private Short mplsTc;
    private Long pbbIsid;

    public MplsData(Short mplsBos, Long mplsLabel, Short mplsTc, Long pbbIsid) {
        this.mplsBos = mplsBos;
        this.mplsLabel = mplsLabel;
        this.mplsTc = mplsTc;
        this.pbbIsid = pbbIsid;
    }

    public static MplsData toMplsData(ProtocolMatchFields match) {
        Short mplsBos = Short.valueOf((short)(-1));
        Long mplsLabel = Long.valueOf(-1);
        Short mplsTc = Short.valueOf((short)(-1));
        Long pbbIsid = Long.valueOf(-1);
        Long pbbMask = Long.valueOf(-1);
        if (match != null) {
            mplsBos = match.getMplsBos();
            mplsLabel = match.getMplsLabel();
            mplsTc = match.getMplsTc();
            if (match.getPbb() != null) {
                pbbIsid = match.getPbb().getPbbIsid();
                pbbMask = match.getPbb().getPbbMask();
                if (pbbMask != Long.valueOf(-1)) {
                    pbbIsid &= pbbMask;
                }
            }
        }
        return new MplsData(mplsBos, mplsLabel, mplsTc, pbbIsid);
    }

    public boolean isSame(ProtocolMatchFields match) {
        MplsData mplsData = toMplsData(match);
        if (!mplsData.mplsBos.equals(this.mplsBos)
            && !mplsData.mplsBos.equals(Short.valueOf((short)(-1)))
            && !this.mplsBos.equals(Short.valueOf((short)(-1)))) {
            return false;
        }
        if (!mplsData.mplsLabel.equals(this.mplsLabel)
            && !mplsData.mplsLabel.equals(Long.valueOf(-1))
            && !this.mplsLabel.equals(Long.valueOf(-1))) {
            return false;
        }
        if (!mplsData.mplsTc.equals(this.mplsTc)
            && !mplsData.mplsTc.equals(Short.valueOf((short)(-1)))
            && !this.mplsTc.equals(Short.valueOf((short)(-1)))) {
            return false;
        }
        if (!mplsData.pbbIsid.equals(this.pbbIsid)
            && !mplsData.pbbIsid.equals(Long.valueOf(-1))
            && !this.pbbIsid.equals(Long.valueOf(-1))) {
            return false;
        }
        return true;
    }
}
