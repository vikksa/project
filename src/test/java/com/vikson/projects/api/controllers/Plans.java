package com.vikson.projects.api.controllers;

import com.vikson.services.issues.CategoryApiClient;
import com.vikson.services.issues.PinApiClient;
import com.vikson.services.issues.TaskApiClient;
import com.vikson.services.issues.resources.PinCategory;
import com.vikson.services.issues.resources.PinColor;
import com.vikson.services.issues.resources.TaskCount;
import com.vikson.services.users.PersonalApiClient;
import com.vikson.services.users.PrivilegeApiClient;
import com.vikson.services.users.SubscriptionApiClient;
import com.vikson.services.users.UserPrivileges;
import com.vikson.services.users.UsersApiClient;
import com.vikson.services.users.resources.Licence;
import com.vikson.services.users.resources.LicenseType;
import com.vikson.services.users.resources.Privilege;
import com.vikson.services.users.resources.PrivilegeCheck;
import com.vikson.services.users.resources.Subscription;
import com.vikson.services.users.resources.UserProfile;
import com.vikson.services.users.resources.UserSettings;
import com.vikson.storage.FileType;
import com.vikson.storage.StorageAccessKey;
import com.vikson.storage.StorageEngine;
import com.vikson.test.auth2.OAuth2Helper;
import com.vikson.projects.api.resources.MoveDto;
import com.vikson.projects.api.resources.PlanDTO;
import com.vikson.projects.api.resources.PlanFolderDTO;
import com.vikson.projects.api.resources.PlanLevelDTO;
import com.vikson.projects.api.resources.PlanQuickInfo;
import com.vikson.projects.api.resources.PlanRevisionDTO;
import com.vikson.projects.api.resources.PlanTreeDTO;
import com.vikson.projects.api.resources.ProjectDTO;
import com.vikson.projects.api.resources.BimplusPlanReferenceDto;
import com.vikson.projects.api.resources.PlanRevisionDifference;
import com.vikson.projects.api.resources.values.ProjectFilter;
import com.vikson.projects.api.resources.RevisionMetadataDTO;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.model.values.BimplusPlanState;
import com.vikson.projects.model.values.PlanType;
import io.restassured.RestAssured;
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

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"dev"})
@DisplayName("Plans")
public class Plans {

    @LocalServerPort
    private int serverPort;
    @Autowired
    private OAuth2Helper oAuth2Helper;
    @Autowired
    private StorageEngine storageEngine;

    @MockBean
    private PersonalApiClient personalApiClient;
    @MockBean
    private PrivilegeApiClient privilegeApiClient;
    @MockBean
    private UserPrivileges userPrivileges;
    @MockBean
    private CategoryApiClient categoryApiClient;
    @MockBean
    private UsersApiClient usersApiClient;
    @MockBean
    private SubscriptionApiClient subscriptionApiClient;
    @MockBean
    private TaskApiClient taskApiClient;
    @MockBean
    private PinApiClient pinApiClient;

    private UserProfile me;

    @BeforeEach
    public void setup() {
        RestAssured.port = serverPort;

        // Mock other Services
        me = new UserProfile();
        me.setUserId(UUID.randomUUID());
        me.setOrganisationId(UUID.randomUUID());
        UserSettings settings = new UserSettings();
        settings.setAdmin(true);
        settings.setProjectCreator(true);
        me.setSettings(settings);
        Licence licence = new Licence();
        licence.setType(LicenseType.Master);
        licence.setUntil(Instant.now().plusSeconds(1000));
        licence.setPaid(true);
        me.setLicense(licence);
        Mockito.when(personalApiClient.getMe())
                .thenReturn(me);
        Mockito.when(usersApiClient.getUser(me.getUserId()))
                .thenReturn(me);

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
        Mockito.when(userPrivileges.hasAnyPrivileges(Mockito.any(UUID.class),Mockito.anyList()))
                .thenReturn(true);
        PinCategory pinCategory = new PinCategory();
        pinCategory.setCategoryId(UUID.randomUUID());
        pinCategory.setName("CategoryName");
        pinCategory.setColor(PinColor.Blue);
        pinCategory.setActive(true);
        Mockito.when(categoryApiClient.getPinCategories(Mockito.any(UUID.class)))
                .thenReturn(Collections.singletonList(pinCategory));
        Subscription subscription = new Subscription();
        subscription.setType(Subscription.SubscriptionType.Master);
        Mockito.when(subscriptionApiClient.getSubscription()).thenReturn(subscription);
        Mockito.when(taskApiClient.getTaskCountInPlan(Mockito.any(UUID.class))).thenReturn(new TaskCount(0,0));
    }

