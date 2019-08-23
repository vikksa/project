package com.vikson.projects.api.controllers;

import com.vikson.projects.api.resources.MoveDto;
import com.vikson.projects.api.resources.PlanDTO;
import com.vikson.projects.api.resources.PlanFolderDTO;
import com.vikson.projects.api.resources.PlanQuickInfo;
import com.vikson.projects.api.resources.PlanRevisionDTO;
import com.vikson.projects.api.resources.PlanTreeDTO;
import com.vikson.projects.api.resources.values.PlanExpandAttribute;
import com.vikson.projects.api.resources.values.ProjectFilter;
import com.vikson.projects.api.resources.values.StateFilter;
import com.vikson.projects.util.DateTimeUtils;
import com.vikson.projects.model.Plan;
import com.vikson.projects.service.PlanService;
import com.vikson.projects.service.translators.LevelSquarifier;
import com.vikson.projects.service.translators.PlanTranslator;
import com.vikson.services.issues.resources.PostsFilter;
import com.vikson.services.issues.resources.SyncSizeDTO;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.vikson.projects.exceptions.ApiExceptionHelper.newInternalServerError;

@RestController
@RequestMapping(path = "/api/v2")
public class PlanController {

    private static final Logger log = LoggerFactory.getLogger(PlanController.class);

    @Autowired
    private PlanService planService;
    @Autowired
    private PlanTranslator planTranslator;

