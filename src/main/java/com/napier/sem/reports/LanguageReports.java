package com.napier.sem.reports;

import com.napier.sem.Database;
import com.napier.sem.models.CountryLanguage;

import java.sql.*;
import java.util.ArrayList;

public class LanguageReports {
    private Database db;

    public LanguageReports(Database db) {
        this.db = db;
    }

    public ArrayList<CountryLanguage> getTopFiveLanguagesBySpeakers() {
        ArrayList<CountryLanguage> langs = new ArrayList<>();

        try {
            String sql = "SELECT cl.Language, SUM(ROUND(co.Population * cl.Percentage / 100)) AS Speakers "
                    + "FROM countrylanguage cl JOIN country co ON cl.CountryCode = co.Code "
                    + "WHERE cl.Language IN ('Chinese','English','Hindi','Spanish','Arabic') "
                    + "GROUP BY cl.Language ORDER BY Speakers DESC";

            Statement stmt = db.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                CountryLanguage l = new CountryLanguage();
                l.language = rs.getString("Language");
                l.speakers = rs.getLong("Speakers");
                langs.add(l);
            }

        } catch (Exception e) {
            System.out.println("Error getting top languages: " + e.getMessage());
        }

        return langs;
    }

    public void printLanguages(ArrayList<CountryLanguage> langs) {
        System.out.println("Top Languages by Speakers");
        System.out.println("Language | Speakers");

        for (CountryLanguage l : langs) {
            System.out.println(l.language + " | " + l.speakers);
        }
    }
}
