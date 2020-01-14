package io.github.therealmone.assembler.parser;

import io.github.tdf4j.parser.*;
import io.github.tdf4j.lexer.*;
import io.github.tdf4j.core.module.*;
import io.github.tdf4j.core.model.*;
import io.github.tdf4j.core.model.ast.*;
import io.github.tdf4j.core.utils.*;

import java.util.*;
import java.util.function.*;

import io.github.tdf4j.core.model.ebnf.*;

import static io.github.tdf4j.core.model.ebnf.EBNFBuilder.*;
import static io.github.tdf4j.core.model.ast.ASTCursor.Movement.*;

import io.github.therealmone.assembler.parser.AssemblerParser;
import io.github.therealmone.cpuemulator.Config;
import io.github.therealmone.cpuemulator.memory.CommandMemory;
import io.github.therealmone.assembler.command.CommandBuilder;

public class AssemblerParserImpl extends AbstractParser implements AssemblerParser {

    //Terminals
    private final Terminal EOF = terminal("EOF");
    private final Terminal MOV_KW = terminal("MOV_KW");
    private final Terminal MOVR_KW = terminal("MOVR_KW");
    private final Terminal ADD_KW = terminal("ADD_KW");
    private final Terminal GOTO_KW = terminal("GOTO_KW");
    private final Terminal EQ_KW = terminal("EQ_KW");
    private final Terminal SUB_KW = terminal("SUB_KW");
    private final Terminal WRT_KW = terminal("WRT_KW");
    private final Terminal REG_KW = terminal("REG_KW");
    private final Terminal LEFT_SQUARE_BRACKET = terminal("LEFT_SQUARE_BRACKET");
    private final Terminal RIGHT_SQUARE_BRACKET = terminal("RIGHT_SQUARE_BRACKET");
    private final Terminal INTEGER = terminal("INTEGER");
    private final Terminal HEX_VALUE = terminal("HEX_VALUE");
    private final Terminal WS = terminal("WS");

    //NonTerminals
    private final CallableNonTerminal goto_command = callableNonTerminal("goto_command", this::goto_command);
    private final CallableNonTerminal add_command = callableNonTerminal("add_command", this::add_command);
    private final CallableNonTerminal mov_command = callableNonTerminal("mov_command", this::mov_command);
    private final CallableNonTerminal sub_command = callableNonTerminal("sub_command", this::sub_command);
    private final CallableNonTerminal lang = callableNonTerminal("lang", this::lang);
    private final CallableNonTerminal eq_command = callableNonTerminal("eq_command", this::eq_command);
    private final CallableNonTerminal movr_command = callableNonTerminal("movr_command", this::movr_command);
    private final CallableNonTerminal command = callableNonTerminal("command", this::command);
    private final CallableNonTerminal wrt_command = callableNonTerminal("wrt_command", this::wrt_command);
    private final CallableNonTerminal register = callableNonTerminal("register", this::register);

    private final Lexer lexer = Lexer.get(new LexerAbstractModule() {
        @Override
        public void configure() {
            tokenize(EOF).pattern("\\$").priority(0).hidden(false);
            tokenize(MOV_KW).pattern("MOV").priority(0).hidden(false);
            tokenize(MOVR_KW).pattern("MOVR").priority(0).hidden(false);
            tokenize(ADD_KW).pattern("ADD").priority(0).hidden(false);
            tokenize(GOTO_KW).pattern("GOTO").priority(0).hidden(false);
            tokenize(EQ_KW).pattern("EQ").priority(0).hidden(false);
            tokenize(SUB_KW).pattern("SUB").priority(0).hidden(false);
            tokenize(WRT_KW).pattern("WRT").priority(0).hidden(false);
            tokenize(REG_KW).pattern("REG").priority(0).hidden(false);
            tokenize(LEFT_SQUARE_BRACKET).pattern("\\[").priority(0).hidden(false);
            tokenize(RIGHT_SQUARE_BRACKET).pattern("\\]").priority(0).hidden(false);
            tokenize(INTEGER).pattern("0|([1-9][0-9]*)").priority(0).hidden(false);
            tokenize(HEX_VALUE).pattern("0x[0-9,A-F,a-f]{8}").priority(0).hidden(false);
            tokenize(WS).pattern("\\s|\\n|\\r").priority(0).hidden(true);
        }
    });


