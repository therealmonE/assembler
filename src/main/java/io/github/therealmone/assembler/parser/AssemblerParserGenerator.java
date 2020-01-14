package io.github.therealmone.assembler.parser;

import io.github.tdf4j.core.model.Grammar;
import io.github.tdf4j.generator.Options;
import io.github.tdf4j.generator.impl.ParserGenerator;
import io.github.tdf4j.parser.ParserMetaInformation;
import io.github.tdf4j.tdfparser.impl.TdfInterpreter;
import io.github.therealmone.assembler.utils.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public final class AssemblerParserGenerator {

    private static final String CLASS_NAME = "AssemblerParserImpl";
    private static final String PACKAGE = "io.github.therealmone.assembler.parser";
    private static final String GRAMMAR = "grammar.tdf";

    public static void main(String[] args) {
        final File file = new File(getFilePath());
        if (file.exists()) {
            file.delete();
        }
        write(file, generateParser(GRAMMAR).getSourceCode());
    }

    private static void write(File file, String content) {
        try (final FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getFilePath() {
        return "src/main/java/"
                + PACKAGE.replaceAll("\\.", "/")
                + "/"
                + CLASS_NAME
                + ".java";
    }

    private static ParserMetaInformation generateParser(String grammarResource) {
        final TdfInterpreter interpreter = new TdfInterpreter();
        interpreter.parse(IOUtils.loadResource("grammar.tdf"));

        return new ParserGenerator(new Options.Builder()
                .setParserModule(interpreter.getParserModule())
                .setLexerModule(interpreter.getLexerModule())
                .setInterface(AssemblerParser.class)
                .setClassName(CLASS_NAME)
                .setPackage(PACKAGE)
                .build())
                .generate();
    }

    public static AssemblerParser newInstance() {
        final Grammar grammar = generateParser(GRAMMAR).getGrammar();
        return new AssemblerParserImpl(grammar);
    }

}
