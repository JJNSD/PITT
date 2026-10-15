package metiers;

public class Ligne {
    private int id;
    private String libelle;
    private String hexColor;
    private Arret TerminusDepart;
    private Arret TerminusArrivee;
    private TypeTransport typeTransport;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public Arret getTerminusDepart() {
        return TerminusDepart;
    }

    public void setTerminusDepart(Arret terminusDepart) {
        TerminusDepart = terminusDepart;
    }

    public Arret getTerminusArrivee() {
        return TerminusArrivee;
    }

    public void setTerminusArrivee(Arret terminusArrivee) {
        TerminusArrivee = terminusArrivee;
    }

    public TypeTransport getTypeTransport() {
        return typeTransport;
    }

    public void setTypeTransport(TypeTransport typeTransport) {
        this.typeTransport = typeTransport;
    }

    public Ligne(int id, String libelle, String hexColor, Arret terminusDepart, Arret terminusArrivee, TypeTransport typeTransport) {
        this.id = id;
        this.libelle = libelle;
        this.hexColor = hexColor;
        TerminusDepart = terminusDepart;
        TerminusArrivee = terminusArrivee;
        this.typeTransport = typeTransport;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
