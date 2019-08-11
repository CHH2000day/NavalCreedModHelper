package com.CHH2000day.navalcreed.modhelper;

public final class VersionBean extends DataBean {

    private VersionInfo commonInfo;
    private VersionInfo ffmpegInfo;

    public VersionInfo getCommonInfo() {
        return commonInfo;
    }

    public VersionBean setCommonInfo(VersionInfo commonInfo) {
        this.commonInfo = commonInfo;
        return this;
    }

    public VersionInfo getFfmpegInfo() {
        return ffmpegInfo;
    }

    public VersionBean setFfmpegInfo(VersionInfo ffmpegInfo) {
        this.ffmpegInfo = ffmpegInfo;
        return this;
    }

    public static final class VersionInfo {
        //构建类型 alpha或者release;
        private String buildType;
        //版本类型ffmpeg或common;
        private String type;
        //当前版本号
        private Integer buildCode;
        //版本名
        private String versionName;
        //下载链接
        private String url;
        //更新日志
        private String changelog;
        //要求更新的最低版本
        private Integer minVer;

        public int getMinVer() {
            return minVer;
        }

        public VersionInfo setMinVer(Integer minVer) {
            this.minVer = minVer;
            return this;
        }

        public String getBuildType() {
            return buildType;
        }

        public VersionInfo setBuildType(String buildType) {
            this.buildType = buildType;
            return this;
        }

        public String getType() {
            return type;
        }

        public VersionInfo setType(String type) {
            this.type = type;
            return this;
        }

        public int getBuildCode() {
            return buildCode;
        }

        public VersionInfo setBuildCode(Integer buildCode) {
            this.buildCode = buildCode;
            return this;
        }

        public String getVersionName() {
            return versionName;
        }

        public VersionInfo setVersionName(String versionName) {
            this.versionName = versionName;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public VersionInfo setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getChangelog() {
            return changelog;
        }

        public VersionInfo setChangelog(String changelog) {
            this.changelog = changelog;
            return this;
        }


    }

}
