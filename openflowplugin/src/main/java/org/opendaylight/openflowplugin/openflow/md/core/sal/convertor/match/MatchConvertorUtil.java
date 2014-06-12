/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;

/**
 * match related tools
 */
public abstract class MatchConvertorUtil {

    /**
     * @param pField
     * @return integer containing lower 9 bits filled with corresponding flags
     */
    public static Integer ipv6ExthdrFlagsToInt(Ipv6ExthdrFlags pField) {
        Integer bitmap = 0;
        bitmap |= pField.isNonext() ? (1 << 0) : 0;
        bitmap |= pField.isEsp() ?    (1 << 1) : 0;
        bitmap |= pField.isAuth() ?   (1 << 2) : 0;
        bitmap |= pField.isDest() ?   (1 << 3) : 0;
        bitmap |= pField.isFrag() ?   (1 << 4) : 0;
        bitmap |= pField.isRouter() ? (1 << 5) : 0;
        bitmap |= pField.isHop() ?    (1 << 6) : 0;
        bitmap |= pField.isUnrep() ?  (1 << 7) : 0;
        bitmap |= pField.isUnseq() ?  (1 << 8) : 0;
        return bitmap;
    }
    
    public static int ipv6NetmaskArrayToCIDRValue(MaskMatchEntry maskMatchEntry){

        /*
         * Openflow Spec : 1.3.2+
         * An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
         * So when user specify 128 as a mask, switch omit that mask and we get null as a mask in flow
         * statistics response.
         */
        

        int maskValue = 128;
        
        if (maskMatchEntry != null) {
            maskValue = 0;
            for(int subArrayCounter=0;subArrayCounter<4;subArrayCounter++){
                int copyFrom = subArrayCounter * 4;

                byte[] subArray = Arrays.copyOfRange(maskMatchEntry.getMask(), copyFrom, copyFrom+4);  
                
                int receivedMask = ByteBuffer.wrap(subArray).getInt();
                
                int shiftCount=0;
                
                while(receivedMask != 0xffffffff){
                    receivedMask = receivedMask >> 1;
                    shiftCount++;
                }
                
                maskValue = maskValue+(32-shiftCount);
                if(shiftCount != 0)
                    break;
            }
        }
        return maskValue;
    }

}
