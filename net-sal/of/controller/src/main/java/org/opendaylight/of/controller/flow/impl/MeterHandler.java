/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.flow.impl;

import org.opendaylight.of.controller.flow.FlowTracker;
import org.opendaylight.of.controller.impl.ListenerService;
import org.opendaylight.of.lib.OpenflowException;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.MeterId;
import org.opendaylight.of.lib.mp.*;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.api.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * This helper class provides the methods for handling meters.
 *
 * @author Ravi
 * @author Anupama G
 */
class MeterHandler {

    // meter configuration cache.
    private static final Map<DataPathId, HashMap<MeterId, MBodyMeterConfig>> deviceMeterConfigMap =
            new ConcurrentHashMap<DataPathId, HashMap<MeterId, MBodyMeterConfig>>();
    // experimenter cache.
    private static final Map <DataPathId, MBodyExperimenter>
    deviceExperimenterMap = new ConcurrentHashMap<DataPathId,
                                   MBodyExperimenter>();
    //meter statistics cache.
    private static final Map<DataPathId, HashMap<MeterId, MBodyMeterStats>>
    deviceMeterMap = new ConcurrentHashMap<DataPathId,
            HashMap<MeterId, MBodyMeterStats>>();
    //get logger handle.
    private Logger log = LoggerFactory.getLogger(FlowTracker.class);
    // error messages.
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            MeterHandler.class, "meterHandler");

    private static final String E_SEND_METERS = RES.getString("e_send_meters");
    private static final String E_UPDATING_CONFIG_CACHE = RES
            .getString("e_updating_config_cache");
    private static final String E_UPDATING_STATS_CACHE = RES
            .getString("e_updating_stats_cache");
    private static final String E_UPDATING_EXP_CACHE = RES
            .getString("e_updating_exp_cache");
    private static final String E_SENDING_MP_REQUEST = RES
            .getString("e_sending_mp_request");
    private static final String E_DPID_NOT_FOUND = RES
            .getString("e_dpid_not_found");

   /**
    * Gets the 1.3 meter configuration from the cache.
    *
    * @param dpid the target datapath
    * @return list of MBodyMeterConfig elements
    */
   public List<MBodyMeterConfig> getMeterConfig(DataPathId dpid) {
       Map<MeterId, MBodyMeterConfig> map = deviceMeterConfigMap.get(dpid);
       if(map == null)
           throw new NotFoundException(E_DPID_NOT_FOUND + dpid);
       ArrayList<MBodyMeterConfig> meterConfigList = new
               ArrayList<MBodyMeterConfig>(map.values());
       return meterConfigList;
   }

   /**
    * Gets the 1.0 meter configuration and statistics from the cache.
    *
    * @param dpid the target datapath
    * @return MBodyExperimenter
    */
   public MBodyExperimenter getExperimenterConfigOrStats(DataPathId dpid) {
       MBodyExperimenter exp = deviceExperimenterMap.get(dpid);
       if(exp == null)
           throw new NotFoundException(E_DPID_NOT_FOUND + dpid);
       return exp;
   }

   /**
    * Updates the cache with 1.3 meter configurations got from the device.
    *
    * @param dataPathId the target datapath
    * @param body the multipart body
    */
   public void postMeterConfig(DataPathId dataPathId, MultipartBody body) {
       MBodyMeterConfig.Array meterConfigArr =
               (MBodyMeterConfig.Array) body;
       addDeviceMetersConfigToMap(dataPathId,meterConfigArr.getList());
   }

   /**
    * Updates the cache with 1.3 meter statistics got from the device.
    *
    * @param dataPathId the target datapath
    * @param body the multipart body
    */
   public void postMeterStats(DataPathId dataPathId, MultipartBody body) {
       MBodyMeterStats.Array meterStatsArr =
               (MBodyMeterStats.Array) body;
       addDeviceMetersStatsToMap(dataPathId,meterStatsArr.getList());
   }

   /**
    * Updates the cache with experimenter configuration and statistics got
    * from the device.
    *
    * @param dataPathId the target datapath
    * @param body the multipart body
    */
   public void postExperimenter(DataPathId dataPathId, MultipartBody body) {
       MBodyExperimenter exp = (MBodyExperimenter) body;
       try {
        deviceExperimenterMap.put(dataPathId, exp);
    } catch (Exception e) {
        log.warn(E_UPDATING_EXP_CACHE, dataPathId, e);
    }
   }

     /**
     * Gets the 1.3 meter statistics from the cache for given datapath id.
     *
     * @param dpid the target datapath
     * @return list of MBodyMeterStats elements
     */
    public List<MBodyMeterStats> getMeters(DataPathId dpid) {
        Map<MeterId, MBodyMeterStats> map = deviceMeterMap.get(dpid);
        if(map == null)
            throw new NotFoundException(E_DPID_NOT_FOUND + dpid);
        ArrayList<MBodyMeterStats> meterStatsList = new
                ArrayList<MBodyMeterStats>(map.values());
        return meterStatsList;
    }

    /**
     * Sends the meter to the device.
     *
     * @param meterMod the METER_MOD message to be sent to datapath
     * @param dpid the target datapath id
     * @param listenerService ListenerService handler
     */
    public void sendMeter(OfmMeterMod meterMod, DataPathId dpid,
                          ListenerService listenerService) {
        try {
            listenerService.send(meterMod, dpid);
            sendMultipartRequest(listenerService, dpid);
        } catch (OpenflowException e) {
              log.error(E_SEND_METERS, meterMod, dpid);
        }
    }

    /**
     * Sends the list of meters to the device.
     *
     * @param meterMod the list of meter_mod messages to be sent to datapath
     * @param dpid the target datapath id
     * @param listenerService  ListenerService handler
     */
    public void sendMeters(List<OfmMeterMod> meterMod, DataPathId dpid,
                           ListenerService listenerService) {
        List<OpenflowMessage> msgs =
                new ArrayList<OpenflowMessage>(meterMod.size());
        msgs.addAll(meterMod);
        try {
            listenerService.send(msgs, dpid);
            sendMultipartRequest(listenerService, dpid);
        } catch (OpenflowException e) {
            log.error(E_SEND_METERS, meterMod, dpid);
        }
    }

    //===========================Private Methods================================

    //Send multipart request after sending meter
    private void sendMultipartRequest(ListenerService listenerService,
                                      DataPathId dpid) {
        OfmMutableMultipartRequest req = (OfmMutableMultipartRequest)
                MessageFactory.create(ProtocolVersion.V_1_3,
                        MessageType.MULTIPART_REQUEST,
                        MultipartType.METER_CONFIG);
        try {
            listenerService.send(req.toImmutable(), dpid);
        } catch (Exception e) {
            log.warn(E_SENDING_MP_REQUEST, dpid, e);
        }
       // }
    }

    //Adds the v1.3 meters configuration to cache.
    void addDeviceMetersConfigToMap(DataPathId dataPathId,
                                    List<MBodyMeterConfig> meterConfigList) {
        HashMap<MeterId, MBodyMeterConfig> meterConfigMap =
                new HashMap<MeterId, MBodyMeterConfig>();
        try {
            ArrayList<MBodyMeterConfig> meterList =
                    new ArrayList<MBodyMeterConfig>(meterConfigList);
            if(deviceMeterConfigMap.get(dataPathId) != null){
                HashMap<MeterId, MBodyMeterConfig> mMap =
                        addMeterConfigWithMeterId(deviceMeterConfigMap.get(dataPathId),
                                                  meterList);
                deviceMeterConfigMap.put(dataPathId, mMap);
            } else {
                for(MBodyMeterConfig config : meterList) {
                    meterConfigMap.put(config.getMeterId(),config);
                }
                deviceMeterConfigMap.put(dataPathId, meterConfigMap);
            }
        } catch (Exception ex) {
            log.warn(E_UPDATING_CONFIG_CACHE, dataPathId, ex);
        }
    }


    //Updates the current meter configurations to map.
    private HashMap<MeterId, MBodyMeterConfig>
    addMeterConfigWithMeterId(HashMap<MeterId, MBodyMeterConfig> meterConfigMap,
                              List<MBodyMeterConfig> currmeterList) {
        for(MBodyMeterConfig config : currmeterList){
            meterConfigMap.put(config.getMeterId(), config);
        }
        return meterConfigMap;
    }

    //Adds the meter statistics to cache.
    void addDeviceMetersStatsToMap(DataPathId dataPathId,
                                   List<MBodyMeterStats> meterStatsList) {
        HashMap<MeterId, MBodyMeterStats> meterMap =
                new HashMap<MeterId, MBodyMeterStats>();
        try {
            ArrayList<MBodyMeterStats> meterList =
                    new ArrayList<MBodyMeterStats>(meterStatsList);
            if(deviceMeterMap.get(dataPathId) != null){
                HashMap<MeterId, MBodyMeterStats> mMap =
                        addMeterStatsWithMeterId(deviceMeterMap.get(dataPathId),
                                                  meterList);
                deviceMeterMap.put(dataPathId, mMap);
            } else {
                for(MBodyMeterStats stats : meterList) {
                    meterMap.put(stats.getMeterId(),stats);
                }
                deviceMeterMap.put(dataPathId, meterMap);
            }
        } catch (Exception ex) {
            log.error(E_UPDATING_STATS_CACHE, dataPathId, ex);
        }
    }

    //Updates the current meter statistics to map.
    private HashMap<MeterId, MBodyMeterStats>
    addMeterStatsWithMeterId(HashMap<MeterId, MBodyMeterStats> meterMap,
                             List<MBodyMeterStats> currmeterList) {
        for(MBodyMeterStats stats : currmeterList){
            meterMap.put(stats.getMeterId(), stats);
        }
        return meterMap;
    }

    //Removes meter configuration and statistics from map when
    //datapath disconnected event occurs.
    void removeFromCache(DataPathId dpid) {
        if(!deviceExperimenterMap.isEmpty()){
            deviceExperimenterMap.remove(dpid);
        }
        if(!deviceMeterConfigMap.isEmpty()){
            deviceMeterConfigMap.remove(dpid);
        }
        if(!deviceMeterMap.isEmpty()) {
            deviceMeterMap.remove(dpid);
        }
    }
}