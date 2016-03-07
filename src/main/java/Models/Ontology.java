package Models;

import Utils.Tokenizer;

import java.util.*;

/**
 * Created by kerata on 28/02/16.
 */
public class Ontology {

    private ArrayList<Tree> dependencyTrees;
    private Map<String, Term> terms;
    private Map<String, List<Term>> invertedIndex;

    public Ontology() {
        terms = new HashMap<>();
        invertedIndex = new HashMap<>();
    }

    public Map<String, Term> getTerms() {
        return terms;
    }

    public ArrayList<Tree> getDependencyTrees() {
        return dependencyTrees;
    }

    public void addTerm(Term term) {
        List<String> tokens = Tokenizer.tokenizeSentence(term.getName());
        for (Synonym synonym: term.getSynonyms())
            tokens.addAll(Tokenizer.tokenizeSentence(synonym.getDetail()));

        for (String token: tokens) {
            List<Term> posting = invertedIndex.get(token);
            if (posting != null) {
                posting.add(term);
            }
            else {
                posting = new ArrayList<>();
                posting.add(term);
                invertedIndex.put(token, posting);
            }
        }

        terms.put(term.getId(), term);
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

    public List<Term> getTermsForKeyword(String keyword) {
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
        dependencyTrees.get(0).printByLevel();
        System.out.println(dependencyTrees.get(0).toString());
        return this;
    }

    @Override
    public String toString() {
        return String.format("terms: %s\n\ninvertedIndex: %s", terms.toString(), invertedIndex.toString());
    }
}
