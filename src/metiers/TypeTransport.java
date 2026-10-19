package metiers;

import java.util.ArrayList;
import java.util.List;

public class TypeTransport {
    // ---------------- Static ----------------
    private static List<TypeTransport> lesTypesTransportEnCache = new ArrayList<>();

    public static TypeTransport getTypeTransportEnCache(int idTypeTransport) {
        for(TypeTransport typeTransport : lesTypesTransportEnCache) {
            if (typeTransport.getId() == idTypeTransport) {
                return typeTransport;
            }
        }

        // Si pas en cache
        return null;
    }

    private static void addEnCache(TypeTransport unTypeTransport) {
        lesTypesTransportEnCache.add(unTypeTransport);
    }

    public static void supprimerDuCache(TypeTransport typeTransport) {
        lesTypesTransportEnCache.removeIf(t -> t.getId() == typeTransport.getId());
    }

    // ---------------- Objet ----------------

    private int id;
    private String nomType;
    private float cout;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomType() {
        return nomType;
    }

    public void setNomType(String nomType) {
        this.nomType = nomType;
    }

    public float getCout() {
        return cout;
    }

    public void setCout(float cout) {
        this.cout = cout;
    }

    public TypeTransport(int id, String nomType, float cout) {
        this.id = id;
        this.nomType = nomType;
        this.cout = cout;
    }
}
