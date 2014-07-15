/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import java.util.Map.Entry;

import org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxMatchRegGrouping;
import org.opendaylight.yangtools.yang.binding.Augmentation;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class RegConvertor implements ConvertorToOFJava<MatchEntries>, ConvertorFromOFJava<MatchEntries, MatchPath> {

    /* (non-Javadoc)
     * @see org.opendaylight.openflowplugin.extension.api.ConvertorFromOFJava#convert(org.opendaylight.yangtools.yang.binding.DataContainer, org.opendaylight.openflowplugin.extension.api.path.AugmentationPath)
     */
    @Override
    public Entry<Class<? extends Augmentation<Extension>>, Augmentation<Extension>> convert(MatchEntries input,
            MatchPath path) {
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension)
     */
    @Override
    public MatchEntries convert(Extension extension) {
        Optional<NxMatchRegGrouping> nxMatchRegGrouping = MatchUtil.getNxMatchRegGrouping(extension);
        if (nxMatchRegGrouping.isPresent()) {
            MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
            matchEntriesBuilder.setHasMask(false); // TODO ???
            nxMatchRegGrouping.get().getNxMatchReg().get
            matchEntriesBuilder.se
        }
        return null;
    }
    
    private static resolveOxmClass() {
        
    }

}
