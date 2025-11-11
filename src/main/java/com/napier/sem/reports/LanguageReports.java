package com.napier.sem.reports;

import com.napier.sem.Database;
import com.napier.sem.models.CountryLanguage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LanguageReports {
    private final Database db;

    public LanguageReports(Database db) { this.db = db; }

    // Big 5 languages by total speakers (Population * Percentage)
    public List<CountryLanguage> getTopFiveLanguagesBySpeakers() {
        String sql =
                "SELECT cl.Language, " +
                        "       SUM(ROUND(co.Population * cl.Percentage / 100)) AS Speakers " +
                        "FROM countrylanguage cl " +
                        "JOIN country co ON cl.CountryCode = co.Code " +
                        "WHERE cl.Language IN ('Chinese','English','Hindi','Spanish','Arabic') " +
                        "GROUP BY cl.Language " +
                        "ORDER BY Speakers DESC";

        ArrayList<CountryLanguage> list = new ArrayList<>();
        try (Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                CountryLanguage l = new CountryLanguage();
                l.language = rs.getString("Language");
                l.speakers = rs.getLong("Speakers");
                list.add(l);
            }
        } catch (SQLException e) {
            System.out.println("getTopFiveLanguagesBySpeakers error: " + e.getMessage());
        }
        return list;
    }

    public void printLanguages(List<CountryLanguage> langs) {
        if (langs == null || langs.isEmpty()) {
            System.out.println("No languages");
            return;
        }
        System.out.printf("%-15s %-15s%n", "Language", "Speakers");
        for (CountryLanguage l : langs) {
            System.out.printf("%-15s %-15d%n", l.language, l.speakers);
        }
    }
}
