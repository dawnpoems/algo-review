package com.yegkim.algo_review_api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yegkim.algo_review_api.health.HealthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HealthController.class)
class HealthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthReturnsOk() throws Exception {
		mockMvc.perform(get("/healthz"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ok"));
	}
}
