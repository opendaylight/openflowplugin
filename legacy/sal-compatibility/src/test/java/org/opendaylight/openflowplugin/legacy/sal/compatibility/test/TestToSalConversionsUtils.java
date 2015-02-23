/*
 * Copyright (c) 2013-2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.legacy.sal.compatibility.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.net.InetAddresses;
import org.junit.Test;
import org.opendaylight.controller.sal.action.Output;
import org.opendaylight.controller.sal.action.PushVlan;
import org.opendaylight.controller.sal.action.SetDlDst;
import org.opendaylight.controller.sal.action.SetDlSrc;
import org.opendaylight.controller.sal.action.SetDlType;
import org.opendaylight.controller.sal.action.SetNextHop;
import org.opendaylight.controller.sal.action.SetNwDst;
import org.opendaylight.controller.sal.action.SetNwSrc;
import org.opendaylight.controller.sal.action.SetNwTos;
import org.opendaylight.controller.sal.action.SetTpDst;
import org.opendaylight.controller.sal.action.SetTpSrc;
import org.opendaylight.controller.sal.action.SetVlanCfi;
import org.opendaylight.controller.sal.action.SetVlanId;
import org.opendaylight.controller.sal.action.SetVlanPcp;
import org.opendaylight.controller.sal.core.ConstructionException;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.Node.NodeIDType;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.NodeConnector.NodeConnectorIDType;
import org.opendaylight.openflowplugin.legacy.sal.compatibility.ToSalConversionsUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class TestToSalConversionsUtils {
    // prefix:
    // od|Od = Open Daylight
    private enum MtchType {
        other, untagged, ipv4, ipv6, arp, sctp, tcp, udp
    }

    /**
     * test of {@link ToSalConversionsUtils#fromNodeConnectorRef(Uri, Node)}
     *
     * @throws ConstructionException
     */
    @Test
    public void testFromNodeConnectorRef() throws ConstructionException {
        Node node = new Node(NodeIDType.OPENFLOW, 42L);
        NodeConnector nodeConnector = ToSalConversionsUtils.fromNodeConnectorRef(new Uri("1"), node);
        assertEquals("OF|1@OF|00:00:00:00:00:00:00:2a", nodeConnector.toString());
    }

    @Test
    public void testActionFrom() throws ConstructionException {
        // Bug 2021: Convert AD-SAL notation into MD-SAL notation before calling NodeConnector
        Node node = new Node(NodeIDType.OPENFLOW, 42L);
        List<Action> odActions = new ArrayList<>();

        OutputActionBuilder outputActionBuilder = new OutputActionBuilder();
        outputActionBuilder.setOutputNodeConnector(new Uri("CONTROLLER"));
        OutputActionCaseBuilder outputActionCaseBuilder = new OutputActionCaseBuilder();
        outputActionCaseBuilder.setOutputAction(outputActionBuilder.build());
        odActions.add(new ActionBuilder().setAction(outputActionCaseBuilder.build()).build());

        List<org.opendaylight.controller.sal.action.Action> targetAction =
                ToSalConversionsUtils.actionFrom(odActions, node);
        assertNotNull(targetAction);
        assertTrue(Output.class.isInstance(targetAction.get(0)));
        Output targetActionOutput = (Output) targetAction.get(0);
        NodeConnector port = targetActionOutput.getPort();
        assertNotNull(port);
        assertEquals(port.getType(), NodeConnectorIDType.CONTROLLER);
        assertEquals(port.getID(), org.opendaylight.controller.sal.core.NodeConnector.SPECIALNODECONNECTORID);
    }

    private void checkSalAction(List<org.opendaylight.controller.sal.action.Action> actions, Class<?> cls,
                                int numOfActions, boolean additionalCheck) {
        int numOfEqualClass = 0;
        for (org.opendaylight.controller.sal.action.Action action : actions) {
            if (action.getClass().equals(cls)) {
                if (additionalCheck) {
                    additionalActionCheck(action);
                }
                numOfEqualClass++;
            }
        }
        assertEquals("Incorrect number of actions of type " + cls.getName() + " was found.", numOfActions,
                numOfEqualClass);
    }

    // implement special checks
    private void additionalActionCheck(org.opendaylight.controller.sal.action.Action action) {
        if (action instanceof Output) {
            // ((Output)action).getPort() //TODO finish check when mapping will
            // be defined
        } else if (action instanceof PushVlan) {
            assertEquals("Wrong value for action PushVlan for tag.", 0x8100, ((PushVlan) action).getTag());
        } else if (action instanceof SetDlDst) {
            //assertEquals("Wrong value for action SetDlDst for MAC address.", "3C:A9:F4:00:E0:C8", new String(
            //        ((SetDlDst) action).getDlAddress()));
        } else if (action instanceof SetDlSrc) {
            //assertEquals("Wrong value for action SetDlSrc for MAC address.", "24:77:03:7C:C5:F1", new String(
            //      ((SetDlSrc) action).getDlAddress()));
        } else if (action instanceof SetDlType) {
            assertEquals("Wrong value for action SetDlType for.", 513L, ((SetDlType) action).getDlType());
        } else if (action instanceof SetNextHop) {
            InetAddress inetAddress = ((SetNextHop) action).getAddress();
            checkIpAddresses(inetAddress, "192.168.100.100", "2001:db8:85a3::8a2e:370:7334");
        } else if (action instanceof SetNwDst) {
            InetAddress inetAddress = ((SetNwDst) action).getAddress();
            checkIpAddresses(inetAddress, "192.168.100.101", "2001:db8:85a3::8a2e:370:7335");
        } else if (action instanceof SetNwSrc) {
            InetAddress inetAddress = ((SetNwSrc) action).getAddress();
            checkIpAddresses(inetAddress, "192.168.100.102", "2001:db8:85a3::8a2e:370:7336");
        } else if (action instanceof SetNwTos) {
            assertEquals("Wrong value for action SetNwTos for tos.", 63, ((SetNwTos) action).getNwTos());
        } else if (action instanceof SetTpDst) {
            assertEquals("Wrong value for action SetTpDst for port.", 65535, ((SetTpDst) action).getPort());
        } else if (action instanceof SetTpSrc) {
            assertEquals("Wrong value for action SetTpSrc for port.", 65535, ((SetTpSrc) action).getPort());
        } else if (action instanceof SetVlanCfi) {
            assertEquals("Wrong value for action SetVlanCfi for port.", 1, ((SetVlanCfi) action).getCfi());
        } else if (action instanceof SetVlanId) {
            assertEquals("Wrong value for action SetVlanId for vlan ID.", 4095, ((SetVlanId) action).getVlanId());
        } else if (action instanceof SetVlanPcp) {
            assertEquals("Wrong value for action SetVlanPcp for vlan ID.", 7, ((SetVlanPcp) action).getPcp());
        }
    }

    private void checkIpAddresses(InetAddress inetAddress, String ipv4, String ipv6) {
        if (inetAddress instanceof Inet4Address) {
            assertEquals("Wrong value for IP address.", ipv4, InetAddresses.toAddrString(inetAddress));
        } else if (inetAddress instanceof Inet6Address) {
            assertEquals("Wrong value for IP address.", ipv6, InetAddresses.toAddrString(inetAddress));
        }
    }


}
