---
name: seedu-java-coding-standard
description: Apply the SE-EDU intermediate Java coding standard when creating, editing, or reviewing Java code in this project.
metadata:
  short-description: Apply this project's Java coding standard
---

# SE-EDU Java Coding Standard

Use this skill for every Java source or test change in this repository, and when reviewing its Java code. Apply the intermediate rules in the [SE-EDU Java coding standard](https://se-education.org/guides/conventions/java/intermediate.html). This skill governs style, not program behavior or the project's test workflow.

## Required conventions

- Use descriptive `camelCase` names for variables and methods, `PascalCase` names for types, and `UPPER_SNAKE_CASE` names for constants. Use plural names for collections, and an `is`, `has`, `was`, or similarly readable prefix for boolean values where practical. A boolean setter parameter uses the form `setBooked(boolean isBooked)`.
- Indent with four spaces. Keep lines at 110 characters when practical and never exceed 120. Wrap by improving readability: break after commas and before operators; keep a method name attached to its opening parenthesis; use a continuation indent of eight spaces beyond the parent line.
- Use K&R braces. Always brace loop and conditional bodies, even when they contain one statement. Put `else`, `catch`, and `finally` on the same line as the preceding closing brace. Mark any intentional classic-switch fall-through with `// Fallthrough`.
- Put logical units in a block on separate lines. Surround binary operators with spaces; place one space after keywords, commas, and `for` semicolons; space ternary `:` operators.
- Put every class in a package. Use explicit imports only. Keep imports consistently grouped and alphabetized as: static imports, `java.*`, `javax.*`, third-party imports, then `meepmoop.*` imports, with a blank line between nonempty groups.
- Attach array brackets to the type. Declare variables in the smallest practical scope and initialize them at declaration when a genuine initial value is available. Keep behavior-bearing class fields non-public.
- Write all comments in English, using American spelling and no local slang. Add descriptive Javadoc to every production class and public method, except simple getters/setters and overrides whose inherited Javadoc applies unchanged. Start the first sentence with a third-person verb such as “Returns”, “Adds”, or “Creates”; use complete, punctuated tag descriptions when tags add value.

## Before finishing

Review touched Java files against these rules. Preserve existing behavior unless the requested work changes it. Run the project's required tests and UI test workflow after production-code updates.
