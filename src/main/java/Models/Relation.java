package Models;

/**
 * Created by kerata on 28/02/16.
 */
public class Relation {

    private String relativeID;
    private String detail;

    public Relation(String relativeID, String detail) {
        this.relativeID = relativeID;
        this.detail = detail;
    }

    public String getRelativeID() {
        return relativeID;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return String.format("relativeID: %s; detail: %s", relativeID, detail);
    }
}
