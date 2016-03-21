package Models;

import Utils.Tokenizer;

import java.util.*;

/**
 * Created by kerata on 28/02/16.
 */
public class Ontology {

    private ArrayList<Tree> dependencyTrees;
    private Map<String, Term> terms;
    private Map<String, Set<Term>> invertedIndex;

    public Set<String> vocabulary;
    public Map<String, Integer> docFreq;
    private Map<String, Map<String, Integer> > termFreq;
    public Map<String, List<Double> > tfIdf;

    public Ontology() {
        terms = new HashMap<>();
        invertedIndex = new HashMap<>();
        vocabulary = new HashSet<>();
        docFreq = new HashMap<>();
        termFreq = new HashMap<>();
    }

    public Map<String, Term> getTerms() {
        return terms;
    }

    public ArrayList<Tree> getDependencyTrees() {
        return dependencyTrees;
    }

    public void addTerm(Term term) {
        List<String> tokens = Tokenizer.tokenizeText(term.getName());
        for (Synonym synonym: term.getSynonyms())
            tokens.addAll(Tokenizer.tokenizeText(synonym.getDetail()));

        for (Relation relation : term.getIs_a())
            tokens.addAll(Tokenizer.tokenizeText(relation.getTermName()));

        for (String token: tokens) {
            Set<Term> posting = invertedIndex.get(token);
            if (posting != null) {
                posting.add(term);
            }
            else {
                posting = new HashSet<>();
                posting.add(term);
                invertedIndex.put(token, posting);
            }

            // Will be used in order to compute tf-idf values.
            vocabulary.add(token);
            termFreq.putIfAbsent(term.getId(), new HashMap<>());
            if(termFreq.get(term.getId()).containsKey(token))
                termFreq.get(term.getId()).put(token, termFreq.get(term.getId()).get(token) + 1);
            else{
                docFreq.put(token, docFreq.getOrDefault(token, 0) + 1);
                termFreq.get(term.getId()).put(token, 1);
            }
        }

        terms.put(term.getId(), term);
    }

    // Computes tf-idf values for all terms.
    public Map<String, List<Double> > computeTfIdfValues(){
        if(this.tfIdf != null) return this.tfIdf;

        this.tfIdf = new HashMap<>();
        for(String termId : terms.keySet()){
            List<Double> tfIdfValues = new ArrayList<>();
            for(String v : vocabulary)
                tfIdfValues.add((1 + Math.log10(termFreq.get(termId).getOrDefault(v, 1))) * Math.log10(vocabulary.size() / docFreq.get(v)));
            this.tfIdf.put(termId, tfIdfValues);
        }
        return this.tfIdf;
    }

    public List<Term> getTermsForKeywords(List<String> keywords) {
        List<Term> result = new ArrayList<>();
        for (String keyword: keywords)
            result.addAll(getTermsForKeyword(keyword));
        return result;
    }

    public List<Term> getTermsForKeywords(String[] keywords) {
        List<Term> result = new ArrayList<>();
        for (String keyword: keywords)
            result.addAll(getTermsForKeyword(keyword));
        return result;
    }

    public Set<Term> getTermsForKeyword(String keyword) {
        return invertedIndex.get(keyword);
    }

    public Ontology buildDependencyTrees() {
        dependencyTrees = new ArrayList<>();
        for (Iterator<Term> iterator = terms.values().iterator();iterator.hasNext();iterator = terms.values().iterator()) {
            Term term = iterator.next();
            iterator.remove();
            Tree tree = new Tree();
            tree.constructFromLeaf(this, term);

            if (!tree.hasMerged())
                dependencyTrees.add(tree);
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format("terms: %s\n\ninvertedIndex: %s", terms.toString(), invertedIndex.toString());
    }
}
