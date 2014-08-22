/*
 * (c) Copyright 2002 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.syntax.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/** 
 * Utility class for justifying columnar text output.
 *
 * @author Steve Britt
 * @author Thomas Vachuska
 */
public class ColumnarTextFormat {

    /** Maximum length allowed for formatted help text output.  */
    public final static int DEFAULT_MAXIMUM_LINE_LENGTH = 79;

    /** Maximum length allowed for formatted help text output.  */
    public final static String DEFAULT_EOL = "\n";

    /**
     * Utility method that formats the provided String according to the
     * assumption that it is composed of one continuous block of text that
     * should be justified, using the default maximum line length.
     * 
     * @param toFormat string to be formatted
     * @param leftSpaces number of spaces on the left indent
     * @return formatted string
     */
    public static String alignBlock(String toFormat, int leftSpaces) {
        return alignBlock(toFormat, leftSpaces, DEFAULT_MAXIMUM_LINE_LENGTH,
                          DEFAULT_EOL);
    }


    /**
     * Utility method that formats the provided String according to the
     * assumption that it is composed of one continuous block of text that
     * should be justified.
     * 
     * @param toFormat string to be formatted
     * @param leftSpaces number of spaces on the left indent
     * @param maximumLineLength max line length before wrapping to next line
     * @param eol end of line string to use for wrapping to next line
     * @return formatted string
     */
    public static String alignBlock(String toFormat, int leftSpaces, 
                                    int maximumLineLength, String eol) {

        List<String> leftColumn = new ArrayList<String>();
        List<String> rightColumn = new ArrayList<String>();
        leftColumn.add("");
        rightColumn.add(toFormat);
        return produceTwoColumns(leftColumn, leftSpaces, rightColumn,
                                 0, ' ', maximumLineLength, eol);
    }


    /**
     * Utility method that formats the provided String according to the
     * assumption that it is composed of newline-delimited lines, and that
     * each line has the space-delimited format "left right". The right value
     * may actually consist of multiple words but the left value must be one
     * word. Assumes the default maximum line length and that the left and
     * right columns will be separated only by spaces.
     * 
     * @param toFormat string to be formatted
     * @param leftSpaces number of spaces on the left indent
     * @param separatingSpaces number of spaces for separating two columns
     * @return formatted string
     */
    public static String alignTwoColumns(String toFormat, int leftSpaces,
                                         int separatingSpaces) {

         return alignTwoColumns(toFormat, leftSpaces, separatingSpaces, ' ');
    }


    /**
     * Utility method that formats the provided String according to the
     * assumption that it is composed of newline-delimited lines, and that
     * each line has the space-delimited format "left right". The right value
     * may actually consist of multiple words but the left value must be one
     * word. Assumes the default maximum line length.
     * 
     * @param toFormat string to be formatted
     * @param leftSpaces number of spaces on the left indent
     * @param separation number of separators for separating two columns
     * @param separator separator character
     * @return formatted string
     */
    public static String alignTwoColumns(String toFormat, int leftSpaces,
                                         int separation, int separator) {
         return alignTwoColumns(toFormat, leftSpaces, separation, separator,
                                DEFAULT_MAXIMUM_LINE_LENGTH);
    }


    /**
     * Utility method that formats the provided String according to the
     * assumption that it is composed of newline-delimited lines, and that
     * each line has the space-delimited format "left right". The right value
     * may actually consist of multiple words but the left value must be one
     * word. This method can be built on top of a more generalized method at
     * some point when there is more time available.
     * 
     * @param toFormat string to be formatted
     * @param leftSpaces number of spaces on the left indent
     * @param separation number of separators for separating two columns
     * @param separator separator character
     * @param maximumLineLength max line length before wrapping to next line
     * @return formatted string
     */
    public static String alignTwoColumns(String toFormat, int leftSpaces,
                                         int separation, int separator,
                                         int maximumLineLength) {
        return alignTwoColumns(toFormat, leftSpaces, separation, separator,
                               maximumLineLength, "\n");
    }
    
