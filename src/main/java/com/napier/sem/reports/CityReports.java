package com.napier.sem.reports;

import com.napier.sem.Database;
import com.napier.sem.models.City;

import java.sql.*;
import java.util.ArrayList;

public class CityReports {
    private Database db;

    public CityReports(Database db) {
        this.db = db;
    }

    public ArrayList<City> getTopCitiesWorldwide(int limit) {
        ArrayList<City> cities = new ArrayList<>();

        try {
            String sql = "SELECT ci.ID, ci.Name, co.Name AS Country, ci.District, ci.Population "
                    + "FROM city ci JOIN country co ON ci.CountryCode = co.Code "
                    + "ORDER BY ci.Population DESC LIMIT " + limit;

            Statement stmt = db.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                City c = new City();
                c.id = rs.getInt("ID");
                c.name = rs.getString("Name");
                c.country = rs.getString("Country");
                c.district = rs.getString("District");
                c.population = rs.getInt("Population");
                cities.add(c);
            }

        } catch (Exception e) {
            System.out.println("Error getting top cities: " + e.getMessage());
        }

        return cities;
    }

    public void printCities(ArrayList<City> cities) {
        System.out.println("Top Cities in the World");
        System.out.println("ID | City | Country | District | Population");

        for (City c : cities) {
            System.out.println(c.id + " | " + c.name + " | " + c.country + " | " + c.district + " | " + c.population);
        }
    }
}
