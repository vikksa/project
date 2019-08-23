package com.vikson.projects.api.controllers;

import com.vikson.services.core.resources.Page;
import com.vikson.services.issues.CategoryApiClient;
import com.vikson.services.issues.IssueRequestApiClient;
import com.vikson.services.issues.resources.PinCategory;
import com.vikson.services.issues.resources.PinColor;
import com.vikson.services.issues.resources.PostsFilter;
import com.vikson.services.issues.resources.SyncSizeDTO;
import com.vikson.services.users.MembershipsApiClient;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.PrivilegeApiClient;
import com.vikson.services.users.SubscriptionApiClient;
import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.UsersApiClient;
import com.vikson.services.users.resources.Licence;
import com.vikson.services.users.resources.MembershipState;
import com.vikson.services.users.resources.Organisation;
import com.vikson.services.users.resources.Privilege;
import com.vikson.services.users.resources.PrivilegeCheck;
import com.vikson.services.users.resources.Subscription;
import com.vikson.services.users.resources.TeamMember;
import com.vikson.services.users.resources.UserProfile;
import com.vikson.services.users.resources.UserSettings;
import com.vikson.test.auth2.OAuth2Helper;
import com.vikson.projects.api.resources.MoveDto;
import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.api.resources.ProjectFolderDTO;
import com.vikson.projects.api.resources.ProjectTreeDTO;
import com.vikson.projects.api.resources.ScaleDefinitionDTO;
import com.vikson.projects.api.resources.BimplusProjectConfigDto;
import com.vikson.projects.api.resources.ConstructionTypeDTO;
import com.vikson.projects.api.resources.ProjectAddressDTO;
import com.vikson.projects.api.resources.ProjectDurationDTO;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.model.values.BimplusProjectState;
import com.vikson.projects.service.ProjectService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"dev"})
@DisplayName("Projects")
public class Projects {

    @LocalServerPort
    private int serverPort;
    @Autowired
    private OAuth2Helper oAuth2Helper;

    @MockBean
    private PersonalApiClient personalApiClient;
    @MockBean
    private PrivilegeApiClient privilegeApiClient;
    @MockBean
    private UserPrivileges userPrivileges;
    @MockBean
    private MembershipsApiClient membershipsApiClient;
    @MockBean
    private CategoryApiClient categoryApiClient;
    @MockBean
    private SubscriptionApiClient subscriptionApiClient;
    @MockBean
    private UsersApiClient usersApiClient;
    @Autowired
    private ProjectService projectService;
    @MockBean
    IssueRequestApiClient issueRequestApiClient;

    private UserProfile me;
    private PinCategory pinCategory;

