# Voyager User Guide

**Voyager** is a travel-planning chatbot that helps you organise activities,
accommodation, and transport in one itinerary.

Enter a command in the chat field, then press <kbd>Enter</kbd> or select
**Send**. Your itinerary is shown in the left panel and is saved automatically
between sessions.

## Quick start

Try these commands:

```text
activity Visit Gardens by the Bay /at 2026-09-01 1000
stay City Hotel /from 2026-09-01 /to 2026-09-03
transport Airport Shuttle /from Changi Airport /to City Hall
book 1
```

## Features

### Add an activity

Adds an activity to your itinerary.

```text
activity DESCRIPTION [/at YYYY-MM-DD HHmm]
```

Examples:

```text
activity Visit the museum
activity Visit Gardens by the Bay /at 2026-09-01 1000
```

The date and time are optional. Use a real date in `YYYY-MM-DD` format and a
24-hour time in `HHmm` format.

### Add accommodation

Adds a stay to your itinerary.

```text
stay NAME /from START_DATE /to END_DATE
```

Example:

```text
stay City Hotel /from 2026-09-01 /to 2026-09-03
```

Dates must use `YYYY-MM-DD`, and the end date must be later than the start
date.

### Add transport

Adds a transport arrangement to your itinerary.

```text
transport NAME /from ORIGIN /to DESTINATION
```

Example:

```text
transport Airport Shuttle /from Changi Airport /to City Hall
```

### Book or unbook an item

Marks an itinerary item as booked or unbooked.

```text
book ITEM_NUMBER
unbook ITEM_NUMBER
```

Example:

```text
book 2
```

Item numbers are shown in the itinerary panel. They start at 1.

### View plans on a date

Shows dated activities and accommodation that occur on a specified date.

```text
view DATE
```

Example:

```text
view 2026-09-02
```

### Find itinerary items

Finds items whose descriptions contain **all** supplied keywords. Searching is
case-insensitive.

```text
find KEYWORD [MORE_KEYWORDS...]
```

Example:

```text
find airport shuttle
```

### Delete an item

Removes an item from your itinerary.

```text
delete ITEM_NUMBER
```

Example:

```text
delete 3
```

Remaining items are renumbered automatically.

### Refresh the itinerary list

```text
list
```

Use this command to refresh the itinerary panel manually.

### Show or hide command help

Select **Hide** or **Show** in the command panel. You can also enter:

```text
help
```

### Exit Voyager

```text
exit
```

## Tips

- Commands and the `/from`, `/to`, and `/at` markers are case-insensitive.
- Extra spaces at the beginning, end, or between command parts are accepted.
- Item numbers must be positive whole numbers.
- Voyager rejects duplicate itinerary items and keeps your existing itinerary
  unchanged when a command is invalid.
