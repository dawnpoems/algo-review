# API 계약

## 문제 생성

`POST /api/problems`

요청:

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

응답:

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

## 문제 목록 조회

`GET /api/problems`

## 풀이 시도 생성

`POST /api/problems/{problemId}/attempts`

요청:

```json
{
  "result": "FAILED",
  "elapsedMinutes": 40,
  "mistakeReason": "DP 상태를 정의하지 못함",
  "memo": "LIS 점화식 복습 필요"
}
```

응답:

```json
{
  "id": 1,
  "problemId": 1,
  "result": "FAILED",
  "nextReviewAt": "2026-06-16"
}
```

## 오늘의 복습 큐

`GET /api/reviews/today`

응답:

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

## 태그별 취약도 통계

`GET /api/stats/tags`

응답:

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
