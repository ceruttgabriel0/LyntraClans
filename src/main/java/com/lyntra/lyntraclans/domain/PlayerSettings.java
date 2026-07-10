package com.lyntra.lyntraclans.domain;

public final class PlayerSettings {

    private boolean allowInvites;
    private boolean showWarnings;
    private boolean showTag;
    private FfMode ffMode;

    public PlayerSettings(boolean allowInvites, boolean showWarnings, boolean showTag, FfMode ffMode) {
        this.allowInvites = allowInvites;
        this.showWarnings = showWarnings;
        this.showTag = showTag;
        this.ffMode = ffMode;
    }

    public boolean isAllowInvites() {
        return allowInvites;
    }

    public void setAllowInvites(boolean allowInvites) {
        this.allowInvites = allowInvites;
    }

    public boolean isShowWarnings() {
        return showWarnings;
    }

    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public boolean isShowTag() {
        return showTag;
    }

    public void setShowTag(boolean showTag) {
        this.showTag = showTag;
    }

    public FfMode getFfMode() {
        return ffMode;
    }

    public void setFfMode(FfMode ffMode) {
        this.ffMode = ffMode;
    }
}
