package BB3.Utils;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by hakansahin on 29/02/16.
 */
public class POSTagger {

    private static MaxentTagger tagger = new MaxentTagger("src/main/resources/taggers/left3words-distsim.tagger");

    public static List tagDocument(String text){
        List result = new ArrayList<>();
        List sentences = MaxentTagger.tokenizeText(new StringReader(text));
        Iterator var3 = sentences.iterator();

        while(var3.hasNext()) {
            List sentence = (List)var3.next();
            List tSentence = tagger.tagSentence(sentence);
            result.add(tSentence);
//            System.out.println(Sentence.listToString(tSentence, false));
        }
        return result;
    }
}
