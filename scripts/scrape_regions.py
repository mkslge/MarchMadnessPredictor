import argparse
import re
from dataclasses import dataclass

from bs4 import BeautifulSoup
import requests


WIKIPEDIA_URL_PREFIX = "https://en.wikipedia.org/wiki/"
WIKIPEDIA_URL_POSTFIX = "_NCAA_Division_I_men%27s_basketball_tournament"
REGION_HEADING_PATTERN = re.compile(
    r"\b(East|West|South|Midwest|Southeast|Southwest)\s+regional\b",
    re.IGNORECASE,
)
SEED_PATTERN = re.compile(r"^(?:[1-9]|1[0-6])$")


@dataclass(frozen=True)
class TournamentTeam:
    name: str
    year: int
    seed: int
    region: str


def get_url_year(year):
    return WIKIPEDIA_URL_PREFIX + str(year) + WIKIPEDIA_URL_POSTFIX


def parse_arguments():
    parser = argparse.ArgumentParser(
        description="Print NCAA tournament team names, years, seeds, and regions from Wikipedia."
    )
    parser.add_argument("--start-year", default=2002, type=int)
    parser.add_argument("--end-year", default=2026, type=int)
    return parser.parse_args()


def clean_cell_text(cell):
    return cell.get_text(" ", strip=True).replace("\xa0", " ")


def parse_region_name(heading):
    heading_text = heading.get_text(" ", strip=True)
    region_match = REGION_HEADING_PATTERN.search(heading_text)

    if region_match is None:
        return None

    return region_match.group(1).title()


def is_region_heading(heading):
    return heading.name == "h3" and parse_region_name(heading) is not None


def find_region_bracket_table(heading):
    current_element = heading.parent

    while current_element is not None:
        current_element = current_element.find_next_sibling()

        if current_element is None:
            return None

        if current_element.name == "div":
            next_heading = current_element.find(["h2", "h3", "h4"])
            if next_heading is not None and is_region_heading(next_heading):
                return None

        if current_element.name == "table":
            return current_element

        nested_table = current_element.find("table")
        if nested_table is not None:
            return nested_table

    return None


def parse_seed_from_cell(cell):
    cell_text = clean_cell_text(cell)

    if not SEED_PATTERN.fullmatch(cell_text):
        return None

    return int(cell_text)


def parse_team_name_from_cell(cell):
    team_link = cell.find("a")

    if team_link is not None:
        return team_link.get_text(" ", strip=True)

    team_name = clean_cell_text(cell)
    if not team_name:
        return None

    return team_name


def looks_like_score_cell(cell):
    cell_text = clean_cell_text(cell)
    return bool(re.match(r"^\d+", cell_text))


def parse_team_from_cell_group(year, region, seed_cell, team_cell, score_cell):
    seed = parse_seed_from_cell(seed_cell)
    if seed is None or not looks_like_score_cell(score_cell):
        return None

    team_name = parse_team_name_from_cell(team_cell)
    if team_name is None:
        return None

    return TournamentTeam(
        name=team_name,
        year=year,
        seed=seed,
        region=region,
    )


def parse_region_teams(year, region, table):
    teams_by_seed = {}

    for row in table.find_all("tr"):
        cells = row.find_all("td")

        for index in range(0, len(cells) - 2):
            team = parse_team_from_cell_group(
                year,
                region,
                cells[index],
                cells[index + 1],
                cells[index + 2],
            )

            if team is None or team.seed in teams_by_seed:
                continue

            teams_by_seed[team.seed] = team

            if len(teams_by_seed) == 16:
                return list(teams_by_seed.values())

    return list(teams_by_seed.values())


def parse_tournament_teams(year, page_html):
    soup = BeautifulSoup(page_html, "html.parser")
    tournament_teams = []

    for heading in soup.find_all("h3"):
        region = parse_region_name(heading)
        if region is None:
            continue

        table = find_region_bracket_table(heading)
        if table is None:
            continue

        tournament_teams.extend(parse_region_teams(year, region, table))

    return tournament_teams


def print_tournament_team(team):
    print(f"{team.year}, {team.region}, Seed {team.seed}, {team.name}")


def scrape(start_year, end_year):
    for year in range(start_year, end_year + 1):
        current_url = get_url_year(year)
        response = requests.get(
            current_url,
            headers={"User-Agent": "MarchMadnessDataCollector/1.0"},
            timeout=30,
        )

        if response.status_code != 200:
            print(f"Error, couldn't find year {year}")
            continue

        tournament_teams = parse_tournament_teams(year, response.text)

        if not tournament_teams:
            print(f"No tournament teams found for {year}")
            continue

        for team in tournament_teams:
            print_tournament_team(team)


def main():
    arguments = parse_arguments()
    scrape(arguments.start_year, arguments.end_year)


if __name__ == "__main__":
    main()
