import { Team } from "@/api";

export const numberFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 1,
});

export function teamName(team?: Team) {
  return team?.name || team?.Team || "Unknown";
}

export function teamSeed(team?: Team) {
  return team?.seed || team?.Seed || 0;
}

export function teamRating(team?: Team) {
  return team?.adjEM || team?.AdjEM || 0;
}

export function formatRegionName(regionName: string) {
  return regionName.charAt(0).toUpperCase() + regionName.slice(1).toLowerCase();
}
