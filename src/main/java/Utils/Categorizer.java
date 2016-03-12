package Utils;

import Models.Ontology;
import Models.Synonym;
import Models.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public String categorize(String habitat){

        String[] tokens = habitat.split(" ");
        for(String token : tokens)
            for(Term term: this.ontology.getTerms().values()){

                if(term.getName().equals(token)) return term.getId();
                for(Synonym synonym : term.getSynonyms())
                    if(synonym.getDetail().equals(token))
                        return term.getId();
            }

        return "Not categorized";
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
