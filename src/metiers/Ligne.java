package metiers;

public class Ligne {
    private int id;
    private String libelle;
    private char hexColor;
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

    public char getHexColor() {
        return hexColor;
    }

    public void setHexColor(char hexColor) {
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

    public Ligne(int id, String libelle, char hexColor, Arret terminusDepart, Arret terminusArrivee, TypeTransport typeTransport) {
        this.id = id;
        this.libelle = libelle;
        this.hexColor = hexColor;
        TerminusDepart = terminusDepart;
        TerminusArrivee = terminusArrivee;
        this.typeTransport = typeTransport;
    }
}
