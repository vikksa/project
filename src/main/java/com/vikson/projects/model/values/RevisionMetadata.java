package com.vikson.projects.model.values;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class RevisionMetadata {

    @Column(name = "file_name")
    private String fileName;
    @Column(name = "content_type")
    private String contentType;
    @Column(name = "image_width")
    private int imageWidth;
    @Column(name = "image_height")
    private int imageHeight;
    @Column(name = "nw_lat")
    private String nwLat;
    @Column(name = "nw_long")
    private String nwLong;
    @Column(name = "se_lat")
    private String seLat;
    @Column(name = "se_long")
    private String seLong;

    /**
     * MD5 Checksum for this revisions TAR Archive file.
     *
     * For newer plans set by plan import service for older plans set afterwards by this service.
     */
    @Column(nullable = true)
    private String archiveChecksum;

    @Column(nullable = true)
    private Long archiveSize;

    @Column(nullable = true)
    private String thumbnailChecksum;

    @Column(nullable = true)
    private Integer rotation;

    @Column(nullable = true)
    private Integer page;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getNwLat() {
        return nwLat;
    }

    public void setNwLat(String nwLat) {
        this.nwLat = nwLat;
    }

    public String getNwLong() {
        return nwLong;
    }

    public void setNwLong(String nwLong) {
        this.nwLong = nwLong;
    }

    public String getSeLat() {
        return seLat;
    }

    public void setSeLat(String seLat) {
        this.seLat = seLat;
    }

    public String getSeLong() {
        return seLong;
    }

    public void setSeLong(String seLong) {
        this.seLong = seLong;
    }

    /**
     * MD5 Checksum for this revisions TAR Archive.
     *
     * @return MD5 checksum, maybe NULL
     */
    public String getArchiveChecksum() {
        return archiveChecksum;
    }

    public void setArchiveChecksum(String archiveChecksum) {
        this.archiveChecksum = archiveChecksum;
    }

    /**
     * Tests if this revision has a checksum.
     *
     * @return False if archiveChecksum is NULL
     */
    public boolean hasChecksum() {
        return archiveChecksum != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RevisionMetadata that = (RevisionMetadata) o;
        return imageWidth == that.imageWidth &&
                imageHeight == that.imageHeight &&
                Objects.equals(fileName, that.fileName) &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(nwLat, that.nwLat) &&
                Objects.equals(nwLong, that.nwLong) &&
                Objects.equals(seLat, that.seLat) &&
                Objects.equals(seLong, that.seLong) &&
                Objects.equals(archiveChecksum, that.archiveChecksum) &&
                Objects.equals(archiveSize, that.archiveSize) &&
                Objects.equals(thumbnailChecksum, that.thumbnailChecksum) &&
                Objects.equals(rotation, that.rotation) &&
                Objects.equals(page, that.page);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, contentType, imageWidth, imageHeight, nwLat, nwLong, seLat, seLong, archiveChecksum, archiveSize, thumbnailChecksum, rotation, page);
    }

    public Long getArchiveSize() {
        return archiveSize;
    }

    public void setArchiveSize(Long archiveSize) {
        this.archiveSize = archiveSize;
    }

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
