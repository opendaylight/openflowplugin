/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.rpc.listener;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TransactionCommitterUtil {


    private static final Logger LOG = LoggerFactory.getLogger(TransactionCommitterUtil.class);
    private static final String NOT_ABLE_TO_WRITE_TO_TRANSACTION = "Not able to write to transaction: ";

    private final TxFacade txFacade;

    public TransactionCommitterUtil(final TxFacade txFacade) {
        this.txFacade = txFacade;
    }

    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void addDataToOperationalDataStore(KeyedInstanceIdentifier<I, K> itemPath, I itemBody) {
        try {
            LOG.debug("onAdded {} transaction {}",itemBody,txFacade.isTransactionsEnabled());
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, itemPath, itemBody);
            txFacade.submitTransaction();
        } catch (Exception e) {
            LOG.warn(NOT_ABLE_TO_WRITE_TO_TRANSACTION, e);
        }
    }


    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void removeDataFromOperationalDataStore(KeyedInstanceIdentifier<I, K> itemPath) {
        try {
            LOG.debug("onRemoved {} transaction {}",itemPath,txFacade);
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, itemPath);
            txFacade.submitTransaction();
        } catch (Exception e) {
            LOG.warn(NOT_ABLE_TO_WRITE_TO_TRANSACTION, e);
        }
    }


    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void updateDataToOperationalDataStore(KeyedInstanceIdentifier<I, K> itemPath, I itemBody) {
        try {
            LOG.debug("onUpdated {}",itemPath);
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, itemPath);
            txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, itemPath, itemBody);
            txFacade.submitTransaction();
        } catch (Exception e) {
            LOG.warn(NOT_ABLE_TO_WRITE_TO_TRANSACTION, e);
        }
    }


}

