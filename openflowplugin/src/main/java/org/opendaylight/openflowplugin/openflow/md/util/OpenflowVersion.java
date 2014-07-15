package org.opendaylight.openflowplugin.openflow.md.util;

/** List of Openflow versions supported by the plugin
 *  Note: If you add a version here, make sure to update {@link OpenflowPortsUtil} as well.
 * Created by kramesha on 5/2/14.
 */
public enum OpenflowVersion {

    OF10((short)0x01),
    OF13((short)0x04),
    UNSUPPORTED((short)0x00);


    private short version;

    OpenflowVersion(short version) {
        this.version = version;
    }

    public static OpenflowVersion get(Short version) {
        for (OpenflowVersion ofv : OpenflowVersion.values()) {
            if (ofv.version == version) {
                return ofv;
            }
        }
        return UNSUPPORTED;
    }
    
    /**
     * @return the version
     */
    public short getVersion() {
        return version;
    }

}
