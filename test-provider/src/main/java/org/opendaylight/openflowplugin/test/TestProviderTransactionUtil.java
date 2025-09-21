/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import java.util.concurrent.ExecutionException;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak mbobak@cisco.com on 8/20/14.
 */
public final class TestProviderTransactionUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TestProviderTransactionUtil.class);

    private TestProviderTransactionUtil() {
        // Hidden on purpose
    }

    public static <T extends DataObject> T getDataObject(final ReadTransaction readOnlyTransaction,
            final DataObjectIdentifier<T> identifier) {
        try {
            return readOnlyTransaction.read(LogicalDatastoreType.OPERATIONAL, identifier).get().orElse(null);
        } catch (ExecutionException | InterruptedException e) {
            LOG.error("Read transaction for identifier {} failed.", identifier, e);
            return null;
        }
    }
}
