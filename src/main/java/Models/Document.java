package Models;

import Utils.LEXParser;
import Utils.POSTagger;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.Tree;

import java.util.List;

/**
 * Created by hakansahin on 28/02/16.
 */
public class Document {

    private String text;
    private List taggedText;
    private Tree parse;

    public Document(String text){
        this.text = text;
    }

    public List posTagger(){
        return this.taggedText = POSTagger.tagDocument(this.text);
    }

    public void parse(){
        if(parse == null)
            parse = LEXParser.initLEXParser().parseDocument(text);
    }

    public void printTaggedDocument(){
        for(Object sentence : this.taggedText)
            System.out.println(Sentence.listToString((List)sentence, false));
    }

    public void printParseTree(){
        parse();
        parse.pennPrint();
    }

    public String getText(){ return this.text; }
    public List getTaggedText(){ return this.taggedText; }
}
