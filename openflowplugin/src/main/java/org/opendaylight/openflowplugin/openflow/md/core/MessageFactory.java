/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;

/**
 * @author mirehak
 *
 */
public abstract class MessageFactory {

    /**
     * @param helloVersion
     * @param helloXid
     * @return HelloInput without elements
     */
    public static HelloInput createHelloInput(short helloVersion, long helloXid) {
        return createHelloInput(helloVersion, helloXid, null);
    }

    /**
     * @param highestVersion
     * @param xid
     * @return builder with prepared header
     */
    private static HelloInputBuilder prepareHelloInputBuilder(
            short highestVersion, long xid) {
        HelloInputBuilder helloInputbuilder = new HelloInputBuilder();
        helloInputbuilder.setVersion(highestVersion);
        helloInputbuilder.setXid(xid);
        return helloInputbuilder;
    }
    
    /**
     * @param helloVersion
     * @param helloXid
     * @param versionOrder
     * @return HelloInput with elements (version bitmap)
     */
    public static HelloInput createHelloInput(short helloVersion, long helloXid, List<Short> versionOrder) {
        HelloInputBuilder helloInputbuilder = prepareHelloInputBuilder(helloVersion, helloXid);
        if (versionOrder != null) {
            List<Elements> elementList = new ArrayList<>();
            
            ElementsBuilder elementsBuilder = new ElementsBuilder();
            elementsBuilder.setType(HelloElementType.VERSIONBITMAP);
            List<Boolean> booleanList = new ArrayList<>();
            
            int versionOrderIndex = versionOrder.size() - 1;
            
            while (versionOrderIndex >= 0) {
                short version = versionOrder.get(versionOrderIndex);
                if (version == booleanList.size()) {
                    booleanList.add(true);
                    versionOrderIndex--;
                } else {
                    booleanList.add(false);
                }
            }
            
            elementsBuilder.setVersionBitmap(booleanList);
            elementList.add(elementsBuilder.build());
            helloInputbuilder.setElements(elementList);
        }
        return helloInputbuilder.build();
    }

    /**
     * @param elements
     * @return version boolean list
     */
    public static List<Boolean> digVersions(List<Elements> elements) {
        List<Boolean> result = null;
        if (elements != null && !elements.isEmpty()) {
            for (Elements elm : elements) {
                if (HelloElementType.VERSIONBITMAP.equals(elm.getType())) {
                    result = elm.getVersionBitmap();
                }
            }
        }
        return result;
    }
}
