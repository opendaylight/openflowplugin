/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.ExperimenterMeterBandSubType;

public class ExperimenterIdMeterBandKey<T extends ExperimenterMeterBandSubType> extends TypeVersionKey<T> {

    private Long experimenterId;

    /**
     * @param type            class of object which vendor defined
     * @param ofVersion       protocol wire version
     * @param experimenterId  experimenter / vendor ID
     */
    public ExperimenterIdMeterBandKey(Class<T> type, short ofVersion, Long experimenterId) {
        super(type, ofVersion);
        this.experimenterId = experimenterId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((experimenterId == null) ? 0 : experimenterId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ExperimenterIdMeterBandKey<?> other = (ExperimenterIdMeterBandKey<?>) obj;
        if (experimenterId == null) {
            if (other.experimenterId != null) {
                return false;
            }
        } else if (!experimenterId.equals(other.experimenterId)) {
            return false;
        }
        return true;
    }

}
