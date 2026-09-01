# MeepMoop UI test plan

Each test case documents its aim, console inputs, and expected output. The JSON
block is the executable representation used by `test-ui`; each command is
paired with its exact expected output.

## Test cases

### 1. Add and list every itinerary item type

- Aim: Confirm that valid activity, accommodation, and transport commands add
  the correct item types and that `list` acknowledges the manual refresh.
- Inputs: Add one item of each type, list the itinerary, then exit.
- Expected output: Each addition is confirmed and `list` reports that the list
  has been manually refreshed. The graphical sidebar presents the refreshed items.

### 2. Book and unbook an item

- Aim: Confirm valid booking transitions and rejection of duplicate state
  changes.
- Inputs: Add an activity, book it twice, unbook it twice, list it, then exit.
- Expected output: The valid transitions change the booking marker, duplicate
  transitions show an error, and the final `list` command confirms a refresh.

### 3. Invalid add commands preserve state

- Aim: Confirm that malformed add commands and an unknown command are rejected
  without changing existing itinerary items.
- Inputs: Add a valid activity, enter malformed stay, transport, and activity
  commands (including duplicate `/at` markers) followed by an unknown command,
  list the itinerary, then exit.
- Expected output: Each bad input shows its documented error and the final
  `list` command confirms a refresh without altering the original activity.

### 4. List an empty itinerary

- Aim: Confirm that `list` works when no items have been added.
- Inputs: List the itinerary, then exit.
- Expected output: The empty itinerary refresh confirmation is followed by one
  separator and the goodbye response.

### 5. Invalid booking inputs preserve state

- Aim: Confirm that out-of-range and nonnumeric booking arguments do not alter
  the item before a later valid booking.
- Inputs: Add an activity, try two invalid booking arguments, list it, book it
  successfully, list it again, then exit.
- Expected output: Both invalid inputs show an error, the first list shows the
  item unbooked, and the second list shows it booked.

### 6. Delete itinerary items

- Aim: Confirm valid deletion and renumbering, and verify that invalid deletion
  arguments preserve the remaining list.
- Inputs: Add two activities, delete the first, list, try out-of-range and
  nonnumeric deletions, list again, then exit.
- Expected output: The second activity is renumbered to item 1 and remains after
  both invalid deletion attempts.

### 7. Reject an item beyond itinerary capacity

- Aim: Confirm the 100-item capacity boundary and verify that a rejected 101st
  item does not alter the itinerary.
- Inputs: Add 101 activities, list the itinerary, then exit.
- Expected output: The first 100 additions succeed, the 101st reports that the
  itinerary is full, and the final list contains exactly items 1 through 100.

### 8. Accept case-insensitive commands and flexible whitespace

- Aim: Confirm that command keywords and route markers are case-insensitive and
  that surrounding or repeated separating whitespace is accepted.
- Inputs: Add all three item types using mixed case and flexible whitespace,
  list the itinerary using mixed case, then exit.
- Expected output: All commands succeed and text fields retain their meaningful
  internal spaces.

### 9. Invalid route markers and stay dates preserve state

- Aim: Reject reversed or repeated route markers, impossible or reversed stay
  dates, and verify that these errors do not add itinerary items.
- Inputs: Add a baseline activity, enter malformed stay and transport commands,
  list the itinerary, then exit.
- Expected output: Each command shows its specific format or date error and the
  final list still contains only the baseline activity.

### 10. Invalid item-number boundaries preserve state

- Aim: Reject missing, zero, negative, and overflowing item numbers without
  modifying the itinerary.
- Inputs: Add an activity, enter invalid book, unbook, and delete commands, list
  the itinerary, then exit.
- Expected output: Every invalid number shows the standard error and the final
  list still contains the original unbooked activity.

### 11. Start without a data file or folder

- Aim: Confirm a first-time launch starts normally without pre-created storage.
- Inputs: List the empty itinerary, then exit.
- Expected output: The chatbot starts without a warning and lists no items.

### 12. Persist data across restarts

- Aim: Confirm successful changes are automatically restored by a new process.
- Inputs: Add and book an item, exit, restart, list the itinerary, then exit.
- Expected output: The restarted chatbot lists the saved item as booked.

### 13. Recover valid data from a corrupted file

- Aim: Confirm valid records load while malformed records are skipped.
- Inputs: Start from a file containing one valid record and one malformed record,
  list the itinerary, then exit.
- Expected output: One startup warning appears and the valid item remains available.

### 14. View items occurring on a date

- Aim: Confirm that `view` includes a dated activity and a stay spanning the
  requested date, ignores a supplied time, and rejects malformed dates without
  changing the itinerary.
