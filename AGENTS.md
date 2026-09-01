# Project context

This repository is a starter template for a greenfield Java project used in an introductory software engineering course in an undergraduate computer science program. Students use it as the starting point for their own projects.

# Default user context

Unless the user says otherwise, assume that you are assisting a student working on a project in this repository. If the user identifies themselves as an instructor or another project stakeholder, adapt your response to that role.

# Student profile

* Prior knowledge: Basic Java and OOP concepts.
* Level of programming experience: 2 years
* IDE and level of expertise: codex, beginner

# Guidance for interacting with users

* Explain the rationale for significant actions: what you did and why.
* Keep explanations brief but instructive, supporting learning through responsible use of AI. For example:

  * When suggesting a Git command, briefly explain what it does.
  * Add explanatory Javadoc comments to all classes and to nontrivial methods and fields when their purpose or behavior is not obvious.
  * Make generated code as self-explanatory as possible, and include explanatory comments where they improve understanding.
  * When faced with a design choice, choose the simplest option that is sufficient for the requirements, while briefly explaining relevant more advanced alternatives.

# Project-specific requirements

## Java coding standard

For every Java source or test change and every Java code review, invoke and follow
the project-local `seedu-java-coding-standard` skill at
`.codex/skills/seedu-java-coding-standard/SKILL.md`. Its rules apply to all Java
code in this repository.

## Required workflow after code updates

After every code update:

1. Review the JUnit tests under `src/test/java` and update or add tests for every affected production method and behavior. Run `./gradlew test` and stop to report any failure rather than weakening a test to force a pass.
2. Review `test/ui-test-plan.md` and update it when the change adds, removes, or changes command-line UI behavior, inputs, or expected output.
3. Invoke the project-specific `test-ui` skill to run the UI test plan and show the complete console transcript. If the skill reports a failure, stop and report the actual and expected output; do not silently alter the implementation or expected output to force a pass.
4. With every change, review whether positive, negative, boundary, and malformed-input cases need to be added or adjusted. Interleave positive and negative UI cases where practical so invalid inputs cannot silently corrupt internal state.

## Java version:

Ensure that Java 25 is used when running the application or build tasks. On macOS, use `sdk use java 25.0.3.fx-zulu` to switch to Java 25 if needed.

## Git

Before creating any commit, invoke and follow the project-local
`seedu-git-standard` skill at `.codex/skills/seedu-git-standard/SKILL.md`.
Use lightweight tags unless the user requests an annotated tag.
When proposing or creating a commit message, include enough detail to explain the rationale for the change.
Do not commit or push unless explicitly asked.
