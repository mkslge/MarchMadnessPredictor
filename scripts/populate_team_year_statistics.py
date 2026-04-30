#!/usr/bin/env python3
"""
Populate the TeamYearStatistics table from the local tournament datasets.

The script keeps team, year, and seed from the JSON files and initializes every
other statistic column to 0.
"""

from __future__ import annotations

import argparse
import json
import os
from dataclasses import dataclass
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_DATASETS_DIRECTORY = REPOSITORY_ROOT / "src" / "main" / "resources" / "datasets"
DEFAULT_SSL_ROOT_CERTIFICATE = REPOSITORY_ROOT / "global-bundle.pem"


@dataclass(frozen=True)
class TeamYearStatisticsRow:
    team: str
    year: int
    seed: int


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Populate TeamYearStatistics using src/main/resources/datasets."
    )
    parser.add_argument("--host", default=os.getenv("AWS_PSQL_HOST"), help="PostgreSQL host.")
    parser.add_argument(
        "--port",
        default=os.getenv("AWS_PSQL_PORT", "5432"),
        type=int,
        help="PostgreSQL port.",
    )
    parser.add_argument(
        "--database",
        default=os.getenv("AWS_PSQL_DATABASE"),
        help="PostgreSQL database name.",
    )
    parser.add_argument(
        "--username",
        default=os.getenv("AWS_PSQL_USERNAME"),
        help="PostgreSQL username.",
    )
    parser.add_argument(
        "--password",
        default=os.getenv("AWS_PSQL_PASSWORD"),
        help="PostgreSQL password.",
    )
    parser.add_argument(
        "--datasets-directory",
        default=DEFAULT_DATASETS_DIRECTORY,
        type=Path,
        help="Directory containing year folders with region JSON files.",
    )
    parser.add_argument(
        "--table",
        default="teamyearstatistics",
        help="Target table name. PostgreSQL folds unquoted names to lowercase.",
    )
    parser.add_argument(
        "--sslmode",
        default=os.getenv("PGSSLMODE"),
        help="Optional PostgreSQL sslmode, such as require or verify-full.",
    )
    parser.add_argument(
        "--sslrootcert",
        default=os.getenv("PGSSLROOTCERT"),
        help="Optional root certificate path for SSL connections.",
    )
    parser.add_argument(
        "--delete-existing",
        action="store_true",
        help="Delete existing rows for dataset years before inserting.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Read datasets and report what would be inserted without connecting to PostgreSQL.",
    )
    return parser.parse_args()


def load_team_year_statistics_rows(datasets_directory: Path) -> list[TeamYearStatisticsRow]:
    rows: list[TeamYearStatisticsRow] = []

    for year_directory in sorted(datasets_directory.iterdir()):
        if not year_directory.is_dir() or not year_directory.name.isdigit():
            continue

        year = int(year_directory.name)
        for dataset_file in sorted(year_directory.glob("*.json")):
            with dataset_file.open(encoding="utf-8") as file:
                dataset = json.load(file)

            for team_entry in dataset.get("Region", []):
                rows.append(
                    TeamYearStatisticsRow(
                        team=team_entry["Team"],
                        year=year,
                        seed=int(team_entry["Seed"]),
                    )
                )

    if not rows:
        raise ValueError(f"No team rows found in {datasets_directory}")

    return rows


def build_connection_parameters(arguments: argparse.Namespace) -> dict[str, object]:
    required_values = {
        "host": arguments.host,
        "database": arguments.database,
        "username": arguments.username,
        "password": arguments.password,
    }
    missing_values = [name for name, value in required_values.items() if not value]

    if missing_values:
        missing_values_text = ", ".join(missing_values)
        raise ValueError(f"Missing required database settings: {missing_values_text}")

    connection_parameters: dict[str, object] = {
        "host": arguments.host,
        "port": arguments.port,
        "dbname": arguments.database,
        "user": arguments.username,
        "password": arguments.password,
    }

    if arguments.sslmode:
        connection_parameters["sslmode"] = arguments.sslmode

    ssl_root_certificate = arguments.sslrootcert
    if not ssl_root_certificate and DEFAULT_SSL_ROOT_CERTIFICATE.exists():
        ssl_root_certificate = str(DEFAULT_SSL_ROOT_CERTIFICATE)

    if ssl_root_certificate:
        connection_parameters["sslrootcert"] = ssl_root_certificate

    return connection_parameters


def quote_identifier(identifier: str) -> str:
    return '"' + identifier.replace('"', '""') + '"'


def insert_rows(
    connection: object,
    table_name: str,
    rows: list[TeamYearStatisticsRow],
    delete_existing: bool,
) -> None:
    quoted_table_name = quote_identifier(table_name)
    dataset_years = sorted({row.year for row in rows})

    insert_statement = f"""
        INSERT INTO {quoted_table_name} (
            team,
            year,
            seed,
            rating,
            bracketsimulations,
            bracketwins,
            bracketsplayed,
            gamesimulations,
            gamewins
        )
        VALUES (%s, %s, %s, 0, 0, 0, 0, 0, 0)
    """
    values = [(row.team, row.year, row.seed) for row in rows]

    with connection.cursor() as cursor:
        if delete_existing:
            cursor.execute(
                f"DELETE FROM {quoted_table_name} WHERE year = ANY(%s)",
                (dataset_years,),
            )

        cursor.executemany(insert_statement, values)


def main() -> None:
    arguments = parse_arguments()
    rows = load_team_year_statistics_rows(arguments.datasets_directory)

    years = ", ".join(str(year) for year in sorted({row.year for row in rows}))
    if arguments.dry_run:
        print(f"Would insert {len(rows)} TeamYearStatistics rows for years: {years}")
        return

    connection_parameters = build_connection_parameters(arguments)

    try:
        import psycopg
    except ImportError as error:
        raise SystemExit(
            "Missing dependency: install psycopg with `python3 -m pip install \"psycopg[binary]\"`."
        ) from error

    with psycopg.connect(**connection_parameters) as connection:
        insert_rows(connection, arguments.table, rows, arguments.delete_existing)
        connection.commit()

    print(f"Inserted {len(rows)} TeamYearStatistics rows for years: {years}")


if __name__ == "__main__":
    main()