    @BeforeEach
    public void setup() {
        RestAssured.port = serverPort;

        // Mock other Services
        UUID userId = UUID.randomUUID();
        me = new UserProfile();
        me.setUserId(userId);
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
        Mockito.when(usersApiClient.getUser(userId)).thenReturn(me);

        Mockito.when(userPrivileges.checkPrivilege(Mockito.any(UUID.class), Mockito.anyList(), Mockito.anyBoolean()))
                .then(args -> {
                    PrivilegeCheck check = new PrivilegeCheck();
                    check.setCheck(true);
                    check.setProjectId(args.getArgument(0));
                    List<Privilege> privileges = args.getArgument(1);
                    Privilege[] privilegesArray = privileges.toArray(new Privilege[0]);
                    check.setPrivileges(privilegesArray);
                    check.setAny(args.getArgument(2));
                    return check;
                });
        Mockito.when(privilegeApiClient.isMember(Mockito.any(UUID.class)))
                .thenReturn(true);
        pinCategory = new PinCategory();
        pinCategory.setCategoryId(UUID.randomUUID());
        pinCategory.setName("CategoryName");
        pinCategory.setColor(PinColor.Blue);
        pinCategory.setActive(true);
        Mockito.when(categoryApiClient.getPinCategories(Mockito.any(UUID.class)))
                .thenReturn(Collections.singletonList(pinCategory));
        Subscription subscription = new Subscription();
        Mockito.when(subscriptionApiClient.getSubscription()).thenReturn(subscription);
        Mockito.when(usersApiClient.getOrganisation(Mockito.any(UUID.class)))
            .then(args -> {
                Organisation organisation = new Organisation();
                organisation.setId(args.getArgument(0));
                return organisation;
            });
        Mockito.when(issueRequestApiClient.getProjectIssueSyncSize(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(new SyncSizeDTO(1024, SyncSizeDTO.SyncSizeType.Project, "1mb"));
    }

    @Test
    @DisplayName("Create a new Project Folder.")
    public void createProjectFolder() {
        // Arrange
        ProjectFolderDTO parentFolder = new ProjectFolderDTO();
        parentFolder.setName("Parent Folder");
        parentFolder = createFolder(parentFolder);

        // Act
        ProjectFolderDTO subFolder = new ProjectFolderDTO();
        subFolder.setName("Sub Folder");
        subFolder.setParentId(parentFolder.getId());

        ProjectFolderDTO createdFolder = createFolder(subFolder);

        // Assert
        assertNotNull(createdFolder);
        assertEquals("Sub Folder", createdFolder.getName());
        assertEquals(parentFolder.getId(), createdFolder.getParentId());
    }

    @Test
    @DisplayName("Update a Project Folder.")
    public void updateProjectFolder() {
        // Arrange
        ProjectFolderDTO prototype = new ProjectFolderDTO();
        prototype.setName("To Update");
        ProjectFolderDTO folder = createFolder(prototype);

        ProjectFolderDTO parentPrototype = new ProjectFolderDTO();
        parentPrototype.setName("Parent");
        ProjectFolderDTO parent = createFolder(parentPrototype);

        // Act
        ProjectFolderDTO updates = new ProjectFolderDTO();
        updates.setName("New Name");
        updates.setParentId(parent.getId());
        ProjectFolderDTO updated = updateFolder(folder.getId(), updates);

        // Assert
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals(parent.getId(), updated.getParentId());
    }

    @Test
    @DisplayName("Remove parent folder.")
    public void removeParentFolder() {
        // Arrange
        ProjectFolderDTO parentPrototype = new ProjectFolderDTO();
        parentPrototype.setName("Parent");
        ProjectFolderDTO parent = createFolder(parentPrototype);

        ProjectFolderDTO prototype = new ProjectFolderDTO();
        prototype.setName("To Update");
        prototype.setParentId(parent.getId());
        ProjectFolderDTO folder = createFolder(prototype);

        // Act
        ProjectFolderDTO removed = removeParent(folder.getId());

        // Assert
        assertNotNull(removed);
        assertNotEquals(parent.getId(), removed.getParentId());
    }

    @Test
    @DisplayName("Delete Empty Folder.")
    public void deleteEmptyFolder(){
        // Arrange
        ProjectFolderDTO prototype = new ProjectFolderDTO();
        prototype.setName("To Delete");
        ProjectFolderDTO folder = createFolder(prototype);

        // Act
        deleteEmptyFolder(folder.getId());
    }

    @Test
    @DisplayName("Show project tree.")
    public void showProjectTree() {
        // Arrange

        // Empty Folder
        ProjectFolderDTO emptyFolderPrototype = new ProjectFolderDTO();
        emptyFolderPrototype.setName("Empty Folder");
        ProjectFolderDTO emptyFolder = createFolder(emptyFolderPrototype);

        // Folder with Sub Folder
        ProjectFolderDTO folderWithSubFolderPrototype = new ProjectFolderDTO();
        folderWithSubFolderPrototype.setName("Folder with Sub Folder");
        ProjectFolderDTO folderWithSubFolder = createFolder(folderWithSubFolderPrototype);

        ProjectFolderDTO subFolder1Prototype = new ProjectFolderDTO();
        subFolder1Prototype.setName("Sub Folder 1");
        subFolder1Prototype.setParentId(folderWithSubFolder.getId());
        ProjectFolderDTO subFolder1 = createFolder(subFolder1Prototype);

        // Project in Root Folder
        ProjectDTO projectInRoot = createProject(new ProjectDTO());

        // Project in Sub Folder
        ProjectDTO projectInSubFolderPrototype = new ProjectDTO();
        projectInSubFolderPrototype.setParentId(folderWithSubFolder.getId());
        ProjectDTO projectInSubFolder = createProject(projectInSubFolderPrototype);

        // External Project
        ProjectDTO projectInOtherOrganisation = createProject(new ProjectDTO(), UUID.randomUUID());
        Mockito.when(membershipsApiClient.listAllMemberships(me.getUserId()))
                .then(args -> {
                    TeamMember membership = new TeamMember();
                    membership.setUserId(me.getUserId());
                    membership.setCompanyId(projectInOtherOrganisation.getOrganisationId());
                    membership.setCompanyName("Other Company");
                    membership.setState(MembershipState.Active);
                    membership.setProjectId(projectInOtherOrganisation.getId());
                    return Collections.singletonList(membership);
                });

        // Act
        ProjectTreeDTO projectTree = getProjectTree();

        // Assert
        assertNotNull(projectTree);

        assertNotNull(projectTree.getExternalProjects());
        List<ProjectDTO> externalProjects = projectTree.getExternalProjects();
        assertEquals(1, externalProjects.size());
        assertTrue(externalProjects.contains(projectInOtherOrganisation),
                "ProjectTreeDTO does not contain all expected external Projects!");

        assertNotNull(projectTree.getChildren());
        List<ProjectDTO> rootProjects = projectTree.getChildren();
        assertEquals(2, rootProjects.size());
        assertTrue(rootProjects.contains(projectInRoot),
                "ProjectTreeDTO children does not contain all expected projects!");

        assertNotNull(projectTree.getSubFolder());
        List<ProjectFolderDTO> rootSubFolder = projectTree.getSubFolder();
        assertEquals(2, rootSubFolder.size());
        Set<ProjectFolderDTO> expectedRootSubFolders = Stream.of(emptyFolder, folderWithSubFolder).collect(Collectors.toSet());
        assertTrue(rootSubFolder.stream().allMatch(expectedRootSubFolders::contains),
                "ProjectFreeDTO.subFolders does not contain all expected sub folders!");
    }

    @Test
    @DisplayName("List all projects in organisation.")
    public void listAllProjectsInOrganisation() {
        // GIVEN
        for(int i=0; i<20;i++) {
            createProject(new ProjectDTO());
        }

        // WHEN
        Response allProjects = getAllProjects();

        // THEN
        assertNotNull(allProjects);
    }

    @Test
    @DisplayName("Create Project with bimplus integration.")
    public void createProjectWithBimplusIntegration() {
        //GIVEN
        ProjectDTO projectDTO = new ProjectDTO();
        BimplusProjectConfigDto bimplusProjectConfigDto = new BimplusProjectConfigDto()
                .withId(UUID.randomUUID())
                .withName("Bimplus project");
        projectDTO.setBimplusProjectConfigDto(bimplusProjectConfigDto);

        //WHEN
        ProjectDTO project = createProject(projectDTO);

        //THEN
        assertNotNull(project.getBimplusProjectConfigDto());
        Assertions.assertEquals(project.getBimplusProjectConfigDto().getId(), projectDTO.getBimplusProjectConfigDto().getId());
        Assertions.assertEquals(project.getBimplusProjectConfigDto().getProjectName(), projectDTO.getBimplusProjectConfigDto().getProjectName());
        assertSame(project.getBimplusProjectConfigDto().getState(), BimplusProjectState.LINKED);
    }

    @Test
    @DisplayName("Search for projects by name.")
    public void searchForProjectsByName() {
        // Arrange
        List<ProjectDTO> matchingProjects = new ArrayList<>();

        ProjectDTO m1 = new ProjectDTO();
        m1.setName("Rohbau Margarethen");
        matchingProjects.add(createProject(m1));

        ProjectDTO m2 = new ProjectDTO();
        m2.setName("Margarethen Bezirksamt");
        matchingProjects.add(createProject(m2));

        ProjectDTO m3 = new ProjectDTO();
        m3.setId(UUID.randomUUID());
        m3.setName("Margarethenstraße 12");
        UUID otherCompanyId = UUID.randomUUID();
        matchingProjects.add(createProject(m3, otherCompanyId));

        TeamMember membership = new TeamMember();
        membership.setProjectId(m3.getId());
        membership.setCompanyId(otherCompanyId);
        membership.setState(MembershipState.Active);
        Mockito.when(membershipsApiClient.listAllMemberships(me.getUserId()))
                .thenReturn(Collections.singletonList(membership));

        ProjectDTO n1 = new ProjectDTO();
        n1.setName("Terminal AC");
        createProject(n1);

        ProjectDTO n2 = new ProjectDTO();
        n2.setName("MargaNOTTEN");
        createProject(n2);

        // Act
        ProjectDTO[] searchResults = searchProjects("Margarethen");

        // Assert
        assertNotNull(searchResults);
        assertEquals(3, searchResults.length);
        assertTrue(Arrays.stream(searchResults).allMatch(matchingProjects::contains));
    }

    @Test
    @DisplayName("Update a project.")
    public void updateAProject() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());

        ProjectDTO updates = new ProjectDTO();
        updates.setName("Starbucks Karlsplatz Passage");
        updates.setNotes("Project Notes");
        updates.setInitials("SKP");
        updates.setActive(true);
        ProjectDurationDTO duration = new ProjectDurationDTO(ZonedDateTime.now().minusMonths(1).toInstant(),
                ZonedDateTime.now().plusYears(1).toInstant(), false, false);
        updates.setDuration(duration);
        ProjectAddressDTO address = new ProjectAddressDTO();
        address.setStreetAddress("Karlsplatz Passage 4");
        address.setZipCode("1010");
        address.setCity("Vienna");
        address.setCc("AT");
        updates.setAddress(address);
        ScaleDefinitionDTO costs = new ScaleDefinitionDTO();
        costs.setName("Costs");
        costs.setActive(true);
        costs.setOneStarLabel("10€ - 100€");
        costs.setTwoStarLabel("100€ - 500€");
        costs.setThreeStarLabel("500€ - 1500€");
        costs.setFourStarLabel("1500€ - 5000€");
        costs.setFiveStarLabel("> 5000€");
        updates.setCostDefinition(costs);
        ScaleDefinitionDTO priority = new ScaleDefinitionDTO();
        priority.setName("Priority");
        priority.setActive(true);
        priority.setOneStarLabel("Low");
        priority.setTwoStarLabel("Medium");
        priority.setThreeStarLabel("High");
        priority.setFourStarLabel("Critical");
        priority.setFiveStarLabel("Blocker");
        updates.setPriorityDefinition(priority);
        ProjectFolderDTO parent = createFolder(new ProjectFolderDTO());
        updates.setParentId(parent.getId());
        updates.setDescription("Renovating the Starbucks Coffee Shop in Karlsplatz Passage");
        updates.setClient("Starbucks Austria GmbH");
        updates.setClientAssistant("Dr. Rudi Mentaer");
        ConstructionTypeDTO constructionTypeDTO = new ConstructionTypeDTO();
        constructionTypeDTO.setType("Renovation");
        updates.setConstructionType(constructionTypeDTO);
        updates.setOccurred(Instant.now().minusSeconds(1));

        // Act
        ProjectDTO updated = updateProject(project.getId(), updates);

        // Assert
        updates.setId(project.getId());
        updates.setOrganisationId(me.getOrganisationId());
        updates.setOwnerId(me.getUserId());

        assertNotNull(updated);
        assertEquals(updates, updated);
    }

