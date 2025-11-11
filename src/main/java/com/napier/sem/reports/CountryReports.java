package com.napier.sem.reports;

import com.napier.sem.Database;
import com.napier.sem.models.Country;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CountryReports {
    private final Database db;

    public CountryReports(Database db) { this.db = db; }

    // Top N countries worldwide by population
    public List<Country> getTopCountriesWorldwide(int limit) {
        String sql =
                "SELECT Code, Name, Continent, Region, Population, Capital " +
                        "FROM country " +
                        "ORDER BY Population DESC " +
                        "LIMIT ?";

        ArrayList<Country> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Country c = new Country();
                    c.code = rs.getString("Code");
                    c.name = rs.getString("Name");
                    c.continent = rs.getString("Continent");
                    c.region = rs.getString("Region");
                    c.population = rs.getInt("Population");
                    int cap = rs.getInt("Capital");
                    c.capitalId = rs.wasNull() ? null : cap;
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("getTopCountriesWorldwide error: " + e.getMessage());
        }
        return list;
    }

    public void printCountries(List<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries");
            return;
        }
        System.out.printf("%-5s %-40s %-13s %-23s %-12s %-8s%n",
                "Code", "Name", "Continent", "Region", "Population", "CapitalId");
        for (Country c : countries) {
            System.out.printf("%-5s %-40s %-13s %-23s %-12d %-8s%n",
                    c.code, c.name, c.continent, c.region, c.population,
                    c.capitalId == null ? "NULL" : c.capitalId.toString());
        }
    }
}
