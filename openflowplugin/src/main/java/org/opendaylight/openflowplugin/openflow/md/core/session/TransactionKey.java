package org.opendaylight.openflowplugin.openflow.md.core.session;

public class TransactionKey {

    private static final long serialVersionUID = 7805731164917659700L;
    final private Long _xId;

    public TransactionKey(Long transactionId) {
        this._xId = transactionId;
    }

    public Long getXId() {
        return _xId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        builder.append("TransactionId [_xId=");
        builder.append(_xId);
        builder.append("]");
        return builder.toString();
    }

}