    @Test
    @DisplayName("Remove a Project's Parent Folder.")
    public void removeAProjectsParentFolder() {
        // Arrange
        ProjectFolderDTO folder = createFolder(new ProjectFolderDTO());
        ProjectDTO prototype = new ProjectDTO();
        prototype.setParentId(folder.getId());
        ProjectDTO project = createProject(prototype);
        // Act
        ProjectDTO updated = removeProjectParent(project.getId());
        // Assert
        assertNotNull(updated);
        assertNotEquals(folder.getId(), updated.getId());
    }

    @Test
    @DisplayName("Search for Construction Types.")
    public void searchForConstructionTypes() {
        // Arrange
        ProjectDTO prototype1 = new ProjectDTO();
        ConstructionTypeDTO ctype1 = new ConstructionTypeDTO();
        ctype1.setType("Civil Engineering");
        prototype1.setConstructionType(ctype1);
        createProject(prototype1);
    }

    @Test
    @DisplayName("Upload a new Logo for a Project.")
    public void uploadLogo() throws IOException {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        ClassPathResource logo = new ClassPathResource("logo.jpg");
        // Act
        uploadLogo(project.getId(), logo, MediaType.IMAGE_JPEG_VALUE);
        // Assert
        try(InputStream is = downloadLogo(project.getId())) {
            String actualMD5 = DigestUtils.md5DigestAsHex(is);
            assertEquals(DigestUtils.md5DigestAsHex(logo.getInputStream()), actualMD5);
        }
    }

