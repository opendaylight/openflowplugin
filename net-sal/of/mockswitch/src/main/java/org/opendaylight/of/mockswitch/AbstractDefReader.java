/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.mockswitch;

import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.ValidationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opendaylight.util.StringUtils.*;

/**
 * Abstract base class for parsing text-based definition files.
 * <p>
 * A definition file is a free format text file where lines beginning with the
 * {@code #} symbol are treated as comments and ignored, as are blank lines.
 * All other lines are treated as input and converted to "logical" lines.
 * There will be one logical line per real (non-comment) line, except in the
 * case where the backslash character is used to indicate line continuation.
 * For example:
 * <pre>
 * 1   # a comment
 * 2
 * 3   some input line
 * 4   another input line \
 * 5      continued on the next line
 * 6
 * 7   # another comment
 * 8   last line
 * </pre>
 * In this example, the parsed definition will consist of 3 logical lines:
 * <pre>
 * [3] some input line
 * [4-5] another input line continued on the next line
 * [8] last line
 * </pre>
 * The line numbers from the source file are indicated in square brackets.
 * <p>
 * As the file is processed, subclasses have the opportunity to further
 * validate each logical line by overriding the {@link #parseLogicalLine}
 * method. The implementation should return {@code null} if the line is
 * acceptable, or a short string describing why it is not. These "errors"
 * are collected and reported to the user in a single
 * {@link ValidationException}, indicating the line numbers at which the
 * errors occurred.
 * <p>
 * The following example accepts only lines of the form
 * {@code "key: some value"}:
 * <pre>
 * private static final Pattern RE_KV = Pattern.compile("(\\w+):\\s+(.*)");
 *
 * &#64;Override
 * protected String parseLogicalLine(LogicalLine line) {
 *    return RE_KV.matcher(line.getText()).matches()
 *            ? null : "invalid format: \"" + line.getText() + "\"";
 * }
 * </pre>
 * If we used this implementation to try and parse the example file above,
 * the following exception would be thrown:
 * <pre>
 * org.opendaylight.util.ValidationException: Failed to parse sample.def
 *   Line 3: invalid format: "some input line"
 *   Line 4-5: invalid format: "another input line continued on the next line"
 *   Line 8: invalid format: "last line"
 * </pre>
 *
 * @author Simon Hunt
 */
public abstract class AbstractDefReader {

    private static final ResourceBundle RES = ResourceUtils.getBundledResource(
            AbstractDefReader.class, "abstractDefReader");

    private static final String E_BAD_FILE = RES.getString("e_bad_file");
    private static final String DQ = "\"";

    private final String path;
    private List<LogicalLine> logicalLines;
    private int maxNum = 0;

