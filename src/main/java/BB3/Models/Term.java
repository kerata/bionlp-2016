package BB3.Models;

import java.util.HashSet;

/**
 * Created by kerata on 28/02/16.
 */
public class Term {

    private String id;
    private String name;
    private HashSet<Synonym> synonyms;
    private HashSet<Relation> is_a;

    public float weight = -1;

    public Term() {}

    public Term(String id, String name) {
        this.id = id;
        this.name = name;
        synonyms = new HashSet<>();
        is_a = new HashSet<>();
    }

    public Term(Term other) {
        this.id = other.id;
        this.name = other.name;
        synonyms = other.getSynonyms();
        is_a = other.getIs_a();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addSynonym(Synonym synonym) { synonyms.add(synonym); }

    public HashSet<Synonym> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(HashSet<Synonym> synonyms) {
        this.synonyms = synonyms;
    }

    public void addRelation(String termId, String termName) {
        is_a.add(new Relation(termId, termName));
    }

    public HashSet<Relation> getIs_a() {
        return is_a;
    }

    public void setIs_a(HashSet<Relation> is_a) {
        this.is_a = is_a;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Term) {
            Term other = (Term) obj;
            return this.id.equals(other.id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        String sum = new StringBuilder()
                .append(String.format("id: %s;", id))
                .append(String.format("name: %s;", name))
                .append(String.format("synonyms: %s;", synonyms.toString()))
                .append(String.format("relations: %s;", is_a.toString()))
                .toString();

        return sum;
    }
}
