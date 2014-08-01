package org.opendaylight.openflowjava.nx.codec.action;

import io.netty.buffer.ByteBuf;

import org.opendaylight.openflowjava.nx.api.NiciraActionDeserializerKey;
import org.opendaylight.openflowjava.nx.api.NiciraActionSerializerKey;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ExperimenterIdAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.NxmNxRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.OfjAugNxActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.ActionRegMove;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowjava.nx.action.rev140421.ofj.nx.action.reg.move.grouping.ActionRegMoveBuilder;

public class RegMoveCodec extends AbstractActionCodec {

    public static final int LENGTH = 24;
    public static final byte SUBTYPE = 6; // NXAST_REG_MOVE
    public static final NiciraActionSerializerKey SERIALIZER_KEY = new NiciraActionSerializerKey(
            EncodeConstants.OF13_VERSION_ID, NxmNxRegMove.class);
    public static final NiciraActionDeserializerKey DESERIALIZER_KEY = new NiciraActionDeserializerKey(
            EncodeConstants.OF13_VERSION_ID, SUBTYPE);

    @Override
    public void serialize(Action input, ByteBuf outBuffer) {
        ActionRegMove actionRegMove = input.getAugmentation(OfjAugNxAction.class).getActionRegMove();
        serializeHeader(LENGTH, SUBTYPE, outBuffer);
        outBuffer.writeShort(actionRegMove.getNBits());
        outBuffer.writeShort(actionRegMove.getSrcOfs());
        outBuffer.writeShort(actionRegMove.getDstOfs());
        outBuffer.writeInt(actionRegMove.getSrc().intValue());
        outBuffer.writeInt(actionRegMove.getDst().intValue());
    }

    @Override
    public Action deserialize(ByteBuf message) {
        ActionBuilder actionBuilder = deserializeHeader(message);
        ActionRegMoveBuilder actionRegMoveBuilder = new ActionRegMoveBuilder();
        actionRegMoveBuilder.setNBits(message.readUnsignedShort());
        actionRegMoveBuilder.setSrcOfs(message.readUnsignedShort());
        actionRegMoveBuilder.setDstOfs(message.readUnsignedShort());
        actionRegMoveBuilder.setSrc(message.readUnsignedInt());
        actionRegMoveBuilder.setDst(message.readUnsignedInt());
        OfjAugNxActionBuilder augNxActionBuilder = new OfjAugNxActionBuilder();
        augNxActionBuilder.setActionRegMove(actionRegMoveBuilder.build());
        actionBuilder.addAugmentation(ExperimenterIdAction.class, createExperimenterIdAction(NxmNxRegMove.class));
        actionBuilder.addAugmentation(OfjAugNxAction.class, augNxActionBuilder.build());
        return actionBuilder.build();
    }

}
