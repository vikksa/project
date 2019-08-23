package com.vikson.projects.lambda;


import com.vikson.projects.ProjectsApplication;
import com.vikson.projects.model.ProjectLocation;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectsApplication.class)
@ActiveProfiles({"dev"})
public class LambdaServiceTest {
    @Autowired
    private LambdaService lambdaService;

    @Test
    @DisplayName("Start Step Machine to retrieve and save weather")
    public void testWeatherVienna() {
        lambdaService.retrieveWeatherData(new ProjectLocation("332028", "IN"));
    }
}
