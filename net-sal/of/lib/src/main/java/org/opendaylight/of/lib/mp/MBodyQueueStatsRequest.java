/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.mp;

import org.opendaylight.of.lib.IncompleteStructureException;
import org.opendaylight.of.lib.OpenflowStructure;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.QueueId;
import org.opendaylight.of.lib.msg.Port;
import org.opendaylight.util.net.BigPortNumber;

import static org.opendaylight.of.lib.CommonUtils.EOLI;
import static org.opendaylight.of.lib.CommonUtils.notNullIncompleteStruct;

/**
 * Represents a Queue stats request (multipart body);Since 1.0.
 *
 * @author Shruthy Mohanram
 */
public class MBodyQueueStatsRequest extends OpenflowStructure
        implements MultipartBody {
    
    private static final int BODY_LEN = 8; 

    BigPortNumber port;
    QueueId queueId;
       
    /**
     * Constructs a multipart body QUEUE type request.
     * 
     * @param pv the protocol version
     */
    public MBodyQueueStatsRequest(ProtocolVersion pv) {
        super(pv);        
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();        
        sb.append("{qstats:port: ").append(Port.portNumberToString(port))       
            .append(",queueId    : ").append(queueId)
                .append(",........}");        
        return sb.toString();        
    }

    @Override
    public String toDebugString() {    
        StringBuilder sb = new StringBuilder();
        sb.append("Port    : ").append(Port.portNumberToString(port));        
        sb.append(EOLI).append("Queue ID: ").append(queueId);        
        return sb.toString();        
    }

    @Override
    public void validate() throws IncompleteStructureException {
        notNullIncompleteStruct(port, queueId);        
    }

    @Override
    public int getTotalLength() {        
        return BODY_LEN;
    }
    
    /** Returns the port for which statistics are requested; Since 1.0.
     * <p>
     * A value of {@link Port#ANY} indicates no restriction.
     * Note that in 1.0, port numbers are u16.
     *
     * @return the requested port
     */
    public BigPortNumber getPort() {
        return port;
    }     

     /** Returns the ID of the queue to read; Since 1.0.
     * <p> 
     * A value of {@link QueueId#ALL} indicates all queues.
     *
     * @return the queue id
     */
    public QueueId getQueueId() {
        return queueId;
    }       
}
