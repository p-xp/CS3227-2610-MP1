#!/usr/bin/env python3
"""Fail-fast runner for the JSON test plan embedded in test/ui-test-plan.md."""
import atexit
import json
import subprocess
import sys
from pathlib import Path


def normalize(value: str) -> str:
    return value.replace("\r\n", "\n").replace("\r", "\n").rstrip("\n")


def load_cases(path: Path):
    text = path.read_text(encoding="utf-8")
    start = text.find("<!-- test-ui:begin -->")
    end = text.find("<!-- test-ui:end -->", start + 1)
    if start < 0 or end < 0:
        raise ValueError("missing test-ui JSON markers")
    cases = json.loads(text[start + len("<!-- test-ui:begin -->") : end].strip())
    if not isinstance(cases, list):
        raise ValueError("test-ui plan must contain a JSON list")
    return cases


def main() -> int:
    plan = Path(sys.argv[1]) if len(sys.argv) == 2 else Path("test/ui-test-plan.md")
    try:
        cases = load_cases(plan)
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"TEST SESSION FAILED: {error}")
        return 1

    data_file = Path("data/meepmoop.txt")
    data_file_existed = data_file.is_file()
    original_data = data_file.read_bytes() if data_file_existed else None
    data_folder_existed = data_file.parent.exists()

    def restore_original_data():
        """Restores personal data after the isolated UI test session."""
        if data_file_existed:
            data_file.parent.mkdir(parents=True, exist_ok=True)
            data_file.write_bytes(original_data)
        else:
            data_file.unlink(missing_ok=True)
            if not data_folder_existed:
                try:
                    data_file.parent.rmdir()
                except OSError:
                    pass

    atexit.register(restore_original_data)
    print("=== TEST SESSION ===")
    for case_number, case in enumerate(cases, 1):
        name = case.get("name", f"case {case_number}")
        print(f"\n--- {name} ---")
        data_file.unlink(missing_ok=True)
        try:
            data_file.parent.rmdir()
        except OSError:
            pass
        for command_number, command in enumerate(case.get("commands", []), 1):
            if not isinstance(command, dict) or "command" not in command or "expected" not in command:
                print(f"TEST SESSION FAILED: malformed command in {name}")
                return 1
            command_text, expected = command["command"], command["expected"]
            print(f"$ {command_text}")
            result = subprocess.run(command_text, shell=True, text=True, capture_output=True)
            actual = result.stdout + result.stderr
            print(actual, end="" if actual.endswith("\n") else "\n")
            if result.returncode != 0 or normalize(actual) != normalize(expected):
                print(f"TEST SESSION FAILED: {name}, command {command_number}")
                print("Expected output:\n" + expected)
                print("Actual output:\n" + actual)
                return 1
    print("\nTEST SESSION PASSED")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
