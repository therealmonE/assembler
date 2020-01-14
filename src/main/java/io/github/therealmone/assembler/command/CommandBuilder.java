package io.github.therealmone.assembler.command;

import static io.github.therealmone.cpuemulator.Config.DESTINATION_SHIFT;
import static io.github.therealmone.cpuemulator.Config.LITERAL_SHIFT;
import static io.github.therealmone.cpuemulator.Config.OP1_SHIFT;
import static io.github.therealmone.cpuemulator.Config.OP2_SHIFT;
import static io.github.therealmone.cpuemulator.Config.TYPE_SHIFT;

public class CommandBuilder {

    private int header;
    private int literal;
    private int destination;
    private int op1;
    private int op2;

    private CommandBuilder() {
    }

    public static CommandBuilder newInstance() {
        return new CommandBuilder();
    }

    public CommandBuilder header(int header) {
        this.header = header;
        return this;
    }

    public CommandBuilder literal(int literal) {
        this.literal = literal;
        return this;
    }

    public CommandBuilder destination(int destination) {
        this.destination = destination;
        return this;
    }

    public CommandBuilder op1(int op1) {
        this.op1 = op1;
        return this;
    }

    public CommandBuilder op2(int op2) {
        this.op2 = op2;
        return this;
    }

    public int build() {
        return (header << TYPE_SHIFT)
                | (literal << LITERAL_SHIFT)
                | (destination << DESTINATION_SHIFT)
                | (op1 << OP1_SHIFT)
                | (op2 << OP2_SHIFT);
    }

}
