package org.opendaylight.openflowjava.protocol.spi.connection;

import org.opendaylight.infrautils.diagstatus.ServiceState;

public interface OpenflowPluginDiagStatusProvider {

    public void reportStatus(ServiceState serviceState);


    public void reportStatus(ServiceState serviceState, Throwable throwable);

    public void reportStatus(ServiceState serviceState, String description);
}
