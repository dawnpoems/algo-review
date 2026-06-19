package com.yegkim.algo_review_api.problem;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemTagRepository extends JpaRepository<ProblemTag, Long> {

	List<ProblemTag> findByProblemIdOrderByTagNameAsc(Long problemId);
}
