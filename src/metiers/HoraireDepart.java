package metiers;

import java.time.LocalTime;

public class HoraireDepart {
    private int id;
    private Ligne ligneAffecte;
    private LocalTime heureDepart;

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

    public HoraireDepart(int id, Ligne ligneAffecte, LocalTime heureDepart) {
        this.id = id;
        this.ligneAffecte = ligneAffecte;
        this.heureDepart = heureDepart;
    }
}
