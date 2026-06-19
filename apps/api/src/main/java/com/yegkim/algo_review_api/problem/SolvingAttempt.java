package com.yegkim.algo_review_api.problem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "solving_attempts")
public class SolvingAttempt {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "problem_id", nullable = false)
	private Problem problem;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private AttemptResult result;

	@Column(name = "elapsed_minutes")
	private Integer elapsedMinutes;

	@Column(name = "mistake_reason", columnDefinition = "text")
	private String mistakeReason;

	@Column(columnDefinition = "text")
	private String memo;

	@Column(name = "attempted_at", nullable = false)
	private Instant attemptedAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected SolvingAttempt() {
	}

	SolvingAttempt(
			Problem problem,
			AttemptResult result,
			Integer elapsedMinutes,
			String mistakeReason,
			String memo,
			Instant attemptedAt
	) {
		this.problem = Objects.requireNonNull(problem, "problem must not be null");
		this.result = Objects.requireNonNull(result, "result must not be null");
		this.elapsedMinutes = validateElapsedMinutes(elapsedMinutes);
		this.mistakeReason = normalizeNullableText(mistakeReason);
		this.memo = normalizeNullableText(memo);
		this.attemptedAt = Objects.requireNonNullElseGet(attemptedAt, Instant::now);
	}

	@PrePersist
	void prePersist() {
		createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Problem getProblem() {
		return problem;
	}

	public AttemptResult getResult() {
		return result;
	}

	public Integer getElapsedMinutes() {
		return elapsedMinutes;
	}

	public String getMistakeReason() {
		return mistakeReason;
	}

	public String getMemo() {
		return memo;
	}

	public Instant getAttemptedAt() {
		return attemptedAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private static Integer validateElapsedMinutes(Integer elapsedMinutes) {
		if (elapsedMinutes != null && elapsedMinutes < 0) {
			throw new IllegalArgumentException("elapsedMinutes must not be negative");
		}
		return elapsedMinutes;
	}

	private static String normalizeNullableText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
