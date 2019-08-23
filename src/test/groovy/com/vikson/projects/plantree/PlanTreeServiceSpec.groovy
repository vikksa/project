package com.vikson.projects.plantree

import com.vikson.projects.api.resources.PlanTreeChildItem
import com.vikson.projects.api.resources.values.PlanTreeSortAttributes
import com.vikson.projects.api.resources.values.PlanTreeStateFilter
import com.vikson.projects.api.resources.values.PlanTreeTypeFilter
import com.vikson.projects.model.OrganisationRootFolder
import com.vikson.projects.model.Plan
import com.vikson.projects.model.PlanFolder
import com.vikson.projects.model.PlanRevision
import com.vikson.projects.model.Project
import com.vikson.projects.model.ProjectFolder
import com.vikson.projects.repositories.PlanFolderRepository
import com.vikson.projects.repositories.PlanRepository
import com.vikson.projects.repositories.ProjectRepository
import com.vikson.projects.service.PlanService
import com.vikson.services.issues.TaskApiClient
import com.vikson.services.issues.resources.PlanTaskDTO
import com.vikson.services.users.PrivilegeApiClient
import com.vikson.projects.help.jpa.auditing.SpringSecurityAuditorAware
import com.vikson.projects.model.values.PlanType
import com.vikson.projects.model.values.RevisionMetadata
import com.vikson.projects.repositories.OrganisationRootFolderRepository
import com.vikson.projects.service.PlanTreeService
import com.vikson.projects.service.PrivilegeUtils
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
class PlanTreeServiceSpec extends Specification {

    @Autowired
    OrganisationRootFolderRepository rootFolderRepository
    @Autowired
    ProjectRepository projectRepository
    @Autowired
    PlanRepository planRepository
    @Autowired
    PlanFolderRepository folderRepository

    @Autowired
    PlanTreeService service

    @MockBean
    SpringSecurityAuditorAware auditorAwareMock
    @MockBean
    PlanService planService
    @MockBean
    PrivilegeUtils privilegeUtils
    @MockBean
    PrivilegeApiClient privilegeApiClient
    @MockBean
    TaskApiClient taskApiClient

    def setup() {
        Mockito.when(auditorAwareMock.getCurrentAuditor())
                .thenReturn(Optional.of(UUID.randomUUID()))
        Mockito.when(privilegeUtils.canViewAllPlans(Mockito.any(UUID))).thenReturn(true)
        Mockito.when(privilegeApiClient.isMember(Mockito.any(UUID))).thenReturn(true)
    }

    def 'Load root children of plan tree.'() {
        given:
        def project = createProject()
        3.times {createFolder(project)}
        3.times {createPlan(project)}

        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.All, null, 0, 6, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 6
        page.first
        page.last
        page.content.size() == 6
    }

    def 'Load active and inactive plans.'() {
        given:
        def project = createProject()
        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)

