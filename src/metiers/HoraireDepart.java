package metiers;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class HoraireDepart {
    // ---------------- Static ----------------
    private static List<HoraireDepart> lesHorairesDepartEnCache = new ArrayList<>();

    public static HoraireDepart getHoraireDepartEnCache(int idHoraireDepart, int idLigne) {
        for(HoraireDepart horaireDepart : lesHorairesDepartEnCache) {
            if (horaireDepart.getId() == idHoraireDepart && horaireDepart.getLigneAffecte().getId() == idLigne) {
                return horaireDepart;
            }
        }

        // Si pas en cache
        return null;
    }

    private static void addEnCache(HoraireDepart horaireDepart) {
        lesHorairesDepartEnCache.add(horaireDepart);
    }

    public static void supprimerDuCache(HoraireDepart horaire) {
        lesHorairesDepartEnCache.removeIf(h -> h.getId() == horaire.getId());
    }

    // ---------------- Objet ----------------

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

        HoraireDepart.addEnCache(this);
    }
}