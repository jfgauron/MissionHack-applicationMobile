package com.codets.hearthattack;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static java.util.Arrays.copyOfRange;


public class ChipListeningService extends Handler {

  private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
  private Activity appActivity;
  private AcceptThread connectionThread;
  private ManageConnectionThread connectedThread;

  int REQUEST_ENABLE_BT = 9001;

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            System.out.println("received broadcast");
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println("Device found !"+ deviceName + deviceHardwareAddress);
            }
        }
    };

    ChipListeningService(Activity app) {
      this.appActivity = app;

        int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
        ActivityCompat.requestPermissions(appActivity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

        //bluetooth.startDiscovery();

        try {
            initConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handleMessage(Message msg) {
        // make json
        byte[] bytes = (byte[]) msg.obj;
        String data = new String(Base64.decode(copyOfRange(bytes, 0, msg.arg1), 0));
        try {
            JSONObject json = new JSONObject(data);
            System.out.println(json.getJSONArray("heartbeats").getInt(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

  protected void initConnection() throws IOException {
      // enable bluetooth
      if (!bluetooth.isEnabled()) {
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          appActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
      }

      // launch thread looking for a socket
      connectionThread = new AcceptThread(bluetooth, this);
      connectionThread.start(); // thread will contact us and kill itself, no need to worry
  }

  protected void handleConnectedSocket(BluetoothSocket socket) {
        System.out.println("CONNECTED SOCKET");
        connectionThread.interrupt();

      Looper.prepare();

        // handler to handle other thread messages
        //IncomingHandler incomingHandler = new IncomingHandler(this);
        connectedThread = new ManageConnectionThread(socket, this);
        connectedThread.start();
    }


  private void initDiscovery() {
      System.out.println("starting bluetooth discovery");

      // Register for broadcasts when a device is discovered.
      IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

      appActivity.registerReceiver(new BroadcastReceiver() {
          public void onReceive(Context context, Intent intent) {
              System.out.println("received broadcast");
              String action = intent.getAction();
              if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                  // Discovery has found a device. Get the BluetoothDevice
                  // object and its info from the Intent.
                  BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                  String deviceName = device.getName();
                  String deviceHardwareAddress = device.getAddress(); // MAC address
                  System.out.println("Device found !"+ deviceName + deviceHardwareAddress);
              }
          }
      }, filter);

      bluetooth.startDiscovery();

      //while(!bluetooth.isDiscovering())
        //System.out.println("nope");


  }

  private void cancelDiscovery() {
        bluetooth.cancelDiscovery();
        appActivity.unregisterReceiver(broadcastReceiver);
  }



  private void cleanUp() {
        appActivity.unregisterReceiver(broadcastReceiver);

  }



}