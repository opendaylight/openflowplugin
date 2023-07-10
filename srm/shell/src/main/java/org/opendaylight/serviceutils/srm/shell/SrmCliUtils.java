/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.serviceutils.srm.shell;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.EntityTypeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusIfm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusIfmInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItmTep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.GeniusItmTz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAclInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtAclInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtDhcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtElan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtElanInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gwConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtL2gwNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtQos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtQosPolicyInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtVpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.NetvirtVpnInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.serviceutils.srm.types.rev180626.Ofplugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Utility class for SRM Shell.
 */
public final class SrmCliUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SrmCliUtils.class);

    private static final ImmutableMap<String, EntityTypeBase> ENTITY_TYPE_MAP =
        ImmutableMap.<String, EntityTypeBase>builder()
            .put("SERVICE", EntityTypeService.VALUE)
            .put("INSTANCE", EntityTypeInstance.VALUE)
            .build();

    private static final ImmutableMap<String, EntityNameBase> SERVICE_NAME_MAP =
        ImmutableMap.<String, EntityNameBase>builder()
            .put("ITM", GeniusItm.VALUE)
            .put("IFM", GeniusIfm.VALUE)
            .put("VPN", NetvirtVpn.VALUE)
            .put("ELAN", NetvirtElan.VALUE)
            .put("DHCP", NetvirtDhcp.VALUE)
            .put("L2GW", NetvirtL2gw.VALUE)
            .put("ACL", NetvirtAcl.VALUE)
            .put("OFPLUGIN", Ofplugin.VALUE)
            .put("QOS", NetvirtQos.VALUE)
            .build();

    private static final ImmutableMap<String, EntityNameBase> INSTANCE_NAME_MAP =
        ImmutableMap.<String, EntityNameBase>builder()
            .put("ITM-TEP", GeniusItmTep.VALUE)
            .put("ITM-TZ", GeniusItmTz.VALUE)
            .put("IFM-IFACE", GeniusIfmInterface.VALUE)
            .put("VPN-INSTANCE", NetvirtVpnInstance.VALUE)
            .put("ELAN-INTERFACE", NetvirtElanInterface.VALUE)
            .put("L2GW-NODE", NetvirtL2gwNode.VALUE)
            .put("L2GW-CONNECTION", NetvirtL2gwConnection.VALUE)
            .put("QOS-POLICY-INSTANCE", NetvirtQosPolicyInstance.VALUE)
            .put("ACL-INTERFACE", NetvirtAclInterface.VALUE)
            .put("ACL-INSTANCE", NetvirtAclInstance.VALUE)
            .build();

    private SrmCliUtils() {
        // Hidden on purpose
    }

    /**
     * Get EntityName given name in string.
     *
     * @param strType Entity Type as a string
     * @return EntityName for use
     */
    public static EntityTypeBase getEntityType(String strType) {
        LOG.debug("Getting entityType for type {}", strType);
        return ENTITY_TYPE_MAP.get(strType.toUpperCase(Locale.ROOT));
    }

    /**
     * Get EntityName given name in string.
     *
     * @param type    Entity Type class
     * @param strName Entity Name as a string
     * @return EntityName for use
     */
    public static @Nullable EntityNameBase getEntityName(EntityTypeBase type, String strName) {
        LOG.debug("Getting entityName for type {} and name: {}", type, strName);
        if (EntityTypeService.VALUE.equals(type)) {
            return SERVICE_NAME_MAP.get(strName.toUpperCase(Locale.ROOT));
        } else if (EntityTypeInstance.VALUE.equals(type)) {
            return INSTANCE_NAME_MAP.get(strName.toUpperCase(Locale.ROOT));
        } else {
            return null;
        }
    }

    public static String getTypeHelp() {
        var sb = new StringBuilder("Supported Entity Types are:\n");
        for (String entityType : SrmCliUtils.ENTITY_TYPE_MAP.keySet()) {
            sb.append("\t").append(entityType).append("/").append(entityType.toLowerCase(Locale.ROOT)).append("\n");
        }
        return sb.toString();
    }

    public static String getNameHelp(EntityTypeBase entityType) {
        var str = new StringBuilder("Supported Entity Names for type");

        if (EntityTypeService.VALUE.equals(entityType)) {
            str.append(" SERVICE are:\n");
            for (String entityName : SrmCliUtils.SERVICE_NAME_MAP.keySet()) {
                str.append(String.format("\t%s/%s%n", entityName.toLowerCase(Locale.ROOT), entityName));
            }
        } else if (EntityTypeInstance.VALUE.equals(entityType)) {
            str.append(" INSTANCE are:\n");
            for (String entityName : SrmCliUtils.INSTANCE_NAME_MAP.keySet()) {
                str.append(String.format("\t%s/%s%n", entityName.toLowerCase(Locale.ROOT), entityName));
            }
        }
        return str.toString();
    }
}
