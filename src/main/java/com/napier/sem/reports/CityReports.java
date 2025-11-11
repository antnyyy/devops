package com.napier.sem.reports;

import com.napier.sem.Database;
import com.napier.sem.models.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CityReports {
    private final Database db;

    public CityReports(Database db) { this.db = db; }

    // Top N cities worldwide by population
    public List<City> getTopCitiesWorldwide(int limit) {
        String sql =
                "SELECT ci.ID, ci.Name, co.Name AS Country, ci.District, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.CountryCode = co.Code " +
                        "ORDER BY ci.Population DESC " +
                        "LIMIT ?";

        ArrayList<City> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.country = rs.getString("Country");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("getTopCitiesWorldwide error: " + e.getMessage());
        }
        return list;
    }

    // Top N cities in a given continent
    public List<City> getTopCitiesByContinent(String continent, int limit) {
        String sql =
                "SELECT ci.ID, ci.Name, co.Name AS Country, ci.District, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.CountryCode = co.Code " +
                        "WHERE co.Continent = ? " +
                        "ORDER BY ci.Population DESC " +
                        "LIMIT ?";

        ArrayList<City> list = new ArrayList<>();
        try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
            ps.setString(1, continent);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.country = rs.getString("Country");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("getTopCitiesByContinent error: " + e.getMessage());
        }
        return list;
    }

    public void printCities(List<City> cities) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities");
            return;
        }
        System.out.printf("%-6s %-25s %-30s %-18s %-12s%n",
                "ID", "City", "Country", "District", "Population");
        for (City c : cities) {
            System.out.printf("%-6d %-25s %-30s %-18s %-12d%n",
                    c.id, c.name, c.country, c.district, c.population);
        }
    }
}
