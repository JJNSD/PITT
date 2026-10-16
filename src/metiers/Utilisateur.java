package metiers;

public class Utilisateur {
    private String username;
    private String password;
    private String prenom;
    private String nom;
    private boolean isAdmin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public Utilisateur(String username, String password, String prenom, String nom, boolean isAdmin) {
        this.username = username;
        this.password = password;
        this.prenom = prenom;
        this.nom = nom;
        this.isAdmin = isAdmin;
    }
}
