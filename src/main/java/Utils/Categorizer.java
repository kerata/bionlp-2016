package Utils;

import Models.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hakansahin on 11/03/16.
 */
public class Categorizer {

    private static Categorizer me;
    private Ontology ontology;
    public Categorizer(){
        this.ontology = Parser.buildOntology("src/main/resources/OntoBiotope_BioNLP-ST-2016.obo");
    }

    public static Categorizer init(){
        return (me == null) ? me = new Categorizer() : me;
    }

    public Set<Term> categorize(String habitat){

        Set<Term> termList = new HashSet<>();
        for(String token : Tokenizer.tokenizeText(habitat)){
            Set<Term> foundTerms = this.ontology.getTermsForKeyword(token.toLowerCase());
            if(foundTerms != null) termList.addAll(foundTerms);
        }

        return termList;
    }

    public void categorizeDocument(Document doc){

        for(Habitat habitat : doc.getHabitats()){

            Set<Term> possibleCategories = categorize(habitat.getEntity());
            String message = habitat.getId() + " " + habitat.getEntity();
            List<String> categoryIdList = doc.getCategories().get(habitat.getId());

            // If there is no possible category, means any categories belong to current habitat is not found, then they added to 'False Negatives'.
            if(possibleCategories.isEmpty()){
                Commons.FN += categoryIdList.size();
                Commons.printYellow(message + " : " + categoryIdList.toString());
                continue;
            }

            int found = 0;
            for(Term term : possibleCategories){
                String categoryId = term.getId();
                if(categoryIdList.contains(categoryId)){
                    categoryIdList.remove(categoryId);              // Found categories removed from categoryIdList.
                    Commons.printBlue(message + " : " + categoryId);
                    found++;
                }
            }
            // Number of found categories added to 'True Positives'.
            Commons.TP += found;

            // If any possible category is not found in categoryIdList than it marks as 'False Positive'.
            if(found == 0){
                Commons.FP++;
                Commons.printRed(message + " : " + possibleCategories.toString());
            }
            // Number of remaining categories added to 'False Negatives'.
            Commons.FN += (categoryIdList.size());
            for(String catID : categoryIdList)
                Commons.printYellow(message + " : " + catID);
        }
    }

    public Map<String, List<String> > splitCategories(String text){

        Pattern pattern = Pattern.compile("(N\\d*)\\W*(\\w*)\\W*.*:(T\\d*)\\W*.*?:(.*)");
        Matcher m = pattern.matcher(text);

        Map<String, List<String> > categories = new HashMap<>();
        while(m.find()){
            String  type        = m.group(2),
                    id          = m.group(3),
                    category    = m.group(4);

            if(!type.equals("OntoBiotope")) continue;

            if(!categories.containsKey(id))
                categories.put(id, new ArrayList<>());

            categories.get(id).add(category);
        }
        return categories;
    }
}
