/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import static org.opendaylight.util.StringUtils.EOL;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class provides utility methods and classes for simple communication between a web client and server.
 * In particular the {@link Url} class can be used to create (correctly encoded) URLs with query strings,
 * and the {@link Tag} class can be used to create simple XML or HTML constructs.
 *
 * @author Simon Hunt
 */
public final class WebUtils {
    
    private static final String SEP = "/";

    // no instantiation
    private WebUtils() { }

    /**
     * A simple class to facilitate building URLs used to interact with servlets.
     * For example, typical usage might be:
     * <pre>
     * WebUtils.Url url = new WebUtils.Url("someServlet")
     *                  .param("name", nameField.getValue())
     *                  .param("pass", passwordField.getValue());
     * RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, url.toString());
     * ...
     * </pre>
     * The URL supplied to the request builder might be something like:
     * <pre>
     * "someServlet?name=Simon&pass=SeCrEt"
     * </pre>
     */
    public static class Url {

        private String url;
        private List<Param> params = new ArrayList<Param>();

        /** Constructs a url with the specified base path.
         *
         * @param url the base url
         */
        public Url(String url) {
            this.url = url;
        }

        /**
         * Adds a path segment to the end of the URL.
         * 
         * @param path path segment to be appended to the URL.
         * @return self
         */
        public Url add(String path) {
            boolean hasSeparator = url.endsWith(SEP) || path.startsWith(SEP);
            url = url + (hasSeparator ? "" : SEP) + path;
            return this;
        }

        /**
         * Adds a parameter to the 'query string'. This method returns a
         * reference to the Url instance, allowing method calls to be chained.
         * 
         * @param name the name of the parameter
         * @param value the value of the parameter
         * @return self
         */
        public Url param(String name, Object value) {
            params.add(new Param(name, value));
            return this;
        }

        // TODO : correctly HTMLencoding the string

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(url);
            if (params.size() > 0) {
                String prefix = "?";
                for (Param p: params) {
                    sb.append(prefix).append(p);
                    prefix = "&";
                }
            }
            return sb.toString();
        }


        /** Simple representation of a name/value pair. */
        private static class Param {
            private String name;
            private Object value;

            private Param(String name, Object value) {
                this.name = name;
                this.value = value;
            }