- Inputs: Add a dated activity and a three-day stay, view their shared date
  with a time, submit an invalid date, then view the following stay-only date.
- Expected output: The first view contains both matching items, the invalid
  input shows the date error, and the final view contains only the stay.

### 15. Find items by description keywords

- Aim: Confirm that `find` matches every case-insensitive keyword in any order,
  preserves original item numbers, and reports no matches or missing keywords.
- Inputs: Add three activities, find two keywords, find a nonmatching keyword,
  submit `find` without keywords, then exit.
- Expected output: The multi-keyword search returns only item 2, the no-match
  response echoes the keyword, and the missing-keyword response shows its format.

<!-- test-ui:begin -->
[
  {
    "name": "Add and list every itinerary item type",
    "aim": "Add each implemented item type and verify the complete list.",
    "inputs": ["activity Museum", "stay Hotel /from 2026-09-01 /to 2026-09-03", "transport Flight /from Singapore /to Tokyo", "list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Museum' 'stay Hotel /from 2026-09-01 /to 2026-09-03' 'transport Flight /from Singapore /to Tokyo' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Museum\nNow you have 1 items in your itinerary.\n____________________________________________________________\nGot it. I've added this accommodation:\n[S] [ ] Hotel (from: 1 Sep 2026 to: 3 Sep 2026)\nNow you have 2 items in your itinerary.\n____________________________________________________________\nGot it. I've added this transport:\n[T] [ ] Flight (from: Singapore to: Tokyo)\nNow you have 3 items in your itinerary.\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Book and unbook an item",
    "aim": "Verify valid booking transitions and duplicate-state errors.",
    "inputs": ["activity Park", "book 1", "book 1", "unbook 1", "unbook 1", "list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Park' 'book 1' 'book 1' 'unbook 1' 'unbook 1' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Park\nNow you have 1 items in your itinerary.\n____________________________________________________________\nBooked: [A] [X] Park\n____________________________________________________________\nItem is already booked\n____________________________________________________________\nUnbooked: [A] [ ] Park\n____________________________________________________________\nItem is already unbooked\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Invalid add commands preserve state",
    "aim": "Reject malformed add and unknown commands without changing the itinerary.",
    "inputs": ["activity Park", "stay bad", "transport bad", "activity", "activity Park /at 2026-09-01 0900 /at 2026-09-02 0900", "wat", "list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Park' 'stay bad' 'transport bad' 'activity' 'activity Park /at 2026-09-01 0900 /at 2026-09-02 0900' 'wat' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Park\nNow you have 1 items in your itinerary.\n____________________________________________________________\nInvalid stay format. Use: stay <name> /from <date> /to <date>\n____________________________________________________________\nInvalid transport format. Use: transport <name> /from <location> /to <location>\n____________________________________________________________\nInvalid activity format. Use: activity <description> [/at YYYY-MM-DD HHmm]\n____________________________________________________________\nInvalid activity format. Use: activity <description> [/at YYYY-MM-DD HHmm]\n____________________________________________________________\nInvalid input\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "List an empty itinerary",
    "aim": "Confirm list output when there are no itinerary items.",
    "inputs": ["list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Invalid booking inputs preserve state",
    "aim": "Reject invalid booking arguments without changing booking state.",
    "inputs": ["activity Park", "book 9", "book x", "list", "book 1", "list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Park' 'book 9' 'book x' 'list' 'book 1' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Park\nNow you have 1 items in your itinerary.\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nBooked: [A] [X] Park\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Delete itinerary items",
    "aim": "Delete a valid item, verify renumbering, and reject invalid deletion arguments without changing state.",
    "inputs": ["activity Museum", "activity Park", "delete 1", "list", "delete 9", "delete x", "list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Museum' 'activity Park' 'delete 1' 'list' 'delete 9' 'delete x' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Museum\nNow you have 1 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Park\nNow you have 2 items in your itinerary.\n____________________________________________________________\nNoted. I've removed this item:\n[A] [ ] Museum\nNow you have 1 items in your itinerary.\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Reject an item beyond itinerary capacity",
    "aim": "Accept 100 items, reject the 101st, and verify that the rejected item does not alter the list.",
    "inputs": [
      "activity Item1",
      "activity Item2",
      "activity Item3",
      "activity Item4",
      "activity Item5",
      "activity Item6",
      "activity Item7",
      "activity Item8",
      "activity Item9",
      "activity Item10",
      "activity Item11",
      "activity Item12",
      "activity Item13",
      "activity Item14",
      "activity Item15",
      "activity Item16",
      "activity Item17",
      "activity Item18",
      "activity Item19",
      "activity Item20",
      "activity Item21",
      "activity Item22",
      "activity Item23",
      "activity Item24",
      "activity Item25",
      "activity Item26",
      "activity Item27",
      "activity Item28",
      "activity Item29",
      "activity Item30",
      "activity Item31",
      "activity Item32",
      "activity Item33",
      "activity Item34",
      "activity Item35",
      "activity Item36",
      "activity Item37",
      "activity Item38",
      "activity Item39",
      "activity Item40",
      "activity Item41",
      "activity Item42",
      "activity Item43",
      "activity Item44",
      "activity Item45",
      "activity Item46",
      "activity Item47",
      "activity Item48",
      "activity Item49",
      "activity Item50",
      "activity Item51",
      "activity Item52",
      "activity Item53",
      "activity Item54",
      "activity Item55",
      "activity Item56",
      "activity Item57",
      "activity Item58",
      "activity Item59",
      "activity Item60",
      "activity Item61",
      "activity Item62",
      "activity Item63",
      "activity Item64",
      "activity Item65",
      "activity Item66",
      "activity Item67",
      "activity Item68",
      "activity Item69",
      "activity Item70",
      "activity Item71",
      "activity Item72",
      "activity Item73",
      "activity Item74",
      "activity Item75",
      "activity Item76",
      "activity Item77",
      "activity Item78",
      "activity Item79",
      "activity Item80",
      "activity Item81",
      "activity Item82",
      "activity Item83",
      "activity Item84",
      "activity Item85",
      "activity Item86",
      "activity Item87",
      "activity Item88",
      "activity Item89",
      "activity Item90",
      "activity Item91",
      "activity Item92",
      "activity Item93",
      "activity Item94",
      "activity Item95",
      "activity Item96",
      "activity Item97",
      "activity Item98",
      "activity Item99",
      "activity Item100",
      "activity Item101",
      "list",
      "exit"
    ],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Item1' 'activity Item2' 'activity Item3' 'activity Item4' 'activity Item5' 'activity Item6' 'activity Item7' 'activity Item8' 'activity Item9' 'activity Item10' 'activity Item11' 'activity Item12' 'activity Item13' 'activity Item14' 'activity Item15' 'activity Item16' 'activity Item17' 'activity Item18' 'activity Item19' 'activity Item20' 'activity Item21' 'activity Item22' 'activity Item23' 'activity Item24' 'activity Item25' 'activity Item26' 'activity Item27' 'activity Item28' 'activity Item29' 'activity Item30' 'activity Item31' 'activity Item32' 'activity Item33' 'activity Item34' 'activity Item35' 'activity Item36' 'activity Item37' 'activity Item38' 'activity Item39' 'activity Item40' 'activity Item41' 'activity Item42' 'activity Item43' 'activity Item44' 'activity Item45' 'activity Item46' 'activity Item47' 'activity Item48' 'activity Item49' 'activity Item50' 'activity Item51' 'activity Item52' 'activity Item53' 'activity Item54' 'activity Item55' 'activity Item56' 'activity Item57' 'activity Item58' 'activity Item59' 'activity Item60' 'activity Item61' 'activity Item62' 'activity Item63' 'activity Item64' 'activity Item65' 'activity Item66' 'activity Item67' 'activity Item68' 'activity Item69' 'activity Item70' 'activity Item71' 'activity Item72' 'activity Item73' 'activity Item74' 'activity Item75' 'activity Item76' 'activity Item77' 'activity Item78' 'activity Item79' 'activity Item80' 'activity Item81' 'activity Item82' 'activity Item83' 'activity Item84' 'activity Item85' 'activity Item86' 'activity Item87' 'activity Item88' 'activity Item89' 'activity Item90' 'activity Item91' 'activity Item92' 'activity Item93' 'activity Item94' 'activity Item95' 'activity Item96' 'activity Item97' 'activity Item98' 'activity Item99' 'activity Item100' 'activity Item101' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item1\nNow you have 1 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item2\nNow you have 2 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item3\nNow you have 3 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item4\nNow you have 4 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item5\nNow you have 5 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item6\nNow you have 6 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item7\nNow you have 7 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item8\nNow you have 8 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item9\nNow you have 9 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item10\nNow you have 10 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item11\nNow you have 11 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item12\nNow you have 12 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item13\nNow you have 13 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item14\nNow you have 14 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item15\nNow you have 15 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item16\nNow you have 16 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item17\nNow you have 17 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item18\nNow you have 18 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item19\nNow you have 19 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item20\nNow you have 20 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item21\nNow you have 21 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item22\nNow you have 22 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item23\nNow you have 23 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item24\nNow you have 24 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item25\nNow you have 25 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item26\nNow you have 26 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item27\nNow you have 27 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item28\nNow you have 28 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item29\nNow you have 29 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item30\nNow you have 30 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item31\nNow you have 31 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item32\nNow you have 32 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item33\nNow you have 33 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item34\nNow you have 34 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item35\nNow you have 35 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item36\nNow you have 36 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item37\nNow you have 37 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item38\nNow you have 38 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item39\nNow you have 39 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item40\nNow you have 40 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item41\nNow you have 41 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item42\nNow you have 42 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item43\nNow you have 43 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item44\nNow you have 44 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item45\nNow you have 45 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item46\nNow you have 46 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item47\nNow you have 47 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item48\nNow you have 48 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item49\nNow you have 49 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item50\nNow you have 50 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item51\nNow you have 51 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item52\nNow you have 52 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item53\nNow you have 53 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item54\nNow you have 54 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item55\nNow you have 55 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item56\nNow you have 56 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item57\nNow you have 57 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item58\nNow you have 58 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item59\nNow you have 59 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item60\nNow you have 60 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item61\nNow you have 61 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item62\nNow you have 62 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item63\nNow you have 63 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item64\nNow you have 64 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item65\nNow you have 65 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item66\nNow you have 66 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item67\nNow you have 67 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item68\nNow you have 68 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item69\nNow you have 69 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item70\nNow you have 70 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item71\nNow you have 71 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item72\nNow you have 72 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item73\nNow you have 73 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item74\nNow you have 74 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item75\nNow you have 75 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item76\nNow you have 76 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item77\nNow you have 77 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item78\nNow you have 78 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item79\nNow you have 79 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item80\nNow you have 80 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item81\nNow you have 81 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item82\nNow you have 82 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item83\nNow you have 83 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item84\nNow you have 84 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item85\nNow you have 85 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item86\nNow you have 86 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item87\nNow you have 87 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item88\nNow you have 88 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item89\nNow you have 89 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item90\nNow you have 90 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item91\nNow you have 91 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item92\nNow you have 92 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item93\nNow you have 93 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item94\nNow you have 94 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item95\nNow you have 95 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item96\nNow you have 96 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item97\nNow you have 97 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item98\nNow you have 98 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item99\nNow you have 99 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Item100\nNow you have 100 items in your itinerary.\n____________________________________________________________\nItinerary is full\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Accept case-insensitive commands and flexible whitespace",
    "aim": "Accept mixed-case keywords and markers together with surrounding and repeated separating whitespace.",
    "inputs": [
      "  AcTiViTy   Night Safari  ",
      "STAY Beach Hotel /FROM 2026-09-01 /TO 2026-09-03",
      "TRANSPORT Airport Shuttle /FrOm Changi Airport /tO City Hall",
      "LiSt",
      "EXIT"
    ],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' '  AcTiViTy   Night Safari  ' 'STAY Beach Hotel /FROM 2026-09-01 /TO 2026-09-03' 'TRANSPORT Airport Shuttle /FrOm Changi Airport /tO City Hall' 'LiSt' 'EXIT' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Night Safari\nNow you have 1 items in your itinerary.\n____________________________________________________________\nGot it. I've added this accommodation:\n[S] [ ] Beach Hotel (from: 1 Sep 2026 to: 3 Sep 2026)\nNow you have 2 items in your itinerary.\n____________________________________________________________\nGot it. I've added this transport:\n[T] [ ] Airport Shuttle (from: Changi Airport to: City Hall)\nNow you have 3 items in your itinerary.\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Invalid route markers and stay dates preserve state",
    "aim": "Reject malformed route markers and invalid dates without adding itinerary items.",
    "inputs": [
      "activity Baseline",
      "stay Hotel /to 2026-09-03 /from 2026-09-01",
      "stay Hotel /from 2026-02-30 /to 2026-03-01",
      "stay Hotel /from 2026-09-03 /to 2026-09-01",
      "stay Hotel /from 2026-09-01 /to 2026-09-03 /to 2026-09-04",
      "transport Bus /from A /to B /to C",
      "list",
      "exit"
    ],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Baseline' 'stay Hotel /to 2026-09-03 /from 2026-09-01' 'stay Hotel /from 2026-02-30 /to 2026-03-01' 'stay Hotel /from 2026-09-03 /to 2026-09-01' 'stay Hotel /from 2026-09-01 /to 2026-09-03 /to 2026-09-04' 'transport Bus /from A /to B /to C' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Baseline\nNow you have 1 items in your itinerary.\n____________________________________________________________\nInvalid stay format. Use: stay <name> /from <date> /to <date>\n____________________________________________________________\nInvalid stay dates. Use valid dates in YYYY-MM-DD order\n____________________________________________________________\nInvalid stay dates. Use valid dates in YYYY-MM-DD order\n____________________________________________________________\nInvalid stay format. Use: stay <name> /from <date> /to <date>\n____________________________________________________________\nInvalid transport format. Use: transport <name> /from <location> /to <location>\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Invalid item-number boundaries preserve state",
    "aim": "Reject missing, zero, negative, and overflowing item numbers without changing state.",
    "inputs": [
      "activity Park",
      "book",
      "book 0",
      "unbook -1",
      "delete 999999999999999999999",
      "list",
      "exit"
    ],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Park' 'book' 'book 0' 'unbook -1' 'delete 999999999999999999999' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Park\nNow you have 1 items in your itinerary.\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nInvalid item number\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Start without a data file or folder",
    "aim": "Start normally when neither the relative data file nor its folder exists.",
    "inputs": ["list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Persist data across restarts",
    "aim": "Save changes immediately and restore them in a second chatbot process.",
    "inputs": ["activity Museum", "book 1", "exit", "restart", "list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Museum' 'book 1' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop && printf '%s\\n' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Museum\nNow you have 1 items in your itinerary.\n____________________________________________________________\nBooked: [A] [X] Museum\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________\nHello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Recover valid data from a corrupted file",
    "aim": "Warn once, skip a malformed record, and retain a valid encoded record.",
    "inputs": ["list", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && mkdir -p data && printf '%s\\n' 'A | 1 | UmVjb3ZlcmVk' 'bad record' > data/meepmoop.txt && printf '%s\\n' 'list' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nWarning: Some saved data could not be loaded.\n____________________________________________________________\nList has been manually refreshed.\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "View items occurring on a date",
    "aim": "Include dated activities and inclusive stay ranges, ignore a supplied view time, and reject bad dates.",
    "inputs": ["activity Dinner /at 2026-09-02 1800", "stay Hotel /from 2026-09-01 /to 2026-09-03", "view 2026-09-02 2359", "view 2026-02-30", "view 2026-09-03", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Dinner /at 2026-09-02 1800' 'stay Hotel /from 2026-09-01 /to 2026-09-03' 'view 2026-09-02 2359' 'view 2026-02-30' 'view 2026-09-03' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Dinner (at: 2 Sep 2026 6pm)\nNow you have 1 items in your itinerary.\n____________________________________________________________\nGot it. I've added this accommodation:\n[S] [ ] Hotel (from: 1 Sep 2026 to: 3 Sep 2026)\nNow you have 2 items in your itinerary.\n____________________________________________________________\nHere are the items in your itinerary on 2 Sep 2026:\n[A] [ ] Dinner (at: 2 Sep 2026 6pm)\n[S] [ ] Hotel (from: 1 Sep 2026 to: 3 Sep 2026)\n____________________________________________________________\nInvalid view date. Use: view YYYY-MM-DD\n____________________________________________________________\nHere are the items in your itinerary on 3 Sep 2026:\n[S] [ ] Hotel (from: 1 Sep 2026 to: 3 Sep 2026)\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "Find items by description keywords",
    "aim": "Match all case-insensitive description keywords and preserve original item numbers.",
    "inputs": ["activity Read Book", "activity Book Tokyo Flight", "activity Return book", "find BOOK flight", "find hotel", "find", "exit"],
    "commands": [
      {
        "command": "./gradlew --quiet classes && printf '%s\\n' 'activity Read Book' 'activity Book Tokyo Flight' 'activity Return book' 'find BOOK flight' 'find hotel' 'find' 'exit' | java -cp build/classes/java/main meepmoop.MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Read Book\nNow you have 1 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Book Tokyo Flight\nNow you have 2 items in your itinerary.\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Return book\nNow you have 3 items in your itinerary.\n____________________________________________________________\nHere are the matching items in your itinerary:\n2. [A] [ ] Book Tokyo Flight\n____________________________________________________________\nNo match found for keyword \"hotel\".\n____________________________________________________________\nInvalid find format. Use: find <keyword> [<keyword>...]\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  }
]
<!-- test-ui:end -->

## Session record

The `test-ui` skill prints the complete console input/output transcript after
execution and stops at the first failed command.