        createPlan(project, "Active Plan")
        createPlan(project, "Inactive Plan", false)
        createFolder(project, "Active Folder")
        createFolder(project, "Inactive Folder", false)

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.All, null, 0, 4, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 4
    }

    def 'Filter inactive plans.'() {
        given:
        def project = createProject()
        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)

        createPlan(project, "Active Plan")
        createPlan(project, "Inactive Plan", false)

        createFolder(project, "Active Folder")
        createFolder(project, "Inactive Folder", false)

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.Inactive, PlanTreeTypeFilter.All, null,  0, 2, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 2
        page.content.stream().allMatch { (!it.active) }
    }

    def 'Search for plans and folders.'() {
        given:
        // (Folder-A)
        // -- (Plan A1)
        // -- (Plan A2)
        // -- (Plan B1)
        // (Folder B)
        // -- (Plan A3)
        def project = createProject()
        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)
        def folderA = createFolder(project, "A")
        createPlan(folderA, "A1")
        createPlan(folderA, "A2")
        createPlan(folderA, "B1")
        def folderB = createFolder(project, "B")
        createPlan(folderB, "A3")

        when:
        def page = service.getRootChildren(project.id, "A", PlanTreeStateFilter.All, PlanTreeTypeFilter.All, null, 0, 4, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 4
        page.content.stream().allMatch {it.name.contains("A")}
    }

    def 'Filter by timestamp.'() {
        given:
        def project = createProject()
        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)
        def since = ZonedDateTime.now().plusDays(1)
        createPlan(project, "Before", true, since.minusMinutes(1))
        createPlan(project, "After", true, since.plusMinutes(1))

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.All, since.toInstant(), 0, 6, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 1
        page.content.stream().allMatch {it.lastActivity.isAfter(since.toInstant())}
    }

    def 'Filter Plan Tree only Folders.'() {
        given:
        def project = createProject()
        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)
        3.times {createFolder(project)}
        3.times {createPlan(project)}

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.Folders, null, 0, 3, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 3
        page.content.stream().allMatch {it.type == PlanTreeChildItem.Type.Folder}
    }

    def 'Filter Plan Tree only Plans.'() {
        given:
        def project = createProject()
        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)
        3.times {createFolder(project)}
        3.times {createPlan(project)}

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.Plans, null, 0, 3, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 3
        page.content.stream().allMatch {it.type == PlanTreeChildItem.Type.Plan}
        page.numberOfPlans == 3
    }

    def 'Load only plans with open tasks as subcontractor.'() {
        given:
        def project = createProject()
        def planWithOpenTasks = createPlan(project)
        // Plans without open tasks
        2.times {createPlan(project)}
        def folderWithOpenTasks = createFolder(project)
        def planInSubFolderWithOpenTasks = createPlan(folderWithOpenTasks)
        // Mock user to subcontractor
        Mockito.when(privilegeUtils.canViewAllPlans(Mockito.any(UUID)))
            .thenReturn(false)
        // Mock Plans with Tasks
        Mockito.when(taskApiClient.getPlansWithOpenTasks(Mockito.any(UUID)))
            .thenReturn([new PlanTaskDTO(planId: planWithOpenTasks.id), new PlanTaskDTO(planId: planInSubFolderWithOpenTasks.id)])

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.Folders, null, 0 , 2, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 2
    }

    def createProject(UUID organisationId = UUID.randomUUID(), String title = UUID.randomUUID().toString()) {
        def folder = new ProjectFolder(organisationId, organisationId.toString(), organisationId, null)
        rootFolderRepository.save new OrganisationRootFolder(organisationId, folder)
        def project = new Project(UUID.randomUUID(), title, folder.organisationId, folder, ZonedDateTime.now())
        projectRepository.save(project).rootFolder
    }

    def createFolder(PlanFolder parent, String name = UUID.randomUUID().toString(), boolean active = true) {
        def folder = new PlanFolder(UUID.randomUUID(), name, parent.project, parent)
        folder.active = active
        folderRepository.save folder
    }

    def createPlan(PlanFolder parent, String name = UUID.randomUUID().toString(), boolean active = true, ZonedDateTime lastActivity = null) {
        def plan = new Plan(UUID.randomUUID(), name, parent.project, parent, ZonedDateTime.now())
        plan.active = active
        plan.lastActivity = lastActivity
        // Create initial revision for this plan.
        def revision = new PlanRevision(UUID.randomUUID(), plan, "1", PlanType.File, new RevisionMetadata(), ZonedDateTime.now(), null)
        plan.currentRevision = revision
        planRepository.save(plan)
    }

    def 'Load root children of plan tree sort by lastActivity.'() {
        given:
        def project = createProject()
        2.times {createFolder(project)}
        2.times {createPlan(project)}
        2.times {createFolder(project)}
        2.times {createPlan(project)}

        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.All, null, 0, 8, PlanTreeSortAttributes.LastActivity, Sort.Direction.DESC)

        then:
        page.totalElements == 8
        page.first
        page.last
        page.content.size() == 8
        page.content.get(0).lastActivity > page.content.get(1).lastActivity
        page.content.get(1).lastActivity > page.content.get(2).lastActivity
        page.content.get(2).lastActivity > page.content.get(3).lastActivity
        page.content.get(4).lastActivity > page.content.get(5).lastActivity
    }

    def 'Load root children of plan tree sort by title.'() {
        given:
        def project = createProject()
        2.times {createFolder(project)}
        2.times {createPlan(project)}
        2.times {createFolder(project)}
        2.times {createPlan(project)}

        Mockito.when(planService.getPlanFolder(project.id)).thenReturn(project)

        when:
        def page = service.getRootChildren(project.id, "", PlanTreeStateFilter.All, PlanTreeTypeFilter.All, null, 0, 8, PlanTreeSortAttributes.Title, Sort.Direction.ASC)

        then:
        page.totalElements == 8
        page.first
        page.last
        page.content.size() == 8
        //Folders
        page.content.get(0).name < page.content.get(1).name
        page.content.get(1).name < page.content.get(2).name
        page.content.get(2).name < page.content.get(3).name
        //Plans
        page.content.get(4).name < page.content.get(5).name
    }


}