            @Override
            public String toString() {
                return new StringBuilder(name).append("=").append(value).toString();
            }
        }

    }

    private static final String CONTAINS_WHITESPACE = ".*\\s+.*";
    private static final String CDATA_PRE = "<![CDATA[";
    private static final String CDATA_POST = "]]>";

    /**
     * A simple class to facilitate building XML or HTML constructs.
     * For example, typical usage might be:
     * <pre>
     * WebUtils.Tag results = new WebUtils.Tag("results");
     * for (Record r: someResultsSet()) {
     *     WebUtils.Tag result = new WebUtils.Tag("result")
     *                      .attr("id", r.getId())
     *                      .attr("status", r.getStatus());
     *     results.addContent(result);
     *  }
     *  ...
     * </pre>
     * Invoking {@code results.toString()} might produce something like...
     * <pre>
     * &lt;results&gt;
     *   &lt;result id="11" status="normal" /&gt;
     *   &lt;result id="12" status="normal" /&gt;
     *   &lt;result id="13" status="unreachable" /&gt;
     * &lt;/results&gt;
     * </pre>
     * <p>
     * <i>Note that this class is not intended as a replacement
     * for more complete XML utilities such as JAXB.</i>
     * <p>
     * Another example, this time HTML...
     * <pre>
     * WebUtils.Tag table = new WebUtils.Tag("table").attr("width", "100%");
     * WebUtils.Tag tr = new WebUtils.Tag("tr");
     * table.addContent(tr);
     * WebUtils.Tag td = new WebUtils.Tag("td").addContent("First Cell");
     * tr.addContent(td);
     * td = new WebUtils.Tag("td").addContent("Second Cell");
     * tr.addContent(td);
     * </pre>
     * Invoking {@code table.toString()} will produce...
     * <pre>
     * &lt;table width="100%"&gt;
     *   &lt;tr&gt;
     *     &lt;td&gt;
     *       First Cell
     *     &lt;/td&gt;
     *     &lt;td&gt;
     *       Second Cell
     *     &lt;/td&gt;
     *   &lt;/tr&gt;
     * &lt;/table&gt;
     * </pre>
     */
    public static class Tag {
        private int indent = 0;
        private String id;
        private List<Attr> attrs;
        private List<Tag> content;
        private boolean shorthand = true;
        private boolean verbatim = false;
        private boolean cdata = false;

        /** Constructs a tag with the given id.
         *
         * @param id the id
         */
        public Tag(String id) {
            if (id==null)
                throw new NullPointerException("id cannot be null");
            if (id.matches(CONTAINS_WHITESPACE))
                throw new IllegalArgumentException("id cannot contain whitespace");
            this.id = id;
            attrs = new ArrayList<Attr>();
            content = new ArrayList<Tag>();
        }

        /** private no-args constructor - used to create a "tag" that is simply a wrapped string.
         *  notice that the attrs and content fields have been left null. This indicates that this tag
         * instance is a wrapped string.
         */
        private Tag() { }

        /** Adds an attribute with the given name and value.
         *
         * @param name the attribute name
         * @param value the attribute value
         * @return self
         */
        public Tag attr(String name, Object value) {
            attrs.add(new Attr(name, value));
            return this;
        }

        public Tag noShorthand() {
            shorthand = false;
            return this;
        }


        /** Adds the specified tag as a child to this tag. Note that the child tag inherits the
         * {@link #noShorthand shorthand} setting of this tag.
         *
         * @param tag the child tag to be added as content
         * @return self
         */
        public Tag addContent(Tag tag) {
            content.add(tag);
            return this;
        }

        /** Adds the specified string as content of this tag.
         * If the textInlining parameter is set true, the textual content will be rendered on the same
         * line as the tag/endtag, for example:
         * <pre>
         * &lt;tag&gt;content&lt;/tag&gt;
         * </pre>
         *
         *
         * @param s the string to be added as content.
         * @param textInlining if true, the text content will be rendered on the same line as the tag/endtag
         * @return self
         */
        public Tag addContent(String s, boolean textInlining) {
            Tag wrappedString = new Tag();  // no-args constructor marks this as a wrapped string
            wrappedString.shorthand = textInlining;       // inlining (e.g. "<tag>content</tag>")
            wrappedString.id = s;
            addContent(wrappedString);
            return this;
        }

        /** Adds the specified string as content of this tag.
         * If the textInlining parameter is set true, the textual content will be rendered on the same
         * line as the tag/endtag, for example:
         * <pre>
         * &lt;tag&gt;content&lt;/tag&gt;
         * </pre>
         *
         * @param s the string to be added as content.
         * @param textInlining if true, the text content will be rendered on the same line as the tag/endtag
         * @param verbatim if true, the text content will be inserted without any protective encoding
         * @return self
         */
        public Tag addContent(String s, boolean textInlining, boolean verbatim) {
            Tag wrappedString = new Tag();  // no-args constructor marks this as a wrapped string
            wrappedString.shorthand = textInlining;       // inlining (e.g. "<tag>content</tag>")
            wrappedString.verbatim = verbatim;            // encode or verbatim
            wrappedString.id = s;
            addContent(wrappedString);
            return this;
        }

        /** Adds the specified string as verbatim content of this tag.
         *
         * @param s the string to be added as content.
         * @return self
         */
        public Tag addContent(String s) {
            return addContent(s, true, true);
        }

        /** Adds the specified string as content of this tag, but wrapped in a CDATA construct.
         * For example:
         * <pre>
         * WebUtils.Tag tag = new WebUtils.Tag("record")
         *                          .addCData("Some arbitrary data");
         * </pre>
         * This will result in the following...
         * <pre>
         * &lt;record&gt;
         *   &lt;![CDATA[Some arbitrary data]]&gt;
         * &lt;/record&gt;
         * </pre>
         * @param data the data to be inserted into a CDATA construct
         * @return self
         */
        public Tag addCData(String data) {
            Tag cdataTag = new Tag();
            cdataTag.shorthand = false;
            cdataTag.id = data;
            cdataTag.cdata = true;
            addContent(cdataTag);
            return this;
        }
        
        /**
         * Returns an indication whether or not the given tag has any content.
         * 
         * @return true if content has been added; false otherwise
         */
        public boolean hasContent() {
            return content != null && content.size() > 0;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(StringUtils.spaces(indent));
            if (attrs == null) {
                // this is a wrapped string instance
                if (cdata) {
                    sb.append(CDATA_PRE);
                    sb.append(id);
                    sb.append(CDATA_POST);
                    sb.append(EOL);
                } else {
                    sb.append(verbatim ? id : encode(id));
                    if (!isInlineText())
                        sb.append(EOL);
                }
            } else {

                // this is a Tag (not wrapped string instance)
                sb.append("<").append(id);
                if (attrs.size() > 0) {
                    for (Attr a: attrs) {
                        sb.append(a);
                    }
                }
                if (content.size() == 0) {
                    if (shorthand) {
                        sb.append(" />").append(EOL);
                    } else {
                        sb.append("></").append(encode(id)).append(">").append(EOL);
                    }
                } else {
                    sb.append(">");
                    if (content.size() == 1 && content.get(0).isInlineText()) {
                        sb.append(content.get(0));
                    } else {
                        sb.append(EOL);
                        for (Tag t: content) {
                            if (!t.isWrappedText()) {
                                t.shorthand = this.shorthand;
                            }
                            t.setIndent(indent + 2);
                            sb.append(t);
                        }
                        sb.append(StringUtils.spaces(indent));
                    }
                    sb.append("</").append(encode(id)).append(">").append(EOL);
                }
            }
            return sb.toString();
        }

        // private predicate that returns true if this is a 'wrapped string' that should be 'inlined'
        private boolean isInlineText() {
            return shorthand && isWrappedText();
        }

        private boolean isWrappedText() {
            return attrs == null;
        }

        private static String encode(Object o) {
            return o == null ? "" : StringEscapeUtils.escapeXml(o.toString());
        }

        private void setIndent(int i) {
            indent = i;
        }

        /** Simple inner class representing an attribute of a tag. */
        private static class Attr {
            private String name;
            private Object value;

            private Attr(String name, Object value) {
                this.name = name;
                this.value = value;
            }

            @Override
            public String toString() {
                return new StringBuilder(" ").append(name).append("=\"").append(encode(value)).append("\"").toString();
            }
        }

    }

}
