create table problems (
    id bigserial primary key,
    platform varchar(30) not null,
    external_problem_id varchar(100) not null,
    title varchar(255) not null,
    url text not null,
    difficulty varchar(100),
    status varchar(30) not null default 'ACTIVE',
    next_review_at date,
    consecutive_success_count integer not null default 0,
    review_interval_days integer not null default 0,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint uk_problems_platform_external_id unique (platform, external_problem_id),
    constraint ck_problems_platform check (platform in ('BAEKJOON', 'PROGRAMMERS', 'LEETCODE', 'ETC')),
    constraint ck_problems_status check (status in ('ACTIVE', 'MASTERED')),
    constraint ck_problems_consecutive_success_count check (consecutive_success_count >= 0),
    constraint ck_problems_review_interval_days check (review_interval_days >= 0)
);

create index idx_problems_review_queue on problems (status, next_review_at);

create table solving_attempts (
    id bigserial primary key,
    problem_id bigint not null,
    result varchar(40) not null,
    elapsed_minutes integer,
    mistake_reason text,
    memo text,
    attempted_at timestamptz not null,
    created_at timestamptz not null,
    constraint fk_solving_attempts_problem foreign key (problem_id) references problems (id) on delete cascade,
    constraint ck_solving_attempts_result check (
        result in ('SOLVED_WITHOUT_HINT', 'SOLVED_WITH_HINT', 'FAILED', 'RETRY_NEEDED')
    ),
    constraint ck_solving_attempts_elapsed_minutes check (
        elapsed_minutes is null or elapsed_minutes >= 0
    )
);

create index idx_solving_attempts_problem_attempted_at on solving_attempts (problem_id, attempted_at desc);
create index idx_solving_attempts_result on solving_attempts (result);

create table problem_tags (
    id bigserial primary key,
    problem_id bigint not null,
    tag_name varchar(100) not null,
    created_at timestamptz not null,
    constraint fk_problem_tags_problem foreign key (problem_id) references problems (id) on delete cascade,
    constraint uk_problem_tags_problem_tag unique (problem_id, tag_name),
    constraint ck_problem_tags_tag_name_not_blank check (btrim(tag_name) <> '')
);

create index idx_problem_tags_problem_id on problem_tags (problem_id);
create index idx_problem_tags_tag_name on problem_tags (tag_name);
