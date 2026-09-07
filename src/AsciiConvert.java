import java.util.Scanner;

public class AsciiConvert {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter a character: ");
        String line = input.nextLine();

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            int decimal = (int) ch;
            System.out.println(ch + " = " + decimal);

        }

        input.close();
    }

}
