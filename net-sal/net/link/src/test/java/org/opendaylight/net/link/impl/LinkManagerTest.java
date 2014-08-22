/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.net.link.impl;

import org.opendaylight.util.event.EventDispatchService;
import org.opendaylight.util.net.BigPortNumber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.net.dispatch.impl.TestEventDispatcher;
import org.opendaylight.net.link.*;
import org.opendaylight.net.model.*;
import org.opendaylight.net.supplier.SupplierId;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.net.model.InterfaceId.valueOf;

/**
 * Unit tests for {@link org.opendaylight.net.link.impl.LinkManager}
 *
 * @author Marjorie Krueger
 */
public class LinkManagerTest {
    private final LinkManager lm = new LinkManager();
    private final LinkTestSupplier lts = new LinkTestSupplier();

    private LinkSupplierService lss;

    private final EventDispatchService eds = new TestEventDispatcher();

    private static final DeviceId DEV_1 = DeviceId.valueOf("42/00001e:123456");
    private static final DeviceId DEV_2 = DeviceId.valueOf("43/00001f:654321");
    private static final DeviceId DEV_3 = DeviceId.valueOf("43/00001f:300321");

    private static final InterfaceId IF_1 = valueOf(BigPortNumber.bpn(1));
    private static final InterfaceId IF_2 = valueOf(BigPortNumber.bpn(2));
    private static final InterfaceId IF_3 = valueOf(BigPortNumber.bpn(3));

    private static final ConnectionPoint CP_1_1 = new DefaultConnectionPoint(DEV_1, IF_1);
    private static final ConnectionPoint CP_1_2 = new DefaultConnectionPoint(DEV_1, IF_2);
    private static final ConnectionPoint CP_2_1 = new DefaultConnectionPoint(DEV_2, IF_1);
    private static final ConnectionPoint CP_2_2 = new DefaultConnectionPoint(DEV_2, IF_2);
    private static final ConnectionPoint CP_2_3 = new DefaultConnectionPoint(DEV_2, IF_3);
    private static final ConnectionPoint CP_3_2 = new DefaultConnectionPoint(DEV_3, IF_2);
    private static final ConnectionPoint CP_3_3 = new DefaultConnectionPoint(DEV_3, IF_3);

    private static final LinkInfo infoDirect = new DefaultLinkInfo(Link.Type.DIRECT_LINK);
    private static final LinkInfo infoMultihop = new DefaultLinkInfo(Link.Type.MULTIHOP_LINK);

    private static final Link linkDirect = new DefaultLink(CP_1_1, CP_2_3, Link.Type.DIRECT_LINK);


    @Before
    public void setUp() {
        lm.dispatchService = eds;
        lm.activate();
        lss = lm.registerSupplier(lts);
    }

    @After
    public void tearDown() {
        lm.deactivate();
    }

    @Test
    public void listeners() {
        LinkListener listener = new TestLinkListener();
        assertEquals("no listeners expected", 0, lm.getListeners().size());
        lm.addListener(listener);
        assertEquals("no listeners expected", 1, lm.getListeners().size());
        lm.removeListener(listener);
        assertEquals("no listeners expected", 0, lm.getListeners().size());
    }

    @Test(expected = NullPointerException.class)
    public void nullLink() throws Exception {
        lss.createOrUpdateLink(null, CP_2_3, infoDirect);
    }

    @Test(expected = NullPointerException.class)
    public void nullConnectionPoint() throws Exception {
        lss.createOrUpdateLink(CP_1_1, null, infoDirect);
    }

    @Test(expected = NullPointerException.class)
    public void nullLinkInfo() throws Exception {
        lss.createOrUpdateLink(CP_1_1, CP_2_3, null);
    }

    @Test
    public void createLink() {
        TestLinkListener listener = new TestLinkListener();
        lm.addListener(listener);

        lss.createOrUpdateLink(CP_1_1, CP_2_3, infoDirect);
        Set<Link> links = lm.getLinks(CP_1_1);
        assertEquals("incorrect link count", 1, links.size());
        assertEquals("stored link does not match", linkDirect, links.iterator().next());
        assertEquals("incorrect number of events received", 1, listener.events.size());

        lm.removeListener(listener);
    }

