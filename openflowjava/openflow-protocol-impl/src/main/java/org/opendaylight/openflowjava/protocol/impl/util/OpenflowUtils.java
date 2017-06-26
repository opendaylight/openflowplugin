/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowjava.protocol.impl.util;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortConfigV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortFeaturesV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortStateV10;

/**
 * Used for common structures translation / conversion
 *
 * @author michal.polkorab
 */
public abstract class OpenflowUtils {

    private OpenflowUtils() {
        //not called
    }

    /**
     * Creates PortState (OF v1.0) from input
     * @param input value read from buffer
     * @return port state
     */
    public static PortStateV10 createPortState(long input){
        final Boolean psLinkDown = ((input) & (1<<0)) != 0;
        final Boolean psBlocked = ((input) & (1<<1)) != 0;
        final Boolean psLive = ((input) & (1<<2)) != 0;
        final Boolean psStpListen = ((input) & (1<<8)) == 0;
        final Boolean psStpLearn = ((input) & (1<<8)) != 0;
        final Boolean psStpForward = ((input) & (1<<9)) != 0; // equals 2 << 8
        final Boolean psStpBlock = (((input) & (1<<9)) != 0) && (((input) & (1<<8)) != 0); // equals 3 << 8
        final Boolean psStpMask = ((input) & (1<<10)) != 0; // equals 4 << 8
        return new PortStateV10(psBlocked, psLinkDown, psLive, psStpBlock, psStpForward, psStpLearn, psStpListen, psStpMask);
    }

    /**
     * Creates PortConfig (OF v1.0) from input
     * @param input value read from buffer
     * @return port state
     */
    public static PortConfigV10 createPortConfig(long input){
        final Boolean pcPortDown = ((input) & (1<<0)) != 0;
        final Boolean pcNoStp = ((input) & (1<<1)) != 0;
        final Boolean pcNoRecv = ((input) & (1<<2)) != 0;
        final Boolean pcNoRecvStp = ((input) & (1<<3)) != 0;
        final Boolean pcNoFlood = ((input) & (1<<4)) != 0;
        final Boolean pcNoFwd  = ((input) & (1<<5)) != 0;
        final Boolean pcNoPacketIn = ((input) & (1<<6)) != 0;
        return new PortConfigV10(pcNoFlood, pcNoFwd, pcNoPacketIn, pcNoRecv, pcNoRecvStp, pcNoStp, pcPortDown);
    }

    /**
     * Creates PortFeatures (OF v1.0) from input
     * @param input value read from buffer
     * @return port state
     */
    public static PortFeaturesV10 createPortFeatures(long input){
        final Boolean pf10mbHd = ((input) & (1<<0)) != 0;
        final Boolean pf10mbFd = ((input) & (1<<1)) != 0;
        final Boolean pf100mbHd = ((input) & (1<<2)) != 0;
        final Boolean pf100mbFd = ((input) & (1<<3)) != 0;
        final Boolean pf1gbHd = ((input) & (1<<4)) != 0;
        final Boolean pf1gbFd = ((input) & (1<<5)) != 0;
        final Boolean pf10gbFd = ((input) & (1<<6)) != 0;
        final Boolean pfCopper = ((input) & (1<<7)) != 0;
        final Boolean pfFiber = ((input) & (1<<8)) != 0;
        final Boolean pfAutoneg = ((input) & (1<<9)) != 0;
        final Boolean pfPause = ((input) & (1<<10)) != 0;
        final Boolean pfPauseAsym = ((input) & (1<<11)) != 0;
        return new PortFeaturesV10(pf100mbFd, pf100mbHd, pf10gbFd, pf10mbFd, pf10mbHd,
                pf1gbFd, pf1gbHd, pfAutoneg, pfCopper, pfFiber, pfPause, pfPauseAsym);
    }
}