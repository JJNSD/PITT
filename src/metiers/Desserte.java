package metiers;

import java.util.ArrayList;
import java.util.List;

public class Desserte {
    // ---------------- Static ----------------
    private static List<Desserte> lesDessertesEnCache = new ArrayList<>();

    public static Desserte getDesserteEnCacheByID(int idArretPrecedent, int idArretSuivant, int idLigne) {
        for(Desserte desserte : lesDessertesEnCache) {
            if (desserte.getArretPrecedent().getId() == idArretPrecedent && desserte.getArretSuivant().getId() == idArretSuivant && desserte.getLigne().getId() == idLigne) {
                return desserte;
            }
        }

        // Si pas en cache
        return null;
    }

    public static Desserte getDesserteEnCache(Arret arretPrecedent, Arret arretSuivant, Ligne ligne) {
        for(Desserte desserte : lesDessertesEnCache) {
            if (desserte.getArretPrecedent().getId() == arretPrecedent.getId() && desserte.getArretSuivant().getId() == arretSuivant.getId() && desserte.getLigne().getId() == ligne.getId()) {
                return desserte;
            }
        }

        // Si pas en cache
        return null;
    }

    private static void addEnCache(Desserte uneDesserte) {
        lesDessertesEnCache.add(uneDesserte);
    }

    // ---------------- Objet ----------------

    private Arret arretPrecedent;
    private Arret arretSuivant;
    private Ligne ligne;
    private int duree; // Durée en minutes
    private int distance; // Distance en mètres

    public Arret getArretPrecedent() {
        return arretPrecedent;
    }

    public void setArretPrecedent(Arret arretPrecedent) {
        this.arretPrecedent = arretPrecedent;
    }

    public Arret getArretSuivant() {
        return arretSuivant;
    }

    public void setArretSuivant(Arret arretSuivant) {
        this.arretSuivant = arretSuivant;
    }

    public Ligne getLigne() {
        return ligne;
    }

    public void setLigne(Ligne ligne) {
        this.ligne = ligne;
    }

    public int getDuree() {
        return duree;
    }

    public void setDuree(int duree) {
        this.duree = duree;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Desserte(Arret arretPrecedent, Arret arretSuivant, Ligne ligne, int duree, int distance) {
        this.arretPrecedent = arretPrecedent;
        this.arretSuivant = arretSuivant;
        this.ligne = ligne;
        this.duree = duree;
        this.distance = distance;

        Desserte.addEnCache(this);
    }
}
