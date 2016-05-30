package BB3.Models;

/**
 * Created by hakansahin on 11/03/16.
 */
public class Habitat {

    private String id, entity;
    private int rank, start, end;

    public Habitat(String id, String start, String end, String entity){
        this.id     = id;
        this.start  = Integer.parseInt(start);
        this.end    = Integer.parseInt(end);
        this.entity = entity;
        this.rank   = Integer.parseInt(id.replace("T",""));
    }

    public String getId(){
        return this.id;
    }
    public String getEntity(){
        return this.entity;
    }
    public int getRank() { return this.rank; }
    public int getStart() {
        return start;
    }
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return String.format("id: %s, entity: %s, start: %d, end: %d", id, entity, start, end);
    }
}