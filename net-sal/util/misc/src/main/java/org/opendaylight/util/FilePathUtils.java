/*
 * (c) Copyright 2008 Hewlett-Packard Co., All Rights Reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;


/**
 * Set of static utilities for file path manipulation.
 *
 * @author Thomas Vachuska
 */
public final class FilePathUtils {
    
    /**
     * Get the path to the system temporary directory.
     * 
     * @return temporary files directory path
     */
    public static String tmp() {
        return System.getProperty("java.io.tmpdir");
    }
    
    /**
     * Return the specified path with all backward slash separators replaced by
     * forward slash separators. In case there are no backward slashes and hence
     * no replacements are needed, the original path will be returned.
     * 
     * @param path path to be normalized
     * @param isDir boolean indicating that the path is that of a directory
     * @return original path with all backward slash separators replaced by
     *         forward slash separators, or if the original path if no
     *         replacements were necessary
     */
    public static String normalizedPath(String path, boolean isDir) {
        String np = path.indexOf('\\') < 0 ? path : path.replace('\\', '/');
        if (isDir)
            np = addSeparator(np, '/');
        return np;
    }

    /**
     * Return the specified path with all backward slash separators replaced by
     * forward slash separators. In case there are no backward slashes and hence
     * no replacements are needed, the original path will be returned.
     * 
     * @param path path to be normalized
     * @return original path with all backward slash separators replaced by
     *         forward slash separators, or if the original path if no
     *         replacements were necessary
     */
    public static String normalizedPath(String path) {
        return normalizedPath(path, false);
    }

    /**
     * Return the parent path of the specified path.
     * 
     * @param path path from which to extract the parent path
     * @param fps character representing the file path separator
     * @return path shortened by one path segment
     */
    public static String parentPath(String path, char fps) {
        // Remember whether the path ended with a trailing file path separator.
        int l = path.length();
        boolean endsWithFPS = path.charAt(l - 1) == fps;
        int o = endsWithFPS ? l - 2 : l - 1;
        
        // Find the parent file path separator while making sure we ignore the 
        // trailing one.
        int i = path.lastIndexOf(fps, o);
        
        // Return the parent path, including the trailing file path separator,
        // unless we were given the top-most path already, in which case do
        // strip off the trailing file path separator.
        return i >= 0 ? path.substring(0, i + 1) : 
            (endsWithFPS ? path.substring(0, l - 1) : path);
    }

    /**
     * Return the relative path using the supplied parent path.
     * 
     * @param parentPath parent path to which the path is relative; this path
     *        is assumed to be normalized
     * @param path normalized path to be made relative
     * @param fps character representing the file path separator
     * @return relative path to the parent path, sans the leading path
     *         separator
     */
    public static String relativePath(String parentPath, String path, char fps) {
        // Verify that the path starts with parent path.
        if (path.startsWith(parentPath))
            return path.substring(parentPath.length()); 
        throw new IllegalArgumentException(path + " is not relative to " + parentPath);
    }
    
    /**
     * Return the leaf name (aka basename) segment of the path.
     * 
     * @param path path from which to extract the leaf name
     * @param fps character representing the file path separator
     * @return leaf name of the path, including the trailing path separator,
     *         if there is one
     */
    public static String leafName(String path, char fps) {
        // Remember whether the path ended with a trailing file path separator.
        int l = path.length();
        if (l == 0)
            return path;
        
        boolean endsWithFPS = path.charAt(l - 1) == fps;
        int o = endsWithFPS ? l - 2 : l - 1;

        // Find the parent file path separator while making sure we ignore the 
        // trailing one.
        int i = path.lastIndexOf(fps, o);
        
        // Return the leaf path segment, including the trailing file path
        // separator.
        return i >= 0 ? path.substring(i + 1) : path; 
    }
    
    /**
     * Return the next child path segment given the model path and base parent
     * path.
     * 
     * @param parentPath parent path to use as a base
     * @param path model path from which to extract the next child using the
     *        parent path
     * @param fps character representing the file path separator
     * @return child path segment relative to the given parent path
     */
    public static String childPath(String parentPath, String path, char fps) {
        int o = parentPath.length();
        int i = path.indexOf(fps, o);
        if (i == o)
            // In case the original path ends with a /, move down by one more.
            i = path.indexOf(fps, ++o);
        return i >= 0 ? path.substring(o, i) : path.substring(o);
    }
    
    
    /**
     * Return the path with the specified file separator added at the end, or
     * the original path if it already has a trailing path separator. 
     * 
     * @param path path to which the separator should be added
     * @param fps file path separator to use if needed, which is if the parent
     *        path already does not end with a trailing path separator
     * @return path with trailing path separator
     */
    public static String addSeparator(String path, char fps) {
        int l = path.length();
        if (l == 0)
            return "" + fps;
        return path.charAt(l - 1) == fps ? path : path + fps;
    }
    
