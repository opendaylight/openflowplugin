package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.util.EventListener;

import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;

public interface SessionListener extends EventListener {

    void onSessionAdded(SwitchConnectionDistinguisher sessionKey, SessionContext context);

}
