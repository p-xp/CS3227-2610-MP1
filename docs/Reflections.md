# Reflection on AI-Assisted Software Engineering

Developing Voyager with an LLM changed the way I approached software
engineering. I found the AI most useful when I treated it as a collaborator
that could propose and implement a focused change, while I retained control of
the requirements, review points, and final decisions. The following prompts
were particularly useful because they made that division of responsibility
clear.

## 1. Breaking a large refactoring into reviewable increments

> “Let's do this iteratively. In each iteration, do the following steps:
> 1. Decide the next natural stand-alone increment that moves the code closer
> to the target. Describe it to me and obtain approval.
> 2. Implement that increment.
> 3. Test it to ensure there are no regressions.
> 4. Commit the changes with a detailed commit message.
> 5. Briefly outline the next increment to be done in the next iteration.”

This prompt was used while refactoring the application into clearer classes,
including UI, storage, parsing, itinerary management, and command classes. It
gave me much finer control over what would otherwise have been one very large
and risky change. Instead of accepting a complete redesign at once, I could
review each proposed phase, understand its purpose, and approve only the next
small change.

This control was useful in practice, not merely in theory. The development log
records that I later asked whether it was safe to skip phases 4 and 5 and go
straight to phase 6. I was therefore able to choose the refactoring work that
was valuable for the project and discard phases I did not want. The AI still
handled much of the implementation and testing effort, but the staged prompt
kept architectural decisions with me. This is a good pattern for using an LLM
on significant changes: ask it to make progress in independently reviewable
increments instead of asking for a single large transformation.

## 2. Describing and validating a graphical interface

> “Implement a third vertical panel in the GUI that showcases a list of
> available commands for users to use. This panel should be collapsible. Same
> width as the leftmost list display panel.”

The AI was very effective at turning a relatively simple verbal description
into a working visual feature. It could add the new command-reference panel,
match its width to the itinerary panel, populate it with commands, and connect
it to the existing JavaFX layout. This made UI implementation much faster than
building every visual detail manually.

However, the feature also showed why manual testing remains necessary for UI
changes. My original use of “collapsible” was ambiguous. The AI initially made
the panel collapse vertically, which did not provide more room for the other
panels and therefore did not meet my actual goal. I had to send the follow-up:

> “the hide should collapse the panel horizontally, to provide more space for
> the other panels”

The correction was small, but it depended on observing the interface and
recognising that the implementation did not satisfy the intended user
experience. An LLM can implement visuals well from concise descriptions, but
it cannot reliably infer every spatial intention. I should give measurable UI
requirements where possible and manually check layout, resizing, and
interaction behaviour after each change.

## 3. Turning engineering rules into reusable AI guidance

> “Update relevant agent files to ensure that after each code update,
> 1. the `test/ui-test-plan.md` is updated (if needed), and,
> 2. the `test-ui` skill is invoked.”

This prompt converted a one-time instruction into a standing project rule. It
made later changes faster and reduced manual coordination: rather than
remembering to request tests after every feature, I could expect the AI to
review and update the JUnit tests and UI test plan as part of its normal
workflow. Similar project-specific guidance was added for the Java coding
standard and Git conventions.

The main lesson is that prompting is not limited to individual feature
requests. Reusable instructions can establish a development process for the
whole repository. They improve consistency and reduce repeated manual work,
especially for routine but important tasks such as test maintenance, UI test
coverage, formatting, and commit-message conventions. I still need to review
whether the generated tests are meaningful and whether they cover negative and
boundary cases, but the rules make it far less likely that those checks are
forgotten entirely.

## Conclusion

AI assistance was most effective when prompts stated both the desired outcome
and the process for reaching it. I retained responsibility for deciding scope,
interpreting visual requirements, and reviewing results, while the AI reduced
the effort needed to implement, test, and document focused changes. The
experience showed me that good prompting is a practical software-engineering
skill: clear constraints and review points lead to more reliable results than
asking an LLM to make broad changes without supervision.
