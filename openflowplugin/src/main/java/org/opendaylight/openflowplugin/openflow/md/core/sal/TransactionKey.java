package org.opendaylight.openflowplugin.openflow.md.core.sal;

import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;

public class TransactionKey {

    private static final long serialVersionUID = 7805731164917659700L;
    final private String _nodeId;
    final private Long _xId;

    public TransactionKey(NodeRef _id, Long transactionId) {
        this._nodeId = _id.getValue().firstKeyOf(Node.class, NodeKey.class).getId().getValue().toString();
        this._xId = transactionId;
    }

    public Long getXId() {
        return _xId;
    }

    public String getNodeId() {
        return _nodeId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_nodeId == null) ? 0 : _nodeId.hashCode());
        result = prime * result + ((_xId == null) ? 0 : _xId.hashCode());
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TransactionKey other = (TransactionKey) obj;
        if (_nodeId == null) {
            if (other._nodeId != null) {
                return false;
            }
        } else if (!_nodeId.equals(other._nodeId)) {
            return false;
        }
        if (_xId == null) {
            if (other._xId != null) {
                return false;
            }
        } else if (!_xId.equals(other._xId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NodeKey [_id=Uri [_value=");
        builder.append(_nodeId + "]]");
        builder.append("TransactionId [_xId=");
        builder.append(_xId);
        builder.append("]");
        return builder.toString();
    }

}
