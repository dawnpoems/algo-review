package com.yegkim.algo_review_api.problem;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvingAttemptRepository extends JpaRepository<SolvingAttempt, Long> {

	List<SolvingAttempt> findByProblemIdOrderByAttemptedAtDesc(Long problemId);
}
