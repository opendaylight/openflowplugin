package org.opendaylight.openflowplugin.impl.common;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.impl.rpc.listener.ItemLifecycleListenerImpl;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionComitterUtil {


    private static final Logger LOG = LoggerFactory.getLogger(TransactionComitterUtil.class);
    private static final String NOT_ABLE_TO_WRITE_TO_TRANSACTION = "Not able to write to transaction: ";

    private final TxFacade txFacade;

    public TransactionComitterUtil(final TxFacade txFacade) {
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


    public <I extends Identifiable<K> & DataObject, K extends Identifier<I>> void removeDataToOperationalDataStore(KeyedInstanceIdentifier<I, K> itemPath) {
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
