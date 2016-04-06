package BB3.Models;

/**
 * Created by hakansahin on 21/03/16.
 */
public class Relation {

    private String termId;
    private String termName;

    public Relation(String termId, String termName) {

        this.termId = termId;
        this.termName = termName;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    @Override
    public String toString() {
        return String.format("%s ! %s", termId, termName);
    }
}
