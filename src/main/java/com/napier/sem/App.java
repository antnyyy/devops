package com.napier.sem;

import java.sql.*;
import java.util.*;


public class App {

    // DB connection
    private Connection con = null;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;

    // Default constructor
    public App() {
        this(
                "jdbc:mysql://db:3306/world?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                "root",
                "example"
        );
    }

    // Extra constructor – for tests or custom configs
    public App(String dbUrl, String dbUser, String dbPassword) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
    }

    public static void main(String[] args) {
        App a = new App();
        a.connect();
        a.menu();
        a.disconnect();
    }

    // db connection

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
                con = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
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
            try {
                con.close();
            } catch (Exception ignored) {
            }
        }
    }


    // Models

    public static class City {
        public int id;
        public String name;
        public String countryCode;   // sometimes country code, sometimes country name for display
        public String district;
        public int population;
    }

    public static class Country {
        public String code;
        public String name;
        public String continent;
        public String region;
        public long population;
        public String capital;
    }

    public static class ContinentPop {
        public String continent;
        public long population;
    }

    public static class CapitalCity {
        public String name;
        public String country;
        public int population;
    }

    public static class PopulationBreakdown {
        public String name;           // continent/region/country
        public long total;
        public long inCities;
        public long notInCities;
        public double inCitiesPct;
        public double notInCitiesPct;
    }

    public static class LanguageStat {
        public String language;
        public long speakers;
        public double percentOfWorld;
    }


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

    public List<City> getTopCitiesInCountry(String countryCode, int limit) {
        List<City> cities = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return cities;
        }
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

    public List<ContinentPop> getPopulationByContinent() {
        List<ContinentPop> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT Continent, SUM(Population) AS Pop " +
                        "FROM country " +
                        "GROUP BY Continent " +
                        "ORDER BY Pop DESC";
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

    // Country reports

    public List<Country> getCountriesInWorldByPopulation() {
        List<Country> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, " +
                        "       cap.Name AS CapitalName " +
                        "FROM country c " +
                        "LEFT JOIN city cap ON cap.ID = c.Capital " +
                        "ORDER BY c.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Country c = new Country();
                c.code = rs.getString("Code");
                c.name = rs.getString("Name");
                c.continent = rs.getString("Continent");
                c.region = rs.getString("Region");
                c.population = rs.getLong("Population");
                c.capital = rs.getString("CapitalName");
                out.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get world countries: " + e.getMessage());
        }
        return out;
    }

    public List<Country> getCountriesInContinentByPopulation(String continent) {
        List<Country> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, cap.Name AS CapitalName " +
                        "FROM country c " +
                        "LEFT JOIN city cap ON cap.ID = c.Capital " +
                        "WHERE c.Continent = ? " +
                        "ORDER BY c.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Country c = new Country();
                    c.code = rs.getString("Code");
                    c.name = rs.getString("Name");
                    c.continent = rs.getString("Continent");
                    c.region = rs.getString("Region");
                    c.population = rs.getLong("Population");
                    c.capital = rs.getString("CapitalName");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get countries for continent: " + e.getMessage());
        }
        return out;
    }

    public List<Country> getCountriesInRegionByPopulation(String region) {
        List<Country> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT c.Code, c.Name, c.Continent, c.Region, c.Population, cap.Name AS CapitalName " +
                        "FROM country c " +
                        "LEFT JOIN city cap ON cap.ID = c.Capital " +
                        "WHERE c.Region = ? " +
                        "ORDER BY c.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Country c = new Country();
                    c.code = rs.getString("Code");
                    c.name = rs.getString("Name");
                    c.continent = rs.getString("Continent");
                    c.region = rs.getString("Region");
                    c.population = rs.getLong("Population");
                    c.capital = rs.getString("CapitalName");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get countries for region: " + e.getMessage());
        }
        return out;
    }

    public List<Country> getTopCountriesInWorldByPopulation(int n) {
        return getTopCountriesByPopulation(n);
    }

    public List<Country> getTopCountriesInContinentByPopulation(String continent, int n) {
        List<Country> all = getCountriesInContinentByPopulation(continent);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    public List<Country> getTopCountriesInRegionByPopulation(String region, int n) {
        List<Country> all = getCountriesInRegionByPopulation(region);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    // City reports

    public List<City> getCitiesInWorldByPopulation() {
        List<City> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ci.ID, ci.Name, co.Name AS CountryName, ci.District, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.CountryCode = co.Code " +
                        "ORDER BY ci.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                City c = new City();
                c.id = rs.getInt("ID");
                c.name = rs.getString("Name");
                c.countryCode = rs.getString("CountryName");
                c.district = rs.getString("District");
                c.population = rs.getInt("Population");
                out.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get world cities: " + e.getMessage());
        }
        return out;
    }

    public List<City> getCitiesInContinentByPopulation(String continent) {
        List<City> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ci.ID, ci.Name, co.Name AS CountryName, ci.District, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.CountryCode = co.Code " +
                        "WHERE co.Continent = ? " +
                        "ORDER BY ci.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.countryCode = rs.getString("CountryName");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get cities for continent: " + e.getMessage());
        }
        return out;
    }

    public List<City> getCitiesInRegionByPopulation(String region) {
        List<City> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ci.ID, ci.Name, co.Name AS CountryName, ci.District, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.CountryCode = co.Code " +
                        "WHERE co.Region = ? " +
                        "ORDER BY ci.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.countryCode = rs.getString("CountryName");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get cities for region: " + e.getMessage());
        }
        return out;
    }

    public List<City> getCitiesInCountryByPopulation(String countryCode) {
        List<City> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ID, Name, CountryCode, District, Population " +
                        "FROM city " +
                        "WHERE CountryCode = ? " +
                        "ORDER BY Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, countryCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.countryCode = rs.getString("CountryCode");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get cities for country: " + e.getMessage());
        }
        return out;
    }

    public List<City> getCitiesInDistrictByPopulation(String district) {
        List<City> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ID, Name, CountryCode, District, Population " +
                        "FROM city " +
                        "WHERE District = ? " +
                        "ORDER BY Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, district);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    City c = new City();
                    c.id = rs.getInt("ID");
                    c.name = rs.getString("Name");
                    c.countryCode = rs.getString("CountryCode");
                    c.district = rs.getString("District");
                    c.population = rs.getInt("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get cities for district: " + e.getMessage());
        }
        return out;
    }

    public List<City> getTopCitiesInWorldByPopulation(int n) {
        List<City> all = getCitiesInWorldByPopulation();
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    public List<City> getTopCitiesInContinentByPopulation(String continent, int n) {
        List<City> all = getCitiesInContinentByPopulation(continent);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    public List<City> getTopCitiesInRegionByPopulation(String region, int n) {
        List<City> all = getCitiesInRegionByPopulation(region);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    public List<City> getTopCitiesInCountryByPopulation(String countryCode, int n) {
        return getTopCitiesInCountry(countryCode, n);
    }

    public List<City> getTopCitiesInDistrictByPopulation(String district, int n) {
        List<City> all = getCitiesInDistrictByPopulation(district);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    // Capital city reports

    public List<CapitalCity> getCapitalCitiesInWorldByPopulation() {
        List<CapitalCity> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ci.Name AS CityName, co.Name AS CountryName, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.ID = co.Capital " +
                        "ORDER BY ci.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CapitalCity c = new CapitalCity();
                c.name = rs.getString("CityName");
                c.country = rs.getString("CountryName");
                c.population = rs.getInt("Population");
                out.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get capital cities: " + e.getMessage());
        }
        return out;
    }

    public List<CapitalCity> getCapitalCitiesInContinentByPopulation(String continent) {
        List<CapitalCity> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ci.Name AS CityName, co.Name AS CountryName, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.ID = co.Capital " +
                        "WHERE co.Continent = ? " +
                        "ORDER BY ci.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CapitalCity c = new CapitalCity();
                    c.name = rs.getString("CityName");
                    c.country = rs.getString("CountryName");
                    c.population = rs.getInt("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get capital cities for continent: " + e.getMessage());
        }
        return out;
    }

    public List<CapitalCity> getCapitalCitiesInRegionByPopulation(String region) {
        List<CapitalCity> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT ci.Name AS CityName, co.Name AS CountryName, ci.Population " +
                        "FROM city ci " +
                        "JOIN country co ON ci.ID = co.Capital " +
                        "WHERE co.Region = ? " +
                        "ORDER BY ci.Population DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CapitalCity c = new CapitalCity();
                    c.name = rs.getString("CityName");
                    c.country = rs.getString("CountryName");
                    c.population = rs.getInt("Population");
                    out.add(c);
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get capital cities for region: " + e.getMessage());
        }
        return out;
    }

    public List<CapitalCity> getTopCapitalCitiesInWorldByPopulation(int n) {
        List<CapitalCity> all = getCapitalCitiesInWorldByPopulation();
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    public List<CapitalCity> getTopCapitalCitiesInContinentByPopulation(String continent, int n) {
        List<CapitalCity> all = getCapitalCitiesInContinentByPopulation(continent);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    public List<CapitalCity> getTopCapitalCitiesInRegionByPopulation(String region, int n) {
        List<CapitalCity> all = getCapitalCitiesInRegionByPopulation(region);
        return all.subList(0, Math.min(Math.max(1, n), all.size()));
    }

    // Population Breakdowns

    public List<PopulationBreakdown> getPopulationBreakdownByContinent() {
        List<PopulationBreakdown> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT co.Continent AS Name, " +
                        "       SUM(co.Population) AS TotalPop, " +
                        "       SUM(ci.Population) AS CityPop " +
                        "FROM country co " +
                        "LEFT JOIN city ci ON ci.CountryCode = co.Code " +
                        "GROUP BY co.Continent " +
                        "ORDER BY TotalPop DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PopulationBreakdown pb = new PopulationBreakdown();
                pb.name = rs.getString("Name");
                pb.total = rs.getLong("TotalPop");
                pb.inCities = rs.getLong("CityPop");
                pb.notInCities = pb.total - pb.inCities;
                if (pb.total > 0) {
                    pb.inCitiesPct = (pb.inCities * 100.0) / pb.total;
                    pb.notInCitiesPct = (pb.notInCities * 100.0) / pb.total;
                }
                out.add(pb);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get population breakdown by continent: " + e.getMessage());
        }
        return out;
    }

    public List<PopulationBreakdown> getPopulationBreakdownByRegion() {
        List<PopulationBreakdown> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT co.Region AS Name, " +
                        "       SUM(co.Population) AS TotalPop, " +
                        "       SUM(ci.Population) AS CityPop " +
                        "FROM country co " +
                        "LEFT JOIN city ci ON ci.CountryCode = co.Code " +
                        "GROUP BY co.Region " +
                        "ORDER BY TotalPop DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PopulationBreakdown pb = new PopulationBreakdown();
                pb.name = rs.getString("Name");
                pb.total = rs.getLong("TotalPop");
                pb.inCities = rs.getLong("CityPop");
                pb.notInCities = pb.total - pb.inCities;
                if (pb.total > 0) {
                    pb.inCitiesPct = (pb.inCities * 100.0) / pb.total;
                    pb.notInCitiesPct = (pb.notInCities * 100.0) / pb.total;
                }
                out.add(pb);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get population breakdown by region: " + e.getMessage());
        }
        return out;
    }

    public List<PopulationBreakdown> getPopulationBreakdownByCountry() {
        List<PopulationBreakdown> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }
        String sql =
                "SELECT co.Name AS Name, " +
                        "       co.Population AS TotalPop, " +
                        "       SUM(ci.Population) AS CityPop " +
                        "FROM country co " +
                        "LEFT JOIN city ci ON ci.CountryCode = co.Code " +
                        "GROUP BY co.Code, co.Name, co.Population " +
                        "ORDER BY TotalPop DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PopulationBreakdown pb = new PopulationBreakdown();
                pb.name = rs.getString("Name");
                pb.total = rs.getLong("TotalPop");
                pb.inCities = rs.getLong("CityPop");
                pb.notInCities = pb.total - pb.inCities;
                if (pb.total > 0) {
                    pb.inCitiesPct = (pb.inCities * 100.0) / pb.total;
                    pb.notInCitiesPct = (pb.notInCities * 100.0) / pb.total;
                }
                out.add(pb);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get population breakdown by country: " + e.getMessage());
        }
        return out;
    }

   // Population lookups

    public long getWorldPopulation() {
        if (con == null) {
            System.out.println("No DB connection.");
            return 0;
        }
        String sql = "SELECT SUM(Population) AS Pop FROM country";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("Pop");
            }
        } catch (SQLException e) {
            System.out.println("Failed to get world population: " + e.getMessage());
        }
        return 0;
    }

    public long getContinentPopulation(String continent) {
        if (con == null) {
            System.out.println("No DB connection.");
            return 0;
        }
        String sql = "SELECT SUM(Population) AS Pop FROM country WHERE Continent = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, continent);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("Pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get continent population: " + e.getMessage());
        }
        return 0;
    }

    public long getRegionPopulation(String region) {
        if (con == null) {
            System.out.println("No DB connection.");
            return 0;
        }
        String sql = "SELECT SUM(Population) AS Pop FROM country WHERE Region = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, region);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("Pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get region population: " + e.getMessage());
        }
        return 0;
    }

    public long getCountryPopulation(String code) {
        if (con == null) {
            System.out.println("No DB connection.");
            return 0;
        }
        String sql = "SELECT Population FROM country WHERE Code = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("Population");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get country population: " + e.getMessage());
        }
        return 0;
    }

    public long getDistrictPopulation(String district) {
        if (con == null) {
            System.out.println("No DB connection.");
            return 0;
        }
        String sql = "SELECT SUM(Population) AS Pop FROM city WHERE District = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, district);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("Pop");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get district population: " + e.getMessage());
        }
        return 0;
    }

    public long getCityPopulation(String cityName) {
        if (con == null) {
            System.out.println("No DB connection.");
            return 0;
        }
        String sql = "SELECT Population FROM city WHERE Name = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, cityName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("Population");
                }
            }
        } catch (SQLException e) {
            System.out.println("Failed to get city population: " + e.getMessage());
        }
        return 0;
    }

    // Language report

    public List<LanguageStat> getLanguageStats() {
        List<LanguageStat> out = new ArrayList<>();
        if (con == null) {
            System.out.println("No DB connection.");
            return out;
        }

        long worldPop = getWorldPopulation();
        if (worldPop == 0) {
            return out;
        }

        String sql =
                "SELECT cl.Language, SUM(c.Population * cl.Percentage / 100) AS Speakers " +
                        "FROM countrylanguage cl " +
                        "JOIN country c ON cl.CountryCode = c.Code " +
                        "WHERE cl.Language IN ('Chinese', 'English', 'Hindi', 'Spanish', 'Arabic') " +
                        "GROUP BY cl.Language " +
                        "ORDER BY Speakers DESC";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LanguageStat ls = new LanguageStat();
                ls.language = rs.getString("Language");
                ls.speakers = rs.getLong("Speakers");
                ls.percentOfWorld = (ls.speakers * 100.0) / worldPop;
                out.add(ls);
            }
        } catch (SQLException e) {
            System.out.println("Failed to get language stats: " + e.getMessage());
        }
        return out;
    }

    // Display helpers

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

    public void displayCountryReport(List<Country> countries) {
        if (countries == null || countries.isEmpty()) {
            System.out.println("No countries.");
            return;
        }
        System.out.printf("%-8s %-35s %-12s %-18s %-12s %-20s%n",
                "Code", "Name", "Continent", "Region", "Population", "Capital");
        for (Country c : countries) {
            System.out.printf("%-8s %-35s %-12s %-18s %-12d %-20s%n",
                    c.code,
                    c.name,
                    c.continent,
                    c.region,
                    c.population,
                    c.capital);
        }
        System.out.println();
    }

    public void displayCityReport(List<City> cities) {
        if (cities == null || cities.isEmpty()) {
            System.out.println("No cities.");
            return;
        }
        System.out.printf("%-30s %-30s %-20s %-12s%n",
                "Name", "Country", "District", "Population");
        for (City c : cities) {
            System.out.printf("%-30s %-30s %-20s %-12d%n",
                    c.name,
                    c.countryCode,
                    c.district,
                    c.population);
        }
        System.out.println();
    }

    public void displayCapitalCities(List<CapitalCity> caps) {
        if (caps == null || caps.isEmpty()) {
            System.out.println("No capital cities.");
            return;
        }
        System.out.printf("%-30s %-30s %-12s%n",
                "Name", "Country", "Population");
        for (CapitalCity c : caps) {
            System.out.printf("%-30s %-30s %-12d%n",
                    c.name,
                    c.country,
                    c.population);
        }
        System.out.println();
    }

    public void displayPopulationBreakdowns(List<PopulationBreakdown> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        System.out.printf("%-20s %-12s %-12s %-8s %-12s %-8s%n",
                "Name", "Total", "InCities", "%In", "NotInCities", "%NotIn");
        for (PopulationBreakdown pb : list) {
            System.out.printf("%-20s %-12d %-12d %-8.2f %-12d %-8.2f%n",
                    pb.name,
                    pb.total,
                    pb.inCities,
                    pb.inCitiesPct,
                    pb.notInCities,
                    pb.notInCitiesPct);
        }
        System.out.println();
    }

    public void displayLanguageStats(List<LanguageStat> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No data.");
            return;
        }
        System.out.printf("%-10s %-15s %-10s%n",
                "Language", "Speakers", "%World");
        for (LanguageStat ls : list) {
            System.out.printf("%-10s %-15d %-10.2f%n",
                    ls.language,
                    ls.speakers,
                    ls.percentOfWorld);
        }
        System.out.println();
    }

    // Menu to select report

    private void menu() {
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== MAIN MENU ===");
                System.out.println(" 1) City by ID");
                System.out.println(" 2) Top N cities in a country");
                System.out.println(" 3) Top N countries by population");
                System.out.println(" 4) Population by continent");
                System.out.println("---- Country reports ----");
                System.out.println(" 5) All countries in the world by population");
                System.out.println(" 6) All countries in a continent by population");
                System.out.println(" 7) All countries in a region by population");
                System.out.println(" 8) Top N countries in the world by population");
                System.out.println(" 9) Top N countries in a continent by population");
                System.out.println("10) Top N countries in a region by population");
                System.out.println("---- City reports ----");
                System.out.println("11) All cities in the world by population");
                System.out.println("12) All cities in a continent by population");
                System.out.println("13) All cities in a region by population");
                System.out.println("14) All cities in a country by population");
                System.out.println("15) All cities in a district by population");
                System.out.println("16) Top N cities in the world by population");
                System.out.println("17) Top N cities in a continent by population");
                System.out.println("18) Top N cities in a region by population");
                System.out.println("19) Top N cities in a country by population");
                System.out.println("20) Top N cities in a district by population");
                System.out.println("---- Capital city reports ----");
                System.out.println("21) All capital cities in the world by population");
                System.out.println("22) All capital cities in a continent by population");
                System.out.println("23) All capital cities in a region by population");
                System.out.println("24) Top N capital cities in the world by population");
                System.out.println("25) Top N capital cities in a continent by population");
                System.out.println("26) Top N capital cities in a region by population");
                System.out.println("---- Population breakdown reports ----");
                System.out.println("27) Population of people in each continent (in / not in cities)");
                System.out.println("28) Population of people in each region (in / not in cities)");
                System.out.println("29) Population of people in each country (in / not in cities)");
                System.out.println("---- Simple population lookups ----");
                System.out.println("30) Population of the world");
                System.out.println("31) Population of a continent");
                System.out.println("32) Population of a region");
                System.out.println("33) Population of a country");
                System.out.println("34) Population of a district");
                System.out.println("35) Population of a city");
                System.out.println("---- Language report ----");
                System.out.println("36) People who speak Chinese, English, Hindi, Spanish, Arabic");
                System.out.println(" q) Quit");
                System.out.print("Choose: ");

                String choice = sc.nextLine().trim();

                try {
                    switch (choice) {
                        case "1": {
                            System.out.print("Enter City ID: ");
                            int id = Integer.parseInt(sc.nextLine().trim());
                            displayCity(getCity(id));
                            break;
                        }
                        case "2": {
                            System.out.print("Enter CountryCode (e.g., GBR): ");
                            String code = sc.nextLine().trim().toUpperCase();
                            System.out.print("Enter N (e.g., 10): ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCities(getTopCitiesInCountry(code, n));
                            break;
                        }
                        case "3": {
                            System.out.print("Enter N (e.g., 10): ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCountries(getTopCountriesByPopulation(n));
                            break;
                        }
                        case "4": {
                            displayContinentPops(getPopulationByContinent());
                            break;
                        }

                        // Country reports
                        case "5": {
                            displayCountryReport(getCountriesInWorldByPopulation());
                            break;
                        }
                        case "6": {
                            System.out.print("Enter continent (e.g., Europe): ");
                            String cont = sc.nextLine().trim();
                            displayCountryReport(getCountriesInContinentByPopulation(cont));
                            break;
                        }
                        case "7": {
                            System.out.print("Enter region (e.g., Caribbean): ");
                            String reg = sc.nextLine().trim();
                            displayCountryReport(getCountriesInRegionByPopulation(reg));
                            break;
                        }
                        case "8": {
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCountryReport(getTopCountriesInWorldByPopulation(n));
                            break;
                        }
                        case "9": {
                            System.out.print("Enter continent: ");
                            String cont = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCountryReport(getTopCountriesInContinentByPopulation(cont, n));
                            break;
                        }
                        case "10": {
                            System.out.print("Enter region: ");
                            String reg = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCountryReport(getTopCountriesInRegionByPopulation(reg, n));
                            break;
                        }

                        // City reports
                        case "11": {
                            displayCityReport(getCitiesInWorldByPopulation());
                            break;
                        }
                        case "12": {
                            System.out.print("Enter continent: ");
                            String cont = sc.nextLine().trim();
                            displayCityReport(getCitiesInContinentByPopulation(cont));
                            break;
                        }
                        case "13": {
                            System.out.print("Enter region: ");
                            String reg = sc.nextLine().trim();
                            displayCityReport(getCitiesInRegionByPopulation(reg));
                            break;
                        }
                        case "14": {
                            System.out.print("Enter country code (e.g., GBR): ");
                            String code = sc.nextLine().trim().toUpperCase();
                            displayCities(getCitiesInCountryByPopulation(code));
                            break;
                        }
                        case "15": {
                            System.out.print("Enter district: ");
                            String dist = sc.nextLine().trim();
                            displayCities(getCitiesInDistrictByPopulation(dist));
                            break;
                        }
                        case "16": {
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCityReport(getTopCitiesInWorldByPopulation(n));
                            break;
                        }
                        case "17": {
                            System.out.print("Enter continent: ");
                            String cont = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCityReport(getTopCitiesInContinentByPopulation(cont, n));
                            break;
                        }
                        case "18": {
                            System.out.print("Enter region: ");
                            String reg = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCityReport(getTopCitiesInRegionByPopulation(reg, n));
                            break;
                        }
                        case "19": {
                            System.out.print("Enter country code: ");
                            String code = sc.nextLine().trim().toUpperCase();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCities(getTopCitiesInCountryByPopulation(code, n));
                            break;
                        }
                        case "20": {
                            System.out.print("Enter district: ");
                            String dist = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCities(getTopCitiesInDistrictByPopulation(dist, n));
                            break;
                        }

                        // Capital city reports
                        case "21": {
                            displayCapitalCities(getCapitalCitiesInWorldByPopulation());
                            break;
                        }
                        case "22": {
                            System.out.print("Enter continent: ");
                            String cont = sc.nextLine().trim();
                            displayCapitalCities(getCapitalCitiesInContinentByPopulation(cont));
                            break;
                        }
                        case "23": {
                            System.out.print("Enter region: ");
                            String reg = sc.nextLine().trim();
                            displayCapitalCities(getCapitalCitiesInRegionByPopulation(reg));
                            break;
                        }
                        case "24": {
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCapitalCities(getTopCapitalCitiesInWorldByPopulation(n));
                            break;
                        }
                        case "25": {
                            System.out.print("Enter continent: ");
                            String cont = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCapitalCities(getTopCapitalCitiesInContinentByPopulation(cont, n));
                            break;
                        }
                        case "26": {
                            System.out.print("Enter region: ");
                            String reg = sc.nextLine().trim();
                            System.out.print("Enter N: ");
                            int n = Integer.parseInt(sc.nextLine().trim());
                            displayCapitalCities(getTopCapitalCitiesInRegionByPopulation(reg, n));
                            break;
                        }

                        // Population breakdowns
                        case "27": {
                            displayPopulationBreakdowns(getPopulationBreakdownByContinent());
                            break;
                        }
                        case "28": {
                            displayPopulationBreakdowns(getPopulationBreakdownByRegion());
                            break;
                        }
                        case "29": {
                            displayPopulationBreakdowns(getPopulationBreakdownByCountry());
                            break;
                        }

                        // Simple population lookups
                        case "30": {
                            System.out.println("World population: " + getWorldPopulation());
                            break;
                        }
                        case "31": {
                            System.out.print("Enter continent: ");
                            String cont = sc.nextLine().trim();
                            System.out.println("Population of " + cont + ": " + getContinentPopulation(cont));
                            break;
                        }
                        case "32": {
                            System.out.print("Enter region: ");
                            String reg = sc.nextLine().trim();
                            System.out.println("Population of " + reg + ": " + getRegionPopulation(reg));
                            break;
                        }
                        case "33": {
                            System.out.print("Enter country code (e.g., GBR): ");
                            String code = sc.nextLine().trim().toUpperCase();
                            System.out.println("Population of " + code + ": " + getCountryPopulation(code));
                            break;
                        }
                        case "34": {
                            System.out.print("Enter district: ");
                            String dist = sc.nextLine().trim();
                            System.out.println("Population of district " + dist + ": " + getDistrictPopulation(dist));
                            break;
                        }
                        case "35": {
                            System.out.print("Enter city name: ");
                            String cityName = sc.nextLine().trim();
                            System.out.println("Population of " + cityName + ": " + getCityPopulation(cityName));
                            break;
                        }

                        // Language report
                        case "36": {
                            displayLanguageStats(getLanguageStats());
                            break;
                        }

                        case "q":
                        case "Q":
                            return;

                        default:
                            System.out.println("Invalid choice.");
                    }
                } catch (NumberFormatException nfe) {
                    System.out.println("Invalid numeric input.");
                } catch (Exception e) {
                    System.out.println("Error running report: " + e.getMessage());
                }
            }
        }
    }
}
