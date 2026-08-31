# MeepMoop User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

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

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details
