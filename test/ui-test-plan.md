# MeepMoop UI test plan

Each test case documents its aim, console inputs, and expected output. The JSON block is the executable representation used by `test-ui`; each command is paired with its exact expected output.

## Test cases

### 1. Add and manage itinerary items

- Aim: Test valid activity, accommodation, and transport commands; listing; booking and unbooking; duplicate state changes; invalid arguments; invalid commands; and exit.
- Inputs: Add three items, list them, toggle booking for item 1, then exercise invalid inputs and exit.
- Expected output: Confirmations for valid operations and the appropriate validation messages for invalid operations.

### 2. List an empty itinerary

- Aim: Confirm that `list` works when no items have been added.
- Inputs: `list`, then `exit`.
- Expected output: The empty itinerary heading and separators, followed by the goodbye message.

<!-- test-ui:begin -->
[
  {
    "name": "Add and manage itinerary items",
    "aim": "Test all implemented command paths in one session.",
    "inputs": ["activity Museum", "stay Hotel /from 2026-09-01 /to 2026-09-03", "transport Flight /from Singapore /to Tokyo", "list", "book 1", "book 1", "unbook 1", "unbook 1", "book x", "stay bad", "transport bad", "activity", "wat", "exit"],
    "commands": [
      {
        "command": "javac src/main/java/*.java && printf '%s\\n' 'activity Museum' 'stay Hotel /from 2026-09-01 /to 2026-09-03' 'transport Flight /from Singapore /to Tokyo' 'list' 'book 1' 'book 1' 'unbook 1' 'unbook 1' 'book x' 'stay bad' 'transport bad' 'activity' 'wat' 'exit' | java -cp src/main/java MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\n____________________________________________________________\nGot it. I've added this activity:\n[A] [ ] Museum\nNow you have 1 items in your itinerary.\n____________________________________________________________\n____________________________________________________________\nGot it. I've added this accommodation:\n[S] [ ] Hotel (from: 2026-09-01 to: 2026-09-03)\nNow you have 2 items in your itinerary.\n____________________________________________________________\n____________________________________________________________\nGot it. I've added this transport:\n[T] [ ] Flight (from: Singapore to: Tokyo)\nNow you have 3 items in your itinerary.\n____________________________________________________________\n____________________________________________________________\nHere are the items in your itinerary:\n1. [A] [ ] Museum\n2. [S] [ ] Hotel (from: 2026-09-01 to: 2026-09-03)\n3. [T] [ ] Flight (from: Singapore to: Tokyo)\n____________________________________________________________\nBooked: [A] [X] Museum\nItem is already booked\nUnbooked: [A] [ ] Museum\nItem is already unbooked\nInvalid item number\nInvalid stay format. Use: stay <name> /from <date> /to <date>\nInvalid transport format. Use: transport <name> /from <location> /to <location>\nInvalid activity format. Use: activity <description>\nInvalid input\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  },
  {
    "name": "List an empty itinerary",
    "aim": "Confirm list output when there are no itinerary items.",
    "inputs": ["list", "exit"],
    "commands": [
      {
        "command": "javac src/main/java/*.java && printf '%s\\n' 'list' 'exit' | java -cp src/main/java MeepMoop",
        "expected": "Hello! I'm MeepMoop. How can I assist you today?\n____________________________________________________________\n____________________________________________________________\nHere are the items in your itinerary:\n____________________________________________________________\nGoodbye! Have a great day!\n____________________________________________________________"
      }
    ]
  }
]
<!-- test-ui:end -->

## Session record

The `test-ui` skill prints the complete console input/output transcript after execution and stops at the first failed command.
