package Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by hakansahin on 14/03/16.
 */
public class Commons {

    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static int TP = 0, FP = 0, FN = 0;

    public static void printBlack(String text){
        System.out.println(ANSI_BLACK + text + ANSI_BLACK);
    }

    public static void printRed(String text){
        System.out.println(ANSI_RED + text + ANSI_RED);
    }

    public static void printYellow(String text){
        System.out.println(ANSI_YELLOW + text + ANSI_YELLOW);
    }

    public static void printBlue(String text){
        System.out.println(ANSI_BLUE + text + ANSI_BLUE);
    }

    public static void printToFile(String path, String fileName, String... texts){
        PrintWriter writer = null;
        try {
            File file = new File("src/main/resources/output/" + path);
            if(!file.exists()) file.mkdirs();
            writer = new PrintWriter(file.getAbsoluteFile() + "/" + fileName, "UTF-8");
            for(String text : texts) writer.println(text);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
