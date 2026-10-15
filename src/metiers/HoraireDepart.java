package metiers;

import java.time.LocalTime;

public class HoraireDepart {
    private int id;
    private Ligne ligneAffecte;
    private LocalTime heureDepart;
    private TypeHoraire typeHoraire;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Ligne getLigneAffecte() {
        return ligneAffecte;
    }

    public void setLigneAffecte(Ligne ligneAffecte) {
        this.ligneAffecte = ligneAffecte;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(LocalTime heureDepart) {
        this.heureDepart = heureDepart;
    }

    public TypeHoraire getTypeHoraire() {
        return typeHoraire;
    }

    public void setTypeHoraire(TypeHoraire typeHoraire) {
        this.typeHoraire = typeHoraire;
    }

    public HoraireDepart(int id, Ligne ligneAffecte, LocalTime heureDepart, TypeHoraire typeHoraire) {
        this.id = id;
        this.ligneAffecte = ligneAffecte;
        this.heureDepart = heureDepart;
        this.typeHoraire = typeHoraire;
    }
}