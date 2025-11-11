package com.napier.sem.reports;

import com.napier.sem.Database;
import com.napier.sem.models.Country;

import java.sql.*;
import java.util.ArrayList;

public class CountryReports {
    private Database db;

    public CountryReports(Database db) {
        this.db = db;
    }

    public ArrayList<Country> getTopCountriesWorldwide(int limit) {
        ArrayList<Country> countries = new ArrayList<>();

        try {
            String sql = "SELECT Code, Name, Continent, Region, Population "
                    + "FROM country ORDER BY Population DESC LIMIT " + limit;

            Statement stmt = db.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Country c = new Country();
                c.code = rs.getString("Code");
                c.name = rs.getString("Name");
                c.continent = rs.getString("Continent");
                c.region = rs.getString("Region");
                c.population = rs.getInt("Population");
                countries.add(c);
            }

        } catch (Exception e) {
            System.out.println("Error getting top countries: " + e.getMessage());
        }

        return countries;
    }

    public void printCountries(ArrayList<Country> countries) {
        System.out.println("Top Countries in the World");
        System.out.println("Code | Name | Continent | Region | Population");

        for (Country c : countries) {
            System.out.println(c.code + " | " + c.name + " | " + c.continent + " | " + c.region + " | " + c.population);
        }
    }
}
