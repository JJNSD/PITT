package metiers;

import models.DesserteDAO;
import models.LigneDAO;
import models.TypeTransportDAO;

import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


public class Graphe {
    private final int coutCorrespondance = 1;

    // Facteurs pour calculer le score => 0 à 10
    private int facteurDuree;
    private int facteurDistance;
    private int facteurCoutMonetaire;

    private Map<Arret, Map<Arret, Map<Ligne,Integer>>> leGraphe;

    public Graphe(int facteurDuree, int facteurDistance, int facteurCoutMonetaire) {
        leGraphe = new HashMap<>();
        this.facteurDuree = facteurDuree;
        this.facteurDistance = facteurDistance;
        this.facteurCoutMonetaire = facteurCoutMonetaire;
    }

    public void ajouterSommet(Arret sommet) {
        leGraphe.put(sommet, new HashMap<>());
    }

    public void ajouterChemin(Arret depart, Arret arrivee, Ligne ligne, int score) {
        // Vérification de la présence des arrêts
        if (leGraphe.get(depart) == null) {
            ajouterSommet(depart);
        }
        if (leGraphe.get(arrivee) == null) {
            ajouterSommet(arrivee);
        }

        // Vérification si un premier chemin existe entre les deux arrêts, toutes lignes confondues
        if (leGraphe.get(depart).get(arrivee) == null) {
            leGraphe.get(depart).put(arrivee,new HashMap<>());
        }

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
        return leGraphe.entrySet().stream()
            .map(g -> {
                String chemin = g.getValue().entrySet().stream()
                    .map(voisin -> voisin.getKey() + " : {"
                        + voisin.getValue().entrySet().stream()
                        .map(l -> "Ligne " + l.getKey() + " : " + l.getValue())
                        .collect(Collectors.joining(", "))
                        + "}")
                    .collect(Collectors.joining(", "));

                return "{" + g.getKey() + ", {" + (chemin.isEmpty() ? " " : chemin) + "} }";
            })
            .collect(Collectors.joining("\n", "", "\n"));
    }

    // ---------------- Chargement du graphe depuis la BDD ----------------