    @ApiOperation(value = "Create new Plan Folder", notes = "Required Attributes: none (But body should not be null)")
    @PostMapping(path = "/projects/{projectId}/planFolders", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PlanFolderDTO createNewFolder(@PathVariable UUID projectId, @RequestBody PlanFolderDTO body) {
        log.debug("POST /projects/{}/planFolders, Body: {}", projectId, body);
        return planTranslator.translate(planService.createFolder(projectId, body));
    }

    @ApiOperation(value = "Get Plan Tree")
    @GetMapping(path = "/projects/{projectId}/plans")
    public PlanTreeDTO getPlanTree(@PathVariable UUID projectId,
                                   @RequestParam(defaultValue = "") String search,
                                   @RequestParam(defaultValue = "Active") StateFilter stateFilter,
                                   @RequestParam(defaultValue = "false") boolean forAndroid,
                                   @RequestParam(required = false) String since,
                                   @RequestParam(required = false, defaultValue = "")PlanExpandAttribute[] expand) {
        String expandString = Arrays.stream(expand).map(Objects::toString).collect(Collectors.joining(","));
        log.debug("GET /projects/{}/plans?search={}&stateFilter={}&since={}&expand={}",
                projectId, search, stateFilter, DateTimeUtils.safelyParse(since),
                expandString);
        PlanTreeDTO planTree = planService.getPlanTree(projectId, search.toLowerCase(), stateFilter, DateTimeUtils.safelyParse(since), expand);
        if(forAndroid)
           return LevelSquarifier.translate(planTree);
        return planTree;
    }

    @ApiOperation(value = "Get Plan Folder")
    @GetMapping(path = "/planFolders/{folderId}")
    public PlanFolderDTO getPlanFolder(@PathVariable UUID folderId) {
        log.debug("GET /planFolders/{}", folderId);
        return planTranslator.translate(planService.getPlanFolder(folderId));
    }

    @ApiOperation(value = "Update Plan Folder", notes = "Required Attributes: none (But body should not be null)")
    @PatchMapping(path = "/planFolders/{folderId}")
    public PlanFolderDTO updateFolder(@PathVariable UUID folderId, @RequestBody PlanFolderDTO body) {
        log.debug("PATCH /planFolders/{}, Body: {}", folderId, body);
        return planTranslator.translate(planService.updateFolder(folderId, body));
    }

    @ApiOperation(value = "Move folder to root")
    @DeleteMapping(path = "/planFolders/{folderId}/parent")
    public PlanFolderDTO removeParentFromFolder(@PathVariable UUID folderId) {
        log.debug("DELETE /planFolders/{}/parent", folderId);
        return planTranslator.translate(planService.removeParentFromFolder(folderId));
    }

    @ApiOperation(value = "Delete empty folder")
    @DeleteMapping(path = "/planFolders/{folderId}")
    public void deleteEmptyFolder(@PathVariable UUID folderId){
        log.debug("DELETE /planFolders/{}", folderId);
        planService.deleteFolder(folderId);
    }

    @ApiOperation(value = "Create a plan", notes = "Required Attributes: none (But body should not be null)")
    @PostMapping(path = "/projects/{projectId}/plans")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanDTO createPlan(@PathVariable UUID projectId, @RequestBody PlanDTO body) {
        log.debug("POST /projects/{}/plans, Body: {}", projectId, body);
        return planTranslator.translate(planService.createPlan(projectId, body));
    }


    @ApiOperation(value = "Unlink plan from bimplus")
    @DeleteMapping(path = "/plans/bimplus/{planId}/unlink")
    public PlanDTO unlink(@PathVariable UUID planId) {
        return planTranslator.translate(planService.unlinkBimplus(planId));
    }

    @ApiOperation(value = "Get Plan")
    @GetMapping(path = "/plans/{planId}")
    public PlanDTO getPlan(@PathVariable UUID planId,
                           @RequestParam(required = false, defaultValue = "false") boolean forAndroid,
                           @RequestParam(required = false, defaultValue = "Revisions")PlanExpandAttribute[] expand) {
        log.debug("GET /plans/{}", planId);
        PlanDTO planDTO = planTranslator.translate(planService.getPlan(planId), expand);
        if(forAndroid){
            return LevelSquarifier.translate(planDTO);
        }
        return planDTO;
    }

    @ApiOperation(value = "Get Plan by Revesion Id")
    @GetMapping(path = "/plans/revision/{revisionId}")
    public PlanDTO getPlanByRevisionId(@PathVariable UUID revisionId) {
        log.debug("GET /plans/revision/{}", revisionId);
        return planTranslator.translate(planService.getPlanByRevisionId(revisionId), new PlanExpandAttribute[]{PlanExpandAttribute.CurrentRevision});
    }

    @ApiOperation(value = "Get Plan Revision")
    @GetMapping(path = "/plans/{planId}/revision")
    public PlanRevisionDTO getPlanRevision(@PathVariable UUID planId,
                                           @RequestParam(required = false)Optional<UUID> revisionId,
                                           @RequestParam(required = false, defaultValue = "false") boolean forAndroid) {
        log.debug("GET /plans/{}/revision?revisionId={}", planId, revisionId.orElse(null));
        PlanRevisionDTO planRevisionDTO = revisionId.map(planService::getRevision)
                .map(planTranslator::translate)
                .orElseGet(() -> planTranslator.translate(planService.getPlan(planId).getCurrentRevision()));
        if(forAndroid) {
            return LevelSquarifier.translate(planRevisionDTO);
        }
        return planRevisionDTO;
    }

    @ApiOperation(value = "Move plan")
    @PutMapping(path = "/plans/{planId}/parent")
    public PlanDTO movePlan(@PathVariable UUID planId, @RequestParam(required = false) UUID newFolderId) {
        log.debug("PUT /plans/{}/parent Form: newFolderId={}", planId, newFolderId);
        return planTranslator.translate(planService.movePlan(planId, newFolderId));
    }

    @ApiOperation(value = "Move Plan to root")
    @DeleteMapping(path = "/plans/{planId}/parent")
    public PlanDTO removePlanFolder(@PathVariable UUID planId) {
        log.debug("DELETE /plans/{}/parent", planId);
        return planTranslator.translate(planService.removePlanFolder(planId));
    }

    @ApiOperation(value = "Add a plan revision",
            notes = "Required Attributes: metadata (imageWidth, imageHeight (If the revision is a file), " +
                    "metadata (nwLatLong, seLatLong (If revision is a map), " +
                    "metadata (archiveSize), level")
    @PutMapping(path = "/plans/{planId}")
    public PlanDTO addPlanRevision(@PathVariable UUID planId,
                                   @RequestBody PlanRevisionDTO revision) {
        log.debug("PUT /plans/{}, Body: {}", planId, revision);
        return planTranslator.translate(planService.addNewPlanRevision(planId, revision).getPlan());
    }

    @ApiOperation(value = "Update Plan", notes = "Required Attributes: none (But body should not be null")
    @PatchMapping(path = "/plans/{planId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PlanDTO updatePlan(@PathVariable UUID planId,
                              @RequestParam(required = false, defaultValue = "true") boolean notify,
                              @RequestBody PlanDTO planDTO) {
        log.debug("PATCH /plans/{}", planId);
        planDTO.setId(planId);
        return planTranslator.translate(planService.updatePlan(planDTO, notify));
    }

    @ApiOperation(value = "Download Plan Thumbnail")
    @GetMapping(path = "/plans/{planId}/thumbnail.jpg", produces = "image/jpeg")
    public void downloadThumbnail(@PathVariable UUID planId, HttpServletResponse response, HttpServletRequest request) {
        log.debug("GET /plans/{}/thumbnail.jpg", planId);
        response.setStatus(HttpStatus.NO_CONTENT.value());
        Plan plan = planService.getPlan(planId);
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        if (!StringUtils.isEmpty(ifNoneMatch) && ifNoneMatch.equals(plan.getCurrentRevision().getMetadata().getThumbnailChecksum())) {
            response.setHeader(HttpHeaders.ETAG, plan.getCurrentRevision().getMetadata().getThumbnailChecksum());
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            return;
        }
        Optional<InputStream> streamOptional = planService.downloadThumbnail(plan);
        if (streamOptional.isPresent()) {
            response.setHeader(HttpHeaders.ETAG, plan.getCurrentRevision().getMetadata().getThumbnailChecksum());
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("image/jpeg");
            try (InputStream is = streamOptional.get()) {
                StreamUtils.copy(is, response.getOutputStream());
            } catch (IOException e) {
                throw newInternalServerError("Error in opening thumbnail stream",e);
            }
        }
    }

    @ApiOperation(value = "Search Plans")
    @GetMapping(path = "/plans/search")
    public List<PlanDTO> searchPlans(@RequestParam String search,
                                     @RequestParam(required = false, defaultValue = "0") int page,
                                     @RequestParam(required = false, defaultValue = "4") int size,
                                     @RequestParam(name = "in", required = false) ProjectFilter projectFilter,
                                     @RequestParam(name = "for", required = false)UUID projectId,
                                     @RequestParam(required = false, defaultValue = "")PlanExpandAttribute[] expand) {
        log.debug("GET /plans/search?search={}&in={}&for={}", search, projectFilter, projectId);
        return planService.searchPlans(projectId, search, page , size, projectFilter)
                .stream()
                .map(p -> planTranslator.translate(p, expand))
                .collect(Collectors.toList());
    }

    @ApiOperation(value = "Download Plan Revision Archive")
    @GetMapping(path = "/planRevisions/{revisionId}/revision.tar", produces = "application/x-tar")
    public void downloadRevisionArchive(@PathVariable UUID revisionId, HttpServletResponse response) {
        log.debug("GET /planRevisions/{}/revision.tar", revisionId);
        response.setStatus(HttpStatus.NOT_FOUND.value());
        Optional<InputStream> streamOptional = planService.downloadRevision(revisionId);
        if (streamOptional.isPresent()) {
            try (InputStream is = streamOptional.get()) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("application/x-tar");
                StreamUtils.copy(is, response.getOutputStream());
            } catch (IOException e) {
                throw newInternalServerError("Error in opening Plan Revision Archive stream",e);
            }
        }
    }

