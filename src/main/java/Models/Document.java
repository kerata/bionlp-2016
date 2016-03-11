package Models;

import Utils.LEXParser;
import Utils.POSTagger;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hakansahin on 28/02/16.
 */
public class Document {

    private String id, text;
    private List taggedText;
    private Tree parse;
    private List<Habitat> habitatList;

    public Document(String id, String text){
        this.id = id;
        this.text = text;
        this.habitatList = new ArrayList<>();
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

    public void setHabitatList(List<Habitat> habitatList){ this.habitatList = habitatList; }

    public String getId(){ return this.id; }
    public String getText(){ return this.text; }
    public List getTaggedText(){ return this.taggedText; }
    public List getHabitats(){ return this.habitatList; }
}
