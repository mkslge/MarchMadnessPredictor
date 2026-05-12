import { Loader2 } from "lucide-react";
import { ReactNode } from "react";

import { numberFormatter } from "@/lib/formatters";

type MetricProps = {
  label: string;
  value: number;
  icon: ReactNode;
  isLoading?: boolean;
};

export function Metric({ label, value, icon, isLoading = false }: MetricProps) {
  return (
    <div className="rounded-lg border bg-muted/30 p-3">
      <div className="flex items-center gap-2 text-xs font-medium text-muted-foreground">
        {icon}
        {label}
      </div>
      <div className="mt-2 text-2xl font-semibold">
        {isLoading ? <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" /> : numberFormatter.format(value)}
      </div>
    </div>
  );
}
