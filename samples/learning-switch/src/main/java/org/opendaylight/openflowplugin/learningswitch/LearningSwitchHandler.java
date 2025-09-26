/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;

public interface LearningSwitchHandler {
    /**
     * Invoked when a switch appears.
     *
     * @param tablePath the table path
     */
    void onSwitchAppeared(DataObjectIdentifier<Table> tablePath);
}
