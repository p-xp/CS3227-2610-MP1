---
name: test-ui
description: Run command-line UI test cases from test/ui-test-plan.md, compare each command's output with its expected output, and stop immediately on the first failure.
---

# Test the command-line UI

Use this project-specific skill when the user asks to run the interactive Java program against a list of console commands and expected outputs.

1. Read `test/ui-test-plan.md`. Each test case must state its aim, inputs, and expected output. Executable cases are in the JSON block marked `test-ui:begin` and `test-ui:end`; each command is paired with its expected output.
2. Ensure Java 25 is active before running Java commands. On macOS, use `sdk use java 25.0.3.fx-zulu` when needed.
3. From the repository root, run `python3 test-ui/scripts/run_ui_tests.py test/ui-test-plan.md`.
4. Compare output exactly, normalizing only platform line endings and one final trailing newline. Do not ignore prompts, spacing, capitalization, or extra output.
5. The helper is fail-fast. On failure, stop immediately and report the test case, command, actual output, and expected output. A non-zero exit status is a failure.
6. After testing, show the complete console transcript, including every console input and output. Do not edit application code or silently alter the plan to make a test pass.
