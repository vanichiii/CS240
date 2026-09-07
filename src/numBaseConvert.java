import java.io.IOException;
import java.util.Scanner;

public class numBaseConvert {
        public static boolean validBase(int base) {
            return base == 2 || base == 8 ||
                    base == 10 || base == 16;
        }

        public static String convert(
                String number, int currentBase, int newBase) {

            boolean negative = number.startsWith("-");
            int value;

            if (negative) {
                // Reads values such as -1 or -1010
                value = Integer.parseInt(number, currentBase);
            } else {
                // Allows unsigned values up to 4294967295
                value = Integer.parseUnsignedInt(number, currentBase);
            }

            // Decimal output may need signed or unsigned formatting
            if (newBase == 10) {
                if (negative) {
                    return Integer.toString(value);
                } else {
                    return Integer.toUnsignedString(value);
                }
            }

            // Negative values are shown using 32-bit two's complement
            return Integer.toUnsignedString(value, newBase).toUpperCase();
        }

    public static void runBoundaryTests() {
        String zeroResult = convert("0", 10, 2);

        System.out.println("Test 1: Zero");
        System.out.println("Expected: 0");
        System.out.println("Actual:   " + zeroResult);
        System.out.println();


        String largestResult =
                convert("4294967295", 10, 16);

        System.out.println(
                "Test 2: Largest unsigned 32-bit value"
        );
        System.out.println("Expected: FFFFFFFF");
        System.out.println("Actual:   " + largestResult);
        System.out.println();


        String negativeResult = convert("-1", 10, 2);

        System.out.println(
                "Test 3: Negative two's-complement value"
        );
        System.out.println(
                "Expected: 11111111111111111111111111111111"
        );
        System.out.println("Actual:   " + negativeResult);
        System.out.println();
    }

        public static void main(String[] args) {
            Scanner input = new Scanner(System.in);

            System.out.print("Enter a number: ");
            String number = input.nextLine().trim();

            System.out.print(
                    "Enter its current base (2, 8, 10, or 16): "
            );
            int currentBase = input.nextInt();

            System.out.print(
                    "Enter the new base (2, 8, 10, or 16): "
            );
            int newBase = input.nextInt();

            if (!validBase(currentBase) || !validBase(newBase)) {
                System.out.println(
                        "The base must be 2, 8, 10, or 16."
                );
                input.close();
                return;
            }

            try {
                String answer =
                        convert(number, currentBase, newBase);
                runBoundaryTests();
                System.out.println(
                        number + " in base " + currentBase +
                                " is " + answer +
                                " in base " + newBase
                );

            } catch (NumberFormatException exception) {
                System.out.println(
                        number + " is not a valid 32-bit value " +
                                "in base " + currentBase + "."
                );
            }

            input.close();
        }
    }
