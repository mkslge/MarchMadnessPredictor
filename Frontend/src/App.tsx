import { useEffect, useMemo, useState } from "react";
import {
  BarChart3,
  CalendarDays,
  Database,
  GitBranch,
  Loader2,
  Play,
  RefreshCcw,
  Search,
  Trophy,
  Users,
} from "lucide-react";

import {
  BracketResult,
  GameResult,
  Team,
  TeamYearStatistics,
  generateBracket,
  getTeamsForYear,
  getTotalBracketsGenerated,
  getYearStatistics,
  getYears,
  simulateGame,
} from "./api";
import { Badge } from "./components/ui/badge";
import { Button } from "./components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "./components/ui/card";
import { Input } from "./components/ui/input";
import { Select } from "./components/ui/select";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "./components/ui/table";

const formatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 1,
});

function teamName(team?: Team) {
  return team?.name || team?.Team || "Unknown";
}

function teamSeed(team?: Team) {
  return team?.seed || team?.Seed || 0;
}

function teamRating(team?: Team) {
  return team?.adjEM || team?.AdjEM || 0;
}

function App() {
  const [years, setYears] = useState<number[]>([]);
  const [gameYearOne, setGameYearOne] = useState<number | "">("");
  const [gameYearTwo, setGameYearTwo] = useState<number | "">("");
  const [teamOne, setTeamOne] = useState("");
  const [teamTwo, setTeamTwo] = useState("");
  const [teamsForYearOne, setTeamsForYearOne] = useState<string[]>([]);
  const [teamsForYearTwo, setTeamsForYearTwo] = useState<string[]>([]);
  const [gameResult, setGameResult] = useState<GameResult | null>(null);
  const [gameError, setGameError] = useState("");
  const [isGameLoading, setIsGameLoading] = useState(false);

  const [bracketYear, setBracketYear] = useState<number | "">("");
  const [bracketResult, setBracketResult] = useState<BracketResult | null>(null);
  const [bracketError, setBracketError] = useState("");
  const [isBracketLoading, setIsBracketLoading] = useState(false);

  const [statisticsYear, setStatisticsYear] = useState<number | "">("");
  const [statistics, setStatistics] = useState<TeamYearStatistics[]>([]);
  const [statisticsSearch, setStatisticsSearch] = useState("");
  const [statisticsError, setStatisticsError] = useState("");
  const [isStatisticsLoading, setIsStatisticsLoading] = useState(false);
  const [totalBracketsGenerated, setTotalBracketsGenerated] = useState(0);
  const [isTotalBracketsLoading, setIsTotalBracketsLoading] = useState(false);

  const yearOptions = years.map((year) => ({ label: String(year), value: year }));

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
    getYears()
      .then((availableYears) => {
        setYears(availableYears);
        const defaultYear = availableYears.length > 0 ? availableYears[availableYears.length - 1] : "";
        setGameYearOne(defaultYear);
        setGameYearTwo(defaultYear);
        setBracketYear(defaultYear);
        setStatisticsYear(defaultYear);
      })
      .catch((error: Error) => {
        setGameError(error.message);
        setBracketError(error.message);
        setStatisticsError(error.message);
      });

    loadTotalBracketsGenerated();
  }, []);

  useEffect(() => {
    if (gameYearOne === "") {
      setTeamsForYearOne([]);
      return;
    }

    getTeamsForYear(gameYearOne)
      .then((teams) => {
        setTeamsForYearOne(teams);
        setTeamOne((currentTeam) => currentTeam || teams[0] || "");
      })
      .catch((error: Error) => setGameError(error.message));
  }, [gameYearOne]);

  useEffect(() => {
    if (gameYearTwo === "") {
      setTeamsForYearTwo([]);
      return;
    }

    getTeamsForYear(gameYearTwo)
      .then((teams) => {
        setTeamsForYearTwo(teams);
        setTeamTwo((currentTeam) => currentTeam || teams[1] || teams[0] || "");
      })
      .catch((error: Error) => setGameError(error.message));
  }, [gameYearTwo]);

  useEffect(() => {
    if (statisticsYear === "") {
      return;
    }

    loadStatistics(statisticsYear);
  }, [statisticsYear]);

  async function handleSimulateGame() {
    if (gameYearOne === "" || gameYearTwo === "" || !teamOne || !teamTwo) {
      setGameError("Select two teams before generating a game.");
      return;
    }

    setIsGameLoading(true);
    setGameError("");

    try {
      const result = await simulateGame(teamOne, gameYearOne, teamTwo, gameYearTwo);
      setGameResult(result);
    } catch (error) {
      setGameError(error instanceof Error ? error.message : "Unable to simulate game.");
    } finally {
      setIsGameLoading(false);
    }
  }

  async function handleGenerateBracket() {
    if (bracketYear === "") {
      setBracketError("Select a year before generating a bracket.");
      return;
    }

    setIsBracketLoading(true);
    setBracketError("");

    try {
      const result = await generateBracket(bracketYear);
      setBracketResult(result);
    } catch (error) {
      setBracketResult(null);
      setBracketError(error instanceof Error ? error.message : "Unable to generate bracket.");
    } finally {
      setIsBracketLoading(false);
    }
  }

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

  async function loadTotalBracketsGenerated() {
    setIsTotalBracketsLoading(true);

    try {
      const totalGenerated = await getTotalBracketsGenerated();
      setTotalBracketsGenerated(totalGenerated);
    } catch (error) {
      setStatisticsError(error instanceof Error ? error.message : "Unable to load total generated brackets.");
    } finally {
      setIsTotalBracketsLoading(false);
    }
  }

  return (
    <main className="min-h-screen court-grid">
      <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 py-5 sm:px-6 lg:px-8">
        <header className="flex flex-col gap-4 border-b bg-background/80 py-4 backdrop-blur sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="flex items-center gap-2 text-sm font-medium text-muted-foreground">
              <Trophy className="h-4 w-4 text-primary" />
              March Madness Model
            </div>
            <h1 className="mt-2 text-3xl font-semibold tracking-normal text-foreground">
              Tournament Simulator
            </h1>
          </div>
          <div className="flex items-center gap-2">
            <Badge variant="secondary">{years.length} seasons</Badge>
            <Badge variant="secondary">
              {isTotalBracketsLoading ? "Loading brackets" : `${formatter.format(totalBracketsGenerated)} brackets`}
            </Badge>
          </div>
        </header>

        <section className="grid gap-6 lg:grid-cols-[1.05fr_0.95fr]">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Play className="h-5 w-5 text-primary" />
                Generate Game
              </CardTitle>
              <CardDescription>Pick any two tournament teams and run a single matchup.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-5">
              <div className="grid gap-4 sm:grid-cols-2">
                <TeamPicker
                  label="Team 1"
                  year={gameYearOne}
                  years={yearOptions}
                  team={teamOne}
                  teams={teamsForYearOne}
                  onYearChange={(year) => {
                    setGameYearOne(year);
                    setTeamOne("");
                  }}
                  onTeamChange={setTeamOne}
                />
                <TeamPicker
                  label="Team 2"
                  year={gameYearTwo}
                  years={yearOptions}
                  team={teamTwo}
                  teams={teamsForYearTwo}
                  onYearChange={(year) => {
                    setGameYearTwo(year);
                    setTeamTwo("");
                  }}
                  onTeamChange={setTeamTwo}
                />
              </div>

              {gameError ? (
                <div className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                  {gameError}
                </div>
              ) : null}

              <Button className="w-full sm:w-auto" disabled={isGameLoading} onClick={handleSimulateGame}>
                {isGameLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Play className="h-4 w-4" />}
                Generate Game
              </Button>
            </CardContent>
          </Card>

          <GameResultCard result={gameResult} isLoading={isGameLoading} />
        </section>

        <section className="grid gap-6 lg:grid-cols-[0.85fr_1.15fr]">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <GitBranch className="h-5 w-5 text-accent" />
                Generate Bracket
              </CardTitle>
              <CardDescription>Run a full tournament simulation for one season.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-5">
              <div className="grid gap-3 sm:grid-cols-[180px_auto]">
                <Select
                  aria-label="Bracket year"
                  value={bracketYear}
                  options={yearOptions}
                  placeholder="Year"
                  onChange={(event) => setBracketYear(Number(event.target.value))}
                />
                <Button disabled={isBracketLoading || bracketYear === ""} onClick={handleGenerateBracket}>
                  {isBracketLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <GitBranch className="h-4 w-4" />}
                  Generate Bracket
                </Button>
              </div>

              {bracketError ? (
                <div className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                  {bracketError}
                </div>
              ) : null}

              <div className="grid gap-3 sm:grid-cols-2">
                <ResultMetric label="Selected year" value={bracketYear === "" ? "None" : String(bracketYear)} />
                <ResultMetric label="Latest champion" value={bracketResult ? teamName(bracketResult.champion) : "None"} />
              </div>
            </CardContent>
          </Card>

          <BracketResultCard result={bracketResult} isLoading={isBracketLoading} />
        </section>

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
                  onClick={() => {
                    if (statisticsYear !== "") {
                      loadStatistics(statisticsYear);
                    }
                    loadTotalBracketsGenerated();
                  }}
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
                <Metric
                  label="Game wins"
                  value={statisticsTotals.gameWins}
                  icon={<BarChart3 className="h-4 w-4" />}
                />
              </div>

              {statisticsError ? (
                <div className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
                  {statisticsError}
                </div>
              ) : null}

              <StatisticsTable rows={filteredStatistics} isLoading={isStatisticsLoading} />
            </CardContent>
          </Card>
        </section>
      </div>
    </main>
  );
}

