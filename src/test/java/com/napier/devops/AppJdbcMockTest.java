package com.napier.devops;

import com.napier.sem.App;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests that mock JDBC objects to exercise App's query and error
 * handling paths without needing a real database.
 */
class AppJdbcMockTest {

    // Helper: inject the mock Connection into private field `con` via reflection
    private void injectConnection(App app, Connection con) throws Exception {
        java.lang.reflect.Field f = App.class.getDeclaredField("con");
        f.setAccessible(true);
        f.set(app, con);
    }

    @Test
    void getCity_returnsCity_whenResultSetHasRow() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("ID")).thenReturn(42);
        when(rs.getString("Name")).thenReturn("Mockville");
        when(rs.getString("CountryCode")).thenReturn("MCK");
        when(rs.getString("District")).thenReturn("Central");
        when(rs.getInt("Population")).thenReturn(1234);

        App app = new App("jdbc:mock", "u", "p");
        injectConnection(app, con);

        App.City c = app.getCity(42);
        assertNotNull(c);
        assertEquals(42, c.id);
        assertEquals("Mockville", c.name);

        verify(ps).setInt(1, 42);
        verify(ps).close();
    }

    @Test
    void getCity_returnsNull_whenNoRows() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        App app = new App("jdbc:mock", "u", "p");
        injectConnection(app, con);

        App.City c = app.getCity(999999);
        assertNull(c);
    }

    @Test
    void getTopCities_handlesZeroLimit_andMultipleRows() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        // return two rows then end
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getInt("ID")).thenReturn(1, 2);
        when(rs.getString("Name")).thenReturn("A", "B");
        when(rs.getString("CountryCode")).thenReturn("X", "X");
        when(rs.getString("District")).thenReturn("D1", "D2");
        when(rs.getInt("Population")).thenReturn(1000, 500);

        App app = new App("jdbc:mock", "u", "p");
        injectConnection(app, con);

        List<App.City> list = app.getTopCitiesInCountry("X", 0);
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @Test
    void getTopCountries_handlesSQLExceptionGracefully() throws Exception {
        Connection con = mock(Connection.class);
        when(con.prepareStatement(anyString())).thenThrow(new SQLException("boom"));

        App app = new App("jdbc:mock", "u", "p");
        injectConnection(app, con);

        List<App.Country> out = app.getTopCountriesByPopulation(5);
        assertNotNull(out);
        assertTrue(out.isEmpty());
    }

    @Test
    void getPopulationByContinent_returnsAggregates() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString("Continent")).thenReturn("Europe");
        when(rs.getLong("Pop")).thenReturn(741000000L);

        App app = new App("jdbc:mock", "u", "p");
        injectConnection(app, con);

        List<App.ContinentPop> list = app.getPopulationByContinent();
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Europe", list.get(0).continent);
    }
}
