package BB3.Models;

import BB3.Utils.Commons;
import BB3.Utils.LEXParser;
import BB3.Utils.POSTagger;
import BB3.Utils.Tokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.*;
import edu.stanford.nlp.trees.Tree;

import java.util.*;

/**
 * Created by hakansahin on 28/02/16.
 */
public class Document {

    private String id, text;
    private List taggedText;
    private Tree parse;
    private List<Habitat> habitatList;
    private Map<String, List<String> > categories;
    private Map<String, Integer> invertedIndex;

    private int[] sentencePositions;

    public Document(String id, String text){
        this.id = id;
        this.text = text;
        this.habitatList = new ArrayList<>();
    }

    public Map<String, Integer> buildInvertedIndex(){
        if(this.invertedIndex != null) return this.invertedIndex;
        else return this.invertedIndex = Commons.buildInvertedIndex(this.text);
    }

    public List posTagger(){
        return this.taggedText = POSTagger.tagDocument(this.text);
    }

    public void parse(){
        if(parse == null)
            parse = LEXParser.init().parseDocument(text);
    }

    public void printTaggedDocument(){
        for(Object sentence : this.taggedText)
            System.out.println(Sentence.listToString((List)sentence, false));
    }

    public void printParseTree(){
        parse();
        parse.pennPrint();
    }

    public void printHabitats(){
        for(Habitat habitat : habitatList)
            System.out.println(habitat.getEntity());
    }

    private void findSentencePositions() {
        List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>()
                .process(Tokenizer.tokenize(this.text, "untokenizable=noneKeep"));

        int start = 0, end;
        List<Integer> sentencePositions= new ArrayList<>();
        sentencePositions.add(start);
        for (List<CoreLabel> sentence: sentences) {
            end = sentence.get(sentence.size() - 1).endPosition();
            sentencePositions.add(end);
        }
        this.sentencePositions = new int[sentencePositions.size()];

        int i = 0;
        for (Integer pos: sentencePositions)
            this.sentencePositions[i++] = pos;
    }

    public String getSentenceForPosition(int pos) {
        if (sentencePositions == null) findSentencePositions();

        if (pos < 0 || pos > sentencePositions[sentencePositions.length -1])
            return "";
        for (int i = 1;i < sentencePositions.length;i++)
            if (pos < sentencePositions[i] || pos > sentencePositions[i -1])
                return this.text.substring(sentencePositions[i -1], sentencePositions[i]).trim();
        return "";
    }

    public void setHabitatList(List<Habitat> habitatList){ this.habitatList = habitatList; }

    public void setCategories(Map<String, List<String> > categories){ this.categories = categories; }
    public Map<String, List<String> > getCategories(){ return this.categories; }

    public String getId(){ return this.id; }
    public String getText(){ return this.text; }
    public List getTaggedText(){ return this.taggedText; }
    public List<Habitat> getHabitats(){ return this.habitatList; }
}
