package metiers;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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

    /**
     * Utilise l'algorithme BFS pour calculer un chemin entre 2 arrêts
     * @param depart
     * @param arrivee
     * @return Chemin avec le nombre d'arrêts le plus court
     */
    public List<Arret> cheminBfs(Arret depart, Arret arrivee) {
        if (depart == null || arrivee == null || !leGraphe.containsKey(depart) || !leGraphe.containsKey(arrivee)) {
            return null;
        }

        Queue<Arret> file = new ArrayDeque<>();
        Set<Arret> visites = new HashSet<>();
        Map<Arret, Arret> predecesseurs = new HashMap<>();

        file.add(depart);
        visites.add(depart);

        while (!file.isEmpty()) {
            Arret courant = file.poll();
            if (courant.equals(arrivee)) {
                LinkedList<Arret> chemin = new LinkedList<>();
                Arret arret = arrivee;
                chemin.addFirst(arret);
                while (!arret.equals(depart)) {
                    arret = predecesseurs.get(arret);
                    chemin.addFirst(arret);
                }
                return chemin;
            }
            for (Arret voisin : leGraphe.get(courant).keySet()) {
                if (visites.add(voisin)) {
                    predecesseurs.put(voisin, courant);
                    file.add(voisin);
                }
            }
        }
        return null;
    }

    /**
     * Recherche un chemin de cout minimal entre deux arrets.
     * Utilise une recherche de type A* avec une heuristique nulle.
     *
     * @param depart arret de depart
     * @param arrivee arret d'arrivee
     * @param lesLignes lignes autorisees ; null ou vide = toutes
     * @return chemin de cout minimal, ou null si aucun chemin
     */
    public List<Arret> cheminAStar(
            Arret depart,
            Arret arrivee,
            List<Ligne> lesLignes) {

        if (depart == null || arrivee == null
                || !leGraphe.containsKey(depart)
                || !leGraphe.containsKey(arrivee)) {
            return null;
        }

        // Cout minimal connu depuis le depart
        Map<Arret, Integer> gScore = new HashMap<>();

        // Predecesseur de chaque arret
        Map<Arret, Arret> predecesseurs = new HashMap<>();

        // File prioritaire : le plus petit cout est traite en premier
        PriorityQueue<Arret> file = new PriorityQueue<>(
                Comparator.comparingInt(
                        arret -> gScore.getOrDefault(
                                arret, Integer.MAX_VALUE
                        )
                )
        );

        Set<Arret> visites = new HashSet<>();

        gScore.put(depart, 0);
        file.add(depart);

        while (!file.isEmpty()) {

            Arret courant = file.poll();

            // Ignorer les arrets deja traites
            if (!visites.add(courant)) {
                continue;
            }

            // Arrivee atteinte : reconstruire le chemin
            if (courant.equals(arrivee)) {
                LinkedList<Arret> chemin = new LinkedList<>();
                Arret arret = arrivee;

                chemin.addFirst(arret);

                while (!arret.equals(depart)) {
                    arret = predecesseurs.get(arret);

                    if (arret == null) {
                        return null;
                    }

                    chemin.addFirst(arret);
                }

                return chemin;
            }

            // Explorer les voisins
            for (Arret voisin : leGraphe.get(courant).keySet()) {

                Map<Ligne, Integer> connexions =
                        leGraphe.get(courant).get(voisin);

                // Chercher le poids minimal parmi les lignes autorisees
                int coutTrajet = Integer.MAX_VALUE;

                for (Map.Entry<Ligne, Integer> connexion
                        : connexions.entrySet()) {

                    Ligne ligne = connexion.getKey();
                    int poids = connexion.getValue();

                    boolean ligneAutorisee =
                            lesLignes == null
                                    || lesLignes.isEmpty()
                                    || lesLignes.contains(ligne);

                    if (ligneAutorisee && poids >= 0) {
                        coutTrajet = Math.min(coutTrajet, poids);
                    }
                }

                // Aucune connexion autorisee
                if (coutTrajet == Integer.MAX_VALUE) {
                    continue;
                }

                long nouveauCout =
                        (long) gScore.get(courant) + coutTrajet;

                // Eviter un depassement de capacite d'un int
                if (nouveauCout > Integer.MAX_VALUE) {
                    continue;
                }

                int coutActuel = gScore.getOrDefault(
                        voisin, Integer.MAX_VALUE
                );

                if (nouveauCout < coutActuel) {

                    gScore.put(voisin, (int) nouveauCout);
                    predecesseurs.put(voisin, courant);

                    // Le voisin peut etre reconsidere
                    visites.remove(voisin);
                    file.remove(voisin);
                    file.add(voisin);
                }
            }
        }

        return null;
    }

    public List<Arret> cheminDjikstra(Arret depart, Arret arrivee, List<Ligne> lesLignes) {
        List<Arret> chemin = new ArrayList<>();



        return chemin;
    }
}