    /**
     * Utility method that formats the provided String according to the
     * assumption that it is composed of newline-delimited lines, and that
     * each line has the space-delimited format "left right". The right value
     * may actually consist of multiple words but the left value must be one
     * word. This method can be built on top of a more generalized method at
     * some point when there is more time available.
     * 
     * @param toFormat string to be formatted
     * @param leftSpaces number of spaces on the left indent
     * @param separation number of separators for separating two columns
     * @param separator separator character
     * @param maximumLineLength max line length before wrapping to next line
     * @param eol end of line string to use for wrapping to next line
     * @return formatted string
     */
    public static String alignTwoColumns(String toFormat, int leftSpaces,
                                         int separation, int separator,
                                         int maximumLineLength,
                                         String eol) {
        String result = "";
        if (toFormat != null) {
            // Tokenize each line in the provided text block, splitting each
            // line into a description and explanation.
            String nextLeftToken;
            String nextText;
            StringTokenizer nextLine;
            StringTokenizer original = new StringTokenizer(toFormat, "\n");
            List<String> leftColumn = new ArrayList<String>();
            List<String> rightColumn = new ArrayList<String>();
            while (original.hasMoreTokens()) {
                nextText = original.nextToken();
                nextText = (nextText.replace('\t', ' ')).trim();
                nextText = (nextText.replace('\r', ' ')).trim();
                nextText = (nextText.replace('\f', ' ')).trim();
                nextLine = new StringTokenizer(nextText);
                if (nextLine.countTokens() > 1) {
                    nextLeftToken = nextLine.nextToken().trim();
                    leftColumn.add(nextLeftToken);
                    try {
                        rightColumn.add((nextText.substring(nextText.indexOf
                                         (' ', nextLeftToken.length()))).trim());
                    } catch (IndexOutOfBoundsException iobe) {
                        leftColumn.remove(leftColumn.size() - 1);
                    }
                }
            }
            result = produceTwoColumns
                         (leftColumn, leftSpaces, rightColumn, separation,
                          separator, maximumLineLength, eol);
        }
        return result;
    }

