package com.napier.sem;

import java.sql.*;
import java.util.*;

public class App {

    // DB connection
    private Connection con = null;

    public static void main(String[] args) {
        App a = new App();
        a.connect();
        a.menu();
        a.disconnect();
    }

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database...");
            try {
                Thread.sleep(3000);
                con = DriverManager.getConnection(
                        "jdbc:mysql://db:3306/world?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "root",
                        "example"
                );
                System.out.println("Successfully connected");
                break;
            } catch (SQLException sqle) {
                System.out.println("Failed to connect to database attempt " + i);
                System.out.println(sqle.getMessage());
            } catch (InterruptedException ignored) {}
        }
    }

    public void disconnect() {
        if (con != null) {
            try { con.close(); } catch (Exception ignored) {}
        }
    }

    // Models
    public static class City {
        public int id;
        public String name;
        public String countryCode;
        public String district;
        public int population;
    }

    public static class Country {
        public String code;
        public String name;
        public long population;
    }

    public static class ContinentPop {
        public String continent;
        public long population;
    }

    //Reports
    public City getCity(int id) {
        if (con == null) { System.out.println("No DB connection."); return null; }
        String sql = "SELECT ID, Name, CountryCode, District, Population FROM city WHERE ID = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.countryCode = rs.getString("CountryCode");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    return c;
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get city: " + e.getMessage());
        }
        return null;
    }

    public List<City> getTopCitiesInCountry(String countryCode, int limit) {
        List<City> cities = new ArrayList<>();
        if (con == null) { System.out.println("No DB connection."); return cities; }
        String sql = "SELECT ID, Name, CountryCode, District, Population " +
                "FROM city WHERE CountryCode = ? " +
                "ORDER BY Population DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, countryCode);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.countryCode = rs.getString("CountryCode");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    cities.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get cities: " + e.getMessage());
        }
        return cities;
    }

    public List<Country> getTopCountriesByPopulation(int limit) {
        List<Country> out = new ArrayList<>();
        if (con == null) { System.out.println("No DB connection."); return out; }
        String sql = "SELECT Code, Name, Population FROM country ORDER BY Population DESC LIMIT ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Country c = new Country();
                    c.code = rs.getString("Code");
                    c.name = rs.getString("Name");
                    c.population = rs.getLong("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get countries: " + e.getMessage());
        }
        return out;
    }

    public List<ContinentPop> getPopulationByContinent() {
        List<ContinentPop> out = new ArrayList<>();
        if (con == null) { System.out.println("No DB connection."); return out; }
        String sql = "SELECT Continent, SUM(Population) AS Pop FROM country GROUP BY Continent ORDER BY Pop DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ContinentPop cp = new ContinentPop();
                cp.continent = rs.getString("Continent");
                cp.population = rs.getLong("Pop");
                out.add(cp);
            }
        } catch (SQLException e) {
            System.out.println("Failed to aggregate population by continent: " + e.getMessage());
        }
        return out;
    }

    // Display helpers
    public void displayCity(City c) {
        if (c == null) { System.out.println("City not found."); return; }
        System.out.println("ID: " + c.id);
        System.out.println("Name: " + c.name);
        System.out.println("CountryCode: " + c.countryCode);
        System.out.println("District: " + c.district);
        System.out.println("Population: " + c.population);
        System.out.println();
    }

    public void displayCities(List<City> cities) {
        if (cities == null || cities.isEmpty()) { System.out.println("No cities."); return; }
        System.out.printf("%-6s %-30s %-8s %-20s %-12s%n",
                "ID", "Name", "Code", "District", "Population");
        for (City c : cities) {
            System.out.printf("%-6d %-30s %-8s %-20s %-12d%n",
                    c.id, c.name, c.countryCode, c.district, c.population);
        }
        System.out.println();
    }

    public void displayCountries(List<Country> countries) {
        if (countries == null || countries.isEmpty()) { System.out.println("No countries."); return; }
        System.out.printf("%-8s %-40s %-14s%n", "Code", "Name", "Population");
        for (Country c : countries) {
            System.out.printf("%-8s %-40s %-14d%n", c.code, c.name, c.population);
        }
        System.out.println();
    }

    public void displayContinentPops(List<ContinentPop> list) {
        if (list == null || list.isEmpty()) { System.out.println("No data."); return; }
        System.out.printf("%-20s %-18s%n", "Continent", "Population");
        for (ContinentPop cp : list) {
            System.out.printf("%-20s %-18d%n", cp.continent, cp.population);
        }
        System.out.println();
    }

    //  Menu
    private void menu() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n---- Reports ----");
                System.out.println("1) City by ID");
                System.out.println("2) Top N cities in a country");
                System.out.println("3) Top N countries by population");
                System.out.println("4) Population by continent");
                System.out.println("q) Quit");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1": {
                        System.out.print("Enter City ID: ");
                        int id = Integer.parseInt(sc.nextLine().trim());
                        displayCity(getCity(id));
                        break;
                    }
                    case "2": {
                        System.out.print("Enter CountryCode (e.g, GBR): ");
                        String code = sc.nextLine().trim().toUpperCase();
                        System.out.print("Enter N (e.g, 10): ");
                        int n = Integer.parseInt(sc.nextLine().trim());
                        displayCities(getTopCitiesInCountry(code, n));
                        break;
                    }
                    case "3": {
                        System.out.print("Enter N (e.g, 10): ");
                        int n = Integer.parseInt(sc.nextLine().trim());
                        displayCountries(getTopCountriesByPopulation(n));
                        break;
                    }
                    case "4": {
                        displayContinentPops(getPopulationByContinent());
                        break;
                    }
                    case "q":
                    case "Q":
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            System.out.println("Menu error: " + e.getMessage());
        }
    }
}
