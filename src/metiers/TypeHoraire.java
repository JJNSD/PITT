package metiers;

import java.util.ArrayList;
import java.util.List;

public class TypeHoraire {
    // ---------------- Static ----------------
    private static List<TypeHoraire> lesTypesHorairesEnCache = new ArrayList<>();

    public static TypeHoraire getTypeHoraireEnCache(int idTypeHoraire) {
        for(TypeHoraire typeHoraire : lesTypesHorairesEnCache) {
            if (typeHoraire.getId() == idTypeHoraire) {
                return typeHoraire;
            }
        }

        // Si pas en cache
        return null;
    }

    private static void addEnCache(TypeHoraire typeHoraire) {
        lesTypesHorairesEnCache.add(typeHoraire);
    }

    public static void supprimerDuCache(TypeHoraire typeHoraire) {
        lesTypesHorairesEnCache.removeIf(t -> t.getId() == typeHoraire.getId());
    }

    public static void resetCache() {
        lesTypesHorairesEnCache = new ArrayList<>();
    }

    // ---------------- Objet ----------------

    private int id;
    private String libelle;

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

    public TypeHoraire(int id, String libelle) {
        this.id = id;
        this.libelle = libelle;

        TypeHoraire.addEnCache(this);
    }
}
