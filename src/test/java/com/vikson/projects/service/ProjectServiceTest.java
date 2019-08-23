package com.vikson.projects.service;

import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.resources.Licence;
import com.vikson.services.users.resources.UserProfile;
import com.vikson.services.users.resources.UserSettings;
import com.vikson.projects.ProjectsApplication;
import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.api.resources.values.ProjectExpandAttributes;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.ProjectFolder;
import com.vikson.projects.repositories.PlanRepository;
import com.vikson.projects.service.translators.ProjectTranslator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectsApplication.class)
@ActiveProfiles({"dev"})
public class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectTranslator projectTranslator;
    @MockBean
    private PlanRepository planRepository;
    @MockBean
    private PersonalApiClient personalApiClient;

    @Test
    public void translateProjectToDtoWithExpandProperties() {
        //GIVEN
        UserProfile me = new UserProfile();
        me.setUserId(UUID.randomUUID());
        me.setOrganisationId(UUID.randomUUID());
        UserSettings settings = new UserSettings();
        settings.setAdmin(true);
        settings.setProjectCreator(true);
        me.setSettings(settings);
        Licence licence = new Licence();
        licence.setPaid(true);
        me.setLicense(licence);
        Mockito.when(personalApiClient.getMe())
                .thenReturn(me);

        UUID organisationId = UUID.randomUUID();
        ProjectFolder testFolder = new ProjectFolder(UUID.randomUUID(), "Test Folder", organisationId, null);
        Project project = new Project(UUID.randomUUID(), "Test Project", organisationId, testFolder, null);

        ProjectExpandAttributes[] expandAttributes = {ProjectExpandAttributes.NumberOfPlans};
        Mockito.when(planRepository.countPlansByProject(project.getId())).thenReturn(10);

        //WHEN
        ProjectDTO projectDTO = projectTranslator.translate(project, expandAttributes, false);

        //THEN
        assertThat(projectDTO.getNumberOfPlans()).isEqualTo(10);
    }
}
