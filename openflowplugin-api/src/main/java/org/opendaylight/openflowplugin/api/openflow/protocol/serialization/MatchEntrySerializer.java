/*
 * Copyright (c) 2016 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.protocol.serialization;

import io.netty.buffer.ByteBuf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;

public interface MatchEntrySerializer {

    /**
     * Serialize this match entry if it is present in the match.
     *
     * @param match Openflow match
     * @param outBuffer output buffer
     */
    void serializeIfPresent(Match match, ByteBuf outBuffer);
}