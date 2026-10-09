package metiers;

public class TypeTransport {
    private int id;
    private String nomType;
    private float cout;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomType() {
        return nomType;
    }

    public void setNomType(String nomType) {
        this.nomType = nomType;
    }

    public float getCout() {
        return cout;
    }

    public void setCout(float cout) {
        this.cout = cout;
    }

    public TypeTransport(int id, String nomType, float cout) {
        this.id = id;
        this.nomType = nomType;
        this.cout = cout;
    }
}
