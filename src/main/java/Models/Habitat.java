package Models;

/**
 * Created by hakansahin on 11/03/16.
 */
public class Habitat {

    private String id, entity, type;
    private int start, end;

    public Habitat(String id, String start, String end, String entity, String type){
        this.id     = id;
        this.start  = Integer.parseInt(start);
        this.end    = Integer.parseInt(end);
        this.entity = entity;
        this.type   = type;
    }

    public String getId(){ return this.id; }
    public String getEntity(){ return this.entity; }
    public String getType(){ return this.type; }
}