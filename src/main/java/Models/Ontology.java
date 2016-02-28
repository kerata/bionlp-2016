package Models;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kerata on 28/02/16.
 */
public class Ontology {

    private Map<String, Term> terms;

    public Ontology() {
        terms = new HashMap<>();
    }

    public Map<String, Term> getTerms() {
        return terms;
    }

    public void addTerm(Term term) {
        terms.put(term.getId(), term);
    }

    @Override
    public String toString() {
        return terms.toString();
    }
}