    /**
     * Formats one String combining the contents of the provided Vectors
     * assuming each to be parallel in number of entries. Assumes that the
     * output will be a series of newline-separated lines of this form:
     * <p>
     * left right
     * <p>
     * The left values are extracted from one Vector and the right values from
     * a separate vector, with the number of blank spaces maintained on the
     * left and between columns user-specifiable. The leftmost values are
     * assumed to be relatively short in length. The rightmost values are
     * assumed to be (potentially) long, and will be split into lines. Note
     * that both left and right values may each be multiple words, but the
     * left value will not be split into lines even so. Each right value is
     * split into its individual tokens using a space separator. The longest
     * leftmost value is found, then all left values are justified so they
     * line up and are indented by the specified number of spaces. Then the
     * rightmost values are split into multiple lines by how much space
     * remains on a line and indented appropriately.
     * <p>
     * The separation between columns will actually be a number of the
     * specified separating characters bookended by blank spaces, one on each
     * end. If two or fewer separating characters then only spaces are
     * applied.
     * <p>
     * This method can be built on top of a more generalized method at some
     * point when there is more time available.
     * 
     * @param leftColumn left column values
     * @param leftOffsetSpaces left offset
     * @param rightColumn right column values
     * @param betweenOffset spacing offset
     * @param betweenCharacter spacing character
     * @param maximumLineLength maximum line length
     * @param eol of of line string
     * @return formatted string
     */
    public static String produceTwoColumns(List<String> leftColumn,
                                           int leftOffsetSpaces,
                                           List<String> rightColumn,
                                           int betweenOffset,
                                           int betweenCharacter,
                                           int maximumLineLength,
                                           String eol) {
        // Find the longest entry in the left column.
        int lines = leftColumn.size();
        int longest = 0;
        int nextCount;
        for (int i1 = 0; i1 < lines; i1 ++) {
            nextCount = leftColumn.get(i1).length();
            longest = (nextCount > longest) ? nextCount : longest;
        }

        // Limit the number of spaces that can be prepended to entries in the
        // left column to one quarter of the maximum line length.
        StringBuffer offset = new StringBuffer();
        int maxOffset = maximumLineLength / 4;
        int offsetSpots = (leftOffsetSpaces > 0) ? leftOffsetSpaces : 0;
        offsetSpots = (offsetSpots <= maxOffset) ? offsetSpots : maxOffset;
        for (int i1 = 0; i1 < offsetSpots; i1 ++)
            offset.append(' ');

        // Append enough spaces to each entry in the left column to make its
        // length equal that of the longest entry, and prepend the specified
        // number of spaces to each item in that column.  Limit the number of
        // characters (plus bracketing spaces) that can be appended to entries
        // in the left column, to separate it from the right column, to one
        // quarter of the maximum line length.
        String nextRaw;
        String separator = String.valueOf((char) betweenCharacter);
        StringBuffer nextProcessed;
        offsetSpots = (betweenOffset > 0) ? betweenOffset : 0;
        offsetSpots = (offsetSpots <= maxOffset) ? offsetSpots : maxOffset;
        for (int i1 = 0; i1 < lines; i1 ++) {
            nextRaw = leftColumn.get(i1);
            nextProcessed = new StringBuffer(offset.toString());
            if (nextRaw != null) {
                nextCount = longest - nextRaw.length();
                nextProcessed.append(nextRaw);
            } else
                nextCount = longest;
            if (offsetSpots > 0)
                nextProcessed.append(' ');
            for (int i2 = 0; i2 < nextCount; i2 ++)
                nextProcessed.append(separator);
            leftColumn.set(i1, nextProcessed.toString());
        }
        longest += offset.length();
        offset = new StringBuffer();
        if (offsetSpots > 1) {
            for (int i1 = 2; i1 < offsetSpots; i1 ++)
                offset.append(separator);
            offset.append(' ');
        }

        // Split each entry in the right column into an appropriate number of
        // lines so that each line is indented the length of the longest item
        // from the left column plus its prepended spaces, plus the specified
        // number of separating spaces between the columns.  Note:  the first
        // line will fill the prepended space with the description itself.
        longest += offsetSpots;
        int lastSpace;
        int remainingLength;
        int remainingOnLine = maximumLineLength - longest;
        String remaining;
        StringBuffer precedingSpaces = new StringBuffer();
        for (int i1 = 0; i1 < longest; i1 ++)
            precedingSpaces.append(' ');
        for (int i1 = 0; i1 < lines; i1 ++) {
            nextProcessed = new StringBuffer(offset.toString());
            remaining = rightColumn.get(i1);
            if (remaining == null)
                remaining = "";
            remainingLength = remaining.length();
            while (remainingLength > remainingOnLine) {
                lastSpace = remaining.lastIndexOf(' ', remainingOnLine);
                if (lastSpace != -1)
                    nextRaw = remaining.substring(0, lastSpace);
                else
                    nextRaw = remaining;
                remaining = (remaining.substring(nextRaw.length())).trim();
                remainingLength = remaining.length();
                nextProcessed.append
                    (nextRaw.trim() + eol + precedingSpaces.toString());
            }
            nextProcessed.append(remaining);
            rightColumn.set(i1, nextProcessed.toString());
        }

        // Now form the result from concatenating the individual values from
        // the left and right columns.
        StringBuffer result = new StringBuffer();
        for (int i1 = 0; i1 < lines; i1 ++) {
            if (i1 > 0)
                result.append(eol);
            result.append(leftColumn.get(i1) + rightColumn.get(i1));
        }
        return result.toString();
    }

}
