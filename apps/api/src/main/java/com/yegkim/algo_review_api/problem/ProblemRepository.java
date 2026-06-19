package com.yegkim.algo_review_api.problem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

	Optional<Problem> findByPlatformAndExternalProblemId(Platform platform, String externalProblemId);

	boolean existsByPlatformAndExternalProblemId(Platform platform, String externalProblemId);

	List<Problem> findByStatusAndNextReviewAtLessThanEqualOrderByNextReviewAtAscIdAsc(
			ProblemStatus status,
			LocalDate userLocalDate
	);
}
