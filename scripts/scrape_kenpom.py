import argparse
import re

from bs4 import BeautifulSoup
import requests


KENPOM_YEAR_URL = "https://kenpom.com/index.php?y="
SEED_PATTERN = re.compile(r"\b([1-9]|1[0-6])\b")
ADJUSTED_EFFICIENCY_MARGIN_CELL_INDEX = 4
ADJUSTED_TEMPO_CELL_INDEX = 9


def get_year_url(year):
    return KENPOM_YEAR_URL + str(year)


def parse_arguments():
    parser = argparse.ArgumentParser(description="Print NCAA tournament teams from KenPom year pages.")
    parser.add_argument("--start-year", default=2002, type=int)
    parser.add_argument("--end-year", default=2026, type=int)
    return parser.parse_args()


def get_ratings_rows(page_html):
    soup = BeautifulSoup(page_html, "html.parser")
    ratings_table = soup.find("table", id="ratings-table")

    if ratings_table is None:
        return []

    ratings_rows = []
    for row in ratings_table.find_all("tr"):
        cells = row.find_all("td")
        if len(cells) <= ADJUSTED_TEMPO_CELL_INDEX:
            continue
        ratings_rows.append(row)

    return ratings_rows


def get_team_cells(page_html):
    team_cells = []
    for row in get_ratings_rows(page_html):
        cells = row.find_all("td")
        team_cells.append(cells[1])

    return team_cells


def parse_float_from_cell(table_cell):
    cell_text = table_cell.get_text(" ", strip=True)
    number_match = re.search(r"-?\d+(?:\.\d+)?", cell_text)

    if number_match is None:
        return None

    return float(number_match.group())


def parse_tournament_team_name(team_cell):
    team_link = team_cell.find("a")
    if team_link is None:
        return None

    team_name = team_link.get_text(strip=True)

    seed_text_parts = []
    for text_node in team_cell.find_all(string=True, recursive=False):
        seed_text_parts.append(text_node.strip())

    for seed_candidate in seed_text_parts:
        if SEED_PATTERN.fullmatch(seed_candidate):
            return team_name

    seed_element = team_cell.find(class_=lambda class_name: class_name and "seed" in class_name)
    if seed_element is not None and SEED_PATTERN.search(seed_element.get_text(strip=True)):
        return team_name

    return None

def parse_tournament_team_adjEM(ratings_row):
    cells = ratings_row.find_all("td")
    return parse_float_from_cell(cells[ADJUSTED_EFFICIENCY_MARGIN_CELL_INDEX])


def parse_tournament_team_adjT(ratings_row):
    cells = ratings_row.find_all("td")
    return parse_float_from_cell(cells[ADJUSTED_TEMPO_CELL_INDEX])


def parse_tournament_team_names(page_html):
    tournament_team_names = []
    count = 0
    for ratings_row in get_ratings_rows(page_html):
        team_cell = ratings_row.find_all("td")[1]
        team_name = parse_tournament_team_name(team_cell)
        adjEM = parse_tournament_team_adjEM(ratings_row)
        adjT = parse_tournament_team_adjT(ratings_row)
        if team_name is not None:
            count += 1
            tournament_team_names.append([team_name, adjEM, adjT])

    return tournament_team_names


def scrape(start_year, end_year):
    for year in range(start_year, end_year + 1):
        print("---------------------------------------------------------------------------------------------")
        current_url = get_year_url(year)
        response = requests.get(
            current_url,
            headers={"User-Agent": "MarchMadnessDataCollector/1.0"},
            timeout=30,
        )

        if response.status_code != 200:
            print(f"Failed to download {current_url}: HTTP {response.status_code}")
            continue

        tournament_team_names = parse_tournament_team_names(response.text)

        print(f"{year} tournament teams:")
        if not tournament_team_names:
            print("  No tournament teams found.")
            continue

        for team_name in tournament_team_names:
            print(f"  {team_name}")


def main():
    arguments = parse_arguments()
    scrape(arguments.start_year, arguments.end_year)


if __name__ == "__main__":
    main()
