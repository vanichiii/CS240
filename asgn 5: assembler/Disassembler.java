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

        byte[] text = new byte[textLengthInt];
        byte[] data = new byte[dataLengthInt];

        for (int i = 0; i < textLengthInt; i++) {
            text[i] = (byte) Integer.parseInt(reader.readLine(), 2);
        }

        for (int i = 0; i < dataLengthInt; i++) {
            data[i] = (byte) Integer.parseInt(reader.readLine(), 2);
        }

    }
        return new MachineCode(text, data);
    }



}

