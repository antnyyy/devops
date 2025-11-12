package com.napier.sem;

import java.sql.*;
import java.util.*;

/**
 * Main application for the World database reporting system.
 * Connects to a MySQL container (named "db"), presents a text menu,
 * and runs various population/city reports.
 */
public class App {

    // --------------------------------------------------------------------- //
    // DATABASE CONNECTION
    // --------------------------------------------------------------------- //

    /** JDBC connection to the MySQL server. */
    private Connection con = null;

    /**
     * Entry point – creates an instance, connects, shows the menu and finally
     * disconnects.
     */
    public static void main(String[] args) {
        App a = new App();
        try {
            a.connect();   // may retry several times
            a.menu();      // interactive loop
        } finally {
            a.disconnect(); // guaranteed cleanup
        }
    }

    /**
     * Establishes a connection to the MySQL container.
     * <p>
     * The driver is loaded explicitly (required for some class-loaders).
     * Up to 10 attempts are made with a 3-second pause between them
     * to cope with Docker start-up delays.
     * </p>
     */
    public void connect() {
        // Load the JDBC driver class
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // If the driver JAR is missing the program cannot continue
            throw new IllegalStateException("MySQL JDBC Driver not found.", e);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i) {
            System.out.println("Connecting to database... (attempt " + (i + 1) + ")");
            try {
                // Docker-compose service name "db" resolves inside the network
                Thread.sleep(3000); // give MySQL time to start
                con = DriverManager.getConnection(
                        "jdbc:mysql://db:3306/world?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "root",
                        "example");
                System.out.println("Successfully connected");
                return; // success – exit retry loop
            } catch (SQLException sqle) {
                System.out.println("Failed attempt " + (i + 1) + ": " + sqle.getMessage());
            } catch (InterruptedException ignored) {
                // Sleep interrupted – continue retrying
            }
        }
        // If we get here all retries failed
        throw new IllegalStateException("Could not connect to database after " + retries + " attempts.");
    }

    /** Closes the JDBC connection if it is open. */
    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                System.out.println("Database connection closed.");
            } catch (SQLException ignored) {
                // Swallow – nothing we can do at shutdown
            }
        }
    }

    // --------------------------------------------------------------------- //
    // DATA MODELS
    // --------------------------------------------------------------------- //

    /** Represents a row from the <code>city</code> table. */
    public static class City {
        public int id;
        public String name;
        public String countryCode;
        public String district;
        public int population;
    }

    /** Represents a row from the <code>country</code> table (selected columns). */
    public static class Country {
        public String code;
        public String name;
        public long population;
    }

    /** Holds aggregated population for a continent. */
    public static class ContinentPop {
        public String continent;
        public long population;
    }

    // --------------------------------------------------------------------- //
    // REPORT QUERIES
    // --------------------------------------------------------------------- //

    /**
     * Retrieves a single city by its primary key.
     *
     * @param id the city ID
     * @return a populated {@link City} or <code>null</code> if not found
     */
    public City getCity(int id) {
        if (con == null) {
            System.out.println("No DB connection.");
            return null;
        }

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

    /**
     * Returns the N most populous cities for a given country.
     *
     * @param countryCode ISO-3 country code (e.g. "GBR")
     * @param limit       maximum number of rows (minimum 1)
     * @return list of {@link City} objects, ordered by population descending
     */
    public List<City> getTopCitiesInCountry(String countryCode, int limit) {
        List<City> cities = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return cities;
        }

        String sql = """
                SELECT ID, Name, CountryCode, District, Population
                FROM city
                WHERE CountryCode = ?
                ORDER BY Population DESC
                LIMIT ?""";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, countryCode);
            ps.setInt(2, Math.max(1, limit)); // guard against LIMIT 0
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

    /**
     * Returns the N most populous countries in the world.
     *
     * @param limit maximum number of rows (minimum 1)
     * @return list of {@link Country} objects, ordered by population descending
     */
    public List<Country> getTopCountriesByPopulation(int limit) {
        List<Country> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }

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

    /**
     * Aggregates total population per continent.
     *
     * @return list of {@link ContinentPop}, ordered by total population descending
     */
    public List<ContinentPop> getPopulationByContinent() {
        List<ContinentPop> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }

        String sql = """
                SELECT Continent, SUM(Population) AS Pop
                FROM country
                GROUP BY Continent
                ORDER BY Pop DESC""";
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

    // --------------------------------------------------------------------- //
    // DISPLAY HELPERS
    // --------------------------------------------------------------------- //

    /** Prints a single city in a readable format. */
    public void displayCity(City c) {
        if (c == null) {
            System.out.println("City not found.");
            return;
        }
        System.out.println("ID: " + c.id);
        System.out.println("Name: " + c.name);
        System.out.println("CountryCode: " + c.countryCode);
        System.out.println("District: " + c.district);
        System.out.println("Population: " + c.population);
        System.out.println();
    }

    /** Tabular display of a list of cities. */
    public void displayCities(List<City> cities) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities.");
            return;
        }
        System.out.printf("%-6s %-30s %-8s %-20s %-12s%n",
                "ID", "Name", "Code", "District", "Population");
        for (City c : cities) {
            System.out.printf("%-6d %-30s %-8s %-20s %-12d%n",
                    c.id, c.name, c.countryCode, c.district, c.population);
        }
        System.out.println();
    }

    /** Tabular display of a list of countries. */
    public void displayCountries(List<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries.");
            return;
        }
        System.out.printf("%-8s %-40s %-14s%n", "Code", "Name", "Population");
        for (Country c : countries) {
            System.out.printf("%-8s %-40s %-14d%n", c.code, c.name, c.population);
        }
        System.out.println();
    }

    /** Tabular display of continent population aggregates. */
    public void displayContinentPops(List<ContinentPop> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        System.out.printf("%-20s %-18s%n", "Continent", "Population");
        for (ContinentPop cp : list) {
            System.out.printf("%-20s %-18d%n", cp.continent, cp.population);
        }
        System.out.println();
    }

    // --------------------------------------------------------------------- //
    // INTERACTIVE MENU
    // --------------------------------------------------------------------- //

    /** Runs the console menu until the user chooses to quit. */
    private void menu() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                printMenu();
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();

                switch (choice) {
                    case "1" -> handleCityById(sc);
                    case "2" -> handleTopCitiesInCountry(sc);
                    case "3" -> handleTopCountries(sc);
                    case "4" -> displayContinentPops(getPopulationByContinent());
                    case "q", "Q" -> {
                        System.out.println("Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (Exception e) {
            System.out.println("Menu error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Helper: prints the menu options. */
    private void printMenu() {
        System.out.println("\n---- Reports ----");
        System.out.println("1) City by ID");
        System.out.println("2) Top N cities in a country");
        System.out.println("3) Top N countries by population");
        System.out.println("4) Population by continent");
        System.out.println("q) Quit");
    }

    /** Menu case 1 – fetch city by ID with safe parsing. */
    private void handleCityById(Scanner sc) {
        System.out.print("Enter City ID: ");
        int id = safeParseInt(sc.nextLine(), -1);
        if (id <= 0) {
            System.out.println("Invalid ID – must be a positive integer.");
            return;
        }
        displayCity(getCity(id));
    }

    /** Menu case 2 – top N cities for a country. */
    private void handleTopCitiesInCountry(Scanner sc) {
        System.out.print("Enter CountryCode (e.g., GBR): ");
        String code = sc.nextLine().trim().toUpperCase();
        if (!code.matches("[A-Z]{3}")) {
            System.out.println("Country code must be three uppercase letters.");
            return;
        }

        System.out.print("Enter N (e.g., 10): ");
        int n = safeParseInt(sc.nextLine(), 10);
        if (n < 1) {
            System.out.println("N must be at least 1.");
            return;
        }
        displayCities(getTopCitiesInCountry(code, n));
    }

    /** Menu case 3 – top N countries worldwide. */
    private void handleTopCountries(Scanner sc) {
        System.out.print("Enter N (e.g., 10): ");
        int n = safeParseInt(sc.nextLine(), 10);
        if (n < 1) {
            System.out.println("N must be at least 1.");
            return;
        }
        displayCountries(getTopCountriesByPopulation(n));
    }

    /**
     * Parses a string to int; on failure returns the supplied default.
     */
    private int safeParseInt(String input, int defaultValue) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number – using default: " + defaultValue);
            return defaultValue;
        }
    }
}