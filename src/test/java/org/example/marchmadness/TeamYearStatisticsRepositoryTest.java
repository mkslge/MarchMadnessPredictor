package org.example.marchmadness;

import org.example.marchmadness.entities.TeamYearStatistics;
import org.example.marchmadness.repositories.TeamYearStatisticsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionOperations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeamYearStatisticsRepositoryTest {

    @Test
    @DisplayName("Repository findByTeamAndYear returns statistics when the database row exists")
    void findByTeamAndYear_returnsStatisticsWhenRowExists() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ResultSet resultSet = mock(ResultSet.class);
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mockJdbcTemplateProvider(jdbcTemplate);
        ObjectProvider<TransactionOperations> transactionOperationsProvider = mockTransactionOperationsProvider(null);

        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getString("team")).thenReturn("Maryland");
        when(resultSet.getInt("year")).thenReturn(2023);
        when(resultSet.getInt("seed")).thenReturn(8);
        when(resultSet.getDouble("rating")).thenReturn(91.5);
        when(resultSet.getInt("bracketsimulations")).thenReturn(20);
        when(resultSet.getInt("bracketwins")).thenReturn(2);
        when(resultSet.getInt("gamesimulations")).thenReturn(50);
        when(resultSet.getInt("gamewins")).thenReturn(30);
        when(jdbcTemplate.query(
                eq("""
                        SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
                        FROM teamyearstatistics
                        WHERE team = ? AND year = ?
                        """),
                any(RowMapper.class),
                eq("Maryland"),
                eq(2023)
        )).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RowMapper<TeamYearStatistics> rowMapper = invocation.getArgument(1);
            return List.of(rowMapper.mapRow(resultSet, 0));
        });

        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(
                jdbcTemplateProvider,
                transactionOperationsProvider
        );

        Optional<TeamYearStatistics> result = repository.findByTeamAndYear("Maryland", 2023);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getId());
        assertEquals("Maryland", result.get().getTeam());
        assertEquals(2023, result.get().getYear());
        assertEquals(8, result.get().getSeed());
    }

    @Test
    @DisplayName("Repository findByTeamAndYear returns empty when the database row does not exist")
    void findByTeamAndYear_returnsEmptyWhenRowDoesNotExist() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mockJdbcTemplateProvider(jdbcTemplate);
        ObjectProvider<TransactionOperations> transactionOperationsProvider = mockTransactionOperationsProvider(null);

        when(jdbcTemplate.query(
                eq("""
                        SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
                        FROM teamyearstatistics
                        WHERE team = ? AND year = ?
                        """),
                any(RowMapper.class),
                eq("Maryland"),
                eq(2023)
        )).thenReturn(List.of());

        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(
                jdbcTemplateProvider,
                transactionOperationsProvider
        );

        Optional<TeamYearStatistics> result = repository.findByTeamAndYear("Maryland", 2023);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Repository findByYear returns every team statistics row for the requested year")
    void findByYear_returnsEveryTeamStatisticsRowForYear() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        ResultSet resultSet = mock(ResultSet.class);
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mockJdbcTemplateProvider(jdbcTemplate);
        ObjectProvider<TransactionOperations> transactionOperationsProvider = mockTransactionOperationsProvider(null);

        when(resultSet.getLong("id")).thenReturn(10L);
        when(resultSet.getString("team")).thenReturn("Maryland");
        when(resultSet.getInt("year")).thenReturn(2023);
        when(resultSet.getInt("seed")).thenReturn(8);
        when(resultSet.getDouble("rating")).thenReturn(91.5);
        when(resultSet.getInt("bracketsimulations")).thenReturn(20);
        when(resultSet.getInt("bracketwins")).thenReturn(2);
        when(resultSet.getInt("gamesimulations")).thenReturn(50);
        when(resultSet.getInt("gamewins")).thenReturn(30);
        when(jdbcTemplate.query(
                eq("""
                        SELECT id, team, year, seed, rating, bracketsimulations, bracketwins, gamesimulations, gamewins
                        FROM teamyearstatistics
                        WHERE year = ?
                        ORDER BY bracketwins DESC, gamewins DESC, team ASC
                        """),
                any(RowMapper.class),
                eq(2023)
        )).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RowMapper<TeamYearStatistics> rowMapper = invocation.getArgument(1);
            return List.of(rowMapper.mapRow(resultSet, 0));
        });

        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(
                jdbcTemplateProvider,
                transactionOperationsProvider
        );

        List<TeamYearStatistics> result = repository.findByYear(2023);

        assertEquals(1, result.size());
        assertEquals("Maryland", result.get(0).getTeam());
        assertEquals(2, result.get(0).getBracketWins());
    }

    @Test
    @DisplayName("Repository findByTeamAndYear throws when PostgreSQL is not configured")
    void findByTeamAndYear_throwsWhenDataSourceIsMissing() {
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mockJdbcTemplateProvider(null);
        ObjectProvider<TransactionOperations> transactionOperationsProvider = mockTransactionOperationsProvider(null);
        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(
                jdbcTemplateProvider,
                transactionOperationsProvider
        );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> repository.findByTeamAndYear("Maryland", 2023)
        );

        assertTrue(exception.getMessage().contains("PostgreSQL is not configured"));
    }

    @Test
    @DisplayName("Repository applyStatisticsUpdates commits batch updates when all rows exist")
    void applyStatisticsUpdates_commitsBatchUpdatesWhenAllRowsExist() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        TransactionOperations transactionOperations = mockTransactionOperations();
        PreparedStatement statement = mock(PreparedStatement.class);
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mockJdbcTemplateProvider(jdbcTemplate);
        ObjectProvider<TransactionOperations> transactionOperationsProvider =
                mockTransactionOperationsProvider(transactionOperations);
        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(
                jdbcTemplateProvider,
                transactionOperationsProvider
        );

        when(jdbcTemplate.batchUpdate(
                eq("""
                        UPDATE teamyearstatistics
                        SET bracketsimulations = bracketsimulations + ?,
                            bracketwins = bracketwins + ?,
                            bracketsplayed = bracketsplayed + ?,
                            gamesimulations = gamesimulations + ?,
                            gamewins = gamewins + ?
                        WHERE team = ? AND year = ?
                        """),
                any(BatchPreparedStatementSetter.class)
        )).thenAnswer(invocation -> {
            BatchPreparedStatementSetter batchSetter = invocation.getArgument(1);
            batchSetter.setValues(statement, 0);
            assertEquals(1, batchSetter.getBatchSize());
            return new int[]{1};
        });

        repository.applyStatisticsUpdates(List.of(
                new TeamYearStatisticsRepository.TeamYearStatisticsUpdate(
                        "Maryland",
                        2023,
                        1,
                        0,
                        1,
                        4,
                        2
                )
        ));

        verify(statement).setInt(1, 1);
        verify(statement).setInt(2, 0);
        verify(statement).setInt(3, 1);
        verify(statement).setInt(4, 4);
        verify(statement).setInt(5, 2);
        verify(statement).setString(6, "Maryland");
        verify(statement).setInt(7, 2023);
        verify(transactionOperations).executeWithoutResult(any());
    }

    @Test
    @DisplayName("Repository applyStatisticsUpdates throws when any team row is missing")
    void applyStatisticsUpdates_throwsWhenAnyTeamRowIsMissing() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        TransactionOperations transactionOperations = mockTransactionOperations();
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mockJdbcTemplateProvider(jdbcTemplate);
        ObjectProvider<TransactionOperations> transactionOperationsProvider =
                mockTransactionOperationsProvider(transactionOperations);
        TeamYearStatisticsRepository repository = new TeamYearStatisticsRepository(
                jdbcTemplateProvider,
                transactionOperationsProvider
        );

        when(jdbcTemplate.batchUpdate(
                eq("""
                        UPDATE teamyearstatistics
                        SET bracketsimulations = bracketsimulations + ?,
                            bracketwins = bracketwins + ?,
                            bracketsplayed = bracketsplayed + ?,
                            gamesimulations = gamesimulations + ?,
                            gamewins = gamewins + ?
                        WHERE team = ? AND year = ?
                        """),
                any(BatchPreparedStatementSetter.class)
        )).thenReturn(new int[]{Statement.EXECUTE_FAILED});

        assertThrows(
                IllegalStateException.class,
                () -> repository.applyStatisticsUpdates(List.of(
                        new TeamYearStatisticsRepository.TeamYearStatisticsUpdate(
                                "Maryland",
                                2023,
                                1,
                                0,
                                1,
                                4,
                                2
                        )
                ))
        );
    }

    /*
     * Command: Create a mock provider for the repository JdbcTemplate dependency.
     * Pre-condition: The test has decided whether a JdbcTemplate should be available.
     * Post-condition: Returns an ObjectProvider that supplies that JdbcTemplate value.
     *
     * Keeps repository tests focused on repository behavior instead of Spring wiring.
     */
    private ObjectProvider<JdbcTemplate> mockJdbcTemplateProvider(JdbcTemplate jdbcTemplate) {
        @SuppressWarnings("unchecked")
        ObjectProvider<JdbcTemplate> jdbcTemplateProvider = mock(ObjectProvider.class);
        when(jdbcTemplateProvider.getIfAvailable()).thenReturn(jdbcTemplate);
        return jdbcTemplateProvider;
    }

    /*
     * Command: Create a mock provider for the repository transaction dependency.
     * Pre-condition: The test has decided whether transactions should be available.
     * Post-condition: Returns an ObjectProvider that supplies that transaction helper value.
     *
     * Keeps missing transaction configuration tests simple.
     */
    private ObjectProvider<TransactionOperations> mockTransactionOperationsProvider(
            TransactionOperations transactionOperations
    ) {
        @SuppressWarnings("unchecked")
        ObjectProvider<TransactionOperations> transactionOperationsProvider = mock(ObjectProvider.class);
        when(transactionOperationsProvider.getIfAvailable()).thenReturn(transactionOperations);
        return transactionOperationsProvider;
    }

    /*
     * Command: Create transaction operations that immediately run the given callback.
     * Pre-condition: The test needs repository write logic to execute.
     * Post-condition: Returns a mock TransactionOperations that invokes its callback.
     *
     * Lets tests focus on repository behavior while Spring owns real rollback behavior in production.
     */
    private TransactionOperations mockTransactionOperations() {
        TransactionOperations transactionOperations = mock(TransactionOperations.class);
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(mock(TransactionStatus.class));
            return null;
        }).when(transactionOperations).executeWithoutResult(any());
        return transactionOperations;
    }
}
