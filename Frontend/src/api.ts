export type Team = {
  year: number;
  name: string;
  seed: number;
  adjT: number;
  adjEM: number;
  Team?: string;
  Seed?: number;
  AdjT?: number;
  AdjEM?: number;
};

export type GameResult = {
  team1: Team;
  team2: Team;
  oddsOutOf100: number;
  winner: Team;
  loser: Team;
};

export type RegionResult = {
  region: string;
  year: number;
  fieldOf64: GameResult[];
  fieldOf32: GameResult[];
  sweet16: GameResult[];
  elite8: GameResult[];
  regionWinner: Team;
};

export type FinalFourResult = {
  east: Team;
  midwest: Team;
  south: Team;
  west: Team;
  eastVSMidwest: GameResult;
  southVSWest: GameResult;
  championship: GameResult;
  champion: Team;
};

export type BracketResult = {
  year: number;
  champion: Team;
  finalFour: FinalFourResult;
  regions: RegionResult[];
};

export type TeamYearStatistics = {
  id: number;
  team: string;
  year: number;
  seed: number;
  rating: number;
  bracketSimulations: number;
  bracketWins: number;
  gameSimulations: number;
  gameWins: number;
};

async function request<T>(path: string): Promise<T> {
  const response = await fetch(path);

  if (!response.ok) {
    const message = await response.text();
    throw new Error(message || `Request failed with status ${response.status}`);
  }

  return response.json() as Promise<T>;
}

export function getYears() {
  return request<number[]>("/bracket/years");
}

export function getTeamsForYear(year: number) {
  return request<string[]>(`/bracket/years/${year}/teams`);
}

export function simulateGame(team1: string, year1: number, team2: string, year2: number) {
  const searchParams = new URLSearchParams({
    team1,
    year1: String(year1),
    team2,
    year2: String(year2),
  });

  return request<GameResult>(`/bracket/game/simulation?${searchParams}`);
}

export function generateBracket(year: number) {
  return request<BracketResult>(`/bracket/simulation/${year}`);
}

export function getYearStatistics(year: number) {
  return request<TeamYearStatistics[]>(`/statistics/year/${year}`);
}

export function getTeamStatistics(team: string, year: number) {
  return request<TeamYearStatistics>(`/statistics/${encodeURIComponent(team)}/${year}`);
}

export function getTotalBracketsGenerated() {
  return request<number>("/statistics/brackets/total");
}
