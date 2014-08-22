/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;


/**
 * Set of utilities for producing HTML pages.
 * 
 * @author Thomas Vachuska
 */
public final class HTMLUtils {
    
    public static final String HTML = "html";
    public static final String HEAD = "head";
    public static final String BODY = "body";
    
    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String CLASS = "class";
    public static final String DIV = "div";
    public static final String SPAN = "span";

    public static final String META = "meta";
    public static final String LINK = "link";
    public static final String SCRIPT = "script";

    public static final String CONTENT = "content";
    public static final String CHARSET = "charset";

    public static final String SRC = "src";
    public static final String IMG = "img";
    public static final String ANCHOR = "a";
    public static final String HREF = "href";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String ALT = "alt";
    
    public static final String UL = "ul";
    public static final String OL = "ol";
    public static final String LI = "li";

    public static final String TABLE = "table";
    public static final String TR = "tr";
    public static final String TH = "th";
    public static final String TD = "td";

    private static final String BR_TAG = "<br/>";


    private HTMLUtils() {} // no instantiation

    /** Replace newline characters with an HTML break tag "&lt;br/&gt;".
     *
     * @param s the original string
     * @return the transformed string
     */
    public static String nl2Br(String s) {
        return s.replace("\n", BR_TAG);
    }

    /**
     * Generic HTML tag.
     */
    public static class HTMLTag extends WebUtils.Tag {
        
        /**
         * Creates a new HTML tag.
         * 
         * @param name HTML tag name
         */
        public HTMLTag(String name) {
            super(name);
        }
        
        /**
         * Adds an HTML tag to this HTML tag.
         * 
         * @param tag HTML tag to be added
         * @return this HTML tag
         */
        public HTMLTag add(WebUtils.Tag tag) {
            if (tag != null)
                addContent(tag);
            return this;
        }
        
        /**
         * Adds an attribute to this HTML tag.
         * 
         * @param name attribute name
         * @param value attribute value
         * @return this HTML tag
         */
        public HTMLTag attr(String name, String value) {
            return (HTMLTag) super.attr(name, value);
        }

        /**
         * Adds an direct string content to this HTML tag.
         * 
         * @param content content to be added to this HTML tag
         * @return this HTML tag
         */
        public HTMLTag add(String content) {
            if (content != null)
                addContent(content, true);
            return this;
        }
        
    }
    
    /**
     * HTML page tag.
     */
    public static class Page extends WebUtils.Tag {
        
        private Head head;
        private Body body;
        
        /**
         * Creates a new HTML page tag using the supplied head and body tags.
         * 
         * @param head HTML head tag
         * @param body HTML body tag
         */
        public Page(Head head, Body body) {
            super(HTML);
            attr("xmlns", "http://www.w3.org/1999/xhtml");

            this.head = head;
            addContent(head);

            this.body = body;
            addContent(body);
        }
        
        /**
         * Creates a new HTML page tag, including the embedded head and body
         * tags. The embedded HTML head tag will be created using the
         * specified title.
         * 
         * @param title HTML head title
         */
        public Page(String title) {
            this(new Head(title), new Body());
        }

        /**
         * Get the embedded HTML head tag.
         * 
         * @return HTML page head tag
         */
        public Head head() {
            return head;
        }
        
        /**
         * Get the embedded HTML body tag.
         * 
         * @return HTML page body tag
         */
        public Body body() {
            return body;
        }
    }
    
    /**
     * HTML head tag.
     */
    public static class Head extends HTMLTag {
        
        /**
         * Creates a new HTML head tag titled with the supplied title.
         * 
         * @param title HTML head title
         */
        public Head(String title) {
            super(HEAD);
            addContent(new WebUtils.Tag(TITLE).addContent(title));
        }

    }
    
    /**
     * HTML body tag.
     */
    public static class Body extends HTMLTag {
        
        /**
         * Creates a new HTML body tag.
         */
        public Body() {
            super(BODY);
        }
        
    }
    
    /**
     * HTML division tag.
     */
    public static class Div extends HTMLTag {

        /**
         * Creates a new HTML division tag with the specified ID attribute.
         * 
         * @param id value of the ID attribute
         */
        public Div(String id) {
            super(DIV);
            attr(ID, id);
        }
        
    }
    
    /**
     * HTML span tag.
     */
    public static class Span extends HTMLTag {

        /**
         * Creates a new HTML span tag with the specified ID attribute.
         * 
         * @param spanClass value of the span class attribute
         */
        public Span(String spanClass) {
            super(SPAN);
            attr(CLASS, spanClass);
        }
        
        /**
         * Creates a new HTML span tag with the specified ID attribute and
         * content.
         * 
         * @param spanClass value of the span class attribute
         * @param content text content
         */
        public Span(String spanClass, String content) {
            this(spanClass);
            addContent(content);
        }
        
    }

    /**
     * HTML anchor tag.
     */
    public static class Link extends HTMLTag {
        
        /**
         * Creates a new HTML anchor tag with the specified URL.
         * 
         * @param url URL to which this anchor links
         */
        public Link(String url) {
            super(ANCHOR);
            attr(HREF, url);
        }
        
        /**
         * Creates a new HTML anchor tag with the specified URL and the given
         * string as the content.
         * 
         * @param url URL to which this anchor links
         * @param content simple anchor tag content
         */
        public Link(String url, String content) {
            this(url);
            if (content != null)
                addContent(content);
        }
        
        /**
         * Creates a new HTML anchor tag with the specified URL, description,
         * and a simple content.
         * 
         * @param url URL to which this anchor links
         * @param description alternate link description
         * @param content simple anchor tag content
         */
        public Link(String url, String content, String description) {
            this(url, content);
            attr(ALT, description);
        }
        
        /**
         * Creates a new HTML anchor tag with the specified URL, description,
         * and a nested content tag
         * 
         * @param url URL to which this anchor links
         * @param description alternate link description
         * @param content HTML tag to be nested in the anchor tag content
         */
        public Link(String url, String description, HTMLTag content) {
            this(url, null, description);
            addContent(content);
        }
        
    }
    
    
    /**
     * HTML image tag.
     */
    public static class Image extends HTMLTag {
        
        /**
         * Creates a new HTML image tag with the specified image URL and
         * description.
         * 
         * @param url URL of the image content
         * @param description alternate image description
         */
        public Image(String url, String description) {
            super(IMG);
            attr(SRC, url).attr(ALT, description);
        }
        
    }
        
}
