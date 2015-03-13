/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionSerializerKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.action.container.action.choice.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class ActionExtensionHelper {

    private static final Logger LOG = LoggerFactory
            .getLogger(ActionExtensionHelper.class);

    private ActionExtensionHelper() {
        throw new IllegalAccessError("singleton enforcement");
    }

    /**
     * @param action
     * @param ofVersion
     * @param actionPath
     * @return augmentation wrapper containing augmentation depending on matchPath
     */
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action
    processAlienAction(Action action, OpenflowVersion ofVersion, ActionPath actionPath) {
        ExperimenterIdCase actionCase = (ExperimenterIdCase) action.getActionChoice();
        /** TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL) */
        ExperimenterActionSerializerKey key = new ExperimenterActionSerializerKey(
                ofVersion.getVersion(),
                actionCase.getExperimenter().getExperimenter().getValue(),
                actionCase.getExperimenter().getSubType());

        org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action alienAction = null;
        ConvertorActionFromOFJava<Action, ActionPath> convertor = OFSessionUtil.getExtensionConvertorProvider().getActionConverter(key);
        if (convertor != null) {
            alienAction = convertor.convert(
                    action, actionPath);
        }
        return alienAction;
    }
}
