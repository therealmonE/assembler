package io.github.therealmone.assembler;

import io.github.therealmone.assembler.parser.AssemblerParser;
import io.github.therealmone.assembler.parser.AssemblerParserGenerator;
import io.github.therealmone.assembler.utils.IOUtils;
import io.github.therealmone.cpuemulator.CPU;
import io.github.therealmone.cpuemulator.memory.CommandMemory;
import io.github.therealmone.cpuemulator.memory.DataLine;
import io.github.therealmone.cpuemulator.memory.DataMemory;
import io.github.therealmone.cpuemulator.memory.Register;


public class Main {

    public static void main(String[] args) {
        final AssemblerParser assemblerParser = AssemblerParserGenerator.newInstance();
        final CommandMemory commandMemory = assemblerParser.loadMemory(IOUtils.loadResource("prog.txt"));
        final CPU cpu = new CPU(commandMemory);

        run(cpu);
        log(cpu);
    }

    private static void run(CPU cpu) {
        while (!cpu.isDone()) {
            cpu.processNextCommand();
        }
    }

    private static void log(CPU cpu) {
        logRegisters(cpu.getRegisters());
        System.out.println("\n");
        logDataMemory(cpu.getDataMemory());
    }

    private static void logDataMemory(DataMemory dataMemory) {
        for (int i = 0; i < dataMemory.size(); i++) {
            final DataLine dataLine = dataMemory.get(i);
            System.out.println(
                    String.format("%s : %s (%s)",
                            Integer.toHexString(i),
                            Integer.toHexString(dataLine.getBits()),
                            dataLine.getBits()));
        }
    }

    private static void logRegisters(Register[] registers) {
        for (int i = 0; i < registers.length; i++) {
            final Register register = registers[i];
            System.out.println(
                    String.format("Reg[%s]: %s", i, Integer.toHexString(register.getValue())));
        }
    }
    
}
