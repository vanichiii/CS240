import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class hw1color {
    public static Color convert(char letter) {
        if (letter == 'R') {
            return new Color(237, 28, 36);
        } else if (letter == 'B') {
            return new Color(0,0,0);
        } else if (letter == 'Y') {
            return new Color(255, 242, 0);
        } else {
            //throw an exception to indicate that the color was not found
            throw new IllegalArgumentException("No color identified for: " + letter);
        }
    }

    public static void main(String[] args) throws IOException {
        //important note to self: need to have the file inside the src, and use 'src/' to direct it to proper address
        //takes in txt file to then convert
        File textFile = new File("src/output.txt");

        //paste all letters into a string array for better reading
        List<String> txt = new ArrayList<String>();

        //for every line in textFile
        for (String line : Files.readAllLines(textFile.toPath())) {
            //if the trimmed line is not empty, add it to the ArrayList txt
            if (!line.trim().isEmpty()) {
                txt.add(line.trim());
            }
        }

        //calculates the amount of lines in the arraylist
        int height = txt.size();
        int width = txt.get(0).trim().split(" ").length;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            //creates an array for every line in txt
            String[] line = txt.get(y).split(" ");
            for (int x = 0; x < width; x++) {
                char currentChar = line[x].charAt(0);
                Color convertedColor = convert(currentChar);
                //sets the color of the current location
                image.setRGB(x, y, convertedColor.getRGB());
            }

        }
        File outputFile = new File("output.png");
        ImageIO.write(image, "png", outputFile);

    }
}
