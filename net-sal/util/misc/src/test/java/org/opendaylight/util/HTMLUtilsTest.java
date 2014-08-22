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
import static org.junit.Assert.assertNotNull;

import org.opendaylight.util.HTMLUtils.Body;
import org.opendaylight.util.HTMLUtils.Div;
import org.opendaylight.util.HTMLUtils.Head;
import org.opendaylight.util.HTMLUtils.Image;
import org.opendaylight.util.HTMLUtils.Link;
import org.opendaylight.util.HTMLUtils.Page;
import org.opendaylight.util.HTMLUtils.Span;

import org.junit.Test;

/**
 * Set of tests for the HTML generation utilities.
 *
 * @author Thomas Vachuska
 */
public class HTMLUtilsTest {
    
    @Test
    public void pageTag() {
        Page page = new Page("Foo");
        assertNotNull("head should always exist", page.head());
        assertNotNull("body should always exist", page.body());
        
        String ehtml = 
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">" + EOL + 
            "  <head>" + EOL +
            "    <title>Foo</title>" + EOL + 
            "  </head>" + EOL +
            "  <body />" + EOL +
            "</html>" + EOL;
        
        String html = page.toString();
        print(html);
        assertEquals("incorrect page format", ehtml, html);
        
        page = new Page(new Head("Foo"), new Body());
        assertNotNull("head should always exist", page.head());
        assertNotNull("body should always exist", page.body());
        assertEquals("incorrect page format", ehtml, html);
    }
    
    @Test
    public void divTag() {
        Div d = new Div("bar");
        String html = d.toString();
        print(html);
        assertEquals("incorrect div format", "<div id=\"bar\" />" + EOL, html); 
        
        d.add(new Span("foo"));
        html = d.toString();
        print(html);
        assertEquals("incorrect div format", 
                     "<div id=\"bar\">" + EOL +
                     "  <span class=\"foo\" />" + EOL +
                     "</div>" + EOL, 
                     html); 
    }

    @Test
    public void divTagAdd() {
        Div d = new Div("bar");
        String html = d.toString();
        print(html);
        assertEquals("incorrect div format", "<div id=\"bar\" />" + EOL, html); 
        
        d.add("foo");
        html = d.toString();
        print(html);
        assertEquals("incorrect div format", "<div id=\"bar\">foo</div>" + EOL, 
                     html); 
    }

    @Test
    public void spanTag() {
        Span s = new Span("bar");
        String html = s.toString();
        print(html);
        assertEquals("incorrect span format", 
                     "<span class=\"bar\" />" + EOL, html); 

        s = new Span("bar", "Something");
        html = s.toString();
        print(html);
        assertEquals("incorrect span format", 
                     "<span class=\"bar\">Something</span>" + EOL, html); 
    }

    @Test
    public void linkTag() {
        Link s = new Link("http://url");
        String html = s.toString();
        print(html);
        assertEquals("incorrect link format", 
                     "<a href=\"http://url\" />" + EOL, html); 

        s = new Link("http://url", "Link");
        html = s.toString();
        print(html);
        assertEquals("incorrect link format", 
                     "<a href=\"http://url\">Link</a>" + EOL, html); 

        s = new Link("http://url", "Link", "Description");
        html = s.toString();
        print(html);
        assertEquals("incorrect link format", 
                     "<a href=\"http://url\" alt=\"Description\">Link</a>" + EOL, 
                     html); 

        s = new Link("http://url", "Description", 
                     new Image("http://image", "Image Description"));
        html = s.toString();
        print(html);
        assertEquals("incorrect link format", 
                     "<a href=\"http://url\" alt=\"Description\">" + EOL +
                     "  <img src=\"http://image\" alt=\"Image Description\" />" + EOL + "</a>" + EOL, 
                     html); 
    }

}
