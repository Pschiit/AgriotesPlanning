package fr.agriotes.planning.dao;

import fr.agriotes.planning.models.Date;
import fr.agriotes.planning.models.Module;
import fr.agriotes.planning.models.Personne;
import fr.agriotes.planning.models.Catalogue;
import fr.agriotes.planning.models.Session;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class CatalogueDao {

    public static Catalogue getPlanning() throws SQLException {
        Catalogue result = new Catalogue();

        //get formateur table
        HashMap<Integer, Personne> formateurs = new HashMap<>();
        String sql = "SELECT id_personne, nom, prenom, mail, est_admin "
                + "FROM personne  "
                + "WHERE id_personne IN ( "
                + "SELECT id_personne "
                + "FROM intervenant "
                + "GROUP BY id_personne);";
        Connection connection = Database.getConnection();
        Statement order = connection.createStatement();
        ResultSet rs = order.executeQuery(sql);
        if (rs.next()) {
            formateurs.put(rs.getInt("id_personne"), new Personne(rs.getInt("id_personne"), rs.getString("mail"), rs.getString("nom"), rs.getString("prenom"), rs.getBoolean("est_admin")));
        } else {
            throw new SQLException("Table formateur vide.");
        }
        while (rs.next()) {
            formateurs.put(rs.getInt("id_personne"), new Personne(rs.getInt("id_personne"), rs.getString("mail"), rs.getString("nom"), rs.getString("prenom"), rs.getBoolean("est_admin")));
        }
        order.close();

        //get intervenant table
        HashMap<Integer, ArrayList<Personne>> intervenants = new HashMap<>();
        sql = "SELECT * FROM intervenant;";
        order = connection.createStatement();
        rs = order.executeQuery(sql);
        if (rs.next()) {
            intervenants.put(rs.getInt("id_module"), new ArrayList<Personne>());
            intervenants.get(rs.getInt("id_module")).add(formateurs.get(rs.getInt("id_personne")));
        } else {
            throw new SQLException("Table intervenant vide.");
        }
        while (rs.next()) {
            if (!intervenants.containsKey(rs.getInt("id_module"))) {
                intervenants.put(rs.getInt("id_module"), new ArrayList<Personne>());
            }
            intervenants.get(rs.getInt("id_module")).add(formateurs.get(rs.getInt("id_personne")));

        }
        order.close();

        //get module table
        HashMap<Integer, Module> modules = new HashMap<>();
        sql = "SELECT id_module, intitule, nb_jours "
                + "FROM module m "
                + "WHERE id_module IN ( "
                + "SELECT id_module "
                + "FROM module_formation "
                + "GROUP BY id_module);";
        order = connection.createStatement();
        rs = order.executeQuery(sql);
        if (rs.next()) {
            modules.put(rs.getInt("id_module"), new Module(rs.getInt("id_module"), rs.getString("intitule"), rs.getInt("nb_jours"), intervenants.get(rs.getInt("id_module"))));
        } else {
            throw new SQLException("Table module vide.");
        }
        while (rs.next()) {
            modules.put(rs.getInt("id_module"), new Module(rs.getInt("id_module"), rs.getString("intitule"), rs.getInt("nb_jours"), intervenants.get(rs.getInt("id_module"))));
        }
        order.close();

        //get module_formation table
        HashMap<Integer, ArrayList<Module>> moduleFormation = new HashMap<>();
        sql = "SELECT * FROM module_formation;";
        order = connection.createStatement();
        rs = order.executeQuery(sql);
        if (rs.next()) {
            moduleFormation.put(rs.getInt("id_formation"), new ArrayList<Module>());
            moduleFormation.get(rs.getInt("id_formation")).add(modules.get(rs.getInt("id_module")));
        } else {
            throw new SQLException("Table module_formation vide.");
        }
        while (rs.next()) {
            if (!moduleFormation.containsKey(rs.getInt("id_formation"))) {
                moduleFormation.put(rs.getInt("id_formation"), new ArrayList<Module>());
            }
            moduleFormation.get(rs.getInt("id_formation")).add(modules.get(rs.getInt("id_module")));

        }
        order.close();

        //get session table
        HashMap<Integer, Session> sessions = new HashMap<>();
        sql = "SELECT s.id_session, s.id_formation, s.date_debut,s.date_fin, f.intitule "
                + "FROM session s "
                + "INNER JOIN formation f "
                + "ON s.id_formation = f.id_formation "
                + "WHERE date_debut > SUBDATE(CURRENT_DATE(), INTERVAL 3 YEAR);";
        order = connection.createStatement();
        rs = order.executeQuery(sql);
        if (rs.next()) {
            sessions.put(rs.getInt("id_session"), new Session(rs.getInt("id_session"), rs.getString("intitule"), Date.FromSQLDate(rs.getDate("date_debut")), Date.FromSQLDate(rs.getDate("date_fin")), moduleFormation.get(rs.getInt("id_formation"))));
        } else {
            throw new SQLException("Table session vide.");
        }
        while (rs.next()) {
            sessions.put(rs.getInt("id_session"), new Session(rs.getInt("id_session"), rs.getString("intitule"), Date.FromSQLDate(rs.getDate("date_debut")), Date.FromSQLDate(rs.getDate("date_fin")), moduleFormation.get(rs.getInt("id_formation"))));
        }
        order.close();
        connection.close();

        result.setLesFormateurs(formateurs);
        result.setLesModules(modules);
        result.setLesSessions(sessions);
        return result;
    }
}