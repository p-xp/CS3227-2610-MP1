---
name: seedu-git-standard
description: Apply the SE-EDU Git conventions when preparing commit messages or naming branches for this project.
metadata:
  short-description: Apply this project's Git conventions
---

# SE-EDU Git Standard

Use this skill when preparing a commit or creating a branch in this repository. Follow the [SE-EDU Git conventions](https://se-education.org/guides/conventions/git.html). This skill defines conventions only; it does not authorize committing, pushing, or creating a branch.

## Commit messages

- Write a clear subject line in imperative mood, beginning with a capital letter and without a final period. Aim for 50 characters; never exceed 72.
- Add an optional scope or category only when it helps identify the affected area, for example `Parser: Reject empty input` or `chore: Update release date`.
- For a non-trivial commit, include a body separated from the subject by a blank line. Wrap body lines at 72 characters and use blank lines to separate paragraphs or bullets.
- Explain what changes and why, rather than implementation mechanics. A useful order is: the current situation (present tense), why it needs to change, the imperative action taken, why that approach is appropriate, then any relevant context.
- If the explanation is becoming long or covers unrelated outcomes, split the work into finer-grained commits instead of hiding multiple purposes in one message.

## Branch names

- Use a meaningful, kebab-case name formed from relevant keywords, for example `refactor-ui-tests`.
- For issue-related branches, use `issueNumber-keywords-from-issue-title`, for example `1234-ui-freeze-error`.

## Before committing

Review the staged diff and ensure the message describes exactly that coherent change. Preserve the user's requested scope and follow the repository's existing authorization and Git workflow rules.
