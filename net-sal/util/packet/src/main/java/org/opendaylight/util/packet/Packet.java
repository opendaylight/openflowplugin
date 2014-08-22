/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Packet (immutable) contains a list of protocol layers.
 * <p>
 * An <i>onion</i> metaphor is used to describe the layers of a packet.  The
 * layer index corresponds to the depth inside the packet. For example, if the
 * following packet is constructed:
 * <pre>
 * Packet p = new Packet(eth, ip, udp, dhcp)
 * </pre>
 * The layers are of the form:
 * <ul>
 * <li>layers[0] = eth (outermost)</li>
 * <li>layers[1] = ip</li>
 * <li>layers[2] = udp</li>
 * <li>layers[3] = dhcp (innermost)</li>
 * </ul>
 * In which case, a call to {@link Packet#innermostId()} would return
 * {@link ProtocolId#DHCP}.
 * <p>
 * 
 * @author Frank Wood
 */
public class Packet {

    private final long protocolMask;
    
    private Protocol[] layers;

    /**
     * Constructor creates a packet of protocol layers.
     * 
     * @param layers protocol layers
     */
    public Packet(Protocol... layers) {
        this.layers = Arrays.copyOf(layers, layers.length);
        this.protocolMask = computeProtocolMask(this.layers);
    }
    
    /**
     * Constructor creates a packet of protocol layers.
     * 
     * @param layers protocol layers
     */
    public Packet(List<Protocol> layers) {
        this.layers = layers.toArray(new Protocol[layers.size()]);
        this.protocolMask = computeProtocolMask(this.layers);
    }    

    /**
     * Computes the mask of all protocol IDs.
     * 
     * @param protocols collection of protocols
     * @return bit mask of all protocol IDs
     */
    private long computeProtocolMask(Protocol... protocols) {
        long mask = 0;
        for (Protocol p : protocols)
            mask |= p.id().bit();
        return mask;
    }

    /**
     * Computes the mask of all protocol IDs.
     * 
     * @param protocols collection of protocols
     * @return bit mask of all protocol IDs
     */
    public static long computeProtocolMask(ProtocolId... protocols) {
        long mask = 0;
        for (ProtocolId pid : protocols)
            mask |= pid.bit();
        return mask;
    }

    /**
     * Returns the number of protocol layers.
     * 
     * @return the number of protocol layers
     */
    public int size() {
        return layers.length;
    }
    
    /**
     * Returns the protocol layer at the given index.
     * 
     * @param i layer index
     * @return the protocol layer at the given index or null
     */
    public Protocol get(int i) {
        if (i < 0 || i >= layers.length)
            return null;
        return layers[i];
    }
    
    /**
     * Returns the first occurrence of the protocol (outer to inner) with the
     * given protocol ID.
     * 
     * @param id protocol ID
     * @return the protocol for the given ID or null
     */
    public <P extends Protocol> P get(ProtocolId id) {
        return get(id, 0);
    }

    /**
     * Returns the Nth occurrence of the protocol (outer to inner) with the
     * given protocol ID.  For example if the layers are of the form:
     * <ul>
     * <li>layers[0] = eth (outermost)</li>
     * <li>layers[1] = ip</li>
     * <li>layers[2] = gre</li>
     * <li>layers[3] = eth</li>
     * <li>layers[4] = ip</li>
     * <li>layers[5] = udp</li>
     * <li>layers[6] = dns</li>
     * </ul>
     * <pre>
     * get(ETHERNET, 0) // returns layers[0], same as get(ETHERNET)
     * get(ETHERNET, 1) // returns layers[3]
     * </pre>
     * <p>
     * Suppress warning is needed here because of the cast from {@link Protocol}
     * to the generic type.  It is safe because each concrete protocol must
     * implement {@link Protocol#id()} to return its distinct ID.   
     * 
     * @param id protocol ID
     * @param nth Nth protocol layer
     * @return the protocol for the given ID or null
     */
    @SuppressWarnings("unchecked")
    public <P extends Protocol> P get(ProtocolId id, int nth) {
        int numFound = nth;
        for (Protocol p: layers) {
            if (p.id() == id) {
                if (--numFound < 0)
                    return (P) p;
            }
        }
        return null;
    }
    
    /**
     * Returns true if the protocols corresponding to the given ID is 
     * contained in the packet.
     * 
     * @param id protocol ID
     * @return true if the protocol is contained in the packet
     */
    public boolean has(ProtocolId id) {
        return (id.bit() & protocolMask) != 0;
    }    

    /**
     * Returns true if <b>all</b> the protocols corresponding to the IDs
     * are contained in the packet.
     * 
     * @param ids protocol IDs
     * @return true if all of the protocols are contained in the packet
     */
    public boolean hasAll(ProtocolId... ids) {
        for (ProtocolId id: ids) {
            if ((id.bit() & protocolMask) == 0)
                return false;
        }
        return true;
    }    

    /**
     * Returns true if <b>any</b> of the protocols corresponding to the IDs
     * are contained in the packet.
     * 
     * @param ids protocol IDs
     * @return true if any of the protocols are contained in the packet
     */
    public boolean hasAny(ProtocolId... ids) {
        for (ProtocolId id: ids) {
            if ((id.bit() & protocolMask) != 0)
                return true;
        }
        return false;
    }    
    
    /**
     * Returns a list of protocol IDs from the outermost to innermost layer.
     * 
     * @return list of protocol IDs
     */
    public List<ProtocolId> protocolIds() {
        List<ProtocolId> ids = new ArrayList<ProtocolId>(size());
        for (Protocol l: layers)
            ids.add(l.id());
        return Collections.unmodifiableList(ids);
    } 
    
    /**
     * Returns the innermost layer protocol (highest in the network stack).
     * 
     * @return the protocol layer or null
     */
    public Protocol innermost() {
        return get(layers.length - 1);
    }

    /**
     * Returns the innermost layer protocol (highest in the network stack) that
     * matches the provided protocol ID.
     *
     * @param id protocol ID 
     * @return the protocol layer or null
     */
    public Protocol innermost(ProtocolId id) {
        for (int i=layers.length-1; i>=0; i--) {
            if (layers[i].id() == id)
                return layers[i];
        }
        return null;
    }
    
    /**
     * Returns the innermost layer protocol ID (highest in the the network
     * stack).
     * 
     * @return the protocol layer ID or {@code UNKNOWN} if there are no known
     *      protocols in the packet
     */
    public ProtocolId innermostId() {
        Protocol p = innermost();
        return (null != p) ? p.id() : ProtocolId.UNKNOWN;
    }
    
    /**
     * Returns the bit mask representing present protocol IDs.
     * 
     * @return bit mask of protocol IDs
     */
    public long protocolMask() {
        return protocolMask;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Packet: [");
        for (int i=0; i<layers.length; i++) {
            Protocol p = layers[i];
            if (0 < i)
                sb.append("|");
            sb.append(p.id());
        }
        return sb.append("]").toString();
    }
            
    /**
     * Returns the formatted debug string for the packet.
     * 
     * @return the formatted debug string representing this packet
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder("Packet (debug):");
        for (Protocol p: layers)
            sb.append(ProtocolUtils.EOLI + p.toDebugString());
        return sb.toString();
    }
    
}
