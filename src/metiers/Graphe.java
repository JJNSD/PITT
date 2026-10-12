package metiers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graphe {
    private final int coutCorrespondance = 1;

    private Map<Arret, Map<Arret, Map<Ligne,Integer>>> leGraphe;

    public Graphe() {
        leGraphe = new HashMap<>();
    }

    public void ajouterSommet(Arret sommet) {
        leGraphe.put(sommet, new HashMap<>());
    }

    public void ajouterChemin(Arret depart, Arret arrivee, Ligne ligne, int score) {
        if (leGraphe.get(depart) == null) {
            ajouterSommet(depart);
        }
        if (leGraphe.get(arrivee) == null) {
            ajouterSommet(arrivee);
        }

        leGraphe.get(depart).put(arrivee,new HashMap<>());
        leGraphe.get(depart).get(arrivee).put(ligne,score);
    }

    public void ajouterCheminMAJSiExistant(Arret depart, Arret arrivee, Ligne ligne, int score) {
        if (leGraphe.containsKey(depart) && leGraphe.get(depart).containsKey(arrivee) && leGraphe.get(depart).get(arrivee).containsKey(ligne)) {
            leGraphe.get(depart).get(arrivee).replace(ligne, score);
        } else {
            ajouterChemin(depart, arrivee, ligne, score);
        }
    }

    @Override
    public String toString() {
        String affichage = "";
        for (Map.Entry g : this.leGraphe.entrySet()) {
            HashMap<String, Integer> val = (HashMap<String, Integer>) g.getValue();

            String chemin = "";
            if (val.size() > 0) {
                int passages = 1;

                for (Map.Entry p : val.entrySet()) {
                    if (passages > 1) {
                        chemin += ", ";
                    }
                    chemin += p.getKey() + " : " + p.getValue();

                    passages++;
                }
            } else {
                chemin = " ";
            }

            affichage += "{"+g.getKey()+", {"+ chemin + "} } \n";
        }
        return  affichage;
    }

    // Algos de recherche

    /**
     * Calcul la distance directe entre 2 arrêts.
     * @param depart l'arrêt de départ
     * @param arrivee l'arrêt d'arrivé
     * @param ligne la ligne sur laquelle on navigue
     * @return distance entre 2 arrêts voisins, -1 sinon
     */
    public int distanceVoisin(Arret depart, Arret arrivee, Ligne ligne) {
        if (leGraphe.containsKey(depart) && leGraphe.get(depart).containsKey(arrivee) && leGraphe.get(depart).get(arrivee).containsKey(ligne)) {
            return leGraphe.get(depart).get(arrivee).get(ligne);
        } else {
            return -1;
        }
    }

    public int bfs(Arret depart, Arret arrivee) {
        return 0;
    }

    public int djikstra(Arret depart, Arret arrivee, List<Ligne> lesLignes) {


        return 0;
    }
}
