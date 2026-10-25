package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.Utilisateur;

class TestUtilisateur {

    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur(
                "testuser",
                "motdepasse123",
                "Jean",
                "Dupont",
                false
        );
    }

    @Test
    void testConstructeur() {
        assertEquals("testuser", utilisateur.getUsername());
        assertEquals("motdepasse123", utilisateur.getPassword());
        assertEquals("Jean", utilisateur.getPrenom());
        assertEquals("Dupont", utilisateur.getNom());
        assertFalse(utilisateur.isAdmin());
    }

    @Test
    void testGettersEtSetters() {
        utilisateur.setUsername("admin");
        utilisateur.setPassword("nouveaumotdepasse");
        utilisateur.setPrenom("Pierre");
        utilisateur.setNom("Martin");
        utilisateur.setAdmin(true);

        assertEquals("admin", utilisateur.getUsername());
        assertEquals("nouveaumotdepasse", utilisateur.getPassword());
        assertEquals("Pierre", utilisateur.getPrenom());
        assertEquals("Martin", utilisateur.getNom());
        assertTrue(utilisateur.isAdmin());
    }

    @Test
    void testUtilisateurAdministrateur() {
        Utilisateur admin = new Utilisateur(
                "admin",
                "motdepasse123",
                "Alice",
                "Durand",
                true
        );

        assertTrue(admin.isAdmin());
    }

    @Test
    void testUtilisateurNonAdministrateur() {
        Utilisateur user = new Utilisateur(
                "utilisateur",
                "motdepasse123",
                "Paul",
                "Bernard",
                false
        );

        assertFalse(user.isAdmin());
    }
}