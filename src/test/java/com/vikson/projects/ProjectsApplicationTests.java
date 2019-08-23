package com.vikson.projects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@Tag("integration")
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"dev","integration"})
@DisplayName("Projects Service Integration Test")
public class ProjectsApplicationTests {

	@Test
	@DisplayName("Context loads.")
	public void contextLoads() {
	}

}
