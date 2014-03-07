package org.opendaylight.openflowplugin.openflow.md.core.translator;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.node.error.notification.object.reference.GroupRefBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.AddGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.RemoveGroupInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.UpdateGroupInput;

public class GroupEntityData extends AbstractDPNEntity {

    GroupRefBuilder groupRef = new GroupRefBuilder();

    @Override
    public GroupRefBuilder getBuilder(Object object) {
        // TODO Auto-generated method stub
        if (object instanceof AddGroupInput) {
            AddGroupInput addGroupinput = ((AddGroupInput) object);
            groupRef.setGroupRef(addGroupinput.getGroupRef());
        } else if (object instanceof UpdateGroupInput) {
            UpdateGroupInput updateGroupinput = ((UpdateGroupInput) object);
            groupRef.setGroupRef(updateGroupinput.getGroupRef());
        } else {
            RemoveGroupInput removeGroupinput = ((RemoveGroupInput) object);
            groupRef.setGroupRef(removeGroupinput.getGroupRef());
        }
        return groupRef;
    }

}