    @Test
    public void updateMultihopWithDirectLink() {
        // Create and store multihop link
        Link link = lss.createOrUpdateLink(CP_1_1, CP_2_3, infoMultihop);
        assertEquals("incorrect type", link.type(), Link.Type.MULTIHOP_LINK);

        //Update with a direct link
        link = lss.createOrUpdateLink(CP_1_1, CP_2_3, infoDirect);
        assertEquals("incorrect type", link.type(), Link.Type.DIRECT_LINK);

        //Make sure there's only one link
        Set<Link> links = lm.getLinks(CP_1_1);
        assertEquals("incorrect link count", 1, links.size());
    }

    @Test
    public void updateDirectWithMultihopLink() {
        // Create and store direct link
        lss.createOrUpdateLink(CP_1_1, CP_2_3, infoDirect);

        //Update with a multihop link
        Link link = lss.createOrUpdateLink(CP_1_1, CP_2_3, infoDirect);
        assertEquals("incorrect type", link.type(), Link.Type.DIRECT_LINK);

        //Make sure there's only one link
        Set<Link> links = lm.getLinks(CP_1_1);
        assertEquals("incorrect link count", 1, links.size());
    }

    @Test
    public void removeLink() {
        fillLinkCache();
        Set<Link> links = lm.getLinks(CP_1_1);
        assertEquals("incorrect link count", 2, links.size());

        lss.removeLink(CP_1_1, CP_2_1);

        links = lm.getLinks(DEV_1);
        assertEquals("incorrect link count", 3, links.size());

        links = lm.getLinks(CP_1_1);
        assertEquals("incorrect link count", 1, links.size());

        links = lm.getLinks(CP_2_1);
        assertEquals("incorrect link count", 1, links.size());
    }

    @Test
    public void removeLinksByDevice() {
        fillLinkCache();
        lss.removeAllLinks(DEV_2);
        Set<Link> links = lm.getLinks(DEV_2);
        assertTrue("device should have no links", links.isEmpty());

        links = lm.getLinks(DEV_1);
        assertEquals("incorrect link count", 2, links.size());
    }

    @Test
    public void removeLinksByConnectionPoint() {
        fillLinkCache();
        lss.removeAllLinks(CP_1_2);
        Set<Link> links = lm.getLinks(CP_1_2);
        assertTrue("connection point should have no links", links.isEmpty());

        links = lm.getLinks(DEV_1);
        assertEquals("device should have no links", 2, links.size());
    }

    @Test
    public void getLinksBetweenDevices() {
        Link l1 = lss.createOrUpdateLink(CP_1_1, CP_2_1, infoDirect);
        Link l2 = lss.createOrUpdateLink(CP_2_1, CP_1_1, infoDirect);
        Set<Link> links = lm.getLinks(DEV_1, DEV_2);
        assertEquals(links(l1, l2), links);
    }

    @Test
    public void getLinksToAndFrom() {
        Link[] l = fillLinkCache();

        Set<Link> links = lm.getLinksTo(DEV_3);
        assertEquals("Incorrect getLinksTo device", links(l[2], l[5]), links);

        links = lm.getLinksTo(CP_3_2);
        assertEquals("Incorrect getLinksTo cp", links(l[2]), links);

        links = lm.getLinksFrom(DEV_2);
        assertEquals("Incorrect getLinksFrom device", links(l[1], l[5]), links);

        links = lm.getLinksFrom(CP_3_3);
        assertEquals("Incorrect getLinksFrom cp", links(l[4]), links);
    }

    private Set<Link> links(Link... link) {
        return new HashSet<>(Arrays.asList(link));
    }

    private Link[] fillLinkCache() {
        return new Link[]{
                lss.createOrUpdateLink(CP_1_1, CP_2_1, infoDirect),
                lss.createOrUpdateLink(CP_2_1, CP_1_1, infoDirect),
                lss.createOrUpdateLink(CP_1_2, CP_3_2, infoDirect),
                lss.createOrUpdateLink(CP_3_2, CP_1_2, infoDirect),
                lss.createOrUpdateLink(CP_3_3, CP_2_2, infoMultihop),
                lss.createOrUpdateLink(CP_2_2, CP_3_3, infoMultihop)
        };
    }

    private class LinkTestSupplier implements LinkSupplier {
        private final SupplierId supplierId = new SupplierId("foobar");

        @Override
        public SupplierId supplierId() {
            return supplierId;
        }
    }

    private class TestLinkListener implements LinkListener {
        final List<LinkEvent> events = new ArrayList<>();

        @Override
        public void event(LinkEvent event) {
            events.add(event);
        }
    }

}