type TeamPickerProps = {
  label: string;
  year: number | "";
  years: Array<{ label: string; value: number }>;
  team: string;
  teams: string[];
  onYearChange: (year: number) => void;
  onTeamChange: (team: string) => void;
};

function TeamPicker({ label, year, years, team, teams, onYearChange, onTeamChange }: TeamPickerProps) {
  return (
    <div className="rounded-lg border bg-muted/30 p-4">
      <div className="mb-3 text-sm font-semibold">{label}</div>
      <div className="grid gap-3">
        <Select
          aria-label={`${label} year`}
          value={year}
          options={years}
          placeholder="Year"
          onChange={(event) => onYearChange(Number(event.target.value))}
        />
        <Select
          aria-label={`${label} team`}
          value={team}
          options={teams.map((teamNameValue) => ({ label: teamNameValue, value: teamNameValue }))}
          placeholder="Team"
          onChange={(event) => onTeamChange(event.target.value)}
        />
      </div>
    </div>
  );
}

function GameResultCard({ result, isLoading }: { result: GameResult | null; isLoading: boolean }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Trophy className="h-5 w-5 text-secondary" />
          Game Result
        </CardTitle>
        <CardDescription>Winner, loser, seed, and model probability from the latest generated game.</CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex min-h-[221px] items-center justify-center rounded-lg border border-dashed">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          </div>
        ) : result ? (
          <div className="grid gap-4">
            <div className="rounded-lg border bg-primary/10 p-4">
              <div className="text-sm text-muted-foreground">Winner</div>
              <div className="mt-1 flex flex-wrap items-center gap-2">
                <span className="text-2xl font-semibold">{teamName(result.winner)}</span>
                <Badge>Seed {teamSeed(result.winner)}</Badge>
              </div>
            </div>
            <div className="grid gap-3 sm:grid-cols-3">
              <ResultMetric label="Win odds" value={`${formatter.format(result.oddsOutOf100)}%`} />
              <ResultMetric label="Loser" value={teamName(result.loser)} />
              <ResultMetric label="Winner rating" value={formatter.format(teamRating(result.winner))} />
            </div>
            <div className="rounded-lg border p-4">
              <div className="flex items-center justify-between gap-3 text-sm">
                <span>{teamName(result.team1)}</span>
                <span className="text-muted-foreground">vs</span>
                <span className="text-right">{teamName(result.team2)}</span>
              </div>
            </div>
          </div>
        ) : (
          <div className="flex min-h-[221px] items-center justify-center rounded-lg border border-dashed px-6 text-center text-sm text-muted-foreground">
            No game generated yet.
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function BracketResultCard({ result, isLoading }: { result: BracketResult | null; isLoading: boolean }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Trophy className="h-5 w-5 text-secondary" />
          Bracket Result
        </CardTitle>
        <CardDescription>Champion, Final Four, and regional winners from the latest generated bracket.</CardDescription>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <div className="flex min-h-[259px] items-center justify-center rounded-lg border border-dashed">
            <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          </div>
        ) : result ? (
          <div className="grid gap-4">
            <div className="rounded-lg border bg-secondary/20 p-4">
              <div className="text-sm text-muted-foreground">Champion</div>
              <div className="mt-1 flex flex-wrap items-center gap-2">
                <span className="text-2xl font-semibold">{teamName(result.champion)}</span>
                <Badge>Seed {teamSeed(result.champion)}</Badge>
                <Badge variant="secondary">{result.year}</Badge>
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-4">
              {(["east", "midwest", "south", "west"] as const).map((regionName) => (
                <ResultMetric
                  key={regionName}
                  label={formatRegionName(regionName)}
                  value={teamName(result.finalFour[regionName])}
                />
              ))}
            </div>

            
          </div>
        ) : (
          <div className="flex min-h-[259px] items-center justify-center rounded-lg border border-dashed px-6 text-center text-sm text-muted-foreground">
            No bracket generated yet.
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function Metric({
  label,
  value,
  icon,
  isLoading = false,
}: {
  label: string;
  value: number;
  icon: React.ReactNode;
  isLoading?: boolean;
}) {
  return (
    <div className="rounded-lg border bg-muted/30 p-3">
      <div className="flex items-center gap-2 text-xs font-medium text-muted-foreground">
        {icon}
        {label}
      </div>
      <div className="mt-2 text-2xl font-semibold">
        {isLoading ? <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" /> : formatter.format(value)}
      </div>
    </div>
  );
}

function ResultMetric({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border bg-muted/30 p-3">
      <div className="text-xs font-medium text-muted-foreground">{label}</div>
      <div className="mt-2 truncate text-base font-semibold">{value}</div>
    </div>
  );
}

function formatRegionName(regionName: string) {
  return regionName.charAt(0).toUpperCase() + regionName.slice(1).toLowerCase();
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
              <TableCell className="text-right">{formatter.format(gameWinRate)}%</TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}

export default App;
