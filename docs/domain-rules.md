# 도메인 규칙

## 문제

문제는 다음 정보를 가진다.

- 플랫폼
- 외부 문제 ID
- 제목
- URL
- 난이도
- 태그

## 풀이 시도 결과

허용되는 풀이 시도 결과는 다음과 같다.

- `SOLVED_WITHOUT_HINT`
- `SOLVED_WITH_HINT`
- `FAILED`
- `RETRY_NEEDED`

## 복습 일정

초기 단순 규칙은 다음과 같다.

- `FAILED`: 1일 뒤 복습
- `SOLVED_WITH_HINT`: 2일 뒤 복습
- `RETRY_NEEDED`: 3일 뒤 복습
- `SOLVED_WITHOUT_HINT`: 7일 뒤 복습

문제를 여러 번 성공적으로 복습한 경우, 이후 복습 간격은 늘어날 수 있다.

## 오늘의 복습 큐

다음 조건을 모두 만족하면 문제는 오늘의 복습 큐에 나타나야 한다.

- `nextReviewAt`이 오늘이거나 오늘보다 이전이다.
- 문제가 `mastered`로 표시되어 있지 않다.
