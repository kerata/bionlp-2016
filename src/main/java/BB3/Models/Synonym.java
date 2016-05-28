package BB3.Models;

/**
 * Created by kerata on 28/02/16.
 */
public class Synonym {

    public enum Type{
        EXACT, RELATED;

        @Override
        public String toString() {
            switch (this) {
                case EXACT:
                    return "EXACT";
                case RELATED:
                    return "RELATED";
                default:
                    return "UNKNOWN";
            }
        }
    }

    private Type type;
    private String detail;

    public Synonym(Type type, String detail) {
        this.type = type;
        this.detail = detail;
    }

    public Type getType() {
        return type;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Synonym) {
            Synonym other = (Synonym) obj;
            return this.detail.equals(other.detail);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return detail.hashCode();
    }

    @Override
    public String toString() {
        return String.format("\"%s\" %s []", detail, type != null ? type.toString() : "UNKNOWN");
    }
}
