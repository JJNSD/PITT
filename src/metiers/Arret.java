package metiers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Arret {
    private int id;
    private String nomArret;
    private float lattitude;
    private float longitude;

    // être perturbé
    private List<Perturbation> lesPerturbations;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomArret() {
        return nomArret;
    }

    public void setNomArret(String nomArret) {
        this.nomArret = nomArret;
    }

    public float getLattitude() {
        return lattitude;
    }

    public void setLattitude(float lattitude) {
        this.lattitude = lattitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public List<Perturbation> getLesPerturbations() {
        return lesPerturbations;
    }

    public void setLesPerturbations(List<Perturbation> lesPerturbations) {
        this.lesPerturbations = lesPerturbations;
    }

    public boolean perturbationPresente(Perturbation perturbation) {
        return this.lesPerturbations.stream()
                .anyMatch(p -> p.getId() == perturbation.getId());
    }

    public void ajouterPerturbation(Perturbation perturbation) {
        this.lesPerturbations.add(perturbation);
        if (perturbationPresente(perturbation)) {
            perturbation.ajouterUnArretPerturbe(this);
        }
    }

    public Arret(int id, String nomArret, float lattitude, float longitude) {
        this.id = id;
        this.nomArret = nomArret;
        this.lattitude = lattitude;
        this.longitude = longitude;
        this.lesPerturbations = new ArrayList<>();
    }

    public boolean estPerturbe(LocalDateTime instant) {
        for (Perturbation p : lesPerturbations) {
            boolean commence = instant.isAfter(p.getDateHeureDebut());
            boolean pasFinie = p.getDateHeureFin() == null || instant.isBefore(p.getDateHeureFin());
            return (commence && pasFinie);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + nomArret;
    }
}
