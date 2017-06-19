/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

public enum ForwardingRulesProperty {
    DISABLE_RECONCILIATION,
    STALE_MARKING_ENABLED,
    RECONCILIATION_RETRY_COUNT;

    /**
     * Converts enum name to property key
     *
     * @return the property key
     */
    @Override
    public String toString() {
        return this.name().toLowerCase().replace('_', '-');
    }
}
