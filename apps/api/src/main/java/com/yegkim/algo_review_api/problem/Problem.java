package com.yegkim.algo_review_api.problem;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
		name = "problems",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_problems_platform_external_id",
				columnNames = {"platform", "external_problem_id"}
		)
)
public class Problem {

	private static final int MASTERED_SUCCESS_THRESHOLD = 2;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private Platform platform;

	@Column(name = "external_problem_id", nullable = false, length = 100)
	private String externalProblemId;

	@Column(nullable = false, length = 255)
	private String title;

	@Column(nullable = false, columnDefinition = "text")
	private String url;

	@Column(length = 100)
	private String difficulty;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ProblemStatus status = ProblemStatus.ACTIVE;

	@Column(name = "next_review_at")
	private LocalDate nextReviewAt;

	@Column(name = "consecutive_success_count", nullable = false)
	private int consecutiveSuccessCount;

	@Column(name = "review_interval_days", nullable = false)
	private int reviewIntervalDays;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<SolvingAttempt> solvingAttempts = new ArrayList<>();

	@OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<ProblemTag> tags = new ArrayList<>();

	protected Problem() {
	}

	public Problem(Platform platform, String externalProblemId, String title, String url, String difficulty) {
		this.platform = Objects.requireNonNull(platform, "platform must not be null");
		this.externalProblemId = requireText(externalProblemId, "externalProblemId");
		this.title = requireText(title, "title");
		this.url = requireText(url, "url");
		this.difficulty = normalizeNullableText(difficulty);
	}

	public ProblemTag addTag(String tagName) {
		ProblemTag tag = new ProblemTag(tagName);
		tag.assignProblem(this);
		tags.add(tag);
		return tag;
	}

	public SolvingAttempt recordAttempt(
			AttemptResult result,
			Integer elapsedMinutes,
			String mistakeReason,
			String memo,
			LocalDate userLocalDate,
			Instant attemptedAt
	) {
		Objects.requireNonNull(userLocalDate, "userLocalDate must not be null");
		SolvingAttempt attempt = new SolvingAttempt(this, result, elapsedMinutes, mistakeReason, memo, attemptedAt);
		solvingAttempts.add(attempt);
		applyReviewSchedule(result, userLocalDate);
		return attempt;
	}

	private void applyReviewSchedule(AttemptResult result, LocalDate userLocalDate) {
		if (result.isSuccess()) {
			consecutiveSuccessCount++;
			reviewIntervalDays = nextSuccessInterval(result);
			if (consecutiveSuccessCount >= MASTERED_SUCCESS_THRESHOLD) {
				status = ProblemStatus.MASTERED;
				nextReviewAt = null;
				return;
			}
			status = ProblemStatus.ACTIVE;
			nextReviewAt = userLocalDate.plusDays(reviewIntervalDays);
			return;
		}

		consecutiveSuccessCount = 0;
		status = ProblemStatus.ACTIVE;
		reviewIntervalDays = result.baseReviewIntervalDays();
		nextReviewAt = userLocalDate.plusDays(reviewIntervalDays);
	}

	private int nextSuccessInterval(AttemptResult result) {
		int baseInterval = result.baseReviewIntervalDays();
		if (reviewIntervalDays <= 0) {
			return baseInterval;
		}
		return Math.max(baseInterval, reviewIntervalDays * 2);
	}

	@PrePersist
	void prePersist() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Platform getPlatform() {
		return platform;
	}

	public String getExternalProblemId() {
		return externalProblemId;
	}

	public String getTitle() {
		return title;
	}

	public String getUrl() {
		return url;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public ProblemStatus getStatus() {
		return status;
	}

	public LocalDate getNextReviewAt() {
		return nextReviewAt;
	}

	public int getConsecutiveSuccessCount() {
		return consecutiveSuccessCount;
	}

	public int getReviewIntervalDays() {
		return reviewIntervalDays;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public List<SolvingAttempt> getSolvingAttempts() {
		return Collections.unmodifiableList(solvingAttempts);
	}

	public List<ProblemTag> getTags() {
		return Collections.unmodifiableList(tags);
	}

	private static String requireText(String value, String fieldName) {
		String normalized = normalizeNullableText(value);
		if (normalized == null) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return normalized;
	}

	private static String normalizeNullableText(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
