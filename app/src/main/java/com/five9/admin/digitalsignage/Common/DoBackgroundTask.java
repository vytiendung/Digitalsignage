package com.five9.admin.digitalsignage.Common;

import android.os.AsyncTask;

public class DoBackgroundTask extends AsyncTask<Void, Void, Void> {
	private IDoBackgroundTask mIDoBackgroundTask;

	public DoBackgroundTask(IDoBackgroundTask iBackgroundTask){
		this.mIDoBackgroundTask = iBackgroundTask;
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			mIDoBackgroundTask.doInBackGround();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
