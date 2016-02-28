package Models;

import java.util.ArrayList;

/**
 * Created by kerata on 28/02/16.
 */
public class Term {

    private String id;
    private String name;
    private ArrayList<Synonym> synonyms;
    private ArrayList<Relation> is_a;

    public Term() {}

    public Term(String id, String name) {
        this.id = id;
        this.name = name;
        synonyms = new ArrayList<>();
        is_a = new ArrayList<>();
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

    public void addSynonym(Synonym synonym) {
        synonyms.add(synonym);
    }

    public ArrayList<Synonym> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(ArrayList<Synonym> synonyms) {
        this.synonyms = synonyms;
    }

    public void addRelation(Relation relation) {
        is_a.add(relation);
    }

    public ArrayList<Relation> getIs_a() {
        return is_a;
    }

    public void setIs_a(ArrayList<Relation> is_a) {
        this.is_a = is_a;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(String.format("id: %s;", id));
        stringBuilder.append(String.format("name: %s;", name));
        stringBuilder.append(String.format("synonyms: %s;", synonyms.toString()));
        stringBuilder.append(String.format("relations: %s;", is_a.toString()));

        return stringBuilder.toString();
    }
}
