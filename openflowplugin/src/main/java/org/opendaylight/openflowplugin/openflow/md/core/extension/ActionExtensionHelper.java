/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowjava.protocol.api.keys.ActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.keys.ExperimenterActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.ConvertorActionFromOFJava;
import org.opendaylight.openflowplugin.extension.api.core.extension.ExtensionConverterProvider;
import org.opendaylight.openflowplugin.extension.api.path.ActionPath;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev150225.experimenter.id.action.ExperimenterIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;

public final class ActionExtensionHelper {
    private ActionExtensionHelper() {
        // Hidden on purpose
    }

    /**
     * Processes an alien action.
     *
     * @param action openflow action
     * @param ofVersion openflow version
     * @param actionPath openflow action path
     * @return augmentation wrapper containing augmentation depending on matchPath
     */
    public static org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action
            processAlienAction(final Action action, final OpenflowVersion ofVersion, final ActionPath actionPath) {
        ConvertorActionFromOFJava<Action, ActionPath> convertor = null;
        final ExtensionConverterProvider extensionConvertorProvider = OFSessionUtil.getExtensionConvertorProvider();

        if (extensionConvertorProvider == null) {
            return null;
        }
        if (action == null) {
            return null;
        }

        if (action.getActionChoice() instanceof ExperimenterIdCase) {
            ExperimenterIdCase actionCase = (ExperimenterIdCase) action.getActionChoice();
            // TODO: EXTENSION PROPOSAL (action, OFJava to MD-SAL)
            ExperimenterActionSerializerKey key = new ExperimenterActionSerializerKey(
                    ofVersion.getVersion(),
                    actionCase.getExperimenter().getExperimenter().getValue(),
                    actionCase.getExperimenter().getSubType());
            convertor = extensionConvertorProvider.getActionConverter(key);
        } else if (action.getActionChoice() != null) {
            ActionSerializerKey<?> key = new ActionSerializerKey(EncodeConstants.OF_VERSION_1_3,
                    action.getActionChoice().implementedInterface(), null);
            convertor = extensionConvertorProvider.getActionConverter(key);
        }

        return convertor != null ? convertor.convert(action, actionPath) : null;
    }
}