    @ApiOperation(value = "Download Plan Tile")
    @GetMapping(path = "/plans/revisions/{revisionId}/{level}/{y}_{x}.jpg", produces = "image/jpeg")
    public void downloadTile(@PathVariable UUID revisionId,
                             @PathVariable int level,
                             @PathVariable int y,
                             @PathVariable int x,
                             @RequestParam(required = false, defaultValue = "false") boolean alwaysReturn,
                             @RequestParam(required = false, defaultValue = "false") boolean forAndroid,
                             HttpServletResponse response) {
        log.debug("GET /planLevels");
        response.setStatus(HttpStatus.NOT_FOUND.value());
        Optional<InputStream> streamOptional = planService.downloadTile(revisionId, level, y, x, alwaysReturn, forAndroid);
        if (streamOptional.isPresent()) {
            try (InputStream is = streamOptional.get()) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType("image/jpeg");
                StreamUtils.copy(is, response.getOutputStream());
            } catch (IOException e) {
                throw newInternalServerError("Error in opening Plan Tile stream", e);
            }
        }
    }

    @ApiOperation(value = "Move Plan and Folders", notes = "Required Attributes: none")
    @PostMapping("/plans/folders/{folderId}")
    public PlanFolderDTO movePlanAndFolders(@PathVariable UUID folderId,
                                            @RequestBody MoveDto dto) {
        log.debug("POST /plans/folders/{}", folderId);
        Assert.notNull(folderId, "Cannot move Plans/PlanFolders to new PlanFolder when ID is NULL!");
        return planTranslator.translate(planService.moveFolderAndPlans(folderId, dto));
    }

    @ApiOperation(value = "Move Plan and Folders to root", notes = "Required Attributes: projectId")
    @DeleteMapping("/plans/folders")
    public PlanFolderDTO moveToRootFolder(@RequestBody MoveDto dto) {
        log.debug("DELETE /plans/folders");
        log.info("Move Plan and plan folder to root folder");
        return planTranslator.translate(planService.moveFolderAndPlans(null, dto));
    }

    @ApiOperation(value = "Get Plan Sync Size")
    @GetMapping(path = "/plans/{projectId}/syncSize")
    public SyncSizeDTO getProject(@PathVariable UUID projectId,
                                  @RequestParam(required = false) String since,
                                  @RequestParam(required = false) PostsFilter[] filter) {
        if(filter == null){
            filter = new PostsFilter[0];
        }
        return planService.getPlanSyncSize(projectId, since, filter);
    }

    @ApiOperation(value = "Get Quick info of plans")
    @GetMapping("/plans/quickInfo")
    public Page<PlanQuickInfo> planQuickInfo(@RequestParam(required = false) UUID projectId,
                                             @RequestParam(required = false) UUID folderId,
                                             @RequestParam(required = false) List<UUID> planIds,
                                             @RequestParam(required = false) String since,
                                             @RequestParam(required = false, defaultValue = "0") int page,
                                             @RequestParam(required = false, defaultValue = "10") int size) {
        log.debug("GET /api/v2/plans/quickInfo?projectId={}&folderId={}&planIds={}&since={}&page={}&size={}", projectId, folderId, planIds, since, page, size);
        return planService.getPlanQuickInfo(projectId, folderId, planIds, since, page, size);
    }

}
