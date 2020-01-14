package io.github.therealmone.assembler.parser;

import io.github.tdf4j.parser.Parser;
import io.github.therealmone.cpuemulator.memory.CommandMemory;

public interface AssemblerParser extends Parser {

    CommandMemory loadMemory(CharSequence assembler);

}
