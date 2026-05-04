package org.example.marchmadness;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.example.marchmadness.repositories.TeamYearStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamYearStatisticsRepositoryTest {

    @Test
    @DisplayName("Repository findByTeamAndYear returns statistics when the database row exists")
    void findByTeamAndYear_returnsStatisticsWhenRowExists() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ObjectProvider<DataSource> dataSourceProvider = mockDataSourceProvider(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("""
                SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
                FROM teamyearstatistics
                WHERE team = ? AND year = ?
                """)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getString("team")).thenReturn("Maryland");
        when(resultSet.getInt("year")).thenReturn(2023);
        when(resultSet.getInt("seed")).thenReturn(8);
        when(resultSet.getDouble("rating")).thenReturn(91.5);
        when(resultSet.getInt("bracketsimulations")).thenReturn(20);
        when(resultSet.getInt("bracketwins")).thenReturn(2);
        when(resultSet.getInt("gamesimulations")).thenReturn(50);
        when(resultSet.getInt("gamewins")).thenReturn(30);

        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(dataSourceProvider);

        Optional<TeamYearStatistics> result = repository.findByTeamAndYear("Maryland", 2023);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
        assertEquals("Maryland", result.get().getTeam());
        assertEquals(2023, result.get().getYear());
        assertEquals(8, result.get().getSeed());

        verify(statement).setString(1, "Maryland");
        verify(statement).setInt(2, 2023);
    }

    @Test
    @DisplayName("Repository findByTeamAndYear returns empty when the database row does not exist")
    void findByTeamAndYear_returnsEmptyWhenRowDoesNotExist() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ObjectProvider<DataSource> dataSourceProvider = mockDataSourceProvider(dataSource);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("""
                SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
                FROM teamyearstatistics
                WHERE team = ? AND year = ?
                """)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(dataSourceProvider);

        Optional<TeamYearStatistics> result = repository.findByTeamAndYear("Maryland", 2023);

        assertTrue(result.isEmpty());
        verify(statement).setString(1, "Maryland");
        verify(statement).setInt(2, 2023);
    }

    @Test
    @DisplayName("Repository findByTeamAndYear throws when PostgreSQL is not configured")
    void findByTeamAndYear_throwsWhenDataSourceIsMissing() {
        ObjectProvider<DataSource> dataSourceProvider = mockDataSourceProvider(null);
        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(dataSourceProvider);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.findByTeamAndYear("Maryland", 2023)
        );

        assertTrue(exception.getMessage().contains("PostgreSQL is not configured"));
    }

    /*
     * Command: Create a mock provider for the repository DataSource dependency.
     * Pre-condition: The test has decided whether a DataSource should be available.
     * Post-condition: Returns an ObjectProvider that supplies that DataSource value.
     *
     * Keeps repository tests focused on repository behavior instead of Spring wiring.
     */
    private ObjectProvider<DataSource> mockDataSourceProvider(DataSource dataSource) {
        @SuppressWarnings("unchecked")
        ObjectProvider<DataSource> dataSourceProvider = mock(ObjectProvider.class);
        when(dataSourceProvider.getIfAvailable()).thenReturn(dataSource);
        return dataSourceProvider;
    }
}
