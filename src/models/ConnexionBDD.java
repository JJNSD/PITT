package models;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.Properties;

public class ConnexionBDD {
    private static Connection instanceConnexion = null;

    public static Connection getConnexion() {
        if (instanceConnexion == null) {
            // Parametres de connexion : url, login, mdp
            Properties properties = new Properties();

            Dotenv dotenv = Dotenv.configure().load();

            // récupération des données pour l'URL
            String host = dotenv.get("IP_DATABASE_SRV");
            String port = dotenv.get("PORT_DATABASE");
            String databaseName = dotenv.get("DATABASE_NAME");

            String url = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;

            // Récupération des données
            String login = dotenv.get("LOGIN_DATABASE");
            String password = dotenv.get("PASSWORD_DATABASE");

            // Creation d'une connexion
            properties.setProperty("user", login);
            properties.setProperty("password", password);

            try {
                instanceConnexion = DriverManager.getConnection(url, properties);
            } catch (
                    SQLException e1) {
                System.err.println("Erreur de parcours de connexion");
                e1.printStackTrace();
            }
        }

        return instanceConnexion;
    }
}
