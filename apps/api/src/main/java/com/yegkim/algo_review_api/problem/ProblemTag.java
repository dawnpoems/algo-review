package com.yegkim.algo_review_api.problem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
		name = "problem_tags",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_problem_tags_problem_tag",
				columnNames = {"problem_id", "tag_name"}
		)
)
public class ProblemTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "problem_id", nullable = false)
	private Problem problem;

	@Column(name = "tag_name", nullable = false, length = 100)
	private String tagName;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	protected ProblemTag() {
	}

	ProblemTag(String tagName) {
		this.tagName = requireText(tagName);
	}

	void assignProblem(Problem problem) {
		this.problem = problem;
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

	public String getTagName() {
		return tagName;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	private static String requireText(String value) {
		if (value == null) {
			throw new IllegalArgumentException("tagName must not be blank");
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException("tagName must not be blank");
		}
		return trimmed;
	}
}
