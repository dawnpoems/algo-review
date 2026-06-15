# API Contract

## Create Problem

POST /api/problems

Request:

```json
{
  "platform": "BAEKJOON",
  "externalProblemId": "11053",
  "title": "가장 긴 증가하는 부분 수열",
  "url": "https://www.acmicpc.net/problem/11053",
  "difficulty": "Silver II",
  "tags": ["DP", "LIS"]
}
```

Response:

```json
{
  "id": 1,
  "platform": "BAEKJOON",
  "externalProblemId": "11053",
  "title": "가장 긴 증가하는 부분 수열",
  "url": "https://www.acmicpc.net/problem/11053",
  "difficulty": "Silver II",
  "tags": ["DP", "LIS"]
}
```

## List Problems

GET /api/problems

## Create Attempt

POST /api/problems/{problemId}/attempts

Request:

```json
{
  "result": "FAILED",
  "elapsedMinutes": 40,
  "mistakeReason": "Could not define DP state",
  "memo": "Need to review LIS transition"
}
```

Response:

```json
{
  "id": 1,
  "problemId": 1,
  "result": "FAILED",
  "nextReviewAt": "2026-06-16"
}
```

## Today Review Queue

GET /api/reviews/today

Response:

```json
[
  {
    "problemId": 1,
    "title": "가장 긴 증가하는 부분 수열",
    "platform": "BAEKJOON",
    "tags": ["DP", "LIS"],
    "nextReviewAt": "2026-06-16",
    "lastAttemptResult": "FAILED"
  }
]
```

## Tag Weakness Stats

GET /api/stats/tags

Response:

```json
[
  {
    "tag": "DP",
    "attemptCount": 10,
    "failedCount": 4,
    "hintUsedCount": 2,
    "weaknessScore": 0.6
  }
]
```
