
package test;

import models.UtilisateurDAO;
import metiers.Utilisateur;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestUtilisateurDAO {

    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    @Test
    public void testGetAllUtilisateurs() throws Exception {
        List<Utilisateur> utilisateurs = utilisateurDAO.getAllUtilisateurs();

        assertNotNull(utilisateurs, "La liste ne doit pas être null");

        for (Utilisateur user : utilisateurs) {
            assertNotNull(user, "Un utilisateur ne doit pas être null");
            assertNotNull(user.getUsername(), "Le username ne doit pas être null");
            assertNotNull(user.getPrenom(), "Le prénom ne doit pas être null");
            assertNotNull(user.getNom(), "Le nom ne doit pas être null");

            System.out.println(
                    "Username : " + user.getUsername()
                            + " | Prénom : " + user.getPrenom()
                            + " | Nom : " + user.getNom()
                            + " | Admin : " + user.isAdmin()
            );
        }
    }

    @Test
    public void testGetUtilisateurByUsername() throws Exception {

        List<Utilisateur> utilisateurs = utilisateurDAO.getAllUtilisateurs();

        assertFalse(
                utilisateurs.isEmpty(),
                "La table doit contenir au moins un utilisateur"
        );

        Utilisateur utilisateurAttendu = utilisateurs.get(0);

        Utilisateur utilisateurTrouve =
                utilisateurDAO.getUtilisateurByUsername(
                        utilisateurAttendu.getUsername()
                );

        assertNotNull(
                utilisateurTrouve,
                "L'utilisateur doit être retrouvé"
        );

        assertEquals(
                utilisateurAttendu.getUsername(),
                utilisateurTrouve.getUsername()
        );

        assertEquals(
                utilisateurAttendu.getPrenom(),
                utilisateurTrouve.getPrenom()
        );

        assertEquals(
                utilisateurAttendu.getNom(),
                utilisateurTrouve.getNom()
        );

        assertEquals(
                utilisateurAttendu.isAdmin(),
                utilisateurTrouve.isAdmin()
        );
    }

    @Test
    public void testGetUtilisateurByUsernameInexistant() throws Exception {
        Utilisateur utilisateur =
                utilisateurDAO.getUtilisateurByUsername(
                        "utilisateur_inexistant_junit_987654"
                );

        assertNull(
                utilisateur,
                "Aucun utilisateur ne devrait correspondre à ce username"
        );
    }


    @Test
    public void testAddUpdateDeleteUser() throws Exception {

        String usernameInitial = "test_junit_utilisateur_987654";

        Utilisateur user = new Utilisateur(
                usernameInitial,
                "MotDePasseTest123!",
                "PrenomTest",
                "NomTest",
                false
        );

        try {
            // AJOUT
            boolean ajoutReussi = utilisateurDAO.addUser(user);

            assertTrue(ajoutReussi,
                    "L'ajout de l'utilisateur doit réussir");

            // Vérification de l'ajout
            Utilisateur utilisateurAjoute =
                    utilisateurDAO.getUtilisateurByUsername(usernameInitial);

            assertNotNull(utilisateurAjoute,
                    "L'utilisateur ajouté doit être retrouvé en base");

            assertEquals(usernameInitial, utilisateurAjoute.getUsername());
            assertEquals("PrenomTest", utilisateurAjoute.getPrenom());
            assertEquals("NomTest", utilisateurAjoute.getNom());
            assertFalse(utilisateurAjoute.isAdmin());

            // MODIFICATION
            Utilisateur utilisateurModifie = new Utilisateur(
                    usernameInitial,
                    "NouveauMotDePasse123!",
                    "PrenomModifie",
                    "NomModifie",
                    true
            );

            boolean modificationReussie =
                    utilisateurDAO.updateUser(utilisateurModifie);

            assertTrue(modificationReussie,
                    "La modification de l'utilisateur doit réussir");

            // Vérification de la modification
            Utilisateur utilisateurApresModification =
                    utilisateurDAO.getUtilisateurByUsername(usernameInitial);

            assertNotNull(utilisateurApresModification);

            assertEquals("NouveauMotDePasse123!",
                    utilisateurApresModification.getPassword());

            assertEquals("PrenomModifie",
                    utilisateurApresModification.getPrenom());

            assertEquals("NomModifie",
                    utilisateurApresModification.getNom());

            assertTrue(utilisateurApresModification.isAdmin());

            // SUPPRESSION
            boolean suppressionReussie =
                    utilisateurDAO.deleteUser(utilisateurApresModification);

            assertTrue(suppressionReussie,
                    "La suppression de l'utilisateur doit réussir");

            assertNull(utilisateurDAO.getUtilisateurByUsername(usernameInitial),
                    "L'utilisateur supprimé ne doit plus être retrouvé");

        } finally {
            // Nettoyage si le test échoue avant la suppression
            Utilisateur utilisateurRestant =
                    utilisateurDAO.getUtilisateurByUsername(usernameInitial);

            if (utilisateurRestant != null) {
                utilisateurDAO.deleteUser(utilisateurRestant);
            }
        }
    }

}
