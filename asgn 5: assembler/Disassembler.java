import java.io.*;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;


public class Disassembler {
    static final int TEXT_BASE = 0x00400000;

   static class MachineCode {
    byte[] text, data;
    MachineCode(byte[] text, byte[] data) {
        this.text = text;
        this.data = data;
   }
}
    private static final String[] REGISTER_NAMES = {
        "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
        "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
        "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
        "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"
    };

    static String register(int number) {
    if (number < 0 || number >= REGISTER_NAMES.length) {
        throw new IllegalArgumentException(
            "Invalid register number: " + number
        );
    }

    return REGISTER_NAMES[number];
}
   public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java Disassembler <input file> <output file>");
            System.exit(1);
        }
       try { 
        MachineCode mc;

        if (args[0].endsWith(".mhc-ascii")) {
            mc = readAsciiFile(args[0]);
        } else if (args[0].endsWith(".mhc-bin")) {
            mc = readBinaryFile(args[0]);
        } else {
            throw (new IllegalArgumentException("Input file must be either .mhc-ascii or .mhc-bin"));
        }

        String disassembled = disassemble(mc.text, mc.data);
        Files.write(Paths.get(args[1]), disassembled.getBytes());

    } catch (IOException e) {
            System.err.println("Error reading or writing files: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    static MachineCode readAsciiFile(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
       //read the headeer for text and data seperately
       String textHeader = reader.readLine();
       String dataHeader = reader.readLine();

       //error for not finding the correct headers
       if (!textHeader.startsWith("TEXT ") || !dataHeader.startsWith("DATA ")) {
           throw new IOException("Invalid headers, found: " + textHeader + ", " + dataHeader);
       }

        int textLength = Integer.parseInt(textHeader.substring(5).trim());
        int dataLength = Integer.parseInt(dataHeader.substring(5).trim());
        
        //creates array of bytes for text and data
        byte[] text = new byte[textLength];
        byte[] data = new byte[dataLength];

        for (int i = 0; i < textLength; i++) {
            text[i] = (byte) Integer.parseInt(reader.readLine(), 2);
        }

        for (int i = 0; i < dataLength; i++) {
            data[i] = (byte) Integer.parseInt(reader.readLine(), 2);
        }
        return new MachineCode(text, data);
        }
    }

    static MachineCode readBinaryFile(String filename) throws IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            int textLength = dis.readInt();
            int dataLength = dis.readInt();

            byte[] text = new byte[textLength];
            byte[] data = new byte[dataLength];

            dis.readFully(text);
            dis.readFully(data);

            return new MachineCode(text, data);
        }
    }
    
    static String labelName(int address) {
        return String.format("L%08X", address);
    }





    //ensures big-endian order when converting bytes to int
    static int word(byte[] bytes, int index) {
        return ((bytes[index] & 0xFF) << 24) | 
               ((bytes[index + 1] & 0xFF) << 16) | 
               ((bytes[index + 2] & 0xFF) << 8) | 
               (bytes[index + 3] & 0xFF);
    }
    
    static String decode(int instruction, int pc) {
    int opcode = instruction >>> 26;

    int rs = (instruction >>> 21) & 31;
    int rt = (instruction >>> 16) & 31;
    int rd = (instruction >>> 11) & 31;
    int shamt = (instruction >>> 6) & 31;
    int funct = instruction & 63;
    int immediate = (short)(instruction & 0xFFFF);

    if (opcode == 0) {
        if (funct == 0)
            return "sll $" + register(rd) + ", $" +
                   register(rt) + ", " + shamt;

        if (funct == 8)
            return "jr $" + register(rs);

        if (funct == 12)
            return "syscall";

        if (funct == 16)
            return "mfhi $" + register(rd);

        if (funct == 26)
            return "div $" + register(rs) + ", $" + register(rt);

        if (funct == 33)
            return "addu $" + register(rd) + ", $" +
                   register(rs) + ", $" + register(rt);

        if (funct == 42)
            return "slt $" + register(rd) + ", $" +
                   register(rs) + ", $" + register(rt);
    }

    if (opcode == 2 || opcode == 3) {
        int target = ((pc + 4) & 0xF0000000)
                   | ((instruction & 0x03FFFFFF) << 2);

        return (opcode == 2 ? "j " : "jal ")
               + labelName(target);
    }

    if (opcode == 4 || opcode == 5) {
        int offset = (short)(instruction & 0xFFFF);
        int target = pc + 4 + offset * 4;

        return (opcode == 4 ? "beq " : "bne ")
               + "$" + register(rs) + ", $"
               + register(rt) + ", "
               + labelName(target);
    }

    if (opcode == 8)
        return "addi $" + register(rt) + ", $" +
               register(rs) + ", " + immediate;

    if (opcode == 9)
        return "addiu $" + register(rt) + ", $" +
               register(rs) + ", " + immediate;

    if (opcode == 10)
        return "slti $" + register(rt) + ", $" +
               register(rs) + ", " + immediate;

    if (opcode == 13)
        return "ori $" + register(rt) + ", $" +
               register(rs) + ", " + (instruction & 0xFFFF);

    if (opcode == 15)
        return "lui $" + register(rt) + ", " +
               (instruction & 0xFFFF);

    if (opcode == 35)
        return "lw $" + register(rt) + ", " +
               immediate + "($" + register(rs) + ")";

    if (opcode == 43)
        return "sw $" + register(rt) + ", " +
               immediate + "($" + register(rs) + ")";

    throw new IllegalArgumentException(
        "Unsupported opcode: " + opcode
    );
    }

    static Set<Integer> findTargets(byte[] text) {
    Set<Integer> targets = new HashSet<>();

    for (int i = 0; i < text.length; i += 4) {
        int instruction = word(text, i);
        int opcode = instruction >>> 26;
        int pc = TEXT_BASE + i;

        if (opcode == 2 || opcode == 3) {
            int target = ((pc + 4) & 0xF0000000)
                       | ((instruction & 0x03FFFFFF) << 2);

            targets.add(target);
        }

        if (opcode == 4 || opcode == 5) {
            int offset = (short)(instruction & 0xFFFF);
            int target = pc + 4 + offset * 4;

            targets.add(target);
        }
    }

    return targets;
}

   static String disassemble(byte[] text, byte[] data) {
    Set<Integer> targets = findTargets(text);
    StringBuilder output = new StringBuilder();

   output.append("\n.text\n");
   output.append(".globl main\n");
   output.append("main:\n");

    if (data.length > 0) {
        output.append("data_bytes: .byte ");

        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                output.append(", ");
            }

            output.append(data[i] & 0xFF);
        }

        output.append("\n");
    }

    output.append("\n.text\n");

    for (int i = 0; i < text.length; i += 4) {
        int pc = TEXT_BASE + i;
        int instruction = word(text, i);

        if (targets.contains(pc)) {
            output.append(labelName(pc)).append(":\n");
        }

        output.append("    ")
              .append(decode(instruction, pc))
              .append("\n");
    }

    return output.toString();
}

}

