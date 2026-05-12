import { useEffect, useState } from "react";
import { Loader2, Play, Trophy } from "lucide-react";

import { GameResult, getTeamsForYear, simulateGame } from "@/api";
import { ErrorMessage } from "@/components/shared/ErrorMessage";
import { ResultMetric } from "@/components/shared/ResultMetric";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Select } from "@/components/ui/select";
import { numberFormatter, teamName, teamRating, teamSeed } from "@/lib/formatters";

type GameSimulatorSectionProps = {
  defaultYear: number | "";
  yearOptions: Array<{ label: string; value: number }>;
};

export function GameSimulatorSection({ defaultYear, yearOptions }: GameSimulatorSectionProps) {
  const [gameYearOne, setGameYearOne] = useState<number | "">(defaultYear);
  const [gameYearTwo, setGameYearTwo] = useState<number | "">(defaultYear);
  const [teamOne, setTeamOne] = useState("");
  const [teamTwo, setTeamTwo] = useState("");
  const [teamsForYearOne, setTeamsForYearOne] = useState<string[]>([]);
  const [teamsForYearTwo, setTeamsForYearTwo] = useState<string[]>([]);
  const [gameResult, setGameResult] = useState<GameResult | null>(null);
  const [gameError, setGameError] = useState("");
  const [isGameLoading, setIsGameLoading] = useState(false);

  useEffect(() => {
    setGameYearOne(defaultYear);
    setGameYearTwo(defaultYear);
  }, [defaultYear]);

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

  return (
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

          <ErrorMessage message={gameError} />

          <Button className="w-full sm:w-auto" disabled={isGameLoading} onClick={handleSimulateGame}>
            {isGameLoading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Play className="h-4 w-4" />}
            Generate Game
          </Button>
        </CardContent>
      </Card>

      <GameResultCard result={gameResult} isLoading={isGameLoading} />
    </section>
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
              <ResultMetric label="Win odds" value={`${numberFormatter.format(result.oddsOutOf100)}%`} />
              <ResultMetric label="Loser" value={teamName(result.loser)} />
              <ResultMetric label="Winner rating" value={numberFormatter.format(teamRating(result.winner))} />
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
