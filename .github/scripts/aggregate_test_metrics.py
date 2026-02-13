import glob
import json
import os
import xml.etree.ElementTree as ET
from collections import defaultdict
from typing import Dict


def main() -> None:
    metrics_dir = "test-metrics"
    if not os.path.isdir(metrics_dir):
        print("No test metrics found.")
        return

    suites: Dict[str, Dict[str, int]] = defaultdict(
        lambda: {"tests": 0, "failures": 0, "errors": 0, "skipped": 0}
    )

    # JUnit XML (backend)
    for xml_file in glob.glob(os.path.join(metrics_dir, "**", "*.xml"), recursive=True):
        try:
            root = ET.parse(xml_file).getroot()
            tests = int(root.attrib.get("tests", 0))
            failures = int(root.attrib.get("failures", 0))
            errors = int(root.attrib.get("errors", 0))
            skipped = int(root.attrib.get("skipped", 0))
            name = os.path.basename(os.path.dirname(xml_file)) or "backend"
            suites[name]["tests"] += tests
            suites[name]["failures"] += failures
            suites[name]["errors"] += errors
            suites[name]["skipped"] += skipped
        except Exception as exc:  # noqa: BLE001
            print(f"Warn: failed to parse {xml_file}: {exc}")

    # Cypress JSON
    for json_file in glob.glob(os.path.join(metrics_dir, "**", "*.json"), recursive=True):
        try:
            with open(json_file, "r", encoding="utf-8") as fh:
                data = json.load(fh)
            stats = data.get("stats", {})
            tests = int(stats.get("tests", 0))
            failures = int(stats.get("failures", 0))
            pending = int(stats.get("pending", 0))
            skipped = int(stats.get("skipped", 0))
            name = os.path.splitext(os.path.basename(json_file))[0]
            suites[f"cypress:{name}"]["tests"] += tests
            suites[f"cypress:{name}"]["failures"] += failures
            suites[f"cypress:{name}"]["skipped"] += pending + skipped
        except Exception as exc:  # noqa: BLE001
            print(f"Warn: failed to parse {json_file}: {exc}")

    if not suites:
        print("No parsed test suites.")
        return

    total_tests = total_failed = total_skipped = 0
    lines = []
    for name, vals in sorted(suites.items()):
        tests = vals["tests"]
        failed = vals["failures"] + vals["errors"]
        skipped = vals["skipped"]
        passed = max(tests - failed - skipped, 0)
        total_tests += tests
        total_failed += failed
        total_skipped += skipped
        lines.append(f"| {name} | {tests} | {passed} | {failed} | {skipped} |")

    summary = []
    summary.append("## Test Counts")
    summary.append("")
    summary.append(f"- Total tests: {total_tests}")
    summary.append(f"- Passed: {total_tests - total_failed - total_skipped}")
    summary.append(f"- Failed: {total_failed}")
    summary.append(f"- Skipped/Pending: {total_skipped}")
    summary.append("")
    summary.append("| Suite | Tests | Passed | Failed | Skipped |")
    summary.append("| --- | --- | --- | --- | --- |")
    summary.extend(lines)

    step_summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if step_summary:
        with open(step_summary, "a", encoding="utf-8") as fh:
            fh.write("\n".join(summary) + "\n")
    else:
        print("\n".join(summary))


if __name__ == "__main__":
    main()
