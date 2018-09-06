package com.five9.admin.digitalsignage.Common;

public interface RequestNetworkListener {
    public void onSuccess(String reponse);
    public void onFail(String reponse);
    public void onCancel(String reponse);
}
