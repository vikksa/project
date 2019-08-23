package com.vikson.projects.projecttree

import com.vikson.projects.api.resources.ProjectTreeChildItem
import com.vikson.projects.api.resources.values.ProjectTreeSortAttributes
import com.vikson.projects.api.resources.values.ProjectTreeStateFilter
import com.vikson.projects.api.resources.values.ProjectTreeTypeFilter
import com.vikson.projects.model.OrganisationRootFolder
import com.vikson.projects.model.Project
import com.vikson.projects.model.ProjectFolder
import com.vikson.projects.repositories.OrganisationRootFolderRepository
import com.vikson.projects.repositories.ProjectFolderRepository
import com.vikson.projects.repositories.ProjectRepository
import com.vikson.projects.service.ProjectService
import com.vikson.services.users.PersonalApiClient
import com.vikson.services.users.resources.UserProfile
import com.vikson.services.users.resources.UserSettings
import com.vikson.projects.help.jpa.auditing.SpringSecurityAuditorAware
import com.vikson.projects.service.ProjectTreeService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles(["dev"])
class ProjectTreeChildItemRepositorySpec extends Specification {

    @Autowired
    ProjectTreeService service

    @Autowired
    OrganisationRootFolderRepository rootFolderRepository
    @Autowired
    ProjectFolderRepository folderRepository
    @Autowired
    ProjectRepository projectRepository

    @MockBean
    SpringSecurityAuditorAware auditorAwareMock
    @MockBean
    ProjectService projectService
    @MockBean
    PersonalApiClient personalApiClient

    def setup() {
        Mockito.when(auditorAwareMock.getCurrentAuditor())
                .thenReturn(Optional.of(UUID.randomUUID()))
        def me = new UserProfile(settings: new UserSettings(admin: true,projectCreator: true))
        Mockito.when(personalApiClient.getMe()).thenReturn(me)
    }

    def 'Retrieve active projects and folders ordered by title.'() {
        given:
        def rootFolder = getRootFolder()
        ['AAAA', 'AAAAB', 'AAAAC'].each {createProject(rootFolder, it)}
        ['AAAA', 'AAAAA', 'AAAAAA'].each {createFolder(rootFolder, it)}
        // Create inactive items
        createProject(rootFolder, 'Inactive Project', false)
        createFolder(rootFolder, 'Inactive Folder', false)

        Mockito.when(projectService.getFolder(rootFolder.getId())).thenReturn(rootFolder)
        when:
        def page = service.getChildren(rootFolder.id, ProjectTreeStateFilter.Active, ProjectTreeTypeFilter.All, 6, 0, ProjectTreeSortAttributes.Title, Sort.Direction.ASC)
        then:
        page.first
        page.totalElements == 6
        page.numberOfElements == 6
        page.size == 6
        page.number == 0
        page.content.collect {it.name} == ['AAAA', 'AAAAA', 'AAAAAA', 'AAAA', 'AAAAB', 'AAAAC']
    }

    def 'Search within project tree.'() {
        given:
        def rootFolder = getRootFolder()
        Mockito.when(projectService.getRootFolderId()).thenReturn(rootFolder.id)
        ['ZapMap', 'LepKep'].each {createFolder(rootFolder, it)}
        ['SmackRap', 'MapRep'].each {createProject(rootFolder, it)}

        Mockito.when(projectService.getRootFolder()).thenReturn(rootFolder)
        when:
        def page = service.getChildren('Map', ProjectTreeStateFilter.Active, ProjectTreeTypeFilter.All, 2, 0, ProjectTreeSortAttributes.Title, Sort.Direction.ASC)
        then:
        page.first
        page.last
        page.totalElements == 2
        page.numberOfElements == 2
        page.size == 2
        page.number == 0
        page.content.collect {it.name} == ['ZapMap', 'MapRep']
    }

    def 'Write number of items into a Folder ProjectTreeChildItem.'() {
        given:
        def rootFolder = getRootFolder()
        Mockito.when(projectService.getRootFolderId()).thenReturn(rootFolder.id)
        def folder = createFolder(rootFolder, 'lol')
        createProject(folder)

        Mockito.when(projectService.getRootFolder()).thenReturn(rootFolder)
        when:
        def page = service.getChildren('lol', ProjectTreeStateFilter.Active, ProjectTreeTypeFilter.All, 1, 0, ProjectTreeSortAttributes.Title, Sort.Direction.ASC)
        then:
        def childItem = page.content.first()
        childItem.items == 1
    }

    def 'When supplying stateFilter=Folders only show Folders in the Result List.'() {
        given:
        def rootFolder = getRootFolder()
        Mockito.when(projectService.getFolder(rootFolder.id)).thenReturn(rootFolder)

        3.times {createFolder(rootFolder)}
        2.times {createProject(rootFolder)}

        when:
        def page = service.getChildren(rootFolder.id, ProjectTreeStateFilter.All, ProjectTreeTypeFilter.Folders, 3, 0, ProjectTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 3
        page.last
        page.content.stream().allMatch {it.type == ProjectTreeChildItem.Type.Folder}
    }

    def getRootFolder(UUID organisationId = UUID.randomUUID()) {
        def folder = new ProjectFolder(organisationId, organisationId.toString(), organisationId, null)
        rootFolderRepository.save new OrganisationRootFolder(organisationId, folder)
        return folder
    }

    def createFolder(ProjectFolder parent, String title = UUID.randomUUID().toString(), boolean active = true) {
        def folder = new ProjectFolder(UUID.randomUUID(), title, parent.organisationId, parent)
        folder.active = active
        folderRepository.save folder
    }

    def createProject(ProjectFolder folder, String title = UUID.randomUUID().toString(), boolean active = true) {
        def project = new Project(UUID.randomUUID(), title, folder.organisationId, folder, ZonedDateTime.now())
        project.active = active
        projectRepository.save project
    }

    def 'Retrieve active projects and folders ordered by last Activity.'() {
        given:
        def rootFolder = getRootFolder()
        ['AAAA', 'AAAAB', 'AAAAC'].each {createProject(rootFolder, it)}
        ['AAAA', 'AAAAA', 'AAAAAA'].each {createFolder(rootFolder, it)}
        // Create inactive items
        createProject(rootFolder, 'Inactive Project', false)
        createFolder(rootFolder, 'Inactive Folder', false)

        Mockito.when(projectService.getFolder(rootFolder.getId())).thenReturn(rootFolder)
        when:
        def page = service.getChildren(rootFolder.id, ProjectTreeStateFilter.All, ProjectTreeTypeFilter.All, 6, 0, ProjectTreeSortAttributes.LastActivity, Sort.Direction.DESC)
        then:
        page.first
        page.totalElements == 8
        page.numberOfElements == 6
        page.size == 6
        page.number == 0
        page.content.get(0).lastActivity > page.content.get(1).lastActivity
        page.content.get(1).lastActivity > page.content.get(2).lastActivity
        page.content.get(2).lastActivity > page.content.get(3).lastActivity
    }

}
