package com.vikson.projects.model


import com.vikson.projects.repositories.PlanRepository
import com.vikson.projects.repositories.ProjectRepository
import com.vikson.projects.help.jpa.auditing.SpringSecurityAuditorAware
import com.vikson.projects.model.values.PlanType
import com.vikson.projects.model.values.RevisionMetadata
import com.vikson.projects.repositories.OrganisationRootFolderRepository
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles(["dev"])
class PlanEntitySpec extends Specification {

    @Autowired
    OrganisationRootFolderRepository folderRepository

    @Autowired
    ProjectRepository projectRepository
    @Autowired
    PlanRepository planRepository

    @MockBean
    SpringSecurityAuditorAware auditorAwareMock

    def setup() {
        Mockito.when(auditorAwareMock.getCurrentAuditor())
                .thenReturn(Optional.of(UUID.randomUUID()))
    }

    def 'Save plan with "last_activity" to database.'() {
        given:
        // Create project and project folder to create this plan in.

        def organisationId = UUID.randomUUID()
        def folder = new ProjectFolder(UUID.randomUUID(), "Root Folder", organisationId, null)
        def rootFolder = folderRepository.save(new OrganisationRootFolder(organisationId, folder))

        def project = projectRepository.save(new Project(UUID.randomUUID(), "New Project", organisationId, folder, ZonedDateTime.now()))

        def plan = new Plan(UUID.randomUUID(), "New Plan", project, project.getRootFolder(), ZonedDateTime.now())

        // Create initial revision for this plan.
        def revision = new PlanRevision(UUID.randomUUID(), plan, "1", PlanType.File, new RevisionMetadata(), ZonedDateTime.now(), null)
        plan.currentRevision = revision

        def lastActivity = ZonedDateTime.now().minusHours(1)
        plan.lastActivity = lastActivity

        when:
        def savedPlan = planRepository.save(plan)

        then:
        savedPlan.lastActivity == lastActivity
    }

}
