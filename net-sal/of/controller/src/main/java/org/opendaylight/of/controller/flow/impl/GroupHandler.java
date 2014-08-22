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
import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.GroupId;
import org.opendaylight.of.lib.mp.MBodyGroupDescStats;
import org.opendaylight.of.lib.mp.MBodyGroupStats;
import org.opendaylight.of.lib.mp.MultipartType;
import org.opendaylight.of.lib.msg.*;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.api.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// FIXME: missing javadocs
public class GroupHandler {

    /** Our reference to the OF logger. */
    private final Logger log = LoggerFactory.getLogger(FlowTracker.class);

    private ListenerService listenerService;

    // All error messages
    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            GroupHandler.class, "groupHandler");

    private static final String E_SEND_GROUP = RES.getString("e_send_group");
    private static final String E_ADD_GROUP_STATS_TO_MAP = RES
            .getString("e_add_group_stats_to_map");
    private static final String E_ADD_GROUP_DESC_TO_MAP = RES
            .getString("e_add_group_desc_to_map");
    private static final String E_SEND_GROUPS = RES.getString("e_send_groups");
    private static final String E_MULTIPART_REQUEST = RES
            .getString("e_multipart_request");
    private static final String E_DPID_NOT_FOUND = RES
            .getString("e_dpid_not_found");

    // info log message strings
    private static final String MSG_SEND_GRP_TO_DEVICE = RES
            .getString("msg_send_grp_to_device");
    private static final String MSG_SEND_GRP_ON_DEVICE = RES
            .getString("msg_send_grp_on_device");

    // cache of every group on every datapath
    private static final Map<DataPathId, HashMap<GroupId, MBodyGroupStats>>
        deviceGroupStatsMap = new ConcurrentHashMap<DataPathId,
                HashMap<GroupId, MBodyGroupStats>>();

    private static final Map<DataPathId, HashMap<GroupId, MBodyGroupDescStats>>
        deviceGroupDescMap = new ConcurrentHashMap<DataPathId,
                HashMap<GroupId, MBodyGroupDescStats>>();

    /**Gets group description from specified datapath.
     *
     * @param dpid the target datapath
     * @return list of MBodyGroupDescStats elements
     */
    public List<MBodyGroupDescStats> getGroups(DataPathId dpid) {
        Map<GroupId, MBodyGroupDescStats> map = deviceGroupDescMap.get(dpid);
        if (map == null)
            throw new NotFoundException(E_DPID_NOT_FOUND + dpid);

        ArrayList<MBodyGroupDescStats> groupList;
        groupList = new ArrayList<MBodyGroupDescStats>(map.values());
        return (groupList.isEmpty() ? groupList :
            Collections.unmodifiableList(groupList));
    }

    /**Gets group description from specified datapath.
     *
     * @param dpid the target datapath
     * @param groupId the ID of the group
     * @return MBodyGroupDescStats object
     */
    public MBodyGroupDescStats getGroup(DataPathId dpid, GroupId groupId) {
        Map<GroupId, MBodyGroupDescStats> map = deviceGroupDescMap.get(dpid);
        if (map == null)
            throw new NotFoundException(E_DPID_NOT_FOUND + dpid);

        ArrayList<MBodyGroupDescStats> groupList = new
                ArrayList<MBodyGroupDescStats>(map.values());
        MBodyGroupDescStats gDescStatsFound = null;

        if(!groupList.isEmpty()) {
            for(MBodyGroupDescStats gDescStats : groupList) {
                if(gDescStats.getGroupId() == groupId) {
                    gDescStatsFound= gDescStats;
                    break;
                }
            }
        }
        return gDescStatsFound;
    }

    /**Gets group statistics from specified datapath.
     *
     * @param dpid the target datapath
     * @return list of MBodyGroupStats elements
     */
    public List<MBodyGroupStats> getGroupsStatistics(DataPathId dpid) {
        Map<GroupId, MBodyGroupStats> map = deviceGroupStatsMap.get(dpid);
        if (map == null)
            throw new NotFoundException(E_DPID_NOT_FOUND + dpid);

        ArrayList<MBodyGroupStats> groupList =
                new ArrayList<MBodyGroupStats>(map.values());
        return (groupList.isEmpty() ? groupList :
            Collections.unmodifiableList(groupList));
    }

    /**Gets group statistics from specified datapath id and group id.
     *
     * @param dpid the target datapath
     * @param groupId the ID of the group
     * @return MBodyGroupStats object
     */
    public MBodyGroupStats getGroupStatistics(DataPathId dpid,
                                              GroupId groupId) {

        Map<GroupId, MBodyGroupStats> map = deviceGroupStatsMap.get(dpid);
        if (map == null)
            throw new NotFoundException(E_DPID_NOT_FOUND + dpid);

        ArrayList<MBodyGroupStats> groupList =
                new ArrayList<MBodyGroupStats>(map.values());
        MBodyGroupStats gStatsFound = null;

        if(!groupList.isEmpty()) {
            for(MBodyGroupStats gStats : groupList) {
                if(gStats.getGroupId() == groupId) {
                    gStatsFound= gStats;
                    break;
                }
            }
        }
        return gStatsFound;
    }

    /**Sends the group message to specified datapath.
     *
     * @param groupMod the {@link OfmGroupMod} message to be sent to datapath
     * @param dpid the target datapath
     */
    public void sendGroup(OfmGroupMod groupMod, DataPathId dpid) {
        log.info(MSG_SEND_GRP_TO_DEVICE, dpid, groupMod);
        try {
            listenerService.send(groupMod, dpid);
        } catch (OpenflowException e) {
            log.error(E_SEND_GROUP, groupMod, dpid);
        }
        sendMultiparRequest(groupMod, dpid);
    }

    /** Sends the list of group messages to specified datapath.
     *
     * @param groupMods the list of {@link OfmGroupMod} messages to be sent
     * to datapath
     * @param dpid the target datapath
     */
    public void sendGroups(List<OfmGroupMod> groupMods, DataPathId dpid) {
        List<OpenflowMessage> msgs =
                   new ArrayList<OpenflowMessage>(groupMods.size());

        log.info(MSG_SEND_GRP_ON_DEVICE, dpid, groupMods);

        msgs.addAll(groupMods);
        OfmGroupMod lastGroupModMsg = groupMods.get(groupMods.size()-1);

        try {
            listenerService.send(msgs, dpid);
        } catch (OpenflowException e) {
            log.error(E_SEND_GROUPS, groupMods, dpid);
        }
        sendMultiparRequest(lastGroupModMsg, dpid);
    }

     /**To handle Multipart Reply messages.
      *
      * @param dataPathId the target datapath
      * @param groupStatsArray the group stats array object
      *
      */
    public void postGroup(DataPathId dataPathId,
                          List<MBodyGroupStats> groupStatsArray) {
        addDeviceGroupsStatsToMap(dataPathId, groupStatsArray);
    }

    /**To handle Multipart Reply messages.
      *
     * @param dataPathId the target datapath
     * @param groupDescArray the group description array
     */
    public void postGroupConfig(DataPathId dataPathId,
                                List<MBodyGroupDescStats> groupDescArray) {
        addDeviceGroupDescToMap(dataPathId,groupDescArray);

    }

    /** Sends <em>Multipart Request</em> to the given datapath.
    *
    * @param mod the GROUP_MOD message multipart request message
    * @param dpid datapath id
    */
    void sendMultiparRequest(OpenflowMessage mod,DataPathId dpid) {
        OfmMutableMultipartRequest msg = (OfmMutableMultipartRequest)
                MessageFactory.create(mod,MessageType.MULTIPART_REQUEST,
                                      MultipartType.GROUP_DESC);
       try {
           listenerService.send(msg.toImmutable(), dpid);
       } catch (OpenflowException e) {
          log.error(E_MULTIPART_REQUEST, dpid);
       }
   }

    /**
     * Gets the ListenerService handler for use
     * @param listenerService  ListenerService Handler
     */
    public void listenet(ListenerService listenerService){
        this.listenerService = listenerService;
    }

   /** Updates the deviceGroupStatsMap with latest group statistics
   *
   * @param dataPathId the controller learnt datapath
   * @param groupStatsList list of Group statistics entries
   */
   private void addDeviceGroupsStatsToMap(DataPathId dataPathId,
                              List<MBodyGroupStats> groupStatsList) {
       HashMap<GroupId, MBodyGroupStats> groupMap =
               new HashMap<GroupId, MBodyGroupStats>();
    try {
        ArrayList<MBodyGroupStats> groupList =
                new ArrayList<MBodyGroupStats>(groupStatsList);
        if(deviceGroupStatsMap.get(dataPathId) != null) {
            HashMap<GroupId, MBodyGroupStats> gMap =
                    addGroupStatsWithGroupId(deviceGroupStatsMap.
                                             get(dataPathId), groupList);
            deviceGroupStatsMap.put(dataPathId, gMap);
        } else {
            for(MBodyGroupStats statsNew : groupList){
                groupMap.put(statsNew.getGroupId(), statsNew);
            }
            deviceGroupStatsMap.put(dataPathId, groupMap);
        }

      } catch (Exception ex) {
          log.error(E_ADD_GROUP_STATS_TO_MAP, dataPathId);
      }
  }

   /** Updates the deviceGroupDescMap with latest group descriptions
   *
   * @param dataPathId the controller learnt datapath
   * @param groupDescStatsList list of Group Description entries
   */
   private void addDeviceGroupDescToMap(DataPathId dataPathId,
                              List<MBodyGroupDescStats> groupDescStatsList) {
       HashMap<GroupId, MBodyGroupDescStats> groupMap =
               new HashMap<GroupId, MBodyGroupDescStats>();
    try {
        ArrayList<MBodyGroupDescStats> groupList =
                new ArrayList<MBodyGroupDescStats>(groupDescStatsList);
        if(deviceGroupDescMap.get(dataPathId) != null) {
            HashMap<GroupId, MBodyGroupDescStats> gMap =
                    addGroupDescWithGroupId(deviceGroupDescMap.get(dataPathId),
                                            groupList);
            deviceGroupDescMap.put(dataPathId, gMap);
        } else {
            for(MBodyGroupDescStats descNew : groupList){
                groupMap.put(descNew.getGroupId(), descNew);
            }

            deviceGroupDescMap.put(dataPathId, groupMap);
        }

      } catch (Exception ex) {
          log.error(E_ADD_GROUP_DESC_TO_MAP, dataPathId);
      }
  }

   /** converts the array list to map
   *
   * @param groupMap the existing hash map
   * @param currentGroupdetails list of Group Description entries
 *   @return new map
   */
   private HashMap<GroupId, MBodyGroupDescStats>
   addGroupDescWithGroupId(HashMap<GroupId, MBodyGroupDescStats> groupMap,
                           List<MBodyGroupDescStats> currentGroupdetails ) {

           for(MBodyGroupDescStats descNew : currentGroupdetails){
               groupMap.put(descNew.getGroupId(), descNew);
           }

     return groupMap;
   }

   /** converts the array list to map
   *
   * @param groupMap the existing hash map
   * @param currentGroupdetails list of Group Description entries
 *   @return new map
   */
   private HashMap<GroupId, MBodyGroupStats>
   addGroupStatsWithGroupId(HashMap<GroupId, MBodyGroupStats> groupMap,
                           List<MBodyGroupStats> currentGroupdetails ) {

           for(MBodyGroupStats descNew : currentGroupdetails){
               groupMap.put(descNew.getGroupId(), descNew);
           }

     return groupMap;
   }

   /**
   * Removes group configuration and statistics from map when
   * datapath disconnected event occurs.
   *
   * @param dpid Datapath Id
   */
   void removeFromCache(DataPathId dpid) {
       if (!deviceGroupDescMap.isEmpty()){
           deviceGroupDescMap.remove(dpid);
       }
       if (!deviceGroupStatsMap.isEmpty()){
           deviceGroupStatsMap.remove(dpid);
       }
   }
}