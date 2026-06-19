package com.yegkim.algo_review_api.problem;

public enum AttemptResult {
	SOLVED_WITHOUT_HINT(7, true),
	SOLVED_WITH_HINT(2, true),
	FAILED(1, false),
	RETRY_NEEDED(3, false);

	private final int baseReviewIntervalDays;
	private final boolean success;

	AttemptResult(int baseReviewIntervalDays, boolean success) {
		this.baseReviewIntervalDays = baseReviewIntervalDays;
		this.success = success;
	}

	int baseReviewIntervalDays() {
		return baseReviewIntervalDays;
	}

	boolean isSuccess() {
		return success;
	}
}