    public AssemblerParserImpl(
            final Grammar grammar
    ) {
        super(new Predictor(grammar.getFirstSet(), grammar.getFollowSet()));
    }


    private int[] cmem = new int[Config.C_MEM_SIZE];
    private int pc = 0;

    @Override
    public CommandMemory loadMemory(CharSequence assembler) {
        parse(assembler);
        final CommandMemory memory = new CommandMemory();
        memory.load(cmem);
        return memory;
    }

    private int getRegisterNumber(ASTNode register) {
        return Integer.parseInt(register.getChildren().get(2).asLeaf().getToken().getValue());
    }


    @Override
    public AST parse(final CharSequence input) {
        this.stream = new BufferedStream<>(lexer.analyze(input));
        this.ast = AST.create(lang);
        lang.call();
        return ast;
    }

    private void goto_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(3);
        match(GOTO_KW);
        match(INTEGER, token -> {
            builder.literal(Integer.parseInt(token.getValue()));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void add_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(2);
        match(ADD_KW);
        call(register, node -> {
            builder.op1(getRegisterNumber(node));
        });
        call(register, node -> {
            builder.op2(getRegisterNumber(node));
        });
        call(register, node -> {
            builder.destination(getRegisterNumber(node));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void mov_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(1);
        match(MOV_KW);
        call(register, node -> {
            builder.destination(getRegisterNumber(node));
        });
        match(HEX_VALUE, token -> {
            builder.literal(Integer.decode(token.getValue()));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void sub_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(4);
        match(SUB_KW);
        call(register, node -> {
            builder.op1(getRegisterNumber(node));
        });
        call(register, node -> {
            builder.op2(getRegisterNumber(node));
        });
        call(register, node -> {
            builder.destination(getRegisterNumber(node));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void lang() {
        while (true) {
            if (canReach(command)) {
                call(command);
            } else {
                break;
            }
        }
        match(EOF);
    }

    private void eq_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(5);
        match(EQ_KW);
        call(register, node -> {
            builder.op1(getRegisterNumber(node));
        });
        call(register, node -> {
            builder.op2(getRegisterNumber(node));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void movr_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(7);
        match(MOVR_KW);
        call(register, node -> {
            builder.destination(getRegisterNumber(node));
        });
        call(register, node -> {
            builder.op1(getRegisterNumber(node));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void command() {
        switch (predict(
                new Alt(0, "mov_command"),
                new Alt(1, "add_command"),
                new Alt(2, "goto_command"),
                new Alt(3, "eq_command"),
                new Alt(4, "sub_command"),
                new Alt(5, "wrt_command"),
                new Alt(6, "movr_command"))
        ) {
            case 0: {
                call(mov_command);
                break;
            }
            case 1: {
                call(add_command);
                break;
            }
            case 2: {
                call(goto_command);
                break;
            }
            case 3: {
                call(eq_command);
                break;
            }
            case 4: {
                call(sub_command);
                break;
            }
            case 5: {
                call(wrt_command);
                break;
            }
            case 6: {
                call(movr_command);
                break;
            }
        }
    }

    private void wrt_command() {
        CommandBuilder builder = CommandBuilder.newInstance().header(6);
        match(WRT_KW);
        call(register, node -> {
            builder.op1(getRegisterNumber(node));
        });
        match(HEX_VALUE, token -> {
            builder.literal(Integer.decode(token.getValue()));
        });

        this.cmem[pc] = builder.build();
        pc++;

    }

    private void register() {
        match(REG_KW);
        match(LEFT_SQUARE_BRACKET);
        match(INTEGER);
        match(RIGHT_SQUARE_BRACKET);
    }


}