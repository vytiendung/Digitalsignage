package com.five9.admin.digitalsignage.Common;

public interface RequestNetworkListener {
    public void onSuccess(String response);
    public void onFail(String response);
    public void onCancel(String response);
}
