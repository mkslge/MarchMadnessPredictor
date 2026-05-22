import { useEffect, useState } from "react";
import { GitBranch, Loader2, Trophy } from "lucide-react";

import { BracketResult, generateBracket } from "@/api";
import { ErrorMessage } from "@/components/shared/ErrorMessage";
import { ResultMetric } from "@/components/shared/ResultMetric";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Select } from "@/components/ui/select";
import { formatRegionName, teamName, teamSeed } from "@/lib/formatters";

type BracketGeneratorSectionProps = {
  defaultYear: number | "";
  onBracketGenerated: () => void;
  yearOptions: Array<{ label: string; value: number }>;
};

export function BracketGeneratorSection({
  defaultYear,
  onBracketGenerated,
  yearOptions,
}: BracketGeneratorSectionProps) {
  const [bracketYear, setBracketYear] = useState<number | "">(defaultYear);
  const [bracketResult, setBracketResult] = useState<BracketResult | null>(null);
  const [bracketError, setBracketError] = useState("");
  const [isBracketLoading, setIsBracketLoading] = useState(false);

  useEffect(() => {
    setBracketYear(defaultYear);
  }, [defaultYear]);

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
      onBracketGenerated();
    } catch (error) {
      setBracketResult(null);
      setBracketError(error instanceof Error ? error.message : "Unable to generate bracket.");
    } finally {
      setIsBracketLoading(false);
    }
  }

  return (
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

          <ErrorMessage message={bracketError} />

          <div className="grid gap-3 sm:grid-cols-2">
            <ResultMetric label="Selected year" value={bracketYear === "" ? "None" : String(bracketYear)} />
            <ResultMetric label="Latest champion" value={bracketResult ? teamName(bracketResult.champion) : "None"} />
          </div>
        </CardContent>
      </Card>

      <BracketResultCard result={bracketResult} isLoading={isBracketLoading} />
    </section>
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
