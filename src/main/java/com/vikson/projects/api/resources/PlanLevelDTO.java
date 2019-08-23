package com.vikson.projects.api.resources;

import com.vikson.projects.model.PlanLevel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@ApiModel(value = "Plan Level Resource")
public class PlanLevelDTO implements Comparable<PlanLevelDTO> {

    @ApiModelProperty(value = "Id of the plan level")
    private UUID id;
    @ApiModelProperty(value = "Width of the plan level")
    private int width;
    @ApiModelProperty(value = "Height of the plan level")
    private int height;
    @ApiModelProperty(value = "The amount of tiles on the x axis")
    private int tilesX;
    @ApiModelProperty(value = "The amount of tiles on the y axis")
    private int tilesY;
    @ApiModelProperty(hidden = true)
    private String storageId;
    @ApiModelProperty(value = "folder id of s3 bucket for given level")
    private String downloadUrl;
    @ApiModelProperty(value = "Whether the plan level is squarified or not (Android needs this to display the plan)")
    private boolean squarified = false;

    public PlanLevelDTO() {
    }

    public PlanLevelDTO(PlanLevel level, String storageId, String downloadUrl) {
        this.id = level.getId();
        this.width = level.getTilesX() * 256;
        this.height = level.getTilesY() * 256;
        this.tilesX = level.getTilesX();
        this.tilesY = level.getTilesY();
        this.storageId = storageId;
        this.downloadUrl = downloadUrl;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTilesX() {
        return tilesX;
    }

    public void setTilesX(int tilesX) {
        this.tilesX = tilesX;
    }

    public int getTilesY() {
        return tilesY;
    }

    public void setTilesY(int tilesY) {
        this.tilesY = tilesY;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    @JsonIgnore
    public boolean isSquarified() {
        return squarified;
    }

    public void setSquarified(boolean squarified) {
        this.squarified = squarified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlanLevelDTO levelDTO = (PlanLevelDTO) o;

        if (width != levelDTO.width) return false;
        if (height != levelDTO.height) return false;
        if (tilesX != levelDTO.tilesX) return false;
        if (tilesY != levelDTO.tilesY) return false;
        if (id != null ? !id.equals(levelDTO.id) : levelDTO.id != null) return false;
        return storageId != null ? storageId.equals(levelDTO.storageId) : levelDTO.storageId == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + tilesX;
        result = 31 * result + tilesY;
        result = 31 * result + (storageId != null ? storageId.hashCode() : 0);
        return result;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    @Override
    public int compareTo(@NotNull PlanLevelDTO o) {
        return tilesX * tilesY - o.tilesX * o.tilesY;
    }
}
