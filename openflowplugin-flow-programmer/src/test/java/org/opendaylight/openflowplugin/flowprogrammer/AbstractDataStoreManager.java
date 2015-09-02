/*
 * Copyright (c) 2015 Intel. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.flowprogrammer;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;

/**
 * This class contains auxiliary methods to manage abstract data store
 *
 * @author Yi Yang <yi.y.yang@intel.com>
 * @since 2015-09-08
 */

/*
 * The purpose of this class is to get DataBroker used in tests
 */
public abstract class AbstractDataStoreManager extends AbstractDataBrokerTest {
    protected DataBroker dataBroker;

    /* set dataBroker for test use */
    protected void setDataBroker() {
        this.dataBroker = getDataBroker();
    }
}
