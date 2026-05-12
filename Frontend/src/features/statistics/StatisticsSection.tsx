import { useEffect, useMemo, useState } from "react";
import { BarChart3, CalendarDays, Database, Loader2, RefreshCcw, Search, Trophy, Users } from "lucide-react";

import { TeamYearStatistics, getYearStatistics } from "@/api";
import { ErrorMessage } from "@/components/shared/ErrorMessage";
import { Metric } from "@/components/shared/Metric";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Select } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { numberFormatter } from "@/lib/formatters";

type StatisticsSectionProps = {
  defaultYear: number | "";
  isTotalBracketsLoading: boolean;
  onRefreshTotalBrackets: () => void;
  totalBracketsGenerated: number;
  yearOptions: Array<{ label: string; value: number }>;
};

export function StatisticsSection({
  defaultYear,
  isTotalBracketsLoading,
  onRefreshTotalBrackets,
  totalBracketsGenerated,
  yearOptions,
}: StatisticsSectionProps) {
  const [statisticsYear, setStatisticsYear] = useState<number | "">(defaultYear);
  const [statistics, setStatistics] = useState<TeamYearStatistics[]>([]);
  const [statisticsSearch, setStatisticsSearch] = useState("");
  const [statisticsError, setStatisticsError] = useState("");
  const [isStatisticsLoading, setIsStatisticsLoading] = useState(false);

  const filteredStatistics = useMemo(() => {
    const normalizedSearch = statisticsSearch.trim().toLowerCase();

    if (!normalizedSearch) {
      return statistics;
    }

    return statistics.filter((row) => row.team.toLowerCase().includes(normalizedSearch));
  }, [statistics, statisticsSearch]);

  const statisticsTotals = useMemo(
    () =>
      statistics.reduce(
        (totals, row) => ({
          bracketWins: totals.bracketWins + row.bracketWins,
          bracketSimulations: Math.max(totals.bracketSimulations, row.bracketSimulations),
          gameWins: totals.gameWins + row.gameWins,
          gameSimulations: totals.gameSimulations + row.gameSimulations,
        }),
        { bracketWins: 0, bracketSimulations: 0, gameWins: 0, gameSimulations: 0 },
      ),
    [statistics],
  );

  useEffect(() => {
    setStatisticsYear(defaultYear);
  }, [defaultYear]);

  useEffect(() => {
    if (statisticsYear === "") {
      return;
    }

    loadStatistics(statisticsYear);
  }, [statisticsYear]);

  async function loadStatistics(year: number) {
    setIsStatisticsLoading(true);
    setStatisticsError("");

    try {
      const rows = await getYearStatistics(year);
      setStatistics(rows);
    } catch (error) {
      setStatistics([]);
      setStatisticsError(error instanceof Error ? error.message : "Unable to load statistics.");
    } finally {
      setIsStatisticsLoading(false);
    }
  }

  function handleRefreshStatistics() {
    if (statisticsYear !== "") {
      loadStatistics(statisticsYear);
    }
    onRefreshTotalBrackets();
  }

  return (
    <section>
      <Card>
        <CardHeader className="gap-4 lg:flex-row lg:items-center lg:justify-between lg:space-y-0">
          <div>
            <CardTitle className="flex items-center gap-2">
              <BarChart3 className="h-5 w-5 text-accent" />
              Generated Statistics
            </CardTitle>
            <CardDescription>Leaderboard of recorded bracket and game outcomes by year.</CardDescription>
          </div>
          <div className="grid gap-2 sm:grid-cols-[150px_240px_auto]">
            <Select
              aria-label="Statistics year"
              value={statisticsYear}
              options={yearOptions}
              placeholder="Year"
              onChange={(event) => setStatisticsYear(Number(event.target.value))}
            />
            <div className="relative">
              <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
              <Input
                className="pl-9"
                placeholder="Filter teams"
                value={statisticsSearch}
                onChange={(event) => setStatisticsSearch(event.target.value)}
              />
            </div>
            <Button
              variant="outline"
              disabled={isStatisticsLoading || statisticsYear === ""}
              onClick={handleRefreshStatistics}
            >
              {isStatisticsLoading || isTotalBracketsLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
              ) : (
                <RefreshCcw className="h-4 w-4" />
              )}
              Refresh
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
            <Metric
              label="All-time brackets"
              value={totalBracketsGenerated}
              icon={<Database className="h-4 w-4" />}
              isLoading={isTotalBracketsLoading}
            />
            <Metric label="Teams" value={statistics.length} icon={<Users className="h-4 w-4" />} />
            <Metric
              label="Bracket wins"
              value={statisticsTotals.bracketWins}
              icon={<Trophy className="h-4 w-4" />}
            />
            <Metric
              label="Max brackets"
              value={statisticsTotals.bracketSimulations}
              icon={<CalendarDays className="h-4 w-4" />}
            />
            <Metric label="Game wins" value={statisticsTotals.gameWins} icon={<BarChart3 className="h-4 w-4" />} />
          </div>

          <ErrorMessage message={statisticsError} />

          <StatisticsTable rows={filteredStatistics} isLoading={isStatisticsLoading} />
        </CardContent>
      </Card>
    </section>
  );
}

function StatisticsTable({ rows, isLoading }: { rows: TeamYearStatistics[]; isLoading: boolean }) {
  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center rounded-lg border border-dashed">
        <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (rows.length === 0) {
    return (
      <div className="flex h-64 items-center justify-center rounded-lg border border-dashed px-6 text-center text-sm text-muted-foreground">
        No generated statistics found for this year.
      </div>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead className="w-12">Rank</TableHead>
          <TableHead>Team</TableHead>
          <TableHead className="text-right">Seed</TableHead>
          <TableHead className="text-right">Bracket wins</TableHead>
          <TableHead className="text-right">Brackets</TableHead>
          <TableHead className="text-right">Game wins</TableHead>
          <TableHead className="text-right">Games</TableHead>
          <TableHead className="text-right">Game win %</TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {rows.map((row, index) => {
          const gameWinRate = row.gameSimulations === 0 ? 0 : (row.gameWins / row.gameSimulations) * 100;

          return (
            <TableRow key={`${row.team}-${row.year}`}>
              <TableCell className="font-medium">{index + 1}</TableCell>
              <TableCell>
                <div className="font-medium">{row.team}</div>
                <div className="text-xs text-muted-foreground">{row.year}</div>
              </TableCell>
              <TableCell className="text-right">{row.seed}</TableCell>
              <TableCell className="text-right font-semibold">{row.bracketWins}</TableCell>
              <TableCell className="text-right">{row.bracketSimulations}</TableCell>
              <TableCell className="text-right">{row.gameWins}</TableCell>
              <TableCell className="text-right">{row.gameSimulations}</TableCell>
              <TableCell className="text-right">{numberFormatter.format(gameWinRate)}%</TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}
