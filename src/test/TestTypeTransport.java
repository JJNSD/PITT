package test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import metiers.TypeTransport;

class TestTypeTransport {

    private TypeTransport typeTransport;

    @BeforeEach
    void setUp() {
        TypeTransport.resetCache();

        typeTransport = new TypeTransport(
                7001,
                "Bus",
                1.70f
        );
    }

    @Test
    void testConstructeur() {
        assertEquals(7001, typeTransport.getId());
        assertEquals("Bus", typeTransport.getNomType());
        assertEquals(1.70f, typeTransport.getCout(), 0.001f);
    }

    @Test
    void testGettersEtSetters() {
        typeTransport.setId(7002);
        typeTransport.setNomType("Tram");
        typeTransport.setCout(1.80f);

        assertEquals(7002, typeTransport.getId());
        assertEquals("Tram", typeTransport.getNomType());
        assertEquals(1.80f, typeTransport.getCout(), 0.001f);
    }

    @Test
    void testGetTypeTransportEnCache() {
        TypeTransport resultat =
                TypeTransport.getTypeTransportEnCache(7001);

        assertNotNull(resultat);
        assertSame(typeTransport, resultat);
    }

    @Test
    void testTypeTransportAbsentDuCache() {
        TypeTransport resultat =
                TypeTransport.getTypeTransportEnCache(-7001);

        assertNull(resultat);
    }

    @Test
    void testSupprimerDuCache() {
        TypeTransport.supprimerDuCache(typeTransport);

        TypeTransport resultat =
                TypeTransport.getTypeTransportEnCache(7001);

        assertNull(resultat);
    }
}