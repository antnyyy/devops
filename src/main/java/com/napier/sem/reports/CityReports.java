package com.napier.sem.reports;

import com.napier.sem.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CityReports {
    private final Database db;

    public CityReports(Database db) {
        this.db = db;
    }

    // Helper class to represent a City
    public static class City {
        public int id;
        public String name;
        public String countryCode;
        public String district;
        public int population;

        public City(int id, String name, String countryCode, String district, int population) {
            this.id = id;
            this.name = name;
            this.countryCode = countryCode;
            this.district = district;
            this.population = population;
        }

        @Override
        public String toString() {
            return "City{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", countryCode='" + countryCode + '\'' +
                    ", district='" + district + '\'' +
                    ", population=" + population +
                    '}';
        }
    }

    // Report: Get all cities in the world
    public List<City> getAllCities() {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT ID, Name, CountryCode, District, Population FROM city ORDER BY Name";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                City city = new City(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getString("CountryCode"),
                        rs.getString("District"),
                        rs.getInt("Population")
                );
                cities.add(city);
            }
        } catch (SQLException e) {
            System.err.println("Error executing getAllCities: " + e.getMessage());
        }
        return cities;
    }

    // Report: Get cities by country code
    public List<City> getCitiesByCountry(String countryCode) {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT ID, Name, CountryCode, District, Population FROM city WHERE CountryCode = ? ORDER BY Population DESC";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, countryCode);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    City city = new City(
                            rs.getInt("ID"),
                            rs.getString("Name"),
                            rs.getString("CountryCode"),
                            rs.getString("District"),
                            rs.getInt("Population")
                    );
                    cities.add(city);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing getCitiesByCountry: " + e.getMessage());
        }
        return cities;
    }}