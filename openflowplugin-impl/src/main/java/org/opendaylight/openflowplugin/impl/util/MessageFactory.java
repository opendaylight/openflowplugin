/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.util;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.HelloElementType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.Elements;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.hello.ElementsBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public abstract class MessageFactory {

    /**
     * Creates hello input.
     *
     * @param helloVersion openflow version for hello message to send to switch
     * @param helloXid     transaction id for hello message
     * @return HelloInput without elements
     */
    public static HelloInput createHelloInput(final Uint8 helloVersion, final Uint32 helloXid) {
        return createHelloInput(helloVersion, helloXid, null);
    }

    /**
     * Creates hello input.
     *
     * @param helloVersion openflow version for hello message to send to switch
     * @param helloXid     transaction id for hello message
     * @param versionOrder list of openflow version in order
     * @return HelloInput with elements (version bitmap)
     */
    public static HelloInput createHelloInput(final Uint8 helloVersion, final Uint32 helloXid,
            final List<Uint8> versionOrder) {
        HelloInputBuilder helloInputbuilder = prepareHelloInputBuilder(helloVersion, helloXid);
        if (versionOrder != null) {

            ElementsBuilder elementsBuilder = new ElementsBuilder();
            elementsBuilder.setType(HelloElementType.VERSIONBITMAP);
            int resultVersionListSize = 0;
            if (!versionOrder.isEmpty()) {
                resultVersionListSize = versionOrder.get(0).toJava() + 1;
            }
            List<Boolean> booleanList = new ArrayList<>(resultVersionListSize);

            int versionOrderIndex = versionOrder.size() - 1;

            while (versionOrderIndex >= 0) {
                short version = versionOrder.get(versionOrderIndex).toJava();
                if (version == booleanList.size()) {
                    booleanList.add(true);
                    versionOrderIndex--;
                } else {
                    booleanList.add(false);
                }
            }

            elementsBuilder.setVersionBitmap(booleanList);

            helloInputbuilder.setElements(List.of(elementsBuilder.build()));
        }
        return helloInputbuilder.build();
    }

    /**
     * Builder.
     *
     * @param highestVersion highest openflow version
     * @param xid            transaction id
     * @return builder with prepared header
     */
    private static HelloInputBuilder prepareHelloInputBuilder(
            final Uint8 highestVersion, final Uint32 xid) {
        HelloInputBuilder helloInputbuilder = new HelloInputBuilder();
        helloInputbuilder.setVersion(highestVersion);
        helloInputbuilder.setXid(xid);
        return helloInputbuilder;
    }

    /**
     *  Lists all versions.
     *
     * @param elements list of versions
     * @return version boolean list
     */
    public static List<Boolean> digVersions(final List<Elements> elements) {
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
