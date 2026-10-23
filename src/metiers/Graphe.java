package metiers;

import models.*;

import java.time.LocalTime;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


public class Graphe {
    private static Graphe grapheObj;

    private final int coutCorrespondance = 1000;

    // Facteurs pour calculer le score => 0 à 10
    private int facteurDuree;
    private int facteurDistance;
    private int facteurCoutMonetaire;

    private List<Horaire> listeHoraires = new ArrayList<>();

    private Map<Arret, Map<Arret, Map<Ligne,Integer>>> leGraphe;

    private Graphe(int facteurDuree, int facteurDistance, int facteurCoutMonetaire) {
        leGraphe = new HashMap<>();
        this.facteurDuree = facteurDuree;
        this.facteurDistance = facteurDistance;
        this.facteurCoutMonetaire = facteurCoutMonetaire;
    }

    public static Graphe getGraphe(int facteurDuree, int facteurDistance, int facteurCoutMonetaire) {
        if (grapheObj == null) {
            grapheObj = new Graphe(facteurDuree, facteurDistance, facteurCoutMonetaire);
        }

        return grapheObj;
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

    public Arret getArretSuivant(Arret arretPrecedent, Arret arretActuel, Ligne ligne) {
        for (Map.Entry<Arret, Map<Ligne, Integer>> arretSuivant : leGraphe.get(arretActuel).entrySet()) {
            if (arretPrecedent == null && arretSuivant.getValue().get(ligne) != null) {
                return arretSuivant.getKey();
            } else if (arretPrecedent != null && arretPrecedent.getId() != arretSuivant.getKey().getId() && arretSuivant.getValue().get(ligne) != null) {
                return arretSuivant.getKey();
            }
        }
        return null;
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

            long startTime = System.nanoTime();

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

            long endTime = System.nanoTime();
            System.out.println("Temps de chargement Graphe : " + (endTime - startTime) + " ns");

            boolean horairesChargees = chargerHoraire();
            if (!horairesChargees) {
                System.out.println("Graphe chargé sans les horaires...");
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean chargerHoraire() throws Exception {
        LigneDAO ligneDAO = new LigneDAO();
        DesserteDAO desserteDAO = new DesserteDAO();
        HoraireDepartDAO horaireDepartDAO = new HoraireDepartDAO();

        try {
            int cpt = 0;
            List<Ligne> lesLignes = ligneDAO.getAllLigne();
            desserteDAO.getAllDessertes(); // Mise en cache de toutes les dessertes

            long startTime = System.nanoTime();

            for (Ligne ligne : lesLignes) {
                List<HoraireDepart> lesDeparts = horaireDepartDAO.getAllHoraireDepartByLigne(ligne);
                int avant = cpt;

                Arret[][] sens = {
                        {ligne.getTerminusDepart(), ligne.getTerminusArrivee()},
                        {ligne.getTerminusArrivee(), ligne.getTerminusDepart()}
                };

                for (Arret[] parcours : sens) {
                    Arret debut = parcours[0];
                    Arret fin = parcours[1];

                    LinkedList<Arret> chemin = new LinkedList<>();

                    // --- CAS 1 : Ligne circulaire (ex: 5a / 5b, AquaVita -> AquaVita) ---
                    if (debut.getId() == fin.getId()) {
                        Arret courant = debut;
                        Arret precedentArret = null;
                        chemin.add(courant);

                        Map<Integer, Integer> nbVisites = new HashMap<>();
                        nbVisites.put(debut.getId(), 1);

                        while (true) {
                            Map<Arret, Map<Ligne, Integer>> voisins = leGraphe.get(courant);
                            if (voisins == null) break;

                            List<Arret> candidatsValides = new ArrayList<>();

                            for (Map.Entry<Arret, Map<Ligne, Integer>> v : voisins.entrySet()) {
                                if (v.getValue().containsKey(ligne)) {
                                    Arret candidate = v.getKey();
                                    int idC = candidate.getId();

                                    // On évite de retourner immédiatement sur l'arrêt précédent (sauf rebroussement validé)
                                    if (precedentArret != null && candidate.getId() == precedentArret.getId()) {
                                        // Autorisé uniquement si c'est un rebroussement en fin d'antenne (ex: Neruda 202 ou Auriol 78)
                                        if (courant.getId() != 202 && courant.getId() != 78) {
                                            continue;
                                        }
                                    }

                                    int maxVisitesAutorisees = 1;
                                    // Arrêts autorisés à 2 visites (rebroussement)
                                    if (idC == 201 || idC == 78 || idC == 202) {
                                        maxVisitesAutorisees = 2;
                                    }

                                    int visitesActuelles = nbVisites.getOrDefault(idC, 0);

                                    // Condition de bouclage vers le terminus initial si le chemin est assez long
                                    if (idC == debut.getId() && chemin.size() > 5) {
                                        candidatsValides.add(candidate);
                                        continue;
                                    }

                                    if (visitesActuelles < maxVisitesAutorisees) {
                                        candidatsValides.add(candidate);
                                    }
                                }
                            }

                            if (candidatsValides.isEmpty()) {
                                break;
                            }

                            // Tri pour un comportement déterministe (priorité aux arrêts jamais visités ou aux antennes)
                            candidatsValides.sort((a, b) -> {
                                int vA = nbVisites.getOrDefault(a.getId(), 0);
                                int vB = nbVisites.getOrDefault(b.getId(), 0);
                                if (vA != vB) {
                                    return Integer.compare(vA, vB);
                                }
                                // Si même nombre de visites, on priorise l'antenne spécifique d'Orgemont (202)
                                if (a.getId() == 202) return -1;
                                if (b.getId() == 202) return 1;
                                return Integer.compare(a.getId(), b.getId());
                            });

                            Arret suivant = candidatsValides.get(0);

                            // Si on boucle sur le début et qu'on a fait le tour
                            if (suivant.getId() == debut.getId() && chemin.size() > 5) {
                                chemin.add(suivant);
                                break;
                            }

                            precedentArret = courant;
                            courant = suivant;
                            chemin.add(courant);
                            nbVisites.put(courant.getId(), nbVisites.getOrDefault(courant.getId(), 0) + 1);
                        }

                        if (chemin.size() <= 1) {
                            System.out.println("Ligne " + ligne + " circulaire : aucun chemin valide trouvé");
                            break;
                        }
                    } else {
                        Map<Arret, Arret> precedent = new HashMap<>();
                        Queue<Arret> file = new ArrayDeque<>();
                        Arret finTrouvee = null;
                        precedent.put(debut, null);
                        file.add(debut);

                        while (!file.isEmpty() && finTrouvee == null) {
                            Arret courant = file.poll();
                            Map<Arret, Map<Ligne, Integer>> voisins = leGraphe.get(courant);
                            if (voisins == null) {
                                continue;
                            }
                            for (Map.Entry<Arret, Map<Ligne, Integer>> v : voisins.entrySet()) {
                                Arret voisin = v.getKey();
                                if (!v.getValue().containsKey(ligne) || precedent.containsKey(voisin)) {
                                    continue;
                                }
                                precedent.put(voisin, courant);
                                if (voisin.getId() == fin.getId()) {
                                    finTrouvee = voisin;
                                    break;
                                }
                                file.add(voisin);
                            }
                        }

                        if (finTrouvee == null) {
                            System.out.println("Ligne " + ligne + " : aucun trajet " + debut + " -> " + fin + " dans le graphe");
                            continue;
                        }

                        // Reconstruction du chemin
                        for (Arret a = finTrouvee; a != null; a = precedent.get(a)) {
                            chemin.addFirst(a);
                        }
                    }

                    System.out.println("Chemin trouvé pour ligne " + ligne + " : " + chemin);

                    // --- Calcul des décalages et création des horaires ---
                    List<Arret> intermediaires = new ArrayList<>();
                    List<Integer> decalages = new ArrayList<>();
                    int decalage = 0;

                    // Pour les intermédiaires, on ne prend pas le dernier arrêt (déjà terminus)
                    for (int i = 1; i < chemin.size() - 1; i++) {
                        Desserte desserte = desserteDAO.getDureeDistance(chemin.get(i - 1).getId(), chemin.get(i).getId(), ligne.getId());
                        decalage += (desserte != null) ? desserte.getDuree() : 0;
                        intermediaires.add(chemin.get(i));
                        decalages.add(decalage);
                    }

                    // Génération des horaires par passage
                    for (HoraireDepart horaireDepart : lesDeparts) {
                        for (int i = 0; i < intermediaires.size(); i++) {
                            LocalTime heure = horaireDepart.getHeureDepart().plusMinutes(decalages.get(i));
                            this.listeHoraires.add(new Horaire(cpt, ligne, heure, intermediaires.get(i), horaireDepart.getTypeHoraire()));
                            cpt++;
                        }
                    }

                    // Pour une ligne circulaire, un seul passage de boucle suffit
                    if (debut.getId() == fin.getId()) {
                        break;
                    }
                }
                System.out.println("Ligne " + ligne + " : " + (cpt - avant) + " horaires");
            }

            long endTime = System.nanoTime();
            System.out.println("Temps de chargement Horaires " + (endTime - startTime) + " ns");
            System.out.println(listeHoraires.size() + " horaires chargés");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ---------------- Gestion des horaires et durées via BDD ----------------

    private LocalTime getProchainHoraire(Arret arret, Ligne ligne, LocalTime heureMin) {
        LocalTime meilleur = null;
        for (Horaire h : listeHoraires) {
            if (h.getArretAffecte().getId() == arret.getId() && h.getLigneAffecte().getId() == ligne.getId()) {
                LocalTime t = h.getHeure();
                if (t != null && !t.isBefore(heureMin)) {
                    if (meilleur == null || t.isBefore(meilleur)) {
                        meilleur = t;
                    }
                }
            }
        }
        return (meilleur != null) ? meilleur : heureMin;
    }

    private int getTempsAttente(Arret arret, Ligne ligne, LocalTime heureCourante) {
        LocalTime prochain = getProchainHoraire(arret, ligne, heureCourante);
        if (prochain.isBefore(heureCourante)) return 0;
        return (int) java.time.Duration.between(heureCourante, prochain).toMinutes();
    }

    // Requête directe en BDD pour récupérer la vraie durée en minutes
    private int getDureeReelle(Arret depart, Arret arrivee, Ligne ligne) {
        try {
            DesserteDAO desserteDAO = new DesserteDAO();
            Desserte desserte = desserteDAO.getDureeDistance(depart.getId(), arrivee.getId(), ligne.getId());
            if (desserte != null) {
                return desserte.getDuree();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1; // Valeur par défaut si non trouvée
    }

    // ---------------- Algos de recherche ----------------

    public int scoreVoisin(Arret depart, Arret arrivee, Ligne ligne) {
        if (leGraphe.containsKey(depart)
                && leGraphe.get(depart).containsKey(arrivee)
                && leGraphe.get(depart).get(arrivee).containsKey(ligne)) {
            return leGraphe.get(depart).get(arrivee).get(ligne);
        }
        return -1;
    }

    public List<List<Object>> cheminBfs(Arret depart, Arret arrivee, LocalTime heure) {
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

    public List<List<Object>> cheminDjikstra(Arret depart, Arret arrivee, List<Ligne> lesLignes, LocalTime heure) {
        return cheminPlusCourt(depart, arrivee, lesLignes, 0, heure);
    }

    public List<List<Object>> cheminAStar(Arret depart, Arret arrivee, List<Ligne> lesLignes, LocalTime heure) {
        return cheminPlusCourt(depart, arrivee, lesLignes, coefficientAdmissible(lesLignes), heure);
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
    private List<List<Object>> cheminPlusCourt(Arret depart, Arret arrivee, List<Ligne> lesLignes, double coef, LocalTime heure) {
        if (depart == null || arrivee == null
                || !leGraphe.containsKey(depart) || !leGraphe.containsKey(arrivee)) {
            return null;
        }
        LocalTime heureDepart = (heure != null) ? heure : LocalTime.now();
        LocalDateTime maintenant = LocalDateTime.now();

        Map<List<Object>, Integer> gScore = new HashMap<>();
        Map<List<Object>, Double> fScore = new HashMap<>();
        Map<List<Object>, List<Object>> predecesseurs = new HashMap<>();
        Map<List<Object>, LocalTime> tempsArrivee = new HashMap<>();

        PriorityQueue<List<Object>> file = new PriorityQueue<>(
                Comparator.comparingDouble((List<Object> e) -> fScore.getOrDefault(e, Double.MAX_VALUE)));

        List<Object> etapeDepart = Arrays.asList(depart, null);
        gScore.put(etapeDepart, 0);
        fScore.put(etapeDepart, heuristique(depart, arrivee, coef));
        tempsArrivee.put(etapeDepart, heureDepart);
        file.add(etapeDepart);

        while (!file.isEmpty()) {
            List<Object> courant = file.poll();
            Arret arretCourant = (Arret) courant.get(0);
            Ligne ligneCourante = (Ligne) courant.get(1);
            LocalTime heureActuelle = tempsArrivee.get(courant);

            if (arretCourant.equals(arrivee)) {
                LocalTime heureArriveeFinale = heureActuelle;
                long dureeTotaleMinutes = java.time.Duration.between(heureDepart, heureArriveeFinale).toMinutes();

                System.out.println("====== RÉSUMÉ DU TRAJET ======");
                System.out.println("Départ : " + heureDepart + " | Arrivée : " + heureArriveeFinale);
                System.out.println("Durée totale du trajet : " + dureeTotaleMinutes + " minutes");
                System.out.println("==============================");

                LinkedList<List<Object>> chemin = new LinkedList<>();
                List<Object> e = courant;
                // On ajoute l'arrêt, la ligne et son heure de passage
                chemin.addFirst(Arrays.asList(e.get(0), e.get(1), heureActuelle));

                while (predecesseurs.get(e) != null) {
                    e = predecesseurs.get(e);
                    LocalTime tempsEtape = tempsArrivee.get(e);
                    chemin.addFirst(Arrays.asList(e.get(0), e.get(1), tempsEtape));
                }
                return chemin;
            }

            for (Map.Entry<Arret, Map<Ligne, Integer>> v : leGraphe.get(arretCourant).entrySet()) {
                Arret voisin = v.getKey();
                if (voisin.estPerturbe(maintenant)) continue;

                for (Map.Entry<Ligne, Integer> c : v.getValue().entrySet()) {
                    Ligne ligne = c.getKey();
                    boolean autorisee = lesLignes == null || lesLignes.isEmpty() || lesLignes.contains(ligne);
                    if (!autorisee || c.getValue() < 0) continue;

                    int correspondance = 0;
                    int attente = 0;
                    LocalTime heureDepartLigne;

                    if (ligneCourante == null || !ligneCourante.equals(ligne)) {
                        if (ligneCourante != null) {
                            correspondance = coutCorrespondance;
                        }
                        attente = getTempsAttente(arretCourant, ligne, heureActuelle);
                        heureDepartLigne = getProchainHoraire(arretCourant, ligne, heureActuelle);
                        if (heureDepartLigne == null) {
                            heureDepartLigne = heureActuelle;
                        }
                    } else {
                        attente = 0;
                        heureDepartLigne = heureActuelle;
                    }

                    long nouveauCout = (long) gScore.get(courant) + c.getValue() + correspondance + attente;
                    if (nouveauCout > Integer.MAX_VALUE) {
                        continue;
                    }

                    int vraieDuree = getDureeReelle(arretCourant, voisin, ligne);
                    LocalTime heureVoisin = heureDepartLigne.plusMinutes(vraieDuree);

                    List<Object> suivant = Arrays.asList(voisin, ligne);
                    if (nouveauCout < gScore.getOrDefault(suivant, Integer.MAX_VALUE)) {
                        file.remove(suivant);
                        gScore.put(suivant, (int) nouveauCout);
                        fScore.put(suivant, nouveauCout + heuristique(voisin, arrivee, coef));
                        predecesseurs.put(suivant, courant);
                        tempsArrivee.put(suivant, heureVoisin);
                        file.add(suivant);
                    }
                }
            }
        }
        return null;
    }

    private double haversine(Arret a, Arret b) {
        double R = 6372.8;
        double dLat = Math.toRadians(b.getLattitude() - a.getLattitude());
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
        double h = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(a.getLattitude()))
                * Math.cos(Math.toRadians(b.getLattitude()))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    private double heuristique(Arret depart, Arret arrivee, double coef) {
        return coef == 0 ? 0 : haversine(depart, arrivee) * coef;
    }
}