    public boolean chargerGrapheBDD() throws Exception {
        DesserteDAO desserteDAO = new DesserteDAO();
        LigneDAO ligneDAO = new LigneDAO();
        try {
            List<Desserte> lesDessertes = desserteDAO.getAllDessertes();
            for (Desserte uneDesserte : lesDessertes) {
                // Gestion du coût monétaire selon le type
                float prix = uneDesserte.getLigne().getTypeTransport().getCout();
                if (uneDesserte.getLigne().getTypeTransport().getNomType().equals("bus") || uneDesserte.getLigne().getTypeTransport().getNomType().equals("tram")) {
                    prix *= (uneDesserte.getDuree()/60)+1;
                } else if (uneDesserte.getLigne().getTypeTransport().getNomType().equals("train")) {
                    prix *= uneDesserte.getDistance();
                }

                // Calcul du score
                int score = (facteurDuree * uneDesserte.getDuree()) + (facteurDistance * uneDesserte.getDistance() + (facteurCoutMonetaire * (int) prix));

                // Ajout du chemin en BDD
                this.ajouterCheminMAJSiExistant(uneDesserte.getArretPrecedent(), uneDesserte.getArretSuivant(), uneDesserte.getLigne(), score);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- Algos de recherche ----------------

    public int distanceVoisin(Arret depart, Arret arrivee, Ligne ligne) {
        if (leGraphe.containsKey(depart)
                && leGraphe.get(depart).containsKey(arrivee)
                && leGraphe.get(depart).get(arrivee).containsKey(ligne)) {
            return leGraphe.get(depart).get(arrivee).get(ligne);
        }
        return -1;
    }

    public List<List<Object>> cheminBfs(Arret depart, Arret arrivee) {
        if (depart == null || arrivee == null
                || !leGraphe.containsKey(depart) || !leGraphe.containsKey(arrivee)) {
            return null;
        }
        LocalDateTime maintenant = LocalDateTime.now();

        Queue<Arret> file = new ArrayDeque<>();
        Set<Arret> visites = new HashSet<>();
        Map<Arret, Arret> predecesseurs = new HashMap<>();
        Map<Arret, Ligne> lignesUtilisees = new HashMap<>();

        file.add(depart);
        visites.add(depart);

        while (!file.isEmpty()) {
            Arret courant = file.poll();
            if (courant.equals(arrivee)) {
                LinkedList<List<Object>> chemin = new LinkedList<>();
                Arret arret = arrivee;
                while (!arret.equals(depart)) {
                    chemin.addFirst(Arrays.asList(arret, lignesUtilisees.get(arret)));
                    arret = predecesseurs.get(arret);
                }
                chemin.addFirst(Arrays.asList(depart, null));
                return chemin;
            }
            for (Map.Entry<Arret, Map<Ligne, Integer>> v : leGraphe.get(courant).entrySet()) {
                Arret voisin = v.getKey();
                if (voisin.estPerturbe(maintenant)) {
                    continue;
                }
                if (visites.add(voisin)) {
                    predecesseurs.put(voisin, courant);

                    // ligne la moins chere entre courant et voisin
                    Ligne meilleure = null;
                    int min = Integer.MAX_VALUE;
                    for (Map.Entry<Ligne, Integer> c : v.getValue().entrySet()) {
                        if (c.getValue() >= 0 && c.getValue() < min) {
                            min = c.getValue();
                            meilleure = c.getKey();
                        }
                    }
                    lignesUtilisees.put(voisin, meilleure);

                    file.add(voisin);
                }
            }
        }
        return null;
    }

    /** Dijkstra : heuristique nulle. */
    public List<List<Object>> cheminDjikstra(Arret depart, Arret arrivee, List<Ligne> lesLignes) {
        return cheminPlusCourt(depart, arrivee, lesLignes, 0);
    }

    /** A* avec un coef calcule automatiquement (admissible et coherent). */
    public List<List<Object>> cheminAStar(Arret depart, Arret arrivee, List<Ligne> lesLignes) {
        return cheminPlusCourt(depart, arrivee, lesLignes, coefficientAdmissible(lesLignes));
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
     * Coeur commun Dijkstra / A* avec cout de correspondance :
     * changer de ligne a un arret ajoute coutCorrespondance.
     *
     * Un etat est un couple Arrays.asList(arret, ligne) : ligne = ligne utilisee pour
     * arriver a l'arret (null au depart). Arrays.asList a un equals/hashCode corrects
     * et accepte null, donc il sert de cle dans les Map et dans la file.
     *
     * @return liste d'etapes [arret, ligne] du depart a l'arrivee, ou null
     */
    private List<List<Object>> cheminPlusCourt(Arret depart, Arret arrivee, List<Ligne> lesLignes, double coef) {
        if (depart == null || arrivee == null
                || !leGraphe.containsKey(depart) || !leGraphe.containsKey(arrivee)) {
            return null;
        }
        LocalDateTime maintenant = LocalDateTime.now();

        Map<List<Object>, Integer> gScore = new HashMap<>();
        Map<List<Object>, Double> fScore = new HashMap<>();
        Map<List<Object>, List<Object>> predecesseurs = new HashMap<>();

        PriorityQueue<List<Object>> file = new PriorityQueue<>(
                Comparator.comparingDouble((List<Object> e) -> fScore.getOrDefault(e, Double.MAX_VALUE)));

        List<Object> etapeDepart = Arrays.asList(depart, null); // pas de ligne au depart
        gScore.put(etapeDepart, 0);
        fScore.put(etapeDepart, heuristique(depart, arrivee, coef));
        file.add(etapeDepart);

        while (!file.isEmpty()) {
            List<Object> courant = file.poll();
            Arret arretCourant = (Arret) courant.get(0);
            Ligne ligneCourante = (Ligne) courant.get(1);

            if (arretCourant.equals(arrivee)) {
                LinkedList<List<Object>> chemin = new LinkedList<>();
                List<Object> e = courant;
                chemin.addFirst(e);
                while (predecesseurs.get(e) != null) {
                    e = predecesseurs.get(e);
                    chemin.addFirst(e);
                }
                return chemin;
            }

            for (Map.Entry<Arret, Map<Ligne, Integer>> v : leGraphe.get(arretCourant).entrySet()) {
                Arret voisin = v.getKey();
                if (voisin.estPerturbe(maintenant)) {
                    continue;
                }
                for (Map.Entry<Ligne, Integer> c : v.getValue().entrySet()) {
                    Ligne ligne = c.getKey();
                    boolean autorisee = lesLignes == null || lesLignes.isEmpty() || lesLignes.contains(ligne);
                    if (!autorisee || c.getValue() < 0) {
                        continue;
                    }

                    int correspondance = (ligneCourante != null && !ligneCourante.equals(ligne))
                            ? coutCorrespondance : 0;
                    long nouveauCout = (long) gScore.get(courant) + c.getValue() + correspondance;
                    if (nouveauCout > Integer.MAX_VALUE) {
                        continue;
                    }

                    List<Object> suivant = Arrays.asList(voisin, ligne);
                    if (nouveauCout < gScore.getOrDefault(suivant, Integer.MAX_VALUE)) {
                        file.remove(suivant); // AVANT de modifier fScore
                        gScore.put(suivant, (int) nouveauCout);
                        fScore.put(suivant, nouveauCout + heuristique(voisin, arrivee, coef));
                        predecesseurs.put(suivant, courant);
                        file.add(suivant);
                    }
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
