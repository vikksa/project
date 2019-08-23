package com.vikson.projects.service;

import com.vikson.services.users.UsersApiClient;
import com.vikson.services.users.resources.UserProfile;
import com.vikson.projects.ProjectsApplication;
import com.vikson.projects.api.resources.PlanRevisionDTO;
import com.vikson.projects.model.Plan;
import com.vikson.projects.model.PlanFolder;
import com.vikson.projects.model.PlanRevision;
import com.vikson.projects.model.Project;
import com.vikson.projects.model.values.PlanType;
import com.vikson.projects.model.values.RevisionMetadata;
import com.vikson.projects.service.translators.PlanTranslator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProjectsApplication.class)
@ActiveProfiles({"dev"})
public class PlanServiceTest {

    @Autowired
    private PlanTranslator planTranslator;

    @MockBean
    private UsersApiClient usersApiClient;

    @Test
    public void revisionNotAllowedToActivateWhenDifferentFromCurrentRevision() {
        //GIVEN
        Plan plan = new Plan(UUID.randomUUID(), "MyPlan", new Project(), new PlanFolder(UUID.randomUUID(), "MyFolder", new Project(), null), null);

        RevisionMetadata someRevisionMetadata = new RevisionMetadata();
        someRevisionMetadata.setImageHeight(100);
        someRevisionMetadata.setImageWidth(100);
        PlanRevision someRevision = new PlanRevision(UUID.randomUUID(), plan, "1", PlanType.File, someRevisionMetadata, null, null);

        RevisionMetadata currentRevisionMetadata = new RevisionMetadata();
        currentRevisionMetadata.setImageHeight(150);
        currentRevisionMetadata.setImageWidth(150);
        PlanRevision currentRevision = new PlanRevision(UUID.randomUUID(), plan, "2", PlanType.File, currentRevisionMetadata, null, null);

        Mockito.when(usersApiClient.getUser(Mockito.any())).thenReturn(new UserProfile());
        //WHEN
        PlanRevisionDTO planRevisionDTO = planTranslator.translate(someRevision, currentRevision);

        //THEN
        assert !planRevisionDTO.isAllowSetActive();
    }
}
