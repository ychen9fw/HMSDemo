package com.ychen9.demo.signin;

/**
 * account info
 */
public class AccountInfo {

    private String displayName = null;

    private String accessToken = null;

    private String photoUrl = null;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" SignInAccount Info [ ");
        sb.append(" displayName = ").append(this.displayName);
        sb.append(" accessToken = ").append(this.accessToken);
       // sb.append(" photoUrl = ").append(this.photoUrl);
        sb.append(" ] ");
        return sb.toString();
    }

}
