package metiers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Perturbation {
    // ---------------- Static ----------------
    private static List<Perturbation> lesPerturbationsEnCache = new ArrayList<>();

    public static Perturbation getPerturbationEnCache(int idPerturbation) {
        for(Perturbation perturbation : lesPerturbationsEnCache) {
            if (perturbation.getId() == idPerturbation) {
                return perturbation;
            }
        }

        // Si pas en cache
        return null;
    }

    private static void addEnCache(Perturbation unePerturbation) {
        lesPerturbationsEnCache.add(unePerturbation);
    }

    public static void supprimerDuCache(Perturbation perturbation) {
        lesPerturbationsEnCache.removeIf(p -> p.getId() == perturbation.getId());
    }

    // ---------------- Objet ----------------

    private int id;
    private String libelle;

    // être perturbé
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private List<Arret> arretsPerturbes;

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

    public LocalDateTime getDateHeureDebut() {
        return dateHeureDebut;
    }

    public void setDateHeureDebut(LocalDateTime dateHeureDebut) {
        this.dateHeureDebut = dateHeureDebut;
    }

    public LocalDateTime getDateHeureFin() {
        return dateHeureFin;
    }

    public void setDateHeureFin(LocalDateTime dateHeureFin) {
        this.dateHeureFin = dateHeureFin;
    }

    public List<Arret> getArretsPerturbes() {
        return arretsPerturbes;
    }

    public void setArretsPerturbes(List<Arret> arretsPerturbes) {
        this.arretsPerturbes = arretsPerturbes;
    }

    public boolean arretPresent(Arret arret) {
        return this.arretsPerturbes.stream()
                .anyMatch(a -> a.getId() == arret.getId());
    }

    public void ajouterUnArretPerturbe(Arret arret) {
        this.arretsPerturbes.add(arret);
        if (arretPresent(arret)) {
            arret.ajouterPerturbation(this);
        }
    }

    public Perturbation(int id, String libelle, LocalDateTime dateHeureDebut, LocalDateTime dateHeureFin) {
        this.id = id;
        this.libelle = libelle;
        this.dateHeureDebut = dateHeureDebut;
        this.dateHeureFin = dateHeureFin;

        this.arretsPerturbes = new ArrayList<>();

        Perturbation.addEnCache(this);
    }
}
