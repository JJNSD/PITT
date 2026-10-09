package metiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graphe {
    private Map<Arret, Map<Arret, List<Map<Ligne,Integer>>>> leGraphe;

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

        leGraphe.get(depart).put(arrivee,new ArrayList<>());
        leGraphe.get(depart).get(arrivee).add(new HashMap<>());
        leGraphe.get(depart).get(arrivee).get(leGraphe.get(depart).get(arrivee).size()-1).put(ligne,score);
    }

    public void ajouterCheminMAJSiExistant(Arret depart, Arret arrivee, Ligne ligne, int score) {
        if (this.leGraphe.containsKey(depart) && this.leGraphe.get(depart).containsKey(arrivee)) {
            for (Map<Ligne,Integer> data : this.leGraphe.get(depart).get(arrivee)) {
                if (data.containsKey(ligne)) {
                    data.replace(ligne, score);
                }
            }
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
    public int bfs(Arret depart, Arret arrivee) {
        return 0;
    }
}
