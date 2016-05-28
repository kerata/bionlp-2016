package BB3.Models;

import BB3.Utils.Commons;
import BB3.Utils.Tokenizer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.*;

import java.util.*;

/**
 * Created by hakansahin on 28/02/16.
 */
public class Document {

    private String id, title, paragraph;
    private Map<String, Habitat> habitatMap;
    private Map<String, List<String> > categoryList;
    private Map<String, Integer> invertedIndex;

    private int[] sentencePositions;

    public Document(String id){
        this.id = id;
        this.habitatMap = new HashMap<>();
        this.categoryList = new HashMap<>();
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setParagraph(String paragraph){
        this.paragraph = paragraph;
    }

    public void addHabitat(Habitat habitat){
        this.habitatMap.put(habitat.getId(), habitat);
    }

    public void addCategoryToHabitat(String habitatId, String categoryId){
        this.categoryList.putIfAbsent(habitatId, new ArrayList<>());
        this.categoryList.get(habitatId).add(categoryId);
    }

    public List<String> getCategoriesForHabitat(String habitatId){
        return this.categoryList.getOrDefault(habitatId, new ArrayList<>());
    }

    public Map<String, Integer> buildInvertedIndex(){
        if(this.invertedIndex != null) return this.invertedIndex;
        else return this.invertedIndex = Commons.buildInvertedIndex(this.title + "\n" + this.paragraph);
    }

    public void printHabitats(){
        for(Habitat habitat : habitatMap.values())
            System.out.println(habitat.getEntity());
    }

    private void findSentencePositions() {
        List<List<CoreLabel>> sentences = new WordToSentenceProcessor<CoreLabel>()
                .process(Tokenizer.tokenize(this.paragraph, "untokenizable=noneKeep"));

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
                return this.paragraph.substring(sentencePositions[i -1], sentencePositions[i]).trim();
        return "";
    }

    public void setHabitatMap(Map<String, Habitat> habitatMap){ this.habitatMap = habitatMap; }
    public void setCategoryList(Map<String, List<String> > categories){ this.categoryList = categories; }
    public Map<String, List<String> > getCategoryList(){ return this.categoryList; }

    public String getId(){ return this.id; }
    public Map<String, Habitat> getHabitatMap(){ return this.habitatMap; }
}
