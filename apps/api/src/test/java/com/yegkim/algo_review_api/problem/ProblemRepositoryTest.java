package com.yegkim.algo_review_api.problem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ProblemRepositoryTest {

	@Container
	static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private ProblemRepository problemRepository;

	@Autowired
	private SolvingAttemptRepository solvingAttemptRepository;

	@Autowired
	private ProblemTagRepository problemTagRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void savesProblemWithTagsAndAttempts() {
		Problem problem = lisProblem();
		problem.addTag(" DP ");
		problem.addTag("LIS");
		problem.recordAttempt(
				AttemptResult.FAILED,
				40,
				"DP 상태를 정의하지 못함",
				"LIS 점화식 복습 필요",
				LocalDate.of(2026, 6, 19),
				Instant.parse("2026-06-19T10:00:00Z")
		);

		Problem saved = problemRepository.saveAndFlush(problem);
		entityManager.clear();

		Problem found = problemRepository
				.findByPlatformAndExternalProblemId(Platform.BAEKJOON, "11053")
				.orElseThrow();

		assertThat(found.getId()).isEqualTo(saved.getId());
		assertThat(found.getStatus()).isEqualTo(ProblemStatus.ACTIVE);
		assertThat(found.getNextReviewAt()).isEqualTo(LocalDate.of(2026, 6, 20));
		assertThat(found.getReviewIntervalDays()).isEqualTo(1);
		assertThat(found.getConsecutiveSuccessCount()).isZero();
		assertThat(problemTagRepository.findByProblemIdOrderByTagNameAsc(found.getId()))
				.extracting(ProblemTag::getTagName)
				.containsExactly("DP", "LIS");
		assertThat(solvingAttemptRepository.findByProblemIdOrderByAttemptedAtDesc(found.getId()))
				.extracting(SolvingAttempt::getResult)
				.containsExactly(AttemptResult.FAILED);
	}

	@Test
	void enforcesDuplicateProblemByPlatformAndExternalProblemId() {
		problemRepository.saveAndFlush(lisProblem());

		Problem duplicate = lisProblem();

		assertThatThrownBy(() -> problemRepository.saveAndFlush(duplicate))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void findsTodayReviewQueueUsingUserLocalDate() {
		Problem noAttempt = new Problem(
				Platform.LEETCODE,
				"two-sum",
				"Two Sum",
				"https://leetcode.com/problems/two-sum/",
				"Easy"
		);

		Problem dueYesterday = new Problem(
				Platform.BAEKJOON,
				"1000",
				"A+B",
				"https://www.acmicpc.net/problem/1000",
				"Bronze V"
		);
		dueYesterday.recordAttempt(
				AttemptResult.FAILED,
				10,
				"입출력 형식 실수",
				null,
				LocalDate.of(2026, 6, 18),
				Instant.parse("2026-06-18T09:00:00Z")
		);

		Problem dueToday = new Problem(
				Platform.PROGRAMMERS,
				"12901",
				"2016년",
				"https://school.programmers.co.kr/learn/courses/30/lessons/12901",
				"Level 1"
		);
		dueToday.recordAttempt(
				AttemptResult.RETRY_NEEDED,
				20,
				"시간이 지나서 다시 풀 필요가 있음",
				null,
				LocalDate.of(2026, 6, 16),
				Instant.parse("2026-06-16T09:00:00Z")
		);

		Problem future = new Problem(
				Platform.ETC,
				"custom-1",
				"Custom Problem",
				"https://example.com/problems/custom-1",
				null
		);
		future.recordAttempt(
				AttemptResult.SOLVED_WITHOUT_HINT,
				15,
				null,
				null,
				LocalDate.of(2026, 6, 19),
				Instant.parse("2026-06-19T09:00:00Z")
		);

		Problem mastered = new Problem(
				Platform.BAEKJOON,
				"2557",
				"Hello World",
				"https://www.acmicpc.net/problem/2557",
				"Bronze V"
		);
		mastered.recordAttempt(
				AttemptResult.SOLVED_WITHOUT_HINT,
				5,
				null,
				null,
				LocalDate.of(2026, 6, 1),
				Instant.parse("2026-06-01T09:00:00Z")
		);
		mastered.recordAttempt(
				AttemptResult.SOLVED_WITHOUT_HINT,
				4,
				null,
				null,
				LocalDate.of(2026, 6, 8),
				Instant.parse("2026-06-08T09:00:00Z")
		);

		problemRepository.saveAll(List.of(noAttempt, dueYesterday, dueToday, future, mastered));
		problemRepository.flush();
		entityManager.clear();

		List<Problem> queue = problemRepository.findByStatusAndNextReviewAtLessThanEqualOrderByNextReviewAtAscIdAsc(
				ProblemStatus.ACTIVE,
				LocalDate.of(2026, 6, 19)
		);

		assertThat(queue)
				.extracting(Problem::getExternalProblemId)
				.containsExactly("1000", "12901");
	}

	@Test
	void marksProblemAsMasteredAfterTwoConsecutiveSuccessfulAttempts() {
		Problem problem = lisProblem();
		problem.recordAttempt(
				AttemptResult.SOLVED_WITHOUT_HINT,
				30,
				null,
				null,
				LocalDate.of(2026, 6, 1),
				Instant.parse("2026-06-01T09:00:00Z")
		);
		problem.recordAttempt(
				AttemptResult.SOLVED_WITHOUT_HINT,
				25,
				null,
				null,
				LocalDate.of(2026, 6, 8),
				Instant.parse("2026-06-08T09:00:00Z")
		);

		Problem saved = problemRepository.saveAndFlush(problem);
		entityManager.clear();

		Problem found = problemRepository.findById(saved.getId()).orElseThrow();

		assertThat(found.getStatus()).isEqualTo(ProblemStatus.MASTERED);
		assertThat(found.getConsecutiveSuccessCount()).isEqualTo(2);
		assertThat(found.getReviewIntervalDays()).isEqualTo(14);
		assertThat(found.getNextReviewAt()).isNull();
	}

	@Test
	void cascadesDeleteToAttemptsAndTags() {
		Problem problem = lisProblem();
		problem.addTag("DP");
		problem.recordAttempt(
				AttemptResult.FAILED,
				40,
				"DP 상태를 정의하지 못함",
				null,
				LocalDate.of(2026, 6, 19),
				Instant.parse("2026-06-19T10:00:00Z")
		);
		Problem saved = problemRepository.saveAndFlush(problem);
		entityManager.clear();

		problemRepository.deleteById(saved.getId());
		problemRepository.flush();

		assertThat(solvingAttemptRepository.count()).isZero();
		assertThat(problemTagRepository.count()).isZero();
	}

	private static Problem lisProblem() {
		return new Problem(
				Platform.BAEKJOON,
				"11053",
				"가장 긴 증가하는 부분 수열",
				"https://www.acmicpc.net/problem/11053",
				"Silver II"
		);
	}
}
