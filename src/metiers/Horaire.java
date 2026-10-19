package metiers;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class Horaire {
    // ---------------- Static ----------------
    private static List<Horaire> lesHoraires = new ArrayList<>();

    public static Horaire getHorairesEnCache(Ligne ligneAffecte, Arret arretAffecte) {
        for(Horaire horaire : lesHoraires) {
            if (horaire.getArretAffecte().getId() == arretAffecte.getId() && horaire.getLigneAffecte().getId() == ligneAffecte.getId()) {
                return horaire;
            }
        }

        // Si pas en cache
        return null;
    }

    private static void addEnCache(Horaire horaire) {
        lesHoraires.add(horaire);
    }

    // ---------------- Objet ----------------
    private int id;
    private Ligne ligneAffecte;
    private Arret arretAffecte;
    private LocalTime heure;
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

    public Arret getArretAffecte() {
        return arretAffecte;
    }

    public void setArretAffecte(Arret arretAffecte) {
        this.arretAffecte = arretAffecte;
    }

    public LocalTime getHeure() {
        return heure;
    }

    public void setHeure(LocalTime heure) {
        this.heure = heure;
    }

    public TypeHoraire getTypeHoraire() {
        return typeHoraire;
    }

    public void setTypeHoraire(TypeHoraire typeHoraire) {
        this.typeHoraire = typeHoraire;
    }

    public Horaire(int id, Ligne ligneAffecte, LocalTime heure, Arret arretAffecte, TypeHoraire typeHoraire) {
        this.id = id;
        this.ligneAffecte = ligneAffecte;
        this.arretAffecte = arretAffecte;
        this.heure = heure;
        this.typeHoraire = typeHoraire;

        Horaire.addEnCache(this);
    }
}