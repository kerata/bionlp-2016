package BB3.Utils;

import BB3.BB3Runner;
import BB3.Models.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by hakansahin on 11/03/16.
 */
public class Categorizer {

    private static Categorizer me;

    public Categorizer() {
//        BB3Runner.ontology = Parser.buildOntology("src/main/resources/OntoBiotope_BioNLP-ST-2016.obo");
//        BB3Runner.ontology.computeTfIdfValues();
    }

    public static Categorizer init() {
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

    public ArrayList<Term> categorize(ArrayList<Commons.Pair<Term, Double>> sortedTerms, String habitat) {
        Set<Term> termList = new HashSet<>();
        Tree ontologyTree = BB3Runner.ontology.getDependencyTrees().get(0);
        for(String token : Tokenizer.tokenizeText(habitat)) {
            BB3Runner.ontology.getTermsForKeyword(token.toLowerCase())
                    .forEach(term -> {
                        termList.add(term);
//                        ArrayList<Tree.Node> nodes = ontologyTree.getNode(term);
//                        nodes.forEach(node -> {
//                             do {
//                                termList.add(node.getData());
//                                node = node.getParent();
//                            } while (node.getLevel() != 0);
//                        });
                    });
        }

        ArrayList<Term> result = new ArrayList<>();
        sortedTerms.stream()
                .filter(pair -> termList.contains(pair.l))
                .forEach(pair -> result.add(pair.l));

        return result;
    }

    // Computes cosine similarity between Terms and Doc.
    // Returns sorted map of terms with respect to similarities.
    public ArrayList<Commons.Pair<Term, Double>> sortTerms(Map<String, Integer> invertedIndex) {
        List<Double> tfIdfValues = BB3Runner.ontology.vocabulary.stream()
                .map(v -> (1 + Math.log10(invertedIndex.getOrDefault(v, 1))) *
                    Math.log10(BB3Runner.ontology.vocabulary.size() / BB3Runner.ontology.docFreq.get(v)))
                .collect(Collectors.toList());

        Map<Term, Double> res = new HashMap<>();
        for(Map.Entry<String, List<Double> > entry : BB3Runner.ontology.tfIdf.entrySet())
            res.put(BB3Runner.ontology.getTerms().get(entry.getKey()), computeCosSim(entry.getValue(), tfIdfValues));

        ArrayList<Commons.Pair<Term, Double>> result = new ArrayList<>();
        res.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .forEachOrdered(e -> result.add(new Commons.Pair<>(e.getKey(), e.getValue())));

        return result;
    }

    public StringBuilder categorizeDocument(Document doc) {

        int maxTrialCount = 1;
        Commons.N = 0;
        String a2Result = "";
        StringBuilder sb = new StringBuilder();

        ArrayList<Commons.Pair<Term, Double>> sortedTerms = sortTerms(doc.buildInvertedIndex());
        for(Habitat habitat : doc.getHabitats()) {
            int trialCount = 0;

            if(habitat.getType().equals("Bacteria")){
                Commons.N++;
                a2Result += (new StringBuilder())
                        .append(String.format("N%d\tNCBI_Taxonomy ", Commons.N))
                        .append(String.format("Annotation:%s ",habitat.getId()))
                        .append("Referent:2104\n")
                        .toString();
                continue;
            }

            ArrayList<Term> possibleCategories = categorize(sortedTerms, habitat.getEntity());
//            Set<Term> possibleCategories = categorize(
//                    sortTerms(Commons.buildInvertedIndex(habitat.getEntity())), habitat.getEntity());
//            maxTrialCount = possibleCategories.size();

            String message = habitat.getId() + " " + habitat.getEntity();
            List<String> categoryIdList = doc.getCategories().get(habitat.getId());

            // prints categories to a2 files.
            for(Term term : possibleCategories){
                Commons.N++;
                a2Result += (new StringBuilder())
                        .append(String.format("N%d\tOntoBiotope ", Commons.N))
                        .append(String.format("Annotation:%s ",habitat.getId()))
                        .append(String.format("Referent:%s\n",term.getId()))
                        .toString();
                break;
            }

            if(possibleCategories.isEmpty()){
                Commons.N++;
                a2Result += (new StringBuilder())
                        .append(String.format("N%d\tOntoBiotope ", Commons.N))
                        .append(String.format("Annotation:%s ",habitat.getId()))
                        .append("Referent:OBT:000000\n")
                        .toString();
            }

            for(int i = 0;i < sortedTerms.size();i++)
                if (sortedTerms.get(i).l.getId().equals(categoryIdList.get(0))) {
                    Commons.printBlack(message + " <===> " + i);
                    break;
                }

            // If there is no possible category, means any categories belong to current habitat is not found, then they added to 'False Negatives'.
            if(possibleCategories.isEmpty()) {
                Commons.FN += categoryIdList.size();
                Commons.printYellow(message + " : " + categoryIdList.toString());
                continue;
            }

            int found = 0;
            for(Term term : possibleCategories) {
                String categoryId = term.getId();
                if(categoryIdList.contains(categoryId)){
                    categoryIdList.remove(categoryId);              // Found categories removed from categoryIdList.
                    Commons.printBlue(message + " : " + categoryId);
                    found++;
                }
                else {
                    Commons.FP++;
                    Commons.printRed(message + " : " + possibleCategories.toString());
                }

                if (++trialCount == maxTrialCount) break;
            }
            // Number of found categories added to 'True Positives'.
            Commons.TP += found;
            Commons.trial += trialCount;
            if(found == 0)  {
                sb.append(doc.getId()).append(", ").append(String.format("%s", habitat.getEntity())).append("\n")
                        .append(String.format("%-30s", possibleCategories.stream().findFirst().get().getName())).append("/")
                        .append(String.format("%-30s", BB3Runner.ontology.getTerms().get(categoryIdList.get(0)).getName())).append("\n");
            }

            // If any possible category is not found in categoryIdList than it marks as 'False Positive'.
//            if(found == 0) {
//                Commons.FP += categoryIdList.size() - found;
//                possibleCategories.stream()
//                        .limit(maxTrialCount)
//                        .forEach(term -> Commons.printRed(message + " : " + term.toString()));
//            }

            // Number of remaining categories added to 'False Negatives'.
            Commons.FN += categoryIdList.size();
            categoryIdList.forEach(catId -> Commons.printYellow(message + " : " + catId));
        }

        // Prints a2 results to file.
//        Commons.printToFile("result",doc.getId() + ".a2", a2Result);

        return sb;
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
