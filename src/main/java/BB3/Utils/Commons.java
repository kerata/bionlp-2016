package BB3.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by hakansahin on 14/03/16.
 */
public class Commons {

    public interface tokenExtractionListener {
        void tokenDetected(String token);
    }

    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static int TP = 0, FP = 0, FN = 0, trial = 0, N = 0;

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

    public static void printToFile(String path, String fileName, String... texts) {
        try {
            File file = new File("src/main/resources/output/" + path);
            if(!file.exists()) file.mkdirs();
            PrintWriter writer = new PrintWriter(file.getAbsoluteFile() + "/" + fileName, "UTF-8");
            for(String text : texts) writer.println(text);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public static Map<String, Integer> buildInvertedIndex(String text) {
        return buildInvertedIndex(text, null);
    }

    public static Map<String, Integer> buildInvertedIndex(String text, tokenExtractionListener listener) {
        Map<String, Integer> invertedIndex = new HashMap<>();

        List<String> tokens = Tokenizer.tokenizeText(text);
        tokens.addAll(Commons.expandWithNGram(tokens, 2));
        for(String token : tokens) {
            invertedIndex.put(token, invertedIndex.getOrDefault(token, 0) + 1);
            if (listener != null) listener.tokenDetected(token);
        }
        return invertedIndex;
    }

    public static List<String> expandWithNGram(List<String> tokens, int N) {
        if (tokens.size() < N || N < 2) return tokens;

        int index = 0;
        int[] tokenLength = new int[N];

        Iterator<String> iterator = tokens.iterator();
        List<String> nGrams = new ArrayList<>();
        nGrams.addAll(tokens);
        StringBuilder sb = new StringBuilder();

        // build first NGram
        for (int i = 0;i < N;i++) {
            String token = iterator.next();
            sb.append(token).append(" ");

            tokenLength[i] = token.length() + 1;
        }
        sb.deleteCharAt(sb.length() - 1);
        nGrams.add(sb.toString());

        while (iterator.hasNext()) {
            // delete first token
            sb.delete(0, tokenLength[index]);
            // find first start index
            String token = iterator.next();
            tokenLength[index] = token.length() + 1;
            index = index == N - 1 ? 0 : index + 1;

            // build next NGram (rolling style)
            sb.append(" ").append(token);

            nGrams.add(sb.toString());
        }

        return nGrams;
    }

    public static class Pair<L, R> {
        public L l;
        public R r;

        public Pair(L l, R r) {
            this.l = l;
            this.r = r;
        }

        @Override
        public boolean equals(Object obj) {
            return l.equals(((Pair) obj).l);
        }
    }
}
