package metiers;

import java.util.*;


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

    /** A* avec un coef calcule automatiquement (admissible et coherent). */
    public List<Arret> cheminAStar(Arret depart, Arret arrivee, List<Ligne> lesLignes) {
        return cheminPlusCourt(depart, arrivee, lesLignes, coefficientAdmissible(lesLignes));
    }

    /** Dijkstra : heuristique nulle. */
    public List<Arret> cheminDjikstra(Arret depart, Arret arrivee, List<Ligne> lesLignes) {
        return cheminPlusCourt(depart, arrivee, lesLignes, 0);
    }

    /**
     * coef = min sur les aretes autorisees de (score / distance a vol d'oiseau).
     * Heuristique admissible et coherente.
     */
    public double coefficientAdmissible(List<Ligne> lesLignes) {
        double coef = Double.MAX_VALUE;
        for (Map.Entry<Arret, Map<Arret, Map<Ligne, Integer>>> e : leGraphe.entrySet()) {
            for (Map.Entry<Arret, Map<Ligne, Integer>> f : e.getValue().entrySet()) {
                double d = haversine(e.getKey(), f.getKey());
                if (d <= 0) {
                    continue;
                }
                for (Map.Entry<Ligne, Integer> c : f.getValue().entrySet()) {
                    boolean autorisee = lesLignes == null || lesLignes.isEmpty() || lesLignes.contains(c.getKey());
                    if (autorisee && c.getValue() >= 0) {
                        coef = Math.min(coef, c.getValue() / d);
                    }
                }
            }
        }
        return coef == Double.MAX_VALUE ? 0 : coef;
    }

    /**
     * Coeur commun Dijkstra / A*.
     * Un sommet est retire de la file AVANT que son fScore change
     */
    private List<Arret> cheminPlusCourt(Arret depart, Arret arrivee, List<Ligne> lesLignes, double coef) {
        if (depart == null || arrivee == null
                || !leGraphe.containsKey(depart) || !leGraphe.containsKey(arrivee)) {
            return null;
        }

        Map<Arret, Integer> gScore = new HashMap<>();
        Map<Arret, Double> fScore = new HashMap<>();
        Map<Arret, Arret> predecesseurs = new HashMap<>();

        PriorityQueue<Arret> file = new PriorityQueue<>(
                Comparator.comparingDouble(a -> fScore.getOrDefault(a, Double.MAX_VALUE)));

        gScore.put(depart, 0);
        fScore.put(depart, heuristique(depart, arrivee, coef));
        file.add(depart);

        while (!file.isEmpty()) {
            Arret courant = file.poll();

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

            for (Map.Entry<Arret, Map<Ligne, Integer>> v : leGraphe.get(courant).entrySet()) {
                Arret voisin = v.getKey();

                // poids minimal parmi les lignes autorisees
                int coutTrajet = Integer.MAX_VALUE;
                for (Map.Entry<Ligne, Integer> c : v.getValue().entrySet()) {
                    boolean autorisee = lesLignes == null || lesLignes.isEmpty() || lesLignes.contains(c.getKey());
                    if (autorisee && c.getValue() >= 0) {
                        coutTrajet = Math.min(coutTrajet, c.getValue());
                    }
                }
                if (coutTrajet == Integer.MAX_VALUE) {
                    continue;
                }

                long nouveauCout = (long) gScore.get(courant) + coutTrajet;
                if (nouveauCout > Integer.MAX_VALUE) {
                    continue;
                }

                if (nouveauCout < gScore.getOrDefault(voisin, Integer.MAX_VALUE)) {
                    file.remove(voisin); // AVANT de modifier fScore
                    gScore.put(voisin, (int) nouveauCout);
                    fScore.put(voisin, nouveauCout + heuristique(voisin, arrivee, coef));
                    predecesseurs.put(voisin, courant);
                    file.add(voisin);
                }
            }
        }
        return null;
    }

    // Distance a vol d'oiseau (Haversine) en km
    private double haversine(Arret a, Arret b) {
        double R = 6372.8;
        double dLat = Math.toRadians(b.getLattitude() - a.getLattitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.pow(Math.sin(dLon / 2), 2)
                * Math.cos(Math.toRadians(a.getLattitude()))
                * Math.cos(Math.toRadians(b.getLattitude()));
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    private double heuristique(Arret depart, Arret arrivee, double coef) {
        return coef == 0 ? 0 : haversine(depart, arrivee) * coef;
    }

}