    @Test
    @DisplayName("Remove a logo.")
    public void removeALogo() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        uploadLogo(project.getId(), new ClassPathResource("logo.jpg"), MediaType.IMAGE_JPEG_VALUE);
        // Act
        removeLogo(project.getId());
        // Assert
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/logo", project.getId())
        .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }


    @Test
    @DisplayName("Upload a new Report Logo for a Project.")
    public void uploadReportLogo() throws IOException {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        ClassPathResource logo = new ClassPathResource("logo.jpg");
        // Act
        uploadReportLogo(project.getId(), logo, MediaType.IMAGE_JPEG_VALUE);
        // Assert
        try(InputStream is = downloadReportLogo(project.getId())) {
            String actualMD5 = DigestUtils.md5DigestAsHex(is);
            assertEquals(DigestUtils.md5DigestAsHex(logo.getInputStream()), actualMD5);
        }
    }

    @Test
    @DisplayName("Remove a Report logo.")
    public void removeReportLogo() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        uploadReportLogo(project.getId(), new ClassPathResource("logo.jpg"), MediaType.IMAGE_JPEG_VALUE);
        // Act
        removeReportLogo(project.getId());
        // Assert
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
                .when()
                .get("/api/v2/projects/{projectId}/reportLogo", project.getId())
                .then()
                .log().all()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Get Project with Categories")
    public void getProjectWithCategories(){
        ProjectDTO project = createProject(new ProjectDTO());
        ProjectDTO response = given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}", project.getId())
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectDTO.class);
        assertEquals(response.getId(), project.getId());
        assertEquals(response.getCategoryList().get(0).getCategoryId(), pinCategory.getCategoryId());
    }

    private ProjectFolderDTO createFolder(ProjectFolderDTO prototype) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(prototype)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken("PROJECT_CREATOR"))
                .log().all()
        .when()
                .post("/api/v2/projectFolders")
        .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(ProjectFolderDTO.class);
    }

    private ProjectFolderDTO updateFolder(UUID id, ProjectFolderDTO updates) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updates)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken("PROJECT_CREATOR"))
                .log().all()
        .when()
                .patch("/api/v2/projectFolders/{folderId}", id)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectFolderDTO.class);
    }

    private void deleteEmptyFolder(UUID folderId) {
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken("PROJECT_CREATOR"))
                .log().all()
                .when()
                .delete("/api/v2/projectFolders/{folderId}", folderId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private ProjectFolderDTO removeParent(UUID folderId) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken("PROJECT_CREATOR"))
                .log().all()
        .when()
                .delete("/api/v2/projectFolders/{folderId}/parent", folderId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectFolderDTO.class);
    }

    private ProjectDTO createProject(ProjectDTO prototype) {
        if(StringUtils.isEmpty(prototype.getName())){
            prototype.setName(UUID.randomUUID().toString());
        }
        return createProject(prototype, me.getOrganisationId());
    }

    private ProjectDTO createProject(ProjectDTO prototype, UUID organisationId) {
        UUID previousOrganisationId = me.getOrganisationId();
        me.setOrganisationId(organisationId);
        UUID projectId = given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(prototype)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken("project_creator"))
                .log().all()
        .when()
                .post("/api/v2/projects")
        .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(UUID.class);
        ProjectDTO projectDTO = getProject(projectId);
        me.setOrganisationId(previousOrganisationId);
        return projectDTO;
    }

    @Test
    public void unlinkBimplusProject() {
        //GIVEN
        ProjectDTO projectDTO = new ProjectDTO();
        BimplusProjectConfigDto bimplusProjectConfigDto = new BimplusProjectConfigDto()
                .withId(UUID.randomUUID())
                .withName("Bimplus project");
        projectDTO.setBimplusProjectConfigDto(bimplusProjectConfigDto);

        //WHEN
        ProjectDTO createdProject = createProject(projectDTO);

        ProjectDTO unlinkedProject = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken("project_creator"))
                .log().all()
                .when()
                .delete("/api/v2/projects/bimplus/{projectId}/unlink", createdProject.getId())
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectDTO.class);

        assertSame(unlinkedProject.getBimplusProjectConfigDto().getState(), BimplusProjectState.UNLINKED);
    }

    private ProjectDTO updateProject(UUID id, ProjectDTO updates) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updates)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .patch("/api/v2/projects/{projectId}", id)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectDTO.class);
    }

    private ProjectDTO getProject(UUID id) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/{id}", id)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectDTO.class);
    }

    private ProjectTreeDTO getProjectTree() {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projectFolders")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectTreeDTO.class);
    }

    private ProjectTreeDTO getProjectTreeFiltered() {
        return given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .queryParam("stateFilter", StateFilter.Active)
            .queryParam("search","new")
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .log().all()
            .when()
            .get("/api/v2/projectFolders")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProjectTreeDTO.class);
    }


    private Response getAllProjects() {
        return given()
                .queryParam("page", 0)
                .queryParam("size", 20)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/all/paged")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response();
    }

    private ProjectDTO[] searchProjects(String search) {
        return given()
                .queryParam("search", search)
                .queryParam("page", 0)
                .queryParam("size", 4)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/search")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ProjectDTO[].class);
    }

    private ProjectDTO removeProjectParent(UUID projectId) {
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken("PROJECT_CREATOR"))
                .log().all()
        .when()
                .delete("/api/v2/projects/{projectId}/parent", projectId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
        return getProject(projectId);
    }

    private ConstructionTypeDTO[] searchConstructionTypes(String search) {
        return given()
                .queryParam("search", search)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/constructionTypes")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(ConstructionTypeDTO[].class);
    }

    private void uploadLogo(UUID id, ClassPathResource resource, String mimeType) {
        try {
            given()
                    .multiPart("logo", resource.getFilename(), resource.getInputStream(), mimeType)
                    .auth().oauth2(oAuth2Helper.createBearerToken())
                    .log().all()
            .when()
                    .put("/api/v2/projects/{projectId}/logo", id)
            .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream downloadLogo(UUID id) {
        return given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/logo", id)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().asInputStream();
    }

    private void removeLogo(UUID id) {
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .delete("/api/v2/projects/{projectId}/logo", id)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }

    private void uploadReportLogo(UUID id, ClassPathResource resource, String mimeType) {
        try {
            given()
                    .multiPart("logo", resource.getFilename(), resource.getInputStream(), mimeType)
                    .auth().oauth2(oAuth2Helper.createBearerToken())
                    .log().all()
                    .when()
                    .put("/api/v2/projects/{projectId}/reportLogo", id)
                    .then()
                    .log().all()
                    .statusCode(HttpStatus.OK.value());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream downloadReportLogo(UUID id) {
        return given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
                .when()
                .get("/api/v2/projects/{projectId}/reportLogo", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().asInputStream();
    }

    private void removeReportLogo(UUID id) {
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
                .when()
                .delete("/api/v2/projects/{projectId}/reportLogo", id)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }


    @Test
    @DisplayName("Get project tree with Active Filter and search params.")
    public void showProjectTreeWithFilters() {
        // Arrange

        // Empty Folder
        ProjectFolderDTO emptyFolderPrototype = new ProjectFolderDTO();
        emptyFolderPrototype.setName("Empty Folder");
        ProjectFolderDTO emptyFolder = createFolder(emptyFolderPrototype);

        // Folder with Sub Folder
        ProjectFolderDTO folderWithSubFolderPrototype = new ProjectFolderDTO();
        folderWithSubFolderPrototype.setName("Folder with Sub Folder");
        ProjectFolderDTO folderWithSubFolder = createFolder(folderWithSubFolderPrototype);

        ProjectFolderDTO subFolder1Prototype = new ProjectFolderDTO();
        subFolder1Prototype.setName("Sub Folder 1");
        subFolder1Prototype.setParentId(folderWithSubFolder.getId());
        ProjectFolderDTO subFolder1 = createFolder(subFolder1Prototype);

        // Project in Root Folder
        ProjectDTO projectInRoot = createProject(new ProjectDTO());

        // Project in Sub Folder
        ProjectDTO projectInSubFolderPrototype = new ProjectDTO();
        projectInSubFolderPrototype.setParentId(folderWithSubFolder.getId());
        ProjectDTO projectInSubFolder = createProject(projectInSubFolderPrototype);
        // Project in Sub Folder
        ProjectDTO projectInSubFolderPrototype1 = new ProjectDTO();
        projectInSubFolderPrototype.setParentId(folderWithSubFolder.getId());
        ProjectDTO projectInSubFolder1 = createProject(projectInSubFolderPrototype);

        // External Project
        ProjectDTO projectInOtherOrganisation = createProject(new ProjectDTO(), UUID.randomUUID());
        Mockito.when(membershipsApiClient.listAllMemberships(me.getUserId()))
            .then(args -> {
                TeamMember membership = new TeamMember();
                membership.setUserId(me.getUserId());
                membership.setCompanyId(projectInOtherOrganisation.getOrganisationId());
                membership.setCompanyName("Other Company");
                membership.setState(MembershipState.Active);
                membership.setProjectId(projectInOtherOrganisation.getId());
                return Collections.singletonList(membership);
            });

        // Act
        ProjectTreeDTO projectTree = getProjectTreeFiltered();

        // Assert
        assertNotNull(projectTree);

        assertNotNull(projectTree.getExternalProjects());
        List<ProjectDTO> externalProjects = projectTree.getExternalProjects();
        assertEquals(1, externalProjects.size());

        assertEquals(2,projectTree.getChildren().size());
    }


    @Test
    @DisplayName("Move project and folder to another folder.")
    public void moveProjectAndFolders() {
        // Arrange

        // Empty Folder
        ProjectFolderDTO folder1 = createFolder(new ProjectFolderDTO());

        // Empty Folder
        ProjectFolderDTO folder2 = createFolder(new ProjectFolderDTO());

        // Project in Root Folder
        ProjectDTO projectInRoot = createProject(new ProjectDTO());

        MoveDto moveDto = new MoveDto();
        moveDto.setFolders(Collections.singletonList(folder2.getId()));
        moveDto.setEntities(Collections.singletonList(projectInRoot.getId()));

        ProjectFolderDTO newParent = given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken("admin"))
            .body(moveDto)
            .log().all()
            .when()
            .post("/api/v2/projects/folders/{parentId}", folder1.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().as(ProjectFolderDTO.class);

        // Assert
        assertNotNull(newParent);

        assertEquals(1,newParent.getChildren().size());
        assertEquals(1,newParent.getSubFolder().size());
    }

    @Test
    @DisplayName("Get project tree.")
    public void getProjectTreePage() {

        // GIVEN
        ProjectFolderDTO emptyFolder = createFolder(new ProjectFolderDTO());
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setParentId(emptyFolder.getParentId());
        createProject(projectDTO);

        // WHEN
        ResponseBody responseBody = given().accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken("admin"))
                .log().all()
                .when()
                .get("/projects-api/v3/folders/children")
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().response().getBody();

        // THEN
        assertNotNull(responseBody);
        assertTrue(responseBody.print().contains("bimplusProjectConfigDto"));
    }

    @Test
    @DisplayName("Delete Folder.")
    public void deleteProjectFolder() {
        // Arrange

        // Empty Folder
        ProjectFolderDTO emptyFolder = createFolder(new ProjectFolderDTO());


        ProjectFolderDTO subFolderDto = new ProjectFolderDTO();
        subFolderDto.setParentId(emptyFolder.getId());
        ProjectFolderDTO subFolder = createFolder(subFolderDto);



        // Project in Sub Folder
        ProjectDTO projectInSubFolderDto = new ProjectDTO();
        projectInSubFolderDto.setParentId(emptyFolder.getId());
        ProjectDTO project = createProject(projectInSubFolderDto);

        //Delete sub folder (It will move all project and folder of it to it's parent folder)
        MoveDto removeDto = new MoveDto();
        removeDto.setFolders(Collections.singletonList(subFolder.getId()));
        removeDto.setEntities(Collections.singletonList(project.getId()));

         given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken("admin"))
            .body(removeDto)
            .log().all()
            .when()
            .delete("/api/v2/projects/folders")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value());

        ProjectTreeDTO resultedFolderDto = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .log().all()
            .when()
            .get("/api/v2/projectFolders")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value()).extract().as(ProjectTreeDTO.class);


        //
        // Assert
        assertNotNull(resultedFolderDto);

        assertEquals(1,resultedFolderDto.getChildren().size());
        assertEquals(2,resultedFolderDto.getSubFolder().size());
    }

    @Test
    @DisplayName("Project Inside User's Allowed folder.")
    public void checkProjectInsideUsersAllowedfolder() {
        // Arrange

        // Empty Folder
        ProjectFolderDTO folder1 = createFolder(new ProjectFolderDTO());

        // Project in Root Folder
        ProjectDTO projectInRoot = createProject(new ProjectDTO());


        Boolean isInside = given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken("admin"))
            .queryParam("projectId",projectInRoot.getId())
            .log().all()
            .when()
            .get("/api/v2/projectFolders/contains")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().as(Boolean.class);

        // Assert
        assertNotNull(isInside);

        assertEquals(true,isInside);
    }

    @Test
    @DisplayName("Get Project Sync Size")
    public void getProjectSyncSize(){
        // Arrange
        // Act
        SyncSizeDTO s = projectService.getProjectSyncSize(UUID.randomUUID(), null, new PostsFilter[0]);
        // Assert
        assertEquals(s.getSizeInBytes(), 1024);
    }


    @Test
    @DisplayName("My project List paged")
    public void getMyProjectListPages() {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setName("Project 1");
        ProjectDTO project = createProject(projectDTO);
        projectDTO.setName("Project 2");
        ProjectDTO project2 = createProject(projectDTO);
        projectDTO.setName("Project 3");
        ProjectDTO project3 = createProject(projectDTO);
        projectDTO.setName("Project 4");
        ProjectDTO project4 = createProject(projectDTO);
        projectDTO.setName("Project 5");
        ProjectDTO project5 = createProject(projectDTO);


        Mockito.when(membershipsApiClient.listAllMembershipProjectIds(Mockito.any(), Mockito.anyInt(), Mockito.anyInt()))
            .then(invocationOnMock -> {
                Page<UUID> uuidPage = new Page<>();
                uuidPage.setTotalPages(2);
                uuidPage.setTotalElements(5);
                uuidPage.setSize(3);
                uuidPage.setNumber(0);
                uuidPage.setContent(Arrays.asList(project.getId(), project2.getId(), project3.getId()));
                uuidPage.setFirst(true);
                uuidPage.setLast(false);
                return uuidPage;
            });

        Page<ProjectDTO> as = given()
            .queryParam("page", 0)
            .queryParam("size", 3)
            .queryParam("stateFilter", "Active")
            .auth().oauth2(oAuth2Helper.createBearerToken("admin"))
            .log().all()
            .get("/api/v2/projects/paged")
            .then().log().all().extract().as(Page.class);

        assertTrue(as.isFirst());
        assertFalse(as.isLast());
        assertEquals(5,as.getTotalElements());
        assertEquals(3, as.getContent().size());

    }


}
