package com.CHH2000day.navalcreed.modhelper;

public class AnnouncementBean extends DataBean {
    private Integer id;
    private String announcement;
    private String toCopy;
    private String title;

    public String getTitle() {
        return title;
    }

    public AnnouncementBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public AnnouncementBean setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public AnnouncementBean setAnnouncement(String announcement) {
        this.announcement = announcement;
        return this;
    }

    public String getToCopy() {
        return toCopy;
    }

    public AnnouncementBean setToCopy(String toCopy) {
        this.toCopy = toCopy;
        return this;
    }
}
