/*
 * Copyright (c) 2013 Ericsson , Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.experimenter;

import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ExperimenterConverter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.osgi.framework.BundleContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ExperimenterConverterHooks;
import org.opendaylight.controller.sal.binding.api.data.DataBrokerService;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.action.types.rev140613.action.types.action.action.ExperimenterActionType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.action.types.rev140613.action.types.action.action.experimenter.action.type.action.type.Example1Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.experimenter.action.types.rev140613.action.types.action.action.experimenter.action.type.action.type.Example2Action;

public class ExampleActionConverter implements ExperimenterConverter  {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleActionConverter.class);
    private DataBrokerService dataBrokerService;
    private ProviderContext pc;

    public ExampleActionConverter(BundleContext ctx) {
//        this.ctx = ctx;
    }

    public ExampleActionConverter() {

    }

    public void init() {
        ExperimenterConverterHooks.getInstance().registerExperimenterConverter(ExperimenterActionType.class, this);
    }

    @Override
    public void convert(org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType propType,
                         TableFeaturePropertiesBuilder builder) {
    }

    @Override
	public Action SalToExampleAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, ActionBuilder actionBuilder) {

        ExperimenterActionType expActionTypeCase = ((ExperimenterActionType) action);

        if(expActionTypeCase.getActionType() instanceof Example1Action) {
            LOG.debug("ActionType: Example1Action");
        } else if (expActionTypeCase.getActionType() instanceof Example2Action) {
            LOG.debug("ActionType: Example2Action");
        } else {
            LOG.debug("Not valid Action");
        }
        return null;
    }
}
