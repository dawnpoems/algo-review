# Domain Rules

## Problem

A problem has:

- platform
- external problem id
- title
- url
- difficulty
- tags

## Attempt Result

Allowed attempt results:

- SOLVED_WITHOUT_HINT
- SOLVED_WITH_HINT
- FAILED
- RETRY_NEEDED

## Review Scheduling

Initial simple rules:

- FAILED: review after 1 day
- SOLVED_WITH_HINT: review after 2 days
- RETRY_NEEDED: review after 3 days
- SOLVED_WITHOUT_HINT: review after 7 days

If a problem is reviewed successfully multiple times, the review interval may increase later.

## Today Review Queue

A problem should appear in today's review queue when:

- nextReviewAt is today or earlier
- problem is not marked as mastered
