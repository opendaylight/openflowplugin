/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.driver;

import static org.opendaylight.util.junit.TestTools.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * This JUnit test class tests the DefaultPresentation class.
 *
 * @author Simon Hunt
 */
public class DefaultPresentationTest {

    private DefaultDeviceInfo ddi;
    private Presentation facet;

    private static final String PATH = "path/to/some/resources";
    private static final String PROP_VALUE = "image.jpg";
    private static final String MAP_VALUE = "mapImage.jpg";

    private static final String FOO_TYPE = "type-foo";
    private static final String SLASH = "/";

    @Before
    public void setUp() {
        DefaultDeviceTypeBuilder builder = new DefaultDeviceTypeBuilder(FOO_TYPE);
        PresentationResources pr = new PresentationResources(PATH, PROP_VALUE, MAP_VALUE);
        builder.presentation(pr);

        ddi = new DefaultDeviceInfo(builder.build());
    }

    // == TESTS GO HERE ==
    @Test
    public void basic() {
        print(EOL + "basic()");
        facet = new DefaultPresentation(ddi);
        assertNotNull("no facet", facet);

        DeviceInfo di = facet.getContext();
        assertTrue("context not downcast?", di instanceof DefaultDeviceInfo);
    }

    @Test
    public void checkRefs() {
        print(EOL + "checkRefs()");
        facet = new DefaultPresentation(ddi);
        assertEquals(AM_NEQ, PATH + SLASH + PROP_VALUE, facet.getPropertiesTabImageRef());
        assertEquals(AM_NEQ, PATH + SLASH + MAP_VALUE, facet.getTopologyMapImageRef());
    }
}
