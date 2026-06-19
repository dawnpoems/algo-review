# DB 스키마 초안

이 문서는 MVP 도메인 모델을 기준으로 한 DB 스키마 초안이다.
DB migration 작성은 이 작업의 범위가 아니며, 실제 DDL은 추후 구현 이슈에서 확정한다.

## 전제

- DB는 PostgreSQL을 기준으로 가정한다.
- enum은 우선 `VARCHAR` 컬럼으로 저장하고 애플리케이션 계층에서 허용 값을 검증하는 안을 추천한다.
- 날짜/시간 타입과 시간대 기준은 최종 결정이 필요하다.

## problems

문제 등록 정보와 현재 복습 상태를 저장한다.

| 컬럼 | 타입 초안 | 필수 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | Y | 내부 식별자 |
| `platform` | `VARCHAR(30)` | Y | 외부 플랫폼 enum |
| `external_problem_id` | `VARCHAR(100)` | Y | 외부 플랫폼 문제 ID |
| `title` | `VARCHAR(255)` | Y | 문제 제목 |
| `url` | `TEXT` | Y | 문제 URL |
| `difficulty` | `VARCHAR(100)` | N | 플랫폼 표시 난이도 |
| `status` | `VARCHAR(30)` | Y | `ACTIVE`, `MASTERED` |
| `next_review_at` | `DATE` | N | 현재 다음 복습 예정일 |
| `created_at` | `TIMESTAMPTZ` | Y | 생성 시각 |
| `updated_at` | `TIMESTAMPTZ` | Y | 수정 시각 |

### 제약 조건

- Primary key: `id`
- Unique: `(platform, external_problem_id)`
- Check 후보: `status in ('ACTIVE', 'MASTERED')`
- Check 후보: `platform in ('BAEKJOON', 'PROGRAMMERS', 'LEETCODE', 'ETC')`

### 인덱스 후보

- `idx_problems_review_queue` on `(status, next_review_at)`
- `idx_problems_platform_external_id` unique on `(platform, external_problem_id)`

## solving_attempts

문제 풀이 또는 시도 이력을 저장한다.

| 컬럼 | 타입 초안 | 필수 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | Y | 내부 식별자 |
| `problem_id` | `BIGINT` | Y | `problems.id` 참조 |
| `result` | `VARCHAR(40)` | Y | 풀이 결과 enum |
| `elapsed_minutes` | `INTEGER` | N | 풀이 시간 |
| `mistake_reason` | `TEXT` | N | 실수 원인 |
| `memo` | `TEXT` | N | 자유 메모 |
| `attempted_at` | `TIMESTAMPTZ` | Y | 실제 풀이 또는 시도 시각 |
| `created_at` | `TIMESTAMPTZ` | Y | 기록 생성 시각 |

### 제약 조건

- Primary key: `id`
- Foreign key: `problem_id references problems(id)`
- Check 후보: `elapsed_minutes is null or elapsed_minutes >= 0`
- Check 후보: `result in ('SOLVED_WITHOUT_HINT', 'SOLVED_WITH_HINT', 'FAILED', 'RETRY_NEEDED')`

### 인덱스 후보

- `idx_solving_attempts_problem_attempted_at` on `(problem_id, attempted_at desc)`
- `idx_solving_attempts_result` on `(result)`

## problem_tags

문제와 태그 이름의 연결을 저장한다.

| 컬럼 | 타입 초안 | 필수 | 설명 |
| --- | --- | --- | --- |
| `id` | `BIGSERIAL` | Y | 내부 식별자 |
| `problem_id` | `BIGINT` | Y | `problems.id` 참조 |
| `tag_name` | `VARCHAR(100)` | Y | 태그 이름 |
| `created_at` | `TIMESTAMPTZ` | Y | 연결 생성 시각 |

### 제약 조건

- Primary key: `id`
- Foreign key: `problem_id references problems(id)`
- Unique: `(problem_id, tag_name)`

### 인덱스 후보

- `idx_problem_tags_problem_id` on `(problem_id)`
- `idx_problem_tags_tag_name` on `(tag_name)`

## enum 초안

### Platform

추천안:

- `BAEKJOON`
- `PROGRAMMERS`
- `LEETCODE`
- `ETC`

대안:

- `OTHER`를 사용한다.
- 플랫폼을 enum이 아니라 자유 문자열로 저장한다.

추천 이유:

- MVP의 대상 플랫폼은 제한되어 있고, 잘못된 플랫폼 값이 들어오면 중복 기준과 통계가 흔들린다.

### ProblemStatus

추천안:

- `ACTIVE`
- `MASTERED`

대안:

- `mastered` boolean만 사용한다.

추천 이유:

- 복습 큐 제외 조건을 명확하게 표현하고 이후 `ARCHIVED`, `PAUSED` 같은 상태 확장이 가능하다.

### AttemptResult

추천안:

- `SOLVED_WITHOUT_HINT`
- `SOLVED_WITH_HINT`
- `FAILED`
- `RETRY_NEEDED`

추천 이유:

- 복습 일정 계산과 취약 태그 통계의 기본 입력값으로 사용할 수 있다.

## 복습 예정일 저장 위치

추천안:

- `problems.next_review_at`에 현재 다음 복습 예정일을 저장한다.
- `solving_attempts`에는 MVP에서 복습 예정일 스냅샷을 저장하지 않는다.

대안:

- `solving_attempts.next_review_at`에 시도별 계산 결과를 저장한다.
- 두 테이블에 모두 저장한다.

트레이드오프:

- `problems.next_review_at`은 오늘의 복습 큐 조회가 단순하다.
- `solving_attempts.next_review_at`은 이력 추적에 좋지만 최신 상태 조회가 복잡하다.
- 양쪽 저장은 기능 확장에는 유리하지만 데이터 불일치 위험이 있다.

## 대표 쿼리 초안

오늘의 복습 큐:

```sql
select *
from problems
where status = 'ACTIVE'
  and next_review_at <= current_date
order by next_review_at asc, id asc;
```

태그별 취약도 통계 입력:

```sql
select
  pt.tag_name,
  count(sa.id) as attempt_count,
  count(*) filter (where sa.result = 'FAILED') as failed_count,
  count(*) filter (where sa.result = 'SOLVED_WITH_HINT') as hint_used_count
from problem_tags pt
join solving_attempts sa on sa.problem_id = pt.problem_id
group by pt.tag_name;
```

## 삭제 정책 초안

추천안:

- `Problem` 삭제 시 연결된 `SolvingAttempt`, `ProblemTag`도 함께 삭제한다.

대안:

- 삭제 대신 `ProblemStatus`에 `ARCHIVED`를 추가하고 이력을 보존한다.

결정 필요:

- 실제 삭제를 허용할지, 숨김/보관만 허용할지는 제품 정책 결정이 필요하다.
