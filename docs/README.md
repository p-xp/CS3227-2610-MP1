# MeepMoop User Guide

MeepMoop is a command-line chatbot for building and managing a travel
itinerary. Commands and the `/from` and `/to` markers are case-insensitive.
Leading and trailing whitespace is ignored, and one or more spaces may separate
the command keyword from its arguments.

## Console response formatting

MeepMoop uses the following line to mark the end of each response:

```text
____________________________________________________________
```

The separator follows these rules:

- Every response ends with exactly one separator. This includes the greeting,
  successful command responses, validation errors, and the goodbye response.
- A response does not begin with a separator.
- A multi-line response has only one separator, placed after its final line.
- Consecutive responses therefore have exactly one separator between them,
  with no adjacent duplicate separator lines.

The implementation and executable UI test expectations follow this output
contract.

## Adding an activity

Use `activity <description>`. The description must contain non-whitespace text.

```text
activity Museum visit
```

## Adding accommodation

Use `stay <name> /from <date> /to <date>`.

- Dates must use the ISO `YYYY-MM-DD` format and represent real calendar dates.
- The `/from` date must be on or before the `/to` date.
- `/from` and `/to` are reserved markers. Each must occur exactly once and in
  that order, with nonempty text before, between, and after them.

```text
stay Beach Hotel /from 2026-09-01 /to 2026-09-03
```

## Adding transport

Use `transport <name> /from <origin> /to <destination>`.

The `/from` and `/to` markers follow the same single-use, ordered, and nonempty
field rules as accommodation commands. Names and locations may contain spaces.

```text
transport Airport Shuttle /from Changi Airport /to City Hall
```

## Listing itinerary items

Use `list` without arguments. Items are displayed in insertion order using
one-based item numbers.

## Booking and unbooking

Use `book <item number>` or `unbook <item number>`. The item number must be a
positive whole number that identifies an existing item. Missing, zero,
negative, nonnumeric, overflowing, and out-of-range numbers are rejected.

## Deleting an item

Use `delete <item number>`. The item number follows the same validation rules
as booking commands. Remaining items are renumbered after deletion.

## Exiting

Use `exit` without arguments to end the session.
