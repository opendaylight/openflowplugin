/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.flow;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.Flow;

/**
 * Flow related utils
 */
public abstract class FlowConvertorUtil {

    /**
     * Method wrapping all the actions org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action
     * in org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action, to set appropriate keys
     * for actions.
     *
     * @param actionList the action list
     * @return the list
     */
    public static List<Action> wrapActionList(List<org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action> actionList) {
        List<Action> actions = new ArrayList<>();

        int actionKey = 0;
        for (org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action : actionList) {
            ActionBuilder wrappedAction = new ActionBuilder();
            wrappedAction.setAction(action);
            wrappedAction.setKey(new ActionKey(actionKey));
            wrappedAction.setOrder(actionKey);
            actions.add(wrappedAction.build());
            actionKey++;
        }

        return actions;
    }

    /**
     * Safely gets ip protocol from flow.
     *
     * @param flow the flow
     * @return the ip protocol from flow
     */
    public static Short getIpProtocolFromFlow(Flow flow) {
        Short ipProtocol = null;

        if (flow.getMatch() != null && flow.getMatch().getIpMatch() != null) {
            ipProtocol = flow.getMatch().getIpMatch().getIpProtocol();
        }

        return ipProtocol;
    }
}
