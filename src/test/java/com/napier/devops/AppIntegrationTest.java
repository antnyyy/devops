package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for App.java.
 *
 * These tests exercise the code against a running MySQL instance seeded
 * with the provided `world.sql`. They are useful for CI and local
 * validation of SQL queries and schema expectations. The database URL
 * and credentials may be overridden via `DB_URL`, `DB_USER`, and
 * `DB_PASS` environment variables.
 *
 * Tests expect the "world" database to be available at the configured
 * JDBC URL (defaults to jdbc:mysql://localhost:33060/world in the test
 * configuration used by the project's CI).
 */
public class AppIntegrationTest {

    private static App app;

    @BeforeAll
    static void init() {
        String url  = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:mysql://localhost:33060/world?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC"
        );
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String pass = System.getenv().getOrDefault("DB_PASS", "example");

        app = new App(url, user, pass);
        app.connect();
    }



    @AfterAll
    static void teardown() {
        app.disconnect();
    }

    // ----------- City tests -----------

    @Test
    void testGetCityByValidId() {
        // For the standard "world" database, ID 1 is typically Kabul.
        App.City city = app.getCity(1);

        assertNotNull(city, "City with ID 1 should exist");
        assertEquals(1, city.id);
        assertNotNull(city.name);
        assertFalse(city.name.trim().isEmpty(), "City name should not be empty");
        assertTrue(city.population > 0, "City population should be positive");
    }

    @Test
    void testGetCityByInvalidId() {
        // Assuming a very large ID that does not exist
        App.City city = app.getCity(999999);
        assertNull(city, "Non-existent city ID should return null");
    }

    // ----------- Top cities in country tests -----------

    @Test
    void testGetTopCitiesInCountry() {
        // Using GBR as an example (standard in world DB)
        List<App.City> cities = app.getTopCitiesInCountry("GBR", 5);

        assertNotNull(cities);
        assertFalse(cities.isEmpty(), "There should be at least one city for GBR");
        assertTrue(cities.size() <= 5, "Should not return more than requested limit");

        // Check sorted by population descending
        for (int i = 1; i < cities.size(); i++) {
            assertTrue(
                    cities.get(i - 1).population >= cities.get(i).population,
                    "Cities should be ordered by population descending"
            );
        }
    }

    @Test
    void testGetTopCitiesInCountryWithZeroLimitTreatsAsOne() {
        List<App.City> cities = app.getTopCitiesInCountry("GBR", 0);
        // Method uses Math.max(1, limit), so limit 0 should behave like limit 1
        assertNotNull(cities);
        assertEquals(1, cities.size(), "Zero limit should still return one record");
    }

    // ----------- Top countries by population tests -----------

    @Test
    void testGetTopCountriesByPopulation() {
        List<App.Country> countries = app.getTopCountriesByPopulation(10);

        assertNotNull(countries);
        assertFalse(countries.isEmpty(), "There should be at least one country");
        assertTrue(countries.size() <= 10, "Should not return more than requested limit");

        for (int i = 1; i < countries.size(); i++) {
            assertTrue(
                    countries.get(i - 1).population >= countries.get(i).population,
                    "Countries should be ordered by population descending"
            );
        }
    }

    @Test
    void testGetTopCountriesByPopulationZeroLimitTreatsAsOne() {
        List<App.Country> countries = app.getTopCountriesByPopulation(0);
        assertNotNull(countries);
        assertEquals(1, countries.size(), "Zero limit should still return one record");
    }

    // ----------- Population by continent tests -----------

    @Test
    void testGetPopulationByContinentNotEmptyAndHasPositiveValues() {
        List<App.ContinentPop> list = app.getPopulationByContinent();

        assertNotNull(list);
        assertFalse(list.isEmpty(), "Population by continent should not be empty");

        for (App.ContinentPop cp : list) {
            assertNotNull(cp.continent);
            assertFalse(cp.continent.trim().isEmpty(), "Continent name should not be empty");
            assertTrue(cp.population >= 0, "Continent population should not be negative");
        }

        // Check that list is sorted by population descending
        for (int i = 1; i < list.size(); i++) {
            assertTrue(
                    list.get(i - 1).population >= list.get(i).population,
                    "Continents should be ordered by population descending"
            );
        }
    }
}
