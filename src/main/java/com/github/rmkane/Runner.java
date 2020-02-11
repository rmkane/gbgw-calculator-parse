package com.github.rmkane;

import com.github.rmkane.parser.ScriptParser;

import java.io.IOException;

public class Runner implements Runnable {
    private static final String INPUT_FILENAME = "script-20200209.txt";
    private static final String OUTPUT_FILENAME = "out/script.js";
    private static final int LINES_TO_SKIP = 14;

    public static void main(String[] args) {
        new Runner().run();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        ScriptParser parser = null;

        try {
            parser = new ScriptParser.ScriptParserBuilder(OUTPUT_FILENAME)
                    .withLinesToSkip(LINES_TO_SKIP)
                    .build(ScriptParser.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (parser == null) {
            throw new NullPointerException("The parser was not initialized properly");
        }

        try {
            parser.parse(INPUT_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}