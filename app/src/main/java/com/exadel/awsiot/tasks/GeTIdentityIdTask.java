package com.exadel.awsiot.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.auth.CognitoCredentialsProvider;

public class GeTIdentityIdTask extends AsyncTask<Void, Void, AsyncTaskResult<String>> {

    private CognitoCredentialsProvider credentialProvider;

    public GeTIdentityIdTask(CognitoCredentialsProvider credentialProvider) {
        this.credentialProvider = credentialProvider;
    }

    @Override
    protected AsyncTaskResult<String> doInBackground(Void... voids) {
        String identityId = credentialProvider.getIdentityId();
        return new AsyncTaskResult<>(identityId);
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<String> result) {
        if (result.getError() == null) {
            Log.i(GeTIdentityIdTask.class.getCanonicalName(), result.getResult());
        } else {
            Log.e(GeTIdentityIdTask.class.getCanonicalName(), "Error in Update Shadow",
                    result.getError());
        }
    }
}