package com.vikson.projects.api.resources;

import com.vikson.projects.model.values.PlanType;
import com.vikson.projects.model.values.RevisionMetadata;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

@ApiModel(value = "Revision Metadata Resource")
public class RevisionMetadataDTO {

    @ApiModelProperty(value = "Type of the plan")
    private PlanType planType;
    @ApiModelProperty(value = "The width of the image")
    private Integer imageWidth;
    @ApiModelProperty(value = "The height of the image")
    private Integer imageHeight;
    @ApiModelProperty(value = "The name of the file")
    private String fileName;
    @ApiModelProperty(value = "The north west coordinates (Latitude, Longitude) (Comma Separated String)")
    private String nwLatLong;
    @ApiModelProperty(value = "The south east coordinates (Latitude, Longitude) (Comma Separated String)")
    private String seLatLong;
    @ApiModelProperty(value = "Content type of the uploaded revision file")
    private String contentType;
    @ApiModelProperty(value = "Checksum of the file")
    private String checksum;
    @ApiModelProperty(value = "Size of the file")
    private Long archiveSize;
    @ApiModelProperty(value = "Checksum of the thumbnail of the file")
    private String thumbnailChecksum;
    @ApiModelProperty(value = "Plan rotation angle")
    private Integer rotation;
    @ApiModelProperty(value = "PDF page - null when plan is not a PDF")
    private Integer page;

    public RevisionMetadataDTO() {
    }

    public RevisionMetadataDTO(PlanType planType, RevisionMetadata metadata) {
        this.planType = planType;
        this.imageWidth = metadata.getImageWidth();
        this.imageHeight = metadata.getImageHeight();
        if(planType == PlanType.File) {
            this.fileName = metadata.getFileName();
            this.contentType = metadata.getContentType();
        } else if(planType == PlanType.Map) {
            this.nwLatLong = String.format("%sx%s", metadata.getNwLat(), metadata.getNwLong());
            this.seLatLong = String.format("%sx%s", metadata.getSeLat(), metadata.getSeLong());
        }
        this.checksum = metadata.getArchiveChecksum();
        this.archiveSize = metadata.getArchiveSize();
        this.rotation = metadata.getRotation();
        this.page = metadata.getPage();
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        this.planType = planType;
    }

    public Integer getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getNwLatLong() {
        return nwLatLong;
    }

    public void setNwLatLong(String nwLatLong) {
        this.nwLatLong = nwLatLong;
    }

    public String getSeLatLong() {
        return seLatLong;
    }

    public void setSeLatLong(String seLatLong) {
        this.seLatLong = seLatLong;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevisionMetadataDTO that = (RevisionMetadataDTO) o;
        return planType == that.planType &&
                Objects.equals(imageWidth, that.imageWidth) &&
                Objects.equals(imageHeight, that.imageHeight) &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(nwLatLong, that.nwLatLong) &&
                Objects.equals(seLatLong, that.seLatLong) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(checksum, that.checksum) &&
                Objects.equals(archiveSize, that.archiveSize) &&
                Objects.equals(thumbnailChecksum, that.thumbnailChecksum) &&
                Objects.equals(rotation, that.rotation) &&
                Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planType, imageWidth, imageHeight, fileName, nwLatLong, seLatLong, contentType, checksum, archiveSize, thumbnailChecksum, rotation, page);
    }

    public Long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(Long archiveSize) {
        this.archiveSize = archiveSize;
    }

    @JsonIgnore
    public String getThumbnailChecksum() {
        return thumbnailChecksum;
    }

    public void setThumbnailChecksum(String thumbnailChecksum) {
        this.thumbnailChecksum = thumbnailChecksum;
    }

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }
}
