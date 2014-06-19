package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor;

import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.table.features.properties.grouping.TableFeaturePropertiesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.feature.prop.type.TableFeaturePropType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
/**
 * Uniform interface for converting experimenter data
 * @param <E> message type
 */
//public interface ExperimenterConverter <E extends DataObject> {
public interface ExperimenterConverter {

    public void convert(TableFeaturePropType propType, TableFeaturePropertiesBuilder builder);    
    public Action SalToExampleAction(org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.Action action, ActionBuilder actionBuilder);

}

