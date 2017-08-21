/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.protocol.converter.data;

import org.opendaylight.openflowplugin.extension.api.path.MatchPath;

public class FlowStatsResponseConverterData extends VersionDatapathIdConverterData {
    private MatchPath matchPath;

    public FlowStatsResponseConverterData(short version) {
        super(version);
    }

    public MatchPath getMatchPath() {
        return matchPath;
    }

    public void setMatchPath(MatchPath matchPath) {
        this.matchPath = matchPath;
    }
}
