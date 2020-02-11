package com.github.rmkane.parser;

import com.github.rmkane.utils.FileUtils;
import com.github.rmkane.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ScriptParser {
    protected String outputFilename; // required
    protected int linesToSkip;       // optional (default = 0)
    private String originalText;     // internal
    private List<String> wordList;   // internal
    private int wordListOffset;      // internal

    /**
     * Use {@link com.github.rmkane.parser.ScriptParser.ScriptParserBuilder#build(Class)} instead.
     */
    private ScriptParser() { }

    public String getOutputFilename() {
        return outputFilename;
    }

    public int getLinesToSkip() {
        return linesToSkip;
    }

    public void parse(String inputFilename) throws IOException {
        InputStream is = ScriptParser.class.getResourceAsStream('/' + inputFilename);
        Scanner scan = new Scanner(is).useDelimiter("\\A");

        this.originalText = scan.next();
        this.wordList = extractWords(this.originalText);
        this.wordListOffset = calculateOffset(this.originalText);

        scan.close();
        FileUtils.writeFile(getOutputFilename(), transform());
    }

    private String transform() {
        return StringUtils.substringAfter(replaceHexValues(squareToDotNotation(replaceStrings(originalText, wordList, wordListOffset), 2)), '\n', getLinesToSkip());
    }

    protected int locatePhysicalIndex(String input) {
        Pattern p = Pattern.compile("console\\[_0x[a-f0-9]+\\('0x[a-f0-9]+'\\)\\]\\(_0x[a-f0-9]+\\('0x([a-f0-9]+)'\\)\\);", Pattern.CASE_INSENSITIVE);
        String lastMatch = StringUtils.getLastMatch(input, p, 1);
        return Integer.parseInt(lastMatch, 16);
    }

    protected int calculateOffset(String input) {
        int expectedIndex = wordList.indexOf("Not\\x20Allowed!!!");
        return Math.abs(expectedIndex - locatePhysicalIndex(input));
    }

    protected List<String> extractWords(String input) {
        String str = input.substring(input.indexOf("['") + 1, input.indexOf("'];")).replaceAll("\\s+", " ");
        return Arrays.asList(str.split("'\\s*,\\s*'"));
    }

    private static String replaceHexValues(String input) {
        Pattern pattern = Pattern.compile("\\b0x(?<hex>[a-f0-9]+)\\b", Pattern.CASE_INSENSITIVE);
        return StringUtils.replaceAll(input, pattern, (matcher) -> Long.toString(Long.parseLong(matcher.group("hex"), 16)));
    }

    private static String squareToDotNotation(String input) {
        Pattern pattern = Pattern.compile("(?<!:)(.)\\[[\"'](\\w+)[\"']\\]", Pattern.CASE_INSENSITIVE);
        return StringUtils.replaceAll(input, pattern, (matcher) -> {
            String precedingChar = matcher.group(1);
            String match = matcher.group(2);
            if (precedingChar.equals("\t")) {
                return precedingChar + match;
            } else {
                return precedingChar + "." + match;
            }
        });
    }

    private static String squareToDotNotation(String input, int cycles) {
        for (int i = 0; i < cycles; i++) {
            input = squareToDotNotation(input);
        }
        return input;
    }

    private static String replaceStrings(String input, List<String> words, int offset) {
        Pattern pattern = Pattern.compile("_0x2d2a\\('0x(?<hex>[a-f0-9]+)'\\)", Pattern.CASE_INSENSITIVE);
        return StringUtils.replaceAll(input, pattern, (matcher) -> {
            String hexMatch = matcher.group("hex");
            int stringIndex = Integer.parseInt(hexMatch, 16);
            int finalIndex = (stringIndex + offset) % words.size();
            String lookup = words.get(finalIndex);
            return "\"" + lookup + "\"";
        });
    }

    public static class ScriptParserBuilder<T extends ScriptParser> {
        private String outputFilename; // required
        private int linesToSkip;       // optional (default = 0)

        public ScriptParserBuilder(String outputFilename) {
            this.outputFilename = outputFilename;
            this.linesToSkip = 0;
        }

        public ScriptParserBuilder withLinesToSkip(int linesToSkip) {
            this.linesToSkip = linesToSkip;
            return this;
        }

        public T build(Class<T> clazz) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T parser = constructor.newInstance();

            parser.outputFilename = this.outputFilename;
            parser.linesToSkip = this.linesToSkip;

            return parser;
        }
    }
}
