package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data;


import org.opendaylight.openflowplugin.extension.api.path.MatchPath;

public class FlowStatsResponseConvertorData extends VersionDatapathIdConvertorData {
    private MatchPath matchPath;

    public FlowStatsResponseConvertorData(short version) {
        super(version);
    }

    public MatchPath getMatchPath() {
        return matchPath;
    }

    public void setMatchPath(MatchPath matchPath) {
        this.matchPath = matchPath;
    }
}
