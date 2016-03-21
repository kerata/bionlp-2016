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
        this.ontology.computeTfIdfValues();
    }

    public static Categorizer init(){
        return (me == null) ? me = new Categorizer() : me;
    }

    public double computeCosSim(List<Double> v1, List<Double> v2){
        double a = 1, b = 0, c = 0;

        for(int i=0; i<v1.size(); i++){
            a += v1.get(i) * v2.get(i);
            b += (v1.get(i) * v1.get(i));
            c += (v2.get(i) * v2.get(i));
        }

        return a / (Math.sqrt(b) * Math.sqrt(c));
    }

    public Set<Term> categorize(Map<String,Double> sortedTerms, String habitat){

        Set<Term> termList = new HashSet<>();
        for(String token : Tokenizer.tokenizeText(habitat)){
            Set<Term> foundTerms = this.ontology.getTermsForKeyword(token.toLowerCase());
            if(foundTerms != null) termList.addAll(foundTerms);
        }

        // TODO: to see ranked results.
        Map<String, Double> test = new HashMap<>();
        for(Map.Entry<String, Double> entry : sortedTerms.entrySet()){
            if(termList.contains(this.ontology.getTerms().get(entry.getKey())))
                test.put(entry.getKey(), entry.getValue());
        }
/*
        List<Double> index = new ArrayList<>();
        for(Term term : termList)
            index.add(res.get(term.getId()));*/

        return termList;
    }

    // Computes cosine similarity between Terms and Doc.
    // Returns sorted map of terms with respect to similarities.
    public Map<String, Double> sortTerms(Map<String, Integer> invertedIndex){

        List<Double> tfIdfValues = new ArrayList<>();
        for(String v : this.ontology.vocabulary)
            tfIdfValues.add((1 + Math.log10(invertedIndex.getOrDefault(v,1))) *
                    Math.log10(this.ontology.vocabulary.size() / this.ontology.docFreq.get(v)));

        Map<String, Double> res = new HashMap<>();
        for(Map.Entry<String, List<Double> > entry : this.ontology.tfIdf.entrySet())
            res.put(entry.getKey(), computeCosSim(entry.getValue(), tfIdfValues));

        Map<String,Double> result = new LinkedHashMap<>();
        res.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue(), Comparator.reverseOrder()))
                .forEachOrdered(e ->result.put(e.getKey(),e.getValue()));

        return result;
    }

    public void categorizeDocument(Document doc){

        Map<String, Double> sortedTerms = sortTerms(doc.buildInvertedIndex());
        for(Habitat habitat : doc.getHabitats()){

            Set<Term> possibleCategories = categorize(sortedTerms, habitat.getEntity());

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
//                else {
//                    Commons.FP++;
//                    Commons.printRed(message + " : " + possibleCategories.toString());
//                }
            }
            // Number of found categories added to 'True Positives'.
            Commons.TP += found;
            if(found != 0) Commons.trial += possibleCategories.size();
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
