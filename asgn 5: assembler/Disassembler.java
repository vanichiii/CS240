import java.io.*;
import java.nio.file.*;


public class Disassembler {
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
        String textLength = reader.readLine();
        String dataLength = reader.readLine();

        int textLengthInt = Integer.parseInt(textLength);
        int dataLengthInt = Integer.parseInt(dataLength);
        
        //creates array of bytes for text and data
        byte[] text = new byte[textLengthInt];
        byte[] data = new byte[dataLengthInt];

        for (int i = 0; i < textLengthInt; i++) {
            text[i] = (byte) Integer.parseInt(reader.readLine(), 2);
        }

        for (int i = 0; i < dataLengthInt; i++) {
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

    //ensures big-endian order when converting bytes to int
    static int word(byte[] bytes, int index) {
        return ((bytes[index] & 0xFF) << 24) | 
               ((bytes[index + 1] & 0xFF) << 16) | 
               ((bytes[index + 2] & 0xFF) << 8) | 
               (bytes[index + 3] & 0xFF);
    }
    
    static String decode(int word) {
        int opcode = word >>> 26;

        int rs = (word >>> 21) & 31;
        int rt = (word >>> 16) & 31;
        int rd = (word >>> 11) & 31;
        int funct = word & 63;
        int immediate = (short)(word & 0xFFFF);

        if (opcode == 0) {
            if (funct == 12) return "syscall";
            if (funct == 16) return "mfhi $" + register(rd);
            if (funct == 26)
                return "div $" + register(rs) + ", $" + register(rt);
            if (funct == 33)
                return "addu $" + register(rd) + ", $" +
                    register(rs) + ", $" + register(rt);
            if (funct == 42)
                return "slt $" + register(rd) + ", $" +
                    register(rs) + ", $" + register(rt);
        }

        if (opcode == 4)
            return "beq $" + register(rs) + ", $" +
                register(rt) + ", TARGET";

        if (opcode == 5)
            return "bne $" + register(rs) + ", $" +
                register(rt) + ", TARGET";

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
                register(rs) + ", " + (word & 0xFFFF);

        if (opcode == 15)
            return "lui $" + register(rt) + ", " + (word & 0xFFFF);

        throw new IllegalArgumentException(
            "Unsupported opcode: " + opcode
        );
    }

    public static String disassemble(byte[] text, byte[] data) {
        StringBuilder output = new StringBuilder();
        output.append(".data\n");
        output.append("# Data bytes: ").append(text.length).append("\n");
        for (int i = 0; i < text.length; i += 4) {
            int word = word(text, i);
            String instruction = decode(word);
            int address = 0x00400000 + i; // Assuming text segment starts at 0x00400000
            output.append(String.format("%08X: %s\n", address, instruction));
        }

        return output.toString();
    }

}

