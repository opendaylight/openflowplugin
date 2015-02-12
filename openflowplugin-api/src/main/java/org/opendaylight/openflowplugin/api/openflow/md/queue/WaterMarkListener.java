package org.opendaylight.openflowplugin.api.openflow.md.queue;

public interface WaterMarkListener {

    /**
     * When HighWaterMark reached and currently not flooded
     */
    void onHighWaterMark();

    /**
     * When LowWaterMark reached and currently flooded
     */
    void onLowWaterMark();
}
