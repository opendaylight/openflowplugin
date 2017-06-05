package org.opendaylight.openflowplugin.applications.reconciliation.cli;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationManager;
import org.opendaylight.openflowplugin.applications.reconciliation.IReconciliationTaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by eknnosd on 6/20/2017.
 */
@Command(scope = "reconciliation", name = "get", description = "displaying services registered to Reconciliation Framework")
public class GetRegisteredServices extends OsgiCommandSupport {

    private IReconciliationManager reconciliationmgr;
    private static final Logger LOG = LoggerFactory.getLogger(GetRegisteredServices.class);
    public static final String CLI_FORMAT = "%d %-20s ";


    public void setReconciliationmgr(IReconciliationManager reconciliationmgr) {
        this.reconciliationmgr = reconciliationmgr;
    }
    @Override
    protected Object doExecute() throws Exception {
        for(Map.Entry<Integer,List<IReconciliationTaskFactory>> registeredService : reconciliationmgr.getRegisteredServices().entrySet())
        {
            List<IReconciliationTaskFactory> services = registeredService.getValue();
            for(IReconciliationTaskFactory service  : services)
            {
               session.getConsole().println(String.format(CLI_FORMAT,service.getPriority(),service.getServiceName()));
            }
        }
        return null;
    }

    }


