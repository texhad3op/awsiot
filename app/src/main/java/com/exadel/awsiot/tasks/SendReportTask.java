package com.exadel.awsiot.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.UpdateThingShadowRequest;
import com.amazonaws.services.iotdata.model.UpdateThingShadowResult;
import com.exadel.awsiot.messaging.ShadowDeviceData;
import com.google.gson.Gson;

import java.nio.ByteBuffer;

public class SendReportTask extends AsyncTask<Void, Void, AsyncTaskResult<Void>> {


    private AWSIotDataClient iotDataClient;
    private String deviceName;
    private Integer currentTemperature;

    private Gson gson = new Gson();

    public SendReportTask(AWSIotDataClient iotDataClient, String deviceName, Integer currentTemperature) {
        this.iotDataClient = iotDataClient;
        this.deviceName = deviceName;
        this.currentTemperature = currentTemperature;
    }

    @Override
    protected AsyncTaskResult<Void> doInBackground(Void... voids) {
        try {

            ShadowDeviceData data = new ShadowDeviceData();
            data.getState().getReported().setCurrentTemperature(currentTemperature);
            String json = gson.toJson(data);

            UpdateThingShadowRequest request = new UpdateThingShadowRequest();
            request.setThingName(deviceName);

            ByteBuffer payloadBuffer = ByteBuffer.wrap(json.getBytes());
            request.setPayload(payloadBuffer);
            UpdateThingShadowResult result = iotDataClient.updateThingShadow(request);
            return null;
        } catch (Exception e) {
            Log.e(SendReportTask.class.getCanonicalName(), "updateShadowTask", e);
            return new AsyncTaskResult<>(e);
        }
    }
}