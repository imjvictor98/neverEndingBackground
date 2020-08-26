package uk.ac.shef.oak.jobserviceexample.utilities;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class MQTT {
  final private String TOPIC = "/iot4decision/teste/timer/" + Build.MODEL.trim().toLowerCase();
  final String host = "tcp://mqtt.firstdecision.com.br";
  private String clienteId = "pixel3a_client";
  private String username = "admin";
  private String password = "admin";
  private final String TAG = "MQTT";
  private Context context;
  private MqttAndroidClient mqttAndroidClient;
  
  public MQTT(Context context) {
    this.context = context;
  }
  
  public void initializeMQTT() {
    mqttAndroidClient = new MqttAndroidClient(context, host, clienteId);
    mqttAndroidClient.setCallback(new MqttCallback() {
      @Override
      public void connectionLost(Throwable cause) {
        Log.i(TAG, "connection lost");
        initializeMQTT();
      }
  
      @Override
      public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.i(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
      }
  
      @Override
      public void deliveryComplete(IMqttDeliveryToken token) {
        Log.i(TAG, "msg delivered");
      }
    });
  }
  
  public void subscribeTopic() {
    try {
      if (mqttAndroidClient == null) {
        initializeMQTT();
      } else {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        
        mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
          @Override
          public void onSuccess(IMqttToken asyncActionToken) {
            Log.i(TAG, "Topic Subscribed");
          }
  
          @Override
          public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.i(TAG, "Topic Failed");
            
          }
        });
      }
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
  
  public void publishMessage(String payload) {
    if (mqttAndroidClient == null) {
      initializeMQTT();
    } else if (mqttAndroidClient.isConnected()){
      try {
        final MqttMessage message = new MqttMessage();
        JSONObject payloadJSON = new JSONObject();
  
        payloadJSON.put("Timer", payload);
        
        message.setPayload(payloadJSON.toString().getBytes());
        message.setQos(0);
        
        mqttAndroidClient.publish(TOPIC, message, null, new IMqttActionListener() {
          @Override
          public void onSuccess(IMqttToken asyncActionToken) {
            Log.i(TAG, "Message published: " + message + " on topic: " + TOPIC);
          }
    
          @Override
          public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.e(TAG, "Message Eroor: " + message);
          }
        });
      } catch (MqttException | JSONException e) {
        e.printStackTrace();
      }
    }
  }
}
