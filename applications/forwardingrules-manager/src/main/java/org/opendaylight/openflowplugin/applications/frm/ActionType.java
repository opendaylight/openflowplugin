/*
 * Copyright (c) 2018 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frm;

import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCase;

public enum ActionType {
    APPLY_ACTION(ApplyActionsCase.class),
    GROUP_ACTION(GroupActionCase.class);

    private Class<?> actionType;

    ActionType(Class<?> applyActionsCaseClass) {
        this.actionType = applyActionsCaseClass;
    }

    public Class<?> getActionType() {
        return actionType;
    }

}
