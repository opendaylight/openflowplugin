package org.opendaylight.openflowplugin.applications.southboundcli.cli;

/**
 * Created by eeiillu on 2/6/2018.
 */

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev171004.ForwardingrulesManagerReconciliationService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev171004.InitReconciliationInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev171004.InitReconciliationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.app.reconciliation.service.rev171004.InitReconciliationOutput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Command(scope = "openflow", name = "execresync", description = "Launch an administrative Resync")
public class ExecResync extends OsgiCommandSupport {

    private static final Logger LOG = LoggerFactory.getLogger(ExecResync.class);
    private ForwardingrulesManagerReconciliationService frmReconciliationService;

    public void setFrmReconciliationService(ForwardingrulesManagerReconciliationService forwardRulesMgrReconService) {
        this.frmReconciliationService = forwardRulesMgrReconService;
    }

    @Argument(name = "dpnId", description = "The DPN Id", required = false, multiValued = false)
    String[] dpnId;

    @Override
    protected Object doExecute() throws Exception {
        if (dpnId == null) {
            System.out.println("execresync : dpnId is NULL, Please enter a valid dpnId.");
            return null;
        }
        for(String dpn : dpnId) {
            LOG.debug("Triggering admin resync for DPN {}", dpn);
            BigInteger dpid = new BigInteger(dpn);
            InitReconciliationInput initReconInput = new InitReconciliationInputBuilder().setDpnId(dpid).build();
            Future<RpcResult<InitReconciliationOutput>> initReconOutput = frmReconciliationService.initReconciliation(initReconInput);
            try {
                RpcResult<InitReconciliationOutput> rpcResult = initReconOutput.get();
                if (rpcResult.isSuccessful()) {
                    System.out.println("Resync successfully completed for DPN " + dpn);
                    LOG.info("Resync successfully completed for DPN {}", dpn);
                } else {
                    System.out.println("Resync failed for DPN " + dpn + ", please check logs");
                    LOG.error("Resync failed for DPN {} with error {}", dpn, rpcResult.getErrors());
                }
            } catch (ExecutionException e) {
                LOG.error("Error occurred while invoking execresync RPC for DPN {}", dpn, e);
            }
        }
        return null;
    }
}