package BB3.Utils;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by kerata on 04/03/16.
 */
public class Tokenizer {

    public static List<String> tokenizeText(String sentence) {
        List<String> words = new ArrayList<>();
        Stemmer stemmer = new Stemmer();

        for (CoreLabel coreLabel: Tokenizer.tokenize(sentence, "untokenizable=noneKeep")) {
            String token = coreLabel.value().toLowerCase();

            if(!Pattern.compile("\\w+").matcher(token).find()) continue;

            stemmer.add(token.toCharArray(), token.length());
            stemmer.stem();
            words.add(stemmer.toString());
        }

        return words;
    }

    public static List<CoreLabel> tokenize(String text, String parameters) {
        List<CoreLabel> tokens = new ArrayList<>();
        PTBTokenizer<CoreLabel> tokenizer = new PTBTokenizer<>
                (new StringReader(text), new CoreLabelTokenFactory(), parameters);
        while (tokenizer.hasNext())
            tokens.add(tokenizer.next());
        return tokens;
    }
}
