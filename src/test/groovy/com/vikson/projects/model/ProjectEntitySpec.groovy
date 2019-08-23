package com.vikson.projects.model


import com.vikson.projects.repositories.ProjectRepository
import com.vikson.projects.help.jpa.auditing.SpringSecurityAuditorAware
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
class ProjectEntitySpec extends Specification {

    @Autowired
    OrganisationRootFolderRepository folderRepository

    @Autowired
    ProjectRepository projectRepository

    @MockBean
    SpringSecurityAuditorAware auditorAwareMock

    def setup() {
        Mockito.when(auditorAwareMock.getCurrentAuditor())
            .thenReturn(Optional.of(UUID.randomUUID()))
    }

    def 'Save a project with last_activity to the database.'() {
        given:
        // Get a parent folder for the new project.
        def organisationId = UUID.randomUUID()
        def folder = new ProjectFolder(UUID.randomUUID(), "Root Folder", organisationId, null)
        def rootFolder = folderRepository.save(new OrganisationRootFolder(organisationId, folder))

        def newProject = new Project(UUID.randomUUID(), "New Project", organisationId, folder, ZonedDateTime.now())
        def lastActivity = ZonedDateTime.now().plusHours(1)
        newProject.lastActivity = lastActivity

        when:
        def savedProject = projectRepository.save(newProject)

        then:
        savedProject.lastActivity == lastActivity
    }

}
