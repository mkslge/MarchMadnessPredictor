type ResultMetricProps = {
  label: string;
  value: string;
};

export function ResultMetric({ label, value }: ResultMetricProps) {
  return (
    <div className="rounded-lg border bg-muted/30 p-3">
      <div className="text-xs font-medium text-muted-foreground">{label}</div>
      <div className="mt-2 truncate text-base font-semibold">{value}</div>
    </div>
  );
}