    @Test
    @DisplayName("Create a new Plan Folder.")
    public void createANewPlanFolder() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO parentFolder = createNewFolder(project.getId(), new PlanFolderDTO());
        assertNotNull(parentFolder);
        // Act
        PlanFolderDTO prototype = new PlanFolderDTO();
        prototype.setId(UUID.randomUUID());
        prototype.setName("My Super Plan Folder");
        prototype.setParentId(parentFolder.getId());
        PlanFolderDTO newFolder = createNewFolder(project.getId(), prototype);
        // Assert
        assertNotNull(newFolder);
        assertEquals(prototype.getId(), newFolder.getId());
        assertEquals("My Super Plan Folder", newFolder.getName());
        assertEquals(parentFolder.getId(), newFolder.getParentId());
    }

    @Test
    @DisplayName("Delete Empty Folder.")
    public void deleteEmptyFolder(){
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO prototype = new PlanFolderDTO();
        prototype.setName("To Delete");
        PlanFolderDTO folder = createNewFolder(project.getId(), prototype);

        // Act
        deleteEmptyFolder(folder.getId());
    }

    @Test
    @DisplayName("Build a plan tree.")
    public void buildPlanTree() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());

        // Folder in Root Folder
        PlanFolderDTO root1 = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanFolderDTO root2 = createNewFolder(project.getId(), new PlanFolderDTO());

        // Sub Folder
        PlanFolderDTO subFolderPrototype = new PlanFolderDTO();
        subFolderPrototype.setParentId(root1.getId());
        PlanFolderDTO subFolder = createNewFolder(project.getId(), subFolderPrototype);

        // Plan in Root Folder
        PlanDTO planInRootPrototype = new PlanDTO();
        PlanDTO planInRoot = createPlanWithMockedRevision(project.getId(), planInRootPrototype);

        // Plan in Root Sub Folder
        PlanDTO planInRootSubFolderPrototype = new PlanDTO();
        planInRootSubFolderPrototype.setParentId(root1.getId());
        PlanDTO planInRootSubFolder = createPlanWithMockedRevision(project.getId(), planInRootSubFolderPrototype);

        // Plan in Sub Sub Folder
        PlanDTO planInSubSubFolderPrototype = new PlanDTO();
        planInSubSubFolderPrototype.setParentId(subFolder.getId());
        PlanDTO planInSubSubFolder = createPlanWithMockedRevision(project.getId(), planInSubSubFolderPrototype);

        // Act
        PlanTreeDTO planTree = getPlanTree(project.getId());

        // Assert
        assertNotNull(planTree);
        assertNotNull(planTree.getProjectName());
        assertNotNull(planTree.getId());
        assertNotNull(planTree.getChildren());
        assertEquals(1, planTree.getChildren().size());
        assertTrue(planTree.getChildren().contains(planInRoot));
        assertNotNull(planTree.getSubFolder());
        assertEquals(2, planTree.getSubFolder().size());
        assertTrue(planTree.getSubFolder().containsAll(Arrays.asList(root1, root2)));
        PlanFolderDTO actualRoot1 = planTree.getSubFolder().stream()
                .filter(f -> f.getId().equals(root1.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(actualRoot1);
        assertNotNull(actualRoot1.getChildren());
        assertEquals(1, actualRoot1.getChildren().size());
        assertTrue(actualRoot1.getChildren().contains(planInRootSubFolder));
        assertNotNull(actualRoot1.getSubFolder());
        assertEquals(1, actualRoot1.getSubFolder().size());
        assertTrue(actualRoot1.getSubFolder().contains(subFolder));
        PlanFolderDTO actualSubFolder = actualRoot1.getSubFolder().stream()
                .filter(f -> f.getId().equals(subFolder.getId()))
                .findAny()
                .orElse(null);
        assertNotNull(actualSubFolder);
        assertNotNull(actualSubFolder.getChildren());
        assertEquals(1, actualSubFolder.getChildren().size());
        assertTrue(actualSubFolder.getChildren().contains(planInSubSubFolder));
    }

    @Test
    @DisplayName("Update a Plan Folder.")
    public void updateAPlanFolder() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO folder = createNewFolder(project.getId(), new PlanFolderDTO());

        PlanFolderDTO newParent = createNewFolder(project.getId(), new PlanFolderDTO());

        PlanFolderDTO updates = new PlanFolderDTO();
        updates.setName("Street Plans");
        updates.setParentId(newParent.getId());

        // Act
        PlanFolderDTO updated = updateFolder(folder.getId(), updates);

        // Assert
        assertNotNull(updated);
        assertEquals("Street Plans", updates.getName());
        assertEquals(newParent.getId(), updated.getParentId());
    }

    @Test
    @DisplayName("Remove a Plan Folder parent.")
    public void removeAPlanFolderParent() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO parent = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanFolderDTO planFolderPrototype = new PlanFolderDTO();
        planFolderPrototype.setParentId(parent.getId());
        PlanFolderDTO folder = createNewFolder(project.getId(), planFolderPrototype);

        // Act
        PlanFolderDTO parentRemoved = removeParentFromPlanFolder(folder.getId());

        // Assert
        assertNotEquals(parent.getId(), parentRemoved.getParentId());
    }

    @Test
    @DisplayName("Create new Plan.")
    public void createANewPlan() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO parent = createNewFolder(project.getId(), new PlanFolderDTO());

        PlanDTO prototype = new PlanDTO();
        prototype.setParentId(parent.getId());
        prototype.setId(UUID.randomUUID());
        prototype.setName("Layout Floor 1");

        PlanRevisionDTO revision = new PlanRevisionDTO();
        revision.setId(UUID.randomUUID());
        revision.setVersion("A001");
        revision.setCreated(ZonedDateTime.of(LocalDate.of(2016, 4, 15), LocalTime.of(12, 30), ZoneId.of("UTC")).toInstant());

        PlanLevelDTO level1 = new PlanLevelDTO();
        level1.setTilesX(1);
        level1.setTilesY(1);
        PlanLevelDTO level2 = new PlanLevelDTO();
        level2.setTilesX(2);
        level2.setTilesY(2);
        revision.setLevel(Arrays.asList(level1, level2));

        prototype.setRevision(revision);

        RevisionMetadataDTO metadata = new RevisionMetadataDTO();
        metadata.setFileName("layout-floor-1-a001.jpg");
        metadata.setContentType("image/jpeg");
        metadata.setPlanType(PlanType.File);
        metadata.setImageWidth(512);
        metadata.setImageHeight(512);
        metadata.setArchiveSize(512L);
        prototype.setMetadata(metadata);

        // Act
        PlanDTO plan = createPlan(project.getId(), prototype);

        // Assert
        assertNotNull(plan);
        assertEquals(prototype.getId(), plan.getId());
        assertEquals(prototype.getName(), plan.getName());
        assertNotNull(plan.getCurrentRevision());
        PlanRevisionDTO actualRevision = plan.getCurrentRevision();
        assertEquals(revision.getId(), actualRevision.getId());
        assertEquals(revision.getVersion(), actualRevision.getVersion());
        assertEquals(revision.getCreated(), actualRevision.getCreated());
        assertNotNull(actualRevision.getLevel());
        assertEquals(revision.getLevel().size(), actualRevision.getLevel().size());
        PlanLevelDTO actualLevel1 = actualRevision.getLevel().stream()
                .filter(l -> l.getTilesX() == 1 && l.getTilesY() == 1)
                .findAny()
                .orElse(null);
        assertNotNull(actualLevel1);
        PlanLevelDTO actualLevel2 = actualRevision.getLevel().stream()
                .filter(l -> l.getTilesX() == 2 && l.getTilesY() == 2)
                .findAny()
                .orElse(null);
        assertNotNull(actualLevel2);
        Assertions.assertEquals(metadata, plan.getMetadata());
    }

    @Test
    @DisplayName("Create a bimplus linked plan")
    public void createPlanLinkedToBimplus() {
        //GIVEN
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO planDTO = new PlanDTO();
        BimplusPlanReferenceDto bimplusPlanReferenceDto = new BimplusPlanReferenceDto()
                .withId(UUID.randomUUID())
                .withName("Bimplus attachment plan");
        planDTO.setBimplusPlanReferenceDto(bimplusPlanReferenceDto);

        //WHEN
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), planDTO);

        //THEN
        org.assertj.core.api.Assertions.assertThat(plan.getBimplusPlanReferenceDto().getState()).isEqualTo(BimplusPlanState.LINKED);
    }

    @Test
    @DisplayName("Update state of a bimplus linked plan")
    public void updateBimplusIntegrationState() {
        //GIVEN
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO planDTO = new PlanDTO();
        UUID bimplusAttachmentId = UUID.randomUUID();
        BimplusPlanReferenceDto bimplusPlanReferenceDto = new BimplusPlanReferenceDto()
                .withId(bimplusAttachmentId)
                .withName("Bimplus attachment Plan")
                .withState(BimplusPlanState.LINKED);
        planDTO.setBimplusPlanReferenceDto(bimplusPlanReferenceDto);

        //WHEN
        PlanDTO createdPlan = createPlanWithMockedRevision(project.getId(), planDTO);

        //AND
        PlanDTO planDtoUpdate = new PlanDTO();
        BimplusPlanReferenceDto bimplusPlanReferenceUpdatedDto = new BimplusPlanReferenceDto().withId(bimplusAttachmentId).withState(BimplusPlanState.UNLINKED);
        planDtoUpdate.setBimplusPlanReferenceDto(bimplusPlanReferenceUpdatedDto);
        PlanDTO updatedPlan = updatePlan(createdPlan.getId(), planDtoUpdate);

        //THEN
        org.assertj.core.api.Assertions.assertThat(createdPlan.getBimplusPlanReferenceDto().getState()).isEqualTo(BimplusPlanState.ERROR); //Since revision is not an integer the state will be saved as ERROR
        org.assertj.core.api.Assertions.assertThat(updatedPlan.getBimplusPlanReferenceDto().getState()).isEqualTo(BimplusPlanState.LINKED);
    }

    @Test
    @DisplayName("Update a plan.")
    public void updateAPlan() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());

        PlanRevisionDTO revision = new PlanRevisionDTO();
        revision.setId(UUID.randomUUID());
        revision.setVersion("A002");

        PlanLevelDTO level1 = new PlanLevelDTO();
        level1.setTilesX(1);
        level1.setTilesY(1);
        PlanLevelDTO level2 = new PlanLevelDTO();
        level2.setTilesX(2);
        level2.setTilesY(2);
        revision.setLevel(Arrays.asList(level1, level2));

        RevisionMetadataDTO metadataDTO = new RevisionMetadataDTO();
        metadataDTO.setFileName("my-plan-a002.jpg");
        metadataDTO.setContentType("image/jpeg");
        metadataDTO.setImageWidth(256);
        metadataDTO.setImageHeight(256);
        metadataDTO.setArchiveSize(256L);
        revision.setMetadata(metadataDTO);

        PlanRevisionDTO newRevision = addNewPlanRevision(plan.getId(), revision).getCurrentRevision();

        PlanDTO update = new PlanDTO();
        update.setActive(false);
        update.setName("New Plan Name");
        update.setCurrentRevisionId(plan.getCurrentRevision().getId());

        // Act
        PlanDTO updatedPlan = updatePlan(plan.getId(), update);

        // Assert
        assertNotNull(updatedPlan);
        assertEquals(plan.getCurrentRevision().getId(), updatedPlan.getCurrentRevision().getId());
        assertEquals("New Plan Name", updatedPlan.getName());
        assertFalse(updatedPlan.getActive());
    }

    @Test
    @DisplayName("Retrieve a Plan.")
    public void retrieveAPlan() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO expected = createPlanWithMockedRevision(project.getId(), new PlanDTO());
        // Act
        PlanDTO actual = getPlan(expected.getId());
        // Assert
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Move Plan to other Folder.")
    public void movePlanToOtherFolder() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO oldParent = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanFolderDTO newParent = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanDTO prototype = new PlanDTO();
        project.setParentId(oldParent.getId());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), prototype);
        // Act
        PlanDTO movedPlan = movePlan(plan.getId(), newParent.getId());
        // Assert
        assertNotNull(movedPlan);
        assertEquals(newParent.getId(), movedPlan.getParentId());
    }

    @Test
    @DisplayName("Remove Plans Parent Folder.")
    public void removePlansParentFolder() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO oldParent = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanDTO prototype = new PlanDTO();
        project.setParentId(oldParent.getId());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), prototype);
        // Act
        PlanDTO movedPlan = removePlansParentFolder(plan.getId());
        // Assert
        assertNotNull(movedPlan);
        assertNotEquals(oldParent.getId(), movedPlan.getParentId());
    }

    @Test
    @DisplayName("Add new Revision.")
    public void addNewRevision() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());
        PlanRevisionDTO revision = new PlanRevisionDTO();
        revision.setId(UUID.randomUUID());
        revision.setVersion("A002");
        revision.setCreated(ZonedDateTime.of(LocalDate.of(2017, 04, 29), LocalTime.of(18, 00), ZoneId.of("UTC")).toInstant());

        PlanLevelDTO level1 = new PlanLevelDTO();
        level1.setTilesX(1);
        level1.setTilesY(1);
        PlanLevelDTO level2 = new PlanLevelDTO();
        level2.setTilesX(2);
        level2.setTilesY(2);
        revision.setLevel(Arrays.asList(level1, level2));

        RevisionMetadataDTO metadataDTO = new RevisionMetadataDTO();
        metadataDTO.setFileName("my-plan-a002.jpg");
        metadataDTO.setContentType("image/jpeg");
        metadataDTO.setImageWidth(256);
        metadataDTO.setImageHeight(256);
        metadataDTO.setArchiveSize(256L);
        metadataDTO.setPage(2);
        revision.setMetadata(metadataDTO);

        double factor = 1.554387;
        int moveX = 100;
        int moveY = 150;
        revision.setRevisionLocationDifference(new PlanRevisionDifference().withFactor(factor).withMoveX(moveX).withMoveY(moveY));

        // Act
        PlanRevisionDTO newRevision = addNewPlanRevision(plan.getId(), revision).getCurrentRevision();

        // Assert
        assertNotEquals(revision, newRevision);
        assert newRevision.getRevisionLocationDifference().getMoveX() == moveX;
        assert newRevision.getRevisionLocationDifference().getMoveY() == moveY;
        assert newRevision.getRevisionLocationDifference().getFactor() == factor;
    }

    @Test
    @DisplayName("Get revision from Plan.")
    public void getPlanRevision() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());
        PlanRevisionDTO revision = new PlanRevisionDTO();
        revision.setId(UUID.randomUUID());
        revision.setVersion("A002");
        revision.setCreated(ZonedDateTime.of(LocalDate.of(2017, 04, 29), LocalTime.of(18, 00), ZoneId.of("UTC")).toInstant());

        PlanLevelDTO level1 = new PlanLevelDTO();
        level1.setTilesX(1);
        level1.setTilesY(1);
        PlanLevelDTO level2 = new PlanLevelDTO();
        level2.setTilesX(2);
        level2.setTilesY(2);
        revision.setLevel(Arrays.asList(level1, level2));

        RevisionMetadataDTO metadataDTO = new RevisionMetadataDTO();
        metadataDTO.setFileName("my-plan-a002.jpg");
        metadataDTO.setContentType("image/jpeg");
        metadataDTO.setImageWidth(256);
        metadataDTO.setImageHeight(256);
        metadataDTO.setArchiveSize(256L);
        revision.setMetadata(metadataDTO);

        PlanRevisionDTO newRevision = addNewPlanRevision(plan.getId(), revision).getCurrentRevision();

        // Act
        PlanRevisionDTO actualOldRevision = getRevision(plan.getId(), plan.getCurrentRevision().getId());
        PlanRevisionDTO actualNewRevision = getRevision(plan.getId(), newRevision.getId());

        // Assert
        assertEquals(plan.getCurrentRevision(), actualOldRevision);
        assertEquals(newRevision, actualNewRevision);
    }

    @Test
    @DisplayName("Download Thumbnail for Plan.")
    public void getThumbnail() throws IOException {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());

        // Upload A Thumbnail
        StorageAccessKey key = new StorageAccessKey(FileType.PlanThumbnail, String.format("%s/%s/%s/thumbnail.jpg",
                project.getId(), plan.getId(), plan.getCurrentRevision().getId()));
        ClassPathResource aThumbnail = new ClassPathResource("logo.jpg");
        try(InputStream is = aThumbnail.getInputStream()) {
            storageEngine.save(key, is);
        }

        // Act
        String actualMd5;
        try(InputStream is = downloadThumbnail(plan.getId())) {
            actualMd5 = DigestUtils.md5DigestAsHex(is);
        }
        // Assert
        try(InputStream is = aThumbnail.getInputStream()) {
            assertEquals(DigestUtils.md5DigestAsHex(is), actualMd5);
        }
    }

    @Test
    @DisplayName("Download Plan Tile.")
    public void downloadPlanTile() throws IOException {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());

        // Upload A Thumbnail
        StorageAccessKey key = new StorageAccessKey(FileType.PlanTile, String.format("%s/%s/%s/%d/%d_%d.jpg",
                project.getId(), plan.getId(), plan.getCurrentRevision().getId(), 1, 1, 1));
        ClassPathResource aTile = new ClassPathResource("logo.jpg");
        try(InputStream is = aTile.getInputStream()) {
            storageEngine.save(key, is);
        }

        // Act
        String actualMd5;
        try(InputStream is = downloadTile(plan.getCurrentRevision().getId(), 1, 1, 1)) {
            actualMd5 = DigestUtils.md5DigestAsHex(is);
        }

        // Assert
        try(InputStream is = aTile.getInputStream()) {
            assertEquals(DigestUtils.md5DigestAsHex(is), actualMd5);
        }
    }

    @Test
    @DisplayName("Download a Plan as Archive.")
    public void downloadAPlanArchive() throws IOException {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());

        // Upload A Thumbnail
        StorageAccessKey key = new StorageAccessKey(FileType.PlanTile, String.format("%s/%s/%s/revision.tar",
                project.getId(), plan.getId(), plan.getCurrentRevision().getId()));
        ClassPathResource aArchive = new ClassPathResource("archive.tar");
        try(InputStream is = aArchive.getInputStream()) {
            storageEngine.save(key, is);
        }

        // Act
        String actualMd5;
        try(InputStream is = downloadRevision(plan.getCurrentRevision().getId())) {
            actualMd5 = DigestUtils.md5DigestAsHex(is);
        }

        // Assert
        try(InputStream is = aArchive.getInputStream()) {
            assertEquals(DigestUtils.md5DigestAsHex(is), actualMd5);
        }
    }

    @Test
    @DisplayName("Search plans.")
    public void searchPlans() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        List<PlanDTO> matchingPlans = new ArrayList<>(5);
        for(int i = 0; i < 5; i++) {
            PlanDTO plan = new PlanDTO();
            plan.setName("Hello " + (i + 1));
            matchingPlans.add(createPlanWithMockedRevision(project.getId(), plan));
        }
        for(int i = 0; i < 5; i++) {
            PlanDTO plan = new PlanDTO();
            plan.setName("Bye " + (i + 1));
            createPlanWithMockedRevision(project.getId(), plan);
        }
        // Act
        PlanDTO[] results = searchPlans(project.getId(), "ello");
        // Assert
        assertNotNull(results);
        assertEquals(4, results.length);
        assertTrue(Arrays.stream(results).allMatch(matchingPlans::contains));
    }

    @Test
    @DisplayName("Update Last Activity")
    public void updateLastActivity(){
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());
        PlanDTO updateData = new PlanDTO();
        Instant lastActivity = Instant.now().plusSeconds(60L);
        updateData.setLastActivity(lastActivity);
        // Act
        PlanDTO updatedPlan = updatePlan(plan.getId(), updateData);
        // Assert
        ProjectDTO updatedProject = getProject(project.getId());
        assertEquals(updatedPlan.getLastActivity(), lastActivity);
        assertEquals(updatedProject.getLastActivity(), lastActivity);
    }

    private PlanFolderDTO createNewFolder(UUID projectId, PlanFolderDTO prototype) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(prototype)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .post("/api/v2/projects/{projectId}/planFolders", projectId)
        .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(PlanFolderDTO.class);
    }

    private PlanFolderDTO updateFolder(UUID folderId, PlanFolderDTO updates) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(updates)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .patch("/api/v2/planFolders/{folderId}", folderId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanFolderDTO.class);
    }

    private PlanFolderDTO removeParentFromPlanFolder(UUID folderId) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .delete("/api/v2/planFolders/{folderId}/parent", folderId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanFolderDTO.class);
    }

    private PlanTreeDTO getPlanTree(UUID projectId) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/projects/{projectId}/plans", projectId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanTreeDTO.class);
    }

    private PlanDTO createPlanWithMockedRevision(UUID projectId, PlanDTO prototype) {
        PlanRevisionDTO revisionPrototype = new PlanRevisionDTO();
        revisionPrototype.setS3FolderId(UUID.randomUUID().toString());
        PlanLevelDTO levelDTO = new PlanLevelDTO();
        levelDTO.setTilesX(1);
        levelDTO.setTilesY(1);
        revisionPrototype.setLevel(Collections.singletonList(levelDTO));
        prototype.setRevision(revisionPrototype);
        RevisionMetadataDTO metadataDTO = new RevisionMetadataDTO();
        metadataDTO.setFileName("my-plan.jpg");
        metadataDTO.setContentType("image/jpeg");
        metadataDTO.setImageWidth(256);
        metadataDTO.setImageHeight(256);
        metadataDTO.setArchiveSize(256L);
        prototype.setMetadata(metadataDTO);
        prototype.setPlanType(PlanType.File);
        return createPlan(projectId, prototype);
    }

    private PlanDTO createPlan(UUID projectId, PlanDTO prototype) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(prototype)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .post("/api/v2/projects/{projectId}/plans", projectId)
        .then()
                .log().all()
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(PlanDTO.class);
    }

    private PlanDTO getPlan(UUID planId) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/plans/{planId}", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanDTO.class);
    }

    private PlanDTO updatePlan(UUID planId, PlanDTO update) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(update)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .patch("/api/v2/plans/{planId}", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanDTO.class);
    }

    private PlanDTO movePlan(UUID planId, UUID parentId) {
        return given()
                .param("newFolderId", parentId)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .put("/api/v2/plans/{planId}/parent", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanDTO.class);
    }

    private PlanDTO removePlansParentFolder(UUID planId) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .delete("/api/v2/plans/{planId}/parent", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanDTO.class);
    }

    private PlanDTO addNewPlanRevision(UUID planId, PlanRevisionDTO prototype) {
        return given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(prototype)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .put("/api/v2/plans/{planId}", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanDTO.class);
    }

    private PlanRevisionDTO getRevision(UUID planId, UUID revisionId) {
        return given()
                .queryParam("revisionId", revisionId)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/plans/{planId}/revision", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanRevisionDTO.class);
    }

    private InputStream downloadThumbnail(UUID planId) {
        return given()
                .accept("image/jpeg")
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/plans/{planId}/thumbnail.jpg", planId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().asInputStream();
    }

    private InputStream downloadTile(UUID revisionId, int level, int y, int x) {
        return given()
                .accept("image/jpeg")
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/plans/revisions/{revisionId}/{level}/{x}_{y}.jpg", revisionId, level, y, x)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().asInputStream();
    }

    private InputStream downloadRevision(UUID revisionId) {
        return given()
                .accept("application/x-tar")
                .auth().oauth2(oAuth2Helper.createBearerToken())
                .log().all()
        .when()
                .get("/api/v2/planRevisions/{revisionId}/revision.tar", revisionId)
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().asInputStream();
    }

    private PlanDTO[] searchPlans(UUID projectId, String search) {
        return given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("in", ProjectFilter.MyProjects)
                .queryParam("search", search)
                .queryParam("size", 4)
                .queryParam("page", 0)
                .auth().oauth2(oAuth2Helper.createBearerToken())
        .when()
                .get("/api/v2/plans/search")
        .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract().as(PlanDTO[].class);
    }

    private ProjectDTO createProject(ProjectDTO prototype) {
        prototype.setName(UUID.randomUUID().toString());
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
        return getProject(projectId);
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


    @Test
    @DisplayName("Build a plan tree with filters.")
    public void buildPlanTreeWithFilters() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());

        // Folder in Root Folder
        PlanFolderDTO rootFolder1 = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanFolderDTO updates = new PlanFolderDTO();
        updates.setActive(false);
        PlanFolderDTO root1 =updateFolder(rootFolder1.getId(), updates);
        PlanFolderDTO root2 = createNewFolder(project.getId(), new PlanFolderDTO());
        PlanFolderDTO updates2= new PlanFolderDTO();
        updates2.setActive(false);
        root2 = updateFolder(root2.getId(),updates2);

            // Sub Folder
        PlanFolderDTO subFolderPrototype = new PlanFolderDTO();
        subFolderPrototype.setParentId(root1.getId());
        PlanFolderDTO subFolderPrototypeToUpdate = createNewFolder(project.getId(), subFolderPrototype);
        PlanFolderDTO updateSubFolderDto = new PlanFolderDTO();
        updateSubFolderDto.setActive(false);
        PlanFolderDTO subFolder =updateFolder(subFolderPrototypeToUpdate.getId(),updateSubFolderDto);

        // Plan in Root Folder
        PlanDTO planInRootPrototype = new PlanDTO();
        PlanDTO planInRoot = createPlanWithMockedRevision(project.getId(), planInRootPrototype);

        // Plan in Root Sub Folder
        PlanDTO planInRootSubFolderPrototype = new PlanDTO();
        planInRootSubFolderPrototype.setParentId(root1.getId());
        PlanDTO planInRootSubFolder = createPlanWithMockedRevision(project.getId(), planInRootSubFolderPrototype);
        planInRootSubFolder.setActive(false);
        updatePlan(planInRootSubFolder.getId(), planInRootSubFolder);

        // Plan in Sub Sub Folder
        PlanDTO planInSubSubFolderPrototype = new PlanDTO();
        planInSubSubFolderPrototype.setParentId(subFolder.getId());
        PlanDTO planInSubSubFolder = createPlanWithMockedRevision(project.getId(), planInSubSubFolderPrototype);

        // Act
        PlanTreeDTO planTree = getPlanTreeWithFilter(project.getId());

        // Assert
        assertNotNull(planTree);
        assertNotNull(planTree.getProjectName());
        assertNotNull(planTree.getId());
        assertNotNull(planTree.getSubFolder());
        assertEquals(2, planTree.getSubFolder().size());
        assertTrue(planTree.getSubFolder().containsAll(Arrays.asList(root1, root2)));
        PlanFolderDTO actualRoot1 = planTree.getSubFolder().stream()
            .filter(f -> f.getId().equals(root1.getId()))
            .findFirst()
            .orElse(null);
        assertNotNull(actualRoot1);
        assertNotNull(actualRoot1.getChildren());
        assertEquals(1, actualRoot1.getChildren().size());
        assertTrue(actualRoot1.getChildren().contains(planInRootSubFolder));
        assertNotNull(actualRoot1.getSubFolder());
        assertEquals(1, actualRoot1.getSubFolder().size());
        assertTrue(actualRoot1.getSubFolder().contains(subFolder));
        PlanFolderDTO actualSubFolder = actualRoot1.getSubFolder().stream()
            .filter(f -> f.getId().equals(subFolder.getId()))
            .findAny()
            .orElse(null);
        assertEquals(1, actualSubFolder.getChildren().size());
    }


    private PlanTreeDTO getPlanTreeWithFilter(UUID projectId) {
        return given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .queryParam("stateFilter", StateFilter.Inactive)
            .log().all()
            .when()
            .get("/api/v2/projects/{projectId}/plans", projectId)
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().as(PlanTreeDTO.class);
    }

    private void deleteEmptyFolder(UUID folderId) {
        given()
                .auth().oauth2(oAuth2Helper.createBearerToken("PROJECT_CREATOR"))
                .log().all()
                .when()
                .delete("/api/v2/planFolders/{folderId}", folderId)
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("Move plan and folder to another folder.")
    public void movePlanAndFolders() {

        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());

        // Folder in Root Folder
        PlanFolderDTO root1 = createNewFolder(project.getId(), new PlanFolderDTO());

        PlanFolderDTO root2 = createNewFolder(project.getId(), new PlanFolderDTO());

        // Plan in Root Folder
        PlanDTO planInRootPrototype = new PlanDTO();
        PlanDTO planInRoot = createPlanWithMockedRevision(project.getId(), planInRootPrototype);

        //Move planInRoot and root2 to root1
        MoveDto moveDto = new MoveDto();
        moveDto.setFolders(Collections.singletonList(root2.getId()));
        moveDto.setEntities(Collections.singletonList(planInRoot.getId()));

        PlanFolderDTO newParent = given()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .body(moveDto)
            .log().all()
            .when()
            .post("/api/v2/plans/folders/{parentId}", root1.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().as(PlanFolderDTO.class);

        // Assert
        assertNotNull(newParent);

        assertEquals(1, newParent.getChildren().size());
        assertEquals(1, newParent.getSubFolder().size());
    }

    @Test
    @DisplayName("Delete Folder.")
    public void deletePlanFolder() {
        // Arrange

        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());

        // Folder in Root Folder
        PlanFolderDTO folder = createNewFolder(project.getId(), new PlanFolderDTO());

        //Sub Folder in folder
        PlanFolderDTO subPlanFolder = new PlanFolderDTO();
        subPlanFolder.setParentId(folder.getId());
        PlanFolderDTO subFolder = createNewFolder(project.getId(), subPlanFolder);


        //Plan in subFolder
        PlanDTO planInRootPrototype = new PlanDTO();
        planInRootPrototype.setParentId(folder.getId());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), planInRootPrototype);

        //Delete subFolder, it should move subSubFolder and  planInSubFolder to  folder
        MoveDto removeDto = new MoveDto();
        removeDto.setFolders(Collections.singletonList(subFolder.getId()));
        removeDto.setEntities(Collections.singletonList(plan.getId()));
        removeDto.setProjectId(project.getId());

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .body(removeDto)
            .log().all()
            .when()
            .delete("/api/v2/plans/folders")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value());

        PlanTreeDTO resultedFolderDto = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .log().all()
            .when()
            .get("/api/v2/projects/{projectId}/plans", project.getId())
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value()).extract().as(PlanTreeDTO.class);


        // Assert
        assertNotNull(resultedFolderDto);

        assertEquals(1,resultedFolderDto.getChildren().size());
        assertEquals(2,resultedFolderDto.getSubFolder().size());
    }


    @Test
    @DisplayName("Update plan in bulk.")
    public void bulkUpdatePlan() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanDTO plan = createPlanWithMockedRevision(project.getId(), new PlanDTO());
        PlanDTO plan2 = createPlanWithMockedRevision(project.getId(), new PlanDTO());
        PlanDTO plan3 = createPlanWithMockedRevision(project.getId(), new PlanDTO());

        List<PlanDTO> planDTOS = Stream.of(plan, plan2, plan3)
            .map(planDTO -> {
                PlanDTO dto = new PlanDTO();
                dto.setId(planDTO.getId());
                dto.setActive(false);
                return dto;
            }).collect(Collectors.toList());

        // Act
        PlanDTO[] dtos = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(planDTOS)
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .log().all()
            .when()
            .patch("/plans-api/v3")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract().as(PlanDTO[].class);


        // Assert
        assertNotNull(dtos);
        assertEquals(3,dtos.length);
        assertFalse(dtos[0].getActive());
        assertFalse(dtos[1].getActive());
        assertFalse(dtos[2].getActive());
    }

    @Test
    @DisplayName("Plan Quick Info By Project Id.")
    public void getPlanQuickInfoByProjectId() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        for (int i = 0; i < 5; i++) {
            PlanDTO plan = new PlanDTO();
            plan.setName("Plans " + (i + 1));
            createPlanWithMockedRevision(project.getId(), plan);
        }
        PlanDTO plan = new PlanDTO();
        plan.setName("Plans ");
        PlanDTO planDTO = createPlanWithMockedRevision(project.getId(), plan);
        plan.setActive(false);
        updatePlan(planDTO.getId(), plan);


        //By projectId
        PlanQuickInfo[] planQuickInfos = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .queryParam("projectId", project.getId())
            .log().all()
            .when()
            .get("/api/v2/plans/quickInfo")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .body("number", is(0))
            .body("size", is(10))
            .body("totalElements", is(5))
            .body("first", is(true))
            .body("last", is(true))
            .extract().jsonPath().getObject("content", PlanQuickInfo[].class);

        assertEquals(5, planQuickInfos.length);
    }
    @Test
    @DisplayName("Plan Quick Info By PlanIds.")
    public void getPlanQuickInfoByPlanIds() {
        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        List<PlanDTO> matchingPlans = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            PlanDTO plan = new PlanDTO();
            plan.setName("Plans " + (i + 1));
            matchingPlans.add(createPlanWithMockedRevision(project.getId(), plan));
        }
        PlanDTO plan = new PlanDTO();
        plan.setName("Plans ");
        PlanDTO planDTO = createPlanWithMockedRevision(project.getId(), plan);
        plan.setActive(false);
        updatePlan(planDTO.getId(), plan);

        //By planIds
        PlanQuickInfo[] planQuickInfos = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .queryParam("planIds", matchingPlans.stream().map(PlanDTO::getId).collect(Collectors.toList()))
            .log().all()
            .when()
            .get("/api/v2/plans/quickInfo")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .body("number", is(0))
            .body("size", is(10))
            .body("totalElements", is(5))
            .body("first", is(true))
            .body("last", is(true))
            .extract().jsonPath().getObject("content", PlanQuickInfo[].class);

        assertEquals(5, planQuickInfos.length);
    }


    @Test
    @DisplayName("Plan Quick Info By Folder Id.")
    public void getPlanQuickInfoByFolderId() {

        // Arrange
        ProjectDTO project = createProject(new ProjectDTO());
        PlanFolderDTO parentFolder = createNewFolder(project.getId(), new PlanFolderDTO());
        assertNotNull(parentFolder);
        // Act
        PlanFolderDTO subFolder = new PlanFolderDTO();
        subFolder.setId(UUID.randomUUID());
        subFolder.setName("My Super Plan Folder");
        subFolder.setParentId(parentFolder.getId());
        PlanFolderDTO newFolder = createNewFolder(project.getId(), subFolder);

        PlanFolderDTO subSubFolder = new PlanFolderDTO();
        subSubFolder.setId(UUID.randomUUID());
        subSubFolder.setName("My Super Plan Folder");
        subSubFolder.setParentId(subFolder.getId());
        PlanFolderDTO subSubFolderRes = createNewFolder(project.getId(), subSubFolder);

        PlanDTO plan = new PlanDTO();
        plan.setName("Plans ");
        createPlanWithMockedRevision(project.getId(), plan);

        PlanDTO plan2 = new PlanDTO();
        plan2.setName("Plans ");
        plan2.setParentId(subSubFolderRes.getId());
        PlanDTO planDTO = createPlanWithMockedRevision(project.getId(), plan2);


        //Get By FolderId
        PlanQuickInfo[] planQuickInfos = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .auth().oauth2(oAuth2Helper.createBearerToken())
            .queryParam("folderId", subSubFolderRes.getId())
            .log().all()
            .when()
            .get("/api/v2/plans/quickInfo")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .body("number", is(0))
            .body("size", is(10))
            .body("totalElements", is(1))
            .body("first", is(true))
            .body("last", is(true))
            .extract().jsonPath().getObject("content", PlanQuickInfo[].class);


        PlanQuickInfo planQuickInfo = planQuickInfos[0];

        assertEquals(planDTO.getId(), planQuickInfo.getPlanId());
        assertEquals(planDTO.getProjectId(), planQuickInfo.getProjectId());
        assertEquals(4, planQuickInfo.getParentFolders().size());
        assertEquals(subSubFolderRes.getId(), planQuickInfo.getParentFolders().get(0));
        assertEquals(subSubFolderRes.getParentId(), planQuickInfo.getParentFolders().get(1));
        assertEquals(parentFolder.getId(), planQuickInfo.getParentFolders().get(2));
        assertEquals(parentFolder.getParentId(), planQuickInfo.getParentFolders().get(3));
    }

}
