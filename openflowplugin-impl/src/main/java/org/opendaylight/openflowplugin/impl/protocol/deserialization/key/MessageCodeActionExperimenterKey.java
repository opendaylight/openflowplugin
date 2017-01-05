/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.deserialization.key;

import java.util.Objects;

import org.opendaylight.openflowplugin.api.openflow.protocol.deserialization.MessageCodeExperimenterKey;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;

public class MessageCodeActionExperimenterKey extends MessageCodeExperimenterKey {

    private ActionPath actionPath;

    /**
     * Constructor
     * @param version wire protocol version
     * @param value used as distinguisher (read from binary data / buffer)
     * @param clazz class of object that is going to be deserialized
     * @param experimenterId experimenter id
     */
    public MessageCodeActionExperimenterKey(short version, int value, Class<?> clazz, ActionPath actionPath, Long experimenterId) {
        super(version, value, clazz, experimenterId);
        this.actionPath = actionPath;
    }

    public ActionPath getActionPath() {
        return actionPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        return prime * super.hashCode() + (Objects.isNull(actionPath) ? 0 : actionPath.ordinal());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof MessageCodeActionExperimenterKey)) {
            return false;
        }
        MessageCodeActionExperimenterKey other = (MessageCodeActionExperimenterKey) obj;

        if (actionPath == null) {
            if (other.actionPath != null) {
                return false;
            }
        } else if (!actionPath.equals(other.actionPath)) {
            return false;
        }

        return super.equals(obj);
    }

    @Override
    public String toString() {
        return super.toString() + " actionPath: " + actionPath.name();
    }
}
