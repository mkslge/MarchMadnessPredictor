import { useEffect, useMemo, useState } from "react";
import { Trophy } from "lucide-react";

import { getTotalBracketsGenerated, getYears } from "@/api";
import { Badge } from "@/components/ui/badge";
import { BracketGeneratorSection } from "@/features/bracket/BracketGeneratorSection";
import { GameSimulatorSection } from "@/features/game/GameSimulatorSection";
import { StatisticsSection } from "@/features/statistics/StatisticsSection";
import { numberFormatter } from "@/lib/formatters";

function App() {
  const [years, setYears] = useState<number[]>([]);
  const [initialLoadError, setInitialLoadError] = useState("");
  const [totalBracketsGenerated, setTotalBracketsGenerated] = useState(0);
  const [isTotalBracketsLoading, setIsTotalBracketsLoading] = useState(false);

  const defaultYear = years.length > 0 ? years[years.length - 1] : "";
  const yearOptions = useMemo(() => years.map((year) => ({ label: String(year), value: year })), [years]);

  useEffect(() => {
    getYears()
      .then(setYears)
      .catch((error: Error) => setInitialLoadError(error.message));

    loadTotalBracketsGenerated();
  }, []);

  async function loadTotalBracketsGenerated() {
    setIsTotalBracketsLoading(true);

    try {
      const totalGenerated = await getTotalBracketsGenerated();
      setTotalBracketsGenerated(totalGenerated);
    } catch (error) {
      setInitialLoadError(error instanceof Error ? error.message : "Unable to load total generated brackets.");
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
            <h1 className="mt-2 text-3xl font-semibold tracking-normal text-foreground">Tournament Simulator</h1>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Badge variant="secondary">{years.length} seasons</Badge>
            <Badge variant="secondary">
              {isTotalBracketsLoading ? "Loading brackets" : `${numberFormatter.format(totalBracketsGenerated)} brackets`}
            </Badge>
          </div>
        </header>

        {initialLoadError ? (
          <div className="rounded-md border border-destructive/30 bg-destructive/10 px-3 py-2 text-sm text-destructive">
            {initialLoadError}
          </div>
        ) : null}

        <GameSimulatorSection defaultYear={defaultYear} yearOptions={yearOptions} />
        <BracketGeneratorSection defaultYear={defaultYear} yearOptions={yearOptions} />
        <StatisticsSection
          defaultYear={defaultYear}
          isTotalBracketsLoading={isTotalBracketsLoading}
          onRefreshTotalBrackets={loadTotalBracketsGenerated}
          totalBracketsGenerated={totalBracketsGenerated}
          yearOptions={yearOptions}
        />
      </div>
    </main>
  );
}

export default App;
