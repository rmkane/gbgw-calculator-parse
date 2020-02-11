package com.github.rmkane.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String getLastMatch(String input, Pattern pattern, Integer group) {
        Matcher m = pattern.matcher(input);
        String lastMatch = null;
        while (m.find()) {
            lastMatch = group != null ? m.group(group) : m.group();
        }
        return lastMatch;
    }

    public static String replaceAll(String input, Pattern pattern, Function<Matcher, String> worker) {
        if (pattern == null) {
            return input;
        }
        Matcher matcher = pattern.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String result = worker != null ? worker.apply(matcher) : matcher.group();
            matcher.appendReplacement(buffer, result != null ? result : "");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String substringAfter(String value, char character, int nthAppearance) {
        int index = nthIndex(value, character, nthAppearance);
        return index == 0 ? value : index < value.length() ? value.substring(index + 1) : value;
    }

    public static int nthIndex(String value, char character, int nthAppearance) {
        int index = 0;
        if (nthAppearance == 0) {
            return index;
        }
        while (nthAppearance-- > 0) {
            index = value.indexOf(character, index + 1);
        }
        return index;
    }

    public static String stripQuotes(String input) {
        int len = input.length();
        if (len >= 2 && input.charAt(0) == '\'' && input.charAt(len - 1) == '\'') {
            return input.substring(1, len - 1);
        }
        if (len >= 2 && input.charAt(0) == '"' && input.charAt(len - 1) == '"') {
            return input.substring(1, len - 1);
        }
        return input;
    }
}