    /**
     * Return the path with the specified file separator trimmed from the end,
     * or the original path if it already lacks a trailing path separator.
     * 
     * @param path path from which the separator should be trimed
     * @param fps file path separator to use if needed, which is if the path
     *        ends with a trailing path separator
     * @return path without trailing path separator
     */
    public static String trimSeparator(String path, char fps) {
        int l = path.length();
        if (l <= 1)
            return path;
        return path.charAt(l - 1) == fps ? path.substring(0, l - 1) : path;
    }
    
    /**
     * Return the path that results when the child name is catenated to the
     * parent path and separated using the given file path separator.
     * 
     * @param path parent path to use as the first part of the result
     * @param name child name to append to the parent path
     * @param fps file path separator to use if needed, which is if the path
     *        already does not end with a trailing path separator
     * @return combined path
     */
    public static String combinePath(String path, String name, 
                                      char fps) {
        int l = path.length();
        return l == 0 ? name : (path.charAt(l - 1) == fps ? 
                                    path + name : path + fps + name);
    }
    
    /**
     * Return the mount point portion of the specified path, i.e. the portion
     * of path up to and including the first file path separator.
     * 
     * @param path file path from which to extract the mount-point
     * @param fps file path separator to use for extraction of mount point
     * @return mount point segment of the given path
     */
    public static String mountPoint(String path, char fps) {
        int i = path.indexOf(fps);
        return i >= 0 ? path.substring(0, i + 1) : path;
    }
    
    /**
     * Return the full path stripped of the mount-point drive letter.
     * 
     * @param path path from which to strip-off drive letter portion.
     * @return path with drive letter stripped off
     */
    public static String fullPath(String path) {
        int i = path.indexOf(':');
        return i == 1 ? path.substring(2) : path;
    }
    
    /**
     * Return the drive designation extracted from the specified path.
     * 
     * @param path path from which to extract drive information
     * @return drive designation
     */
    public static String drive(String path) {
        int i = path.indexOf(':');
        return i == 1 ? path.substring(0, i + 1) : path;
    }

    /**
     * Return the path sans the extension of the given file leaf name, i.e.
     * the portion following the last occurrence of the extension separator
     * '.'.
     * <p>
     * Assumes a leaf file name is supplied. Returns an empty string if there
     * is no extension found.
     * 
     * @param name file name from which to extract the extension
     * @param fps file path separator
     * @return extension portion of the file name or empty string if none
     */
    public static String sansExtension(String name, char fps) {
        int i = name.lastIndexOf('.');
        int s = name.lastIndexOf(fps);
        return i >= 0 && s < i ? name.substring(0, i) : name;
    }


    /**
     * Return the extension of the given file name, i.e. the portion
     * following the last occurrence of the extension separator '.'.
     * <p>
     * Returns an empty string if there is no extension found.
     * 
     * @param name file name from which to extract the extension
     * @param fps file path separator
     * @return extension portion of the file name or empty string if none
     */
    public static String extension(String name, char fps) {
        int i = name.lastIndexOf('.');
        int s = name.lastIndexOf(fps);
        return i >= 0 && s < i ? name.substring(i + 1) : "";
    }

    /**
     * Return the extension of the given file leaf name, i.e. the portion
     * following the last occurrence of the extension separator '.'.
     * <p>
     * Assumes a leaf file name is supplied.
     * Returns an empty string if there is no extension found.
     * 
     * @param name file name from which to extract the extension
     * @return extension portion of the file name or empty string if none
     */
    public static String extension(String name) {
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i + 1) : "";
    }

    /**
     * Returns the name of the file without the extension
     * @param path  full name of the file or path
     * @param fps   file path separator
     * @return the given file name/path without the extension
     */
    public static String prefixFilename(String path, char fps) {
        int i = path.lastIndexOf(fps);
        int j = path.lastIndexOf('.');
        return j > 0 ? path.substring(i+1, j) : path.substring(i+1);
    }

    /**
     * Returns the full name of the file only, without the directory path
     * information
     * 
     * @param path full name of the file or path
     * @param fps file path separator
     * @return the leaf file name, without the directory path
     */
    public static String filename(String path, char fps) {
        int i = path.lastIndexOf(fps);
        return path.substring(i + 1);
        // return path.substring(path.lastIndexOf(fps) + 1);
    }
    
}
