package com.exadel.awsiot;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.exadel.awsiot.messaging.ShadowDeviceData;
import com.exadel.awsiot.tasks.AsyncTaskResult;
import com.exadel.awsiot.tasks.GeTIdentityIdTask;
import com.exadel.awsiot.tasks.SendReportTask;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private String cognitoIdentityPoolId = "us-east-1:11e41233-6cb2-4ae3-9fdc-a397471d46c2";
    private String customerSpecificEndpoint = "a7x0omjlvdb9g-ats.iot.us-east-1.amazonaws.com";

    private CognitoCachingCredentialsProvider credentialsProvider;

    private Regions regions = Regions.fromName("us-east-1");
    private AWSIotDataClient iotDataClient;
    private String deviceName = "TrainingDevice";
    private String appClientId = deviceName + "_";
    private Integer currentTemperature = 0;
    private Integer targetTemperature = 0;
    private ScheduledExecutorService schedulerForCommands;
    private AWSIotMqttManager mqttManager;
    private String topic = "$aws/things/" + deviceName + "/shadow/update/accepted";

    private Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initConnections();
        updateTextLabel(R.id.editTextDeviceName, deviceName);
        findViewById(R.id.buttonIdentityId).setOnClickListener((view) -> getIdentityId());

        schedulerForCommands = Executors.newSingleThreadScheduledExecutor();
        schedulerForCommands.scheduleAtFixedRate(() -> {
            changeTemperature();
            sendReport();
            System.out.println();
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void changeTemperature() {
        Integer diff = targetTemperature - currentTemperature;
        if (diff != 0) {
            currentTemperature += Integer.signum(diff);
        }
    }

    private void sendReport() {
        SendReportTask sendAlarmTask = new SendReportTask(iotDataClient, deviceName, currentTemperature);
        sendAlarmTask.execute();
        updateTextLabel(R.id.currentTemperature, currentTemperature.toString());
    }

    private void readCommand(byte[] data) {
        try {
            String message = new String(data, "UTF-8");
            ShadowDeviceData shadowDeviceData = gson.fromJson(message, ShadowDeviceData.class);
            Integer readValue = shadowDeviceData.getState().getDesired().getTargetTemperature();
            if (Objects.nonNull(readValue)) {
                targetTemperature = readValue;
                updateTextLabel(R.id.targetTemperature, targetTemperature.toString());
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    private void initConnections() {
        credentialsProvider = new CognitoCachingCredentialsProvider(getApplicationContext(), cognitoIdentityPoolId, regions);
        iotDataClient = new AWSIotDataClient(credentialsProvider);
        iotDataClient.setRegion(Region.getRegion(regions));
        iotDataClient.setEndpoint(customerSpecificEndpoint);
        mqttManager = new AWSIotMqttManager(appClientId, customerSpecificEndpoint);
        mqttManager.connect(credentialsProvider, (status, throwable) -> {
            if (status.equals(AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected)) {
                try {
                    mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0,
                            (topic, data) -> readCommand(data));
                } catch (Exception e) {
                    System.out.println();
                }
            }
        });
    }

    private void getIdentityId() {
        try {
            GeTIdentityIdTask geTIdentityIdTask = new GeTIdentityIdTask(credentialsProvider);
            AsyncTask<Void, Void, AsyncTaskResult<String>> res = geTIdentityIdTask.execute();
            AsyncTaskResult<String> identityResult = res.get();
            String identityId = identityResult.getResult();
            updateTextLabel(R.id.editTextIdentityId, identityId);
        } catch (InterruptedException | ExecutionException ie) {
            updateTextLabel(R.id.editTextIdentityId, "Can't get identityId:" + ie.getLocalizedMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTextLabel(final int id, final String label) {
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        TextView textView = findViewById(id);
                        textView.setText(label);
                    }
                });
            }
        }).start();
    }
}