import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Disassembler {
   static final int TEXT_BASE = 0x00400000;
   static final int DATA_BASE = 0x10010000;
   static final boolean RAW_CHARACTER_VIEW = false;

   private static final String[] REGISTER_NAMES = {
        "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
        "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
        "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
        "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"
    };

    static class Instruction {
        final int line, address;
        final String text, sourceText;
        Instruction(int line, int address, String text, String sourceText) {
            this.line = line;
            this.address = address;
            this.text = text;
            this.sourceText = sourceText;
        }
    }

    static class EncodedInstruction {
        final Instruction source;
        final int word;
        final String mips;
        final Map<String, Integer> labelLines;
        EncodedInstruction(Instruction source, int word, String mips,
                           Map<String, Integer> labelLines) {
            this.source = source;
            this.word = word;
            this.mips = mips;
            this.labelLines = labelLines;
        }
    }

    static class Result {
        final byte[] text, data;
        final List<EncodedInstruction> instructions;
        Result(byte[] text, byte[] data, List<EncodedInstruction> instructions) {
            this.text = text;
            this.data = data;
            this.instructions = instructions;
        }
    }

    public Disassembler() {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Disassembler <input file> <output file>");
            System.exit(1);
        }

        String inputFile = args[0];
        String outputFile = args[1];

        Result result = Assembler.readBinaryFile(inputFile);
        writeTextFile(result, outputFile);
    }

    private static void writeTextFile(Result result, String filename) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(filename));
    
    }

    
    
    }
}
