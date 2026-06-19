# MVP 도메인 모델 초안

이 문서는 Issue #2의 MVP 도메인 모델 초안이다.
코드 구현, DB migration, JPA Entity, API 구현은 이 문서의 범위가 아니다.

## 모델링 원칙

- AlgoReview는 외부 플랫폼 문제를 직접 채점하지 않는다.
- MVP 모델은 `Problem`, `SolvingAttempt`, `ProblemTag`를 중심으로 둔다.
- 제품/도메인 동작이 확정되지 않은 부분은 구현 기준으로 확정하지 않고 `docs/open-questions.md`에 남긴다.
- 오늘의 복습 큐와 취약 태그 계산에 필요한 최소 데이터만 우선 저장한다.

## Problem

`Problem`은 외부 플랫폼의 문제를 AlgoReview에 등록한 단위다.

### 주요 속성

- `id`: 내부 식별자
- `platform`: 문제 출처 플랫폼
- `externalProblemId`: 외부 플랫폼의 문제 ID
- `title`: 문제 제목
- `url`: 문제 URL
- `difficulty`: 플랫폼에서 표시되는 난이도
- `status`: 문제 상태
- `nextReviewAt`: 현재 기준 다음 복습 예정일
- `createdAt`: 등록 시각
- `updatedAt`: 마지막 수정 시각

### 관계

- 하나의 `Problem`은 여러 `SolvingAttempt`를 가진다.
- 하나의 `Problem`은 여러 `ProblemTag`를 가진다.

### 상태 초안

추천안:

- `ACTIVE`: 복습 대상이 될 수 있는 기본 상태
- `MASTERED`: 숙달 처리되어 오늘의 복습 큐에서 제외되는 상태

대안:

- `mastered` boolean 필드만 둔다.

트레이드오프:

- `ProblemStatus` enum은 `ARCHIVED`, `PAUSED` 같은 상태를 나중에 확장하기 쉽다.
- boolean은 MVP 구현이 단순하지만 상태가 늘어날 때 migration과 의미 변경이 필요하다.

추천 이유:

- 오늘의 복습 큐 조건에 "mastered가 아닌 문제"가 포함되므로 상태를 명시하는 편이 쿼리와 문서 표현이 명확하다.

## SolvingAttempt

`SolvingAttempt`는 사용자가 특정 문제를 풀거나 시도한 기록이다.

### 주요 속성

- `id`: 내부 식별자
- `problemId`: 연결된 문제 ID
- `result`: 풀이 시도 결과
- `elapsedMinutes`: 풀이에 사용한 시간
- `mistakeReason`: 실수 원인
- `memo`: 자유 메모
- `attemptedAt`: 실제 풀이 또는 시도 시각
- `createdAt`: 기록 생성 시각

### 풀이 결과 enum 초안

- `SOLVED_WITHOUT_HINT`: 힌트 없이 해결
- `SOLVED_WITH_HINT`: 힌트를 보고 해결
- `FAILED`: 해결 실패
- `RETRY_NEEDED`: 다시 풀 필요가 있음

### 복습 일정과의 관계

추천안:

- `SolvingAttempt`는 풀이 이력을 저장한다.
- 복습 예정일의 현재 상태는 `Problem.nextReviewAt`에 저장한다.
- 새 `SolvingAttempt`가 생성되면 `result`를 기준으로 `Problem.nextReviewAt`을 갱신한다.

대안:

- 각 `SolvingAttempt`에 `nextReviewAt` 계산 결과를 저장하고 오늘의 복습 큐는 최신 시도에서 파생한다.
- `Problem.nextReviewAt`과 `SolvingAttempt.nextReviewAt`을 모두 저장한다.

트레이드오프:

- `Problem.nextReviewAt`만 저장하면 오늘의 복습 큐 조회가 단순하고 MVP에 적합하다.
- `SolvingAttempt.nextReviewAt`만 저장하면 이력 정합성은 좋지만 최신 시도 판별 쿼리가 복잡해진다.
- 둘 다 저장하면 감사 추적은 쉽지만 중복 데이터 정합성 관리가 필요하다.

추천 이유:

- MVP의 핵심 기능은 "오늘 볼 문제"를 빠르게 보여주는 것이므로 현재 복습 예정일을 `Problem`에 두는 것이 가장 단순하다.
- 복습 간격 규칙이 바뀌거나 감사 이력이 필요해지면 `SolvingAttempt`에 계산 스냅샷을 추가할 수 있다.

## ProblemTag

`ProblemTag`는 문제에 붙은 태그를 나타낸다.

### 주요 속성

- `id`: 내부 식별자
- `problemId`: 연결된 문제 ID
- `name`: 태그 이름
- `createdAt`: 태그 연결 생성 시각

### 태그 저장 방식

추천안:

- MVP에서는 별도 `Tag` 마스터 테이블 없이 `problem_tags`에 `problem_id`, `tag_name`을 직접 저장한다.
- 같은 문제에는 같은 태그 이름을 중복 저장하지 않는다.

대안:

- `tags` 테이블과 `problem_tag_mappings` 조인 테이블을 분리한다.

트레이드오프:

- 직접 저장 방식은 테이블 수가 적고 문제 등록/조회 흐름이 단순하다.
- 별도 `Tag` 테이블은 태그명 변경, 별칭, 설명, 색상 같은 메타데이터를 관리하기 좋다.

추천 이유:

- MVP 목표는 취약 태그 통계와 문제 태그 조회이며, 태그 메타데이터 관리는 비목표에 가깝다.

## 중복 문제 등록 기준

추천안:

- 같은 `platform`과 같은 `externalProblemId`를 가진 문제는 중복 등록으로 본다.
- DB에서는 `UNIQUE(platform, external_problem_id)` 제약으로 보호한다.

대안:

- 정규화된 `url` 기준으로 중복을 판단한다.
- `platform`, `externalProblemId`, `title`을 함께 비교한다.

트레이드오프:

- `platform + externalProblemId`는 가장 명확하고 안정적인 식별 기준이다.
- URL은 쿼리 파라미터, trailing slash, 플랫폼 URL 변경에 영향을 받을 수 있다.
- 제목은 플랫폼에서 바뀔 수 있고 같은 제목의 다른 문제가 존재할 수 있다.

추천 이유:

- MVP에서 외부 플랫폼 문제의 자연키로 가장 적합한 값은 `platform + externalProblemId`다.

## 복습 일정 규칙 초안

초기 복습 간격은 풀이 결과에 따라 계산한다.

- `FAILED`: 1일 뒤
- `SOLVED_WITH_HINT`: 2일 뒤
- `RETRY_NEEDED`: 3일 뒤
- `SOLVED_WITHOUT_HINT`: 7일 뒤

추천안:

- `nextReviewAt`은 날짜 단위로 저장한다.
- 시간대와 날짜 기준은 최종 결정이 필요하므로 `docs/open-questions.md`에 남긴다.

## 오늘의 복습 큐 조건

추천안:

- `Problem.status = ACTIVE`
- `Problem.nextReviewAt`이 오늘이거나 오늘보다 이전

대안:

- 최신 `SolvingAttempt`에서 복습 예정일을 파생한다.

추천 이유:

- MVP에서는 현재 복습 상태를 `Problem`에 두면 오늘의 큐 조회가 간단하고 성능 예측이 쉽다.
