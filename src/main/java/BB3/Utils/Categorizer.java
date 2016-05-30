package BB3.Utils;

import BB3.Models.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by hakansahin on 11/03/16.
 */
public class Categorizer {

    public interface CategorizationListener {
        void onCategoryAddedToHabitat(Habitat habitat, Term category);
        void onCategorizationEnded();
    }

    public static CategorizationListener listener;

    public static double computeCosineSimilarity(List<Double> v1, List<Double> v2){
        double a = 1, b = 0, c = 0;

        for(int i=0; i<v1.size(); i++){
            a += v1.get(i) * v2.get(i);
            b += (v1.get(i) * v1.get(i));
            c += (v2.get(i) * v2.get(i));
        }

        return a / (Math.sqrt(b) * Math.sqrt(c));
    }

    // Computes cosine similarity between Terms and Doc.
    // Returns sorted map of terms with respect to similarities.
    public static ArrayList<Commons.Pair<Term, Double>> sortTerms(Ontology ontology, Map<String, Integer> invertedIndex) {
        List<Double> tfIdfValues = ontology.vocabulary.stream()
                .map(v -> (1 + Math.log10(invertedIndex.getOrDefault(v, 1))) *
                    Math.log10(ontology.vocabulary.size() / ontology.docFreq.get(v)))
                .collect(Collectors.toList());

        Map<Term, Double> res = new HashMap<>();
        for(Map.Entry<String, List<Double> > entry : ontology.tfIdf.entrySet())
            res.put(ontology.getTerms().get(entry.getKey()), computeCosineSimilarity(entry.getValue(), tfIdfValues));

        ArrayList<Commons.Pair<Term, Double>> result = new ArrayList<>();
        res.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .forEachOrdered(e -> result.add(new Commons.Pair<>(e.getKey(), e.getValue())));

        return result;
    }

    public static ArrayList<Term> findPossibleCategories(Ontology ontology, ArrayList<Commons.Pair<Term, Double>> sortedTerms, String habitatEntity) {
        Set<Term> possibleCategories = new HashSet<>();

        // Tries exact matching
        List<String> habitatTokens = Tokenizer.tokenizeText(habitatEntity);
        StringBuilder tokenizedHabitatSB = new StringBuilder();
        habitatTokens.forEach(habitatToken -> tokenizedHabitatSB.append(String.format("%s ", habitatToken)));
        String tokenizedHabitatEntity = tokenizedHabitatSB.toString();

        for(String habitatToken : habitatTokens) {
            termLoop:
            for (Term term : ontology.getTermsForKeyword(habitatToken.toLowerCase())) {

                StringBuilder termTextSB = new StringBuilder();
                Tokenizer.tokenizeText(term.getName()).forEach(t -> termTextSB.append(String.format("%s ", t)));

                // If there is an exact match between name of the term then return it as result.
                if (termTextSB.toString().equals(tokenizedHabitatEntity))
                    return new ArrayList<>(Collections.singleton(term));

                for (Synonym synonym : term.getSynonyms()) {
                    StringBuilder synonymDetailSB = new StringBuilder();
                    Tokenizer.tokenizeText(synonym.getDetail()).forEach(synonymToken -> synonymDetailSB.append(String.format("%s ", synonymToken)));
                    if (synonymDetailSB.toString().equals(tokenizedHabitatEntity)) {
                        if (synonym.getType() == Synonym.Type.EXACT)
                            return new ArrayList<>(Collections.singleton(term));
                        possibleCategories.add(term);
                        continue termLoop;
                    }
                }
            }
        }

        ArrayList<Term> result = new ArrayList<>();

        if(!possibleCategories.isEmpty()){
            sortedTerms.stream()
                    .filter(pair -> possibleCategories.contains(pair.l))
                    .forEach(pair -> result.add(pair.l));
            return result;
        }

        for(String habitatToken : habitatTokens) {
            ontology.getTermsForKeyword(habitatToken.toLowerCase())
                    .forEach(possibleCategories::add);
        }

        sortedTerms.stream()
                .filter(pair -> possibleCategories.contains(pair.l))
                .forEach(pair -> result.add(pair.l));

        return result;
    }

    public static void categorizeDocument(Ontology ontology, Document document, int MAX_TRIAL_CNT, CategorizationListener listener) {
        ArrayList<Commons.Pair<Term, Double>> sortedTerms = sortTerms(ontology, document.buildInvertedIndex());
        document.getHabitatMap().values().stream()
                .sorted((o1, o2) -> Integer.compare(o1.getRank(), o2.getRank()))
                .forEach(habitat -> {
                    int trialCount = 0;
                    ArrayList<Term> possibleCategories = findPossibleCategories(ontology, sortedTerms, habitat.getEntity());

                    if (possibleCategories.isEmpty()) {
                        document.addCategoryToHabitat(habitat.getId(), "OBT:000001");
                        if (listener != null)
                            listener.onCategoryAddedToHabitat(habitat, ontology.getTerms().get("OBT:000001"));
                    } else {

                        for (Term term : possibleCategories) {
                            document.addCategoryToHabitat(habitat.getId(), term.getId());
                            if (listener != null) listener.onCategoryAddedToHabitat(habitat, term);
                            if (++trialCount == MAX_TRIAL_CNT) break;
                        }
                    }
                });
        if(listener != null) listener.onCategorizationEnded();
    }
}