    /** Constructs the definition file reader by reading in the specified
     * text file, and parsing it into logical lines.
     *
     * @param path the path of the file
     */
    protected AbstractDefReader(String path) {
        this.path = path;
        try {
            ClassLoader cl = this.getClass().getClassLoader();
            String contents = getFileContents(path, cl);
            if (contents == null)
                throw new RuntimeException(E_BAD_FILE + path + DQ);
            initStorage();
            parseDefinition(normalizeEOL(contents).split("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Parses the definition file, creating a model of the logical lines.
     *
     * @param lines the lines of the file
     */
    protected void parseDefinition(String[] lines) {
        List<LogicalLine> logicals = new ArrayList<LogicalLine>();
        List<String> errors = new ArrayList<String>();
        boolean cont = false;
        LogicalLine ll = null;
        int lineNum = 0;
        for (String s: lines) {
            Line line = new Line(++lineNum, s);
            if (line.comment) {
                cont = false;
                continue;
            }

            if (!cont) {
                ll = new LogicalLine(line);
                logicals.add(ll);
            } else
                ll.addLine(line);

            cont = line.continued;
            if (!cont) {
                String err = parseLogicalLine(ll);
                if (err != null)
                    errors.add("Line " + ll.lineSpan() + ": " + err);
            }
        }
        maxNum = lineNum;
        if (errors.size() > 0)
            throw new ValidationException("Failed to parse " + path, errors);
        this.logicalLines = logicals;
    }

    /** Subclasses may override this method to initialize their data model
     * before the parsing actually starts. For example, if each logical line
     * is going to be converted into a "Command", the {@code List<Command>}
     * field in which to store the commands should be initialized from this
     * method.
     * <p>
     * This default implementation does nothing.
     */
    protected void initStorage() { }

    /**
     * This method is invoked once for every logical line in the definition
     * file. Note that comments and blank lines are ignored, and continuation
     * lines are joined into a single logical line. If all is well, the method
     * should return null; if there is a parse error, a short string describing
     * the error should be returned.
     *
     * @param line the line to parse
     * @return null if all is well, a short error description if not
     */
    protected abstract String parseLogicalLine(LogicalLine line);

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        sb.append(getClass().getSimpleName()).append(": path=\"").append(path)
                .append("\",#logical=").append(logicalLines.size())
                .append(",#lines=").append(maxNum)
                .append("}");
        return sb.toString();
    }

    /** Returns a multi-line string representation of the definition file.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString());
        for (LogicalLine ll: logicalLines)
            sb.append(EOL).append(ll);
        sb.append(EOL).append("---");
        return sb.toString();
    }

    /** Returns an unmodifiable view of the logical lines.
     *
     * @return the logical lines
     */
    public List<LogicalLine> getLines() {
        return Collections.unmodifiableList(logicalLines);
    }

    /** Returns a list of strings representing each of the logical lines,
     * used by {@link AbstractDefn#toDebugString()}.
     *
     * @return a list of strings, one per logical line
     */
    public List<String> debugLines() {
        List<String> lines = new ArrayList<String>();
        for (LogicalLine ll: logicalLines)
            lines.add(ll.toString());
        return lines;
    }

    /** Returns the definition file path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }


    // ========================================================
    private static final Pattern RE_BLANK = Pattern.compile("\\s*");
    private static final Pattern RE_COMMENT = Pattern.compile("\\s*#.*");
    private static final Pattern RE_CONT = Pattern.compile("(.*)\\s*\\\\\\s*$");


    /** Represents a single line of text from the definition file. */
    public static class Line {
        private final int lineNum;
        private final String text;
        private final boolean comment;
        private final boolean continued;

        /** Constructs a line.
         *
         * @param lineNum the line number (in the file)
         * @param text the text of the line
         */
        public Line(int lineNum, String text) {
            this.lineNum = lineNum;
            String t = text;
            comment = (RE_BLANK.matcher(text).matches() ||
                        RE_COMMENT.matcher(text).matches());
            Matcher m = RE_CONT.matcher(text);
            if (m.matches()) {
                continued = true;
                t = m.group(1);
            } else {
                continued = false;
            }
            this.text = t;
        }

        @Override
        public String toString() {
            return "[" + lineNum + "] " + text +
                    (comment ? "(#)":"") + (continued ? "(\\)":"");
        }
    }

    /** Represents a logical (non-comment) line from the definition
     * file; possibly an aggregate of several lines.
     */
    public static class LogicalLine {
        private final StringBuilder fullLine = new StringBuilder();
        int first;
        int last;

        /** Constructs a logical line, making the given line the first.
         *
         * @param line the first raw line
         */
        private LogicalLine(Line line) {
            fullLine.append(line.text.trim());
            first = line.lineNum;
            last = line.lineNum;
        }

        /** Adds a line to this logical line.
         *
         * @param line the line to add
         */
        private void addLine(Line line) {
            fullLine.append(" ").append(line.text.trim());
            last = line.lineNum;
        }

        /** Returns the text of the logical line
         * (trimmed of leading/trailing whitespace).
         *
         * @return the text of the logical line
         */
        public String getText() {
            return fullLine.toString().trim();
        }

        /** Returns a string representing the line numbers spanned
         * by this logical line.
         *
         * @return the line span string
         */
        public String lineSpan() {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            if (last != first)
                sb.append("-").append(last);
            return sb.toString();
        }

        @Override
        public String toString() {
            return "[" + lineSpan() + "] " + getText();
        }
    }
}