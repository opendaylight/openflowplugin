/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.genius.srm.shell;

import com.google.common.collect.ImmutableMap;
import java.util.Locale;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.EntityNameBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.EntityTypeBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.EntityTypeInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.EntityTypeService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.GeniusIfm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.GeniusIfmInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.GeniusItm;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.GeniusItmTep;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.GeniusItmTz;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtAcl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtAclInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtAclInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtDhcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtElan;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtElanInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtL2gw;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtL2gwConnection;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtL2gwNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtQos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtQosPolicyInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtVpn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.NetvirtVpnInstance;
import org.opendaylight.yang.gen.v1.urn.opendaylight.genius.srm.types.rev170711.Ofplugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Utility class for SRM Shell.
 */
public final class SrmCliUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SrmCliUtils.class);

    private SrmCliUtils() { }

    private static final ImmutableMap<String, Class<? extends EntityTypeBase>> ENTITY_TYPE_MAP =
        new ImmutableMap.Builder<String, Class<? extends EntityTypeBase>>()
            .put("SERVICE", EntityTypeService.class)
            .put("INSTANCE", EntityTypeInstance.class)
            .build();

    private static final ImmutableMap<String, Class<? extends EntityNameBase>> SERVICE_NAME_MAP =
        new ImmutableMap.Builder<String, Class<? extends EntityNameBase>>()
            .put("ITM", GeniusItm.class)
            .put("IFM", GeniusIfm.class)
            .put("VPN", NetvirtVpn.class)
            .put("ELAN", NetvirtElan.class)
            .put("DHCP", NetvirtDhcp.class)
            .put("L2GW", NetvirtL2gw.class)
            .put("ACL", NetvirtAcl.class)
            .put("OFPLUGIN", Ofplugin.class)
            .put("QOS", NetvirtQos.class)
            .build();

    private static final ImmutableMap<String, Class<? extends EntityNameBase>> INSTANCE_NAME_MAP =
        new ImmutableMap.Builder<String, Class<? extends EntityNameBase>>()
            .put("ITM-TEP", GeniusItmTep.class)
            .put("ITM-TZ", GeniusItmTz.class)
            .put("IFM-IFACE", GeniusIfmInterface.class)
            .put("VPN-INSTANCE", NetvirtVpnInstance.class)
            .put("ELAN-INTERFACE", NetvirtElanInterface.class)
            .put("L2GW-NODE", NetvirtL2gwNode.class)
            .put("L2GW-CONNECTION", NetvirtL2gwConnection.class)
            .put("QOS-POLICY-INSTANCE", NetvirtQosPolicyInstance.class)
            .put("ACL-INTERFACE", NetvirtAclInterface.class)
            .put("ACL-INSTANCE", NetvirtAclInstance.class)
            .build();

    /**
     * Get EntityName given name in string.
     *
     * @param strType Entity Type as a string
     * @return EntityName for use
     */
    public static Class<? extends EntityTypeBase> getEntityType(String strType) {
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
    public static Class<? extends EntityNameBase> getEntityName(Class<? extends EntityTypeBase> type, String strName) {
        LOG.debug("Getting entityName for type {} and name: {}", type, strName);
        if (EntityTypeService.class.equals(type)) {
            return SERVICE_NAME_MAP.get(strName.toUpperCase(Locale.ROOT));
        } else if (EntityTypeInstance.class.equals(type)) {
            return INSTANCE_NAME_MAP.get(strName.toUpperCase(Locale.ROOT));
        } else {
            return null;
        }
    }

    public static String getTypeHelp() {
        StringBuilder help = new StringBuilder("Supported Entity Types are:\n");
        for (String entityType : SrmCliUtils.ENTITY_TYPE_MAP.keySet()) {
            help.append("\t").append(entityType).append("/").append(entityType.toLowerCase(Locale.ROOT)).append("\n");
        }
        return help.toString();
    }

    public static String getNameHelp(Class<? extends EntityTypeBase> entityType) {
        StringBuilder help = new StringBuilder("Supported Entity Names for type");

        if (EntityTypeService.class.equals(entityType)) {
            help.append(" SERVICE are:\n");
            for (String entityName : SrmCliUtils.SERVICE_NAME_MAP.keySet()) {
                help.append(String.format("\t%s/%s%n", entityName.toLowerCase(Locale.ROOT), entityName));
            }
        } else if (EntityTypeInstance.class.equals(entityType)) {
            help.append(" INSTANCE are:\n");
            for (String entityName : SrmCliUtils.INSTANCE_NAME_MAP.keySet()) {
                help.append(String.format("\t%s/%s%n", entityName.toLowerCase(Locale.ROOT), entityName));
            }
        }
        return help.toString();
    }

}
