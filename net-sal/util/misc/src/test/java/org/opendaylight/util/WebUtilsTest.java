/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * This JUnit test class tests the WebUtils class.
 *
 * @author Simon Hunt
 */
public class WebUtilsTest {

    private static final String BASE = "someServlet";
    private static final String NAME = "name";
    private static final String PASS = "pass";

    private static final String SIMON = "Simon";
    private static final String SECRET = "SeCrEt!";
    private static final String ADDRESS = "8000 Foothills Blvd.";

    private static final String QUERY = "?";
    private static final String AMP = "&";
    private static final String EQ = "=";

    private static final String RECORD = "record";
    private static final String TABLE = "table";
    private static final String WIDTH = "width";
    private static final String P100 = "100%";
    private static final String TR = "tr";
    private static final String TD = "td";

    private static final String BFT = "badly formatted tag";


    private WebUtils.Url url;
    private WebUtils.Tag tag;
    private WebUtils.Tag tr;
    private WebUtils.Tag td;

    @Test
    public void basicUrl() {
        print(EOL + "basicUrl()");
        url = new WebUtils.Url(BASE);
        print("url [" + url + "]");
        assertEquals("incorrect url base", BASE, url.toString());
    }

    @Test
    public void urlOneParam() {
        print(EOL + "urlOneParam()");
        url = new WebUtils.Url(BASE).param(NAME, SIMON);
        print("url [" + url + "]");
        assertEquals("incorrect query url", BASE + QUERY + NAME + EQ + SIMON,
                     url.toString());
    }

    @Test
    public void urlTwoParams() {
        print(EOL + "urlTwoParams()");
        url = new WebUtils.Url(BASE).param(NAME, SIMON).param(PASS, SECRET);
        print("url [" + url + "]");
        assertEquals("incorrect query url",
                     BASE + QUERY + NAME + EQ + SIMON + AMP + PASS + EQ + SECRET,
                     url.toString());
    }



    @Test
    public void basicTag() {
        print(EOL + "basicTag()");
        tag = new WebUtils.Tag(TABLE);
        print(tag);
        assertEquals(BFT, "<table />" + EOL, tag.toString());
        assertFalse("tag should not have content", tag.hasContent());
    }

    @Test
    public void tagOneParam() {
        print(EOL + "tagOneParam()");
        tag = new WebUtils.Tag(TABLE).attr(WIDTH, P100);
        print(tag);
        assertEquals(BFT, "<table width=\"100%\" />" + EOL, tag.toString());
        assertFalse("tag should not have content", tag.hasContent());
    }

    @Test
    public void tagTableTr() {
        print(EOL + "tagTableTr()");
        tag = new WebUtils.Tag(TABLE);
        tr = new WebUtils.Tag(TR);
        assertFalse("tag should not have content yet", tag.hasContent());
        tag.addContent(tr);
        assertTrue("tag should have content now", tag.hasContent());
        print(tag);
        assertEquals(BFT, "<table>"+EOL+"  <tr />" + EOL + "</table>" + EOL, tag.toString());        
    }

    @Test
    public void tagNoShorthand() {
        print(EOL + "tagNoShorthand()");
        tag = new WebUtils.Tag(TABLE).noShorthand();
        print(tag);
        assertEquals(BFT, "<table></table>" + EOL, tag.toString());
    }

    @Test
    public void tagNoShorthandInherited() {
        print(EOL + "tagNoShorthandInherited()");
        tag = new WebUtils.Tag(TABLE).noShorthand();
        tr = new WebUtils.Tag(TR);
        tag.addContent(tr);
        print(tag);
        assertEquals(BFT, "<table>"+EOL+"  <tr></tr>"+EOL+"</table>"+EOL, tag.toString());
    }

    @Test
    public void tagDeepTableOne() {
        print(EOL + "tagDeepTableOne()");
        tag = new WebUtils.Tag(TABLE).attr(WIDTH, P100);
        tr = new WebUtils.Tag(TR);
        td = new WebUtils.Tag(TD).addContent(SIMON);
        tr.addContent(td);
        td = new WebUtils.Tag(TD).addContent(ADDRESS);
        tr.addContent(td);
        tag.addContent(tr);
        print(tag);
        assertEquals(BFT, "<table width=\"100%\">"+EOL+
                          "  <tr>"+EOL+
                          "    <td>"+SIMON+"</td>"+EOL+
                          "    <td>"+ADDRESS+"</td>"+EOL+
                          "  </tr>"+EOL+
                          "</table>"+EOL, tag.toString());
    }

    @Test
    public void tagDeepTableTwo() {
        print(EOL + "tagDeepTableTwo()");
        tag = new WebUtils.Tag(TABLE).attr(WIDTH, P100);
        tr = new WebUtils.Tag(TR);
        td = new WebUtils.Tag(TD).addContent(SIMON, false);     // do not inline verbatim text
        tr.addContent(td);
        td = new WebUtils.Tag(TD).addContent(ADDRESS, false);   // ditto
        tr.addContent(td);
        tag.addContent(tr);
        print(tag);
        assertEquals(BFT, "<table width=\"100%\">"+EOL+
                          "  <tr>"+EOL+
                          "    <td>"+EOL+
                          "      "+SIMON+EOL+
                          "    </td>"+EOL+
                          "    <td>"+EOL+
                          "      "+ADDRESS+EOL+
                          "    </td>"+EOL+
                          "  </tr>"+EOL+
                          "</table>"+EOL, tag.toString());
    }

    @Test
    public void tagCdata() {
        print(EOL + "tagCdata()");
        tag = new WebUtils.Tag(RECORD).addCData(ADDRESS);
        print(tag);
        assertEquals(BFT, "<record>"+EOL+
                          "  <![CDATA["+ADDRESS+"]]>"+EOL+
                          "</record>"+EOL, tag.toString());
    }

    @Test(expected = NullPointerException.class)
    public void tagNullId() {
        tag = new WebUtils.Tag(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tagEmbeddedSpaceInId() {
        tag = new WebUtils.Tag("some id with spaces in it");
    }
}
