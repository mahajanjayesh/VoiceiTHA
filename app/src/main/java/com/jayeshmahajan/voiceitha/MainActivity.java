package com.jayeshmahajan.voiceitha;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  {
    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 98;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutStream;
    BroadcastReceiver mReceiver;
    String msg;
    ConnectedThread  mConnectedThread;
    static int counter = 0;
    String control = "0",  pcontrol="99";
    volatile boolean stopWorker;
    String Device = "HC-05";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.fragment_main);
        setContentView(R.layout.fragment_main);
        TextView click = (TextView) findViewById(R.id.abc);
        click.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                msg = "STOP";
                txtSpeechInput.setText(msg);
                if (counter == 1)
                    try {

                        sendData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                return false;
            }
        });
        RelativeLayout swipe_layout = (RelativeLayout) findViewById(R.id.textView);


        try {
            findBT();
        } catch (IOException e) {
            e.printStackTrace();
        }
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
        // hide the action bar
        //getActionBar().hide();
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       // if (id == R.id.action_settings) {
            return true;
        }

       // return super.onOptionsItemSelected(item);
   // }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWorker = true;
        try {
            if (mmOutStream != null) {
                mmOutStream.close();
                mmSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Turned off", Toast.LENGTH_LONG).show();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        if(mReceiver!=null)
            unregisterReceiver(mReceiver);


    }

    void findBT() throws IOException {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "No bluetooth adapter available", Toast.LENGTH_LONG).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
            SystemClock.sleep(7000);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            counter = 0;
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(Device)) {
                    counter = 1;
                    mmDevice = device;
                    Toast.makeText(getApplicationContext(), "Bluetooth device already paired", Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), "Checking if in range", Toast.LENGTH_SHORT).show();
                    ConnectThread mConnectThread = new ConnectThread(device);
                    mConnectThread.start();
                    break;
                }
            }

        }

        if (counter == 0 && counter!=1) {
            Toast.makeText(getApplicationContext(), "Bluetooth device not found", Toast.LENGTH_SHORT).show();
            Toast.makeText(getApplicationContext(), "Scanning for other devices", Toast.LENGTH_LONG).show();
            ScanDevice mscaneddevices = new ScanDevice();
            mscaneddevices.start();
        }



    }

    void openBT() throws IOException {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard //SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutStream = mmSocket.getOutputStream();
    }

    void sendData() throws IOException {
        if (msg .equalsIgnoreCase( "light on")|| msg.equalsIgnoreCase("switch on the light") ||   msg.equalsIgnoreCase("lights on") || msg.equalsIgnoreCase("on the light")) {
            control = "*light on";
        } else if (msg.equalsIgnoreCase("light off")|| msg.equalsIgnoreCase("switch off the light") || msg.equalsIgnoreCase("lights off") || msg.equalsIgnoreCase("off the light")) {
            control = "*light off";
        } else if (msg.equalsIgnoreCase("fan on")||msg.equalsIgnoreCase("switch on the fan")) {
            control = "*fan on";
        } else if (msg.equalsIgnoreCase("fan stop")||msg.equalsIgnoreCase("switch off the fan") ||msg.equalsIgnoreCase("fan stops") ||msg.equalsIgnoreCase("stop the fan")) {
            control = "*fan stop";


        }

        if(!control.equals(pcontrol)) {
            Toast.makeText(getApplicationContext(), "Command Accepted", Toast.LENGTH_LONG).show();
            ConnectedThread  mConnectedThread = new ConnectedThread();
            mConnectedThread.start();
        }else{
            Toast.makeText(getApplicationContext(), "Command Rejected", Toast.LENGTH_LONG).show();
        }


        pcontrol=control;
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {

                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                msg = result.get(0).toUpperCase();
                txtSpeechInput.setText(msg);

                if (counter == 1) {
                    try {
                        sendData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                txtSpeechInput.setText(msg);
            }

        }

    }

    private class ConnectThread extends Thread {
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            try {
                mmSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.e("arduino", "1");
            } catch (IOException e) {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), "Device is not in Range", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), "Device connected", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("ardino","2");
            } catch (IOException connectException) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Device is not in Range", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            try {
                mmOutStream=mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }




    private class ScanDevice extends Thread {

        public void run() {
            mBluetoothAdapter.startDiscovery();
            mReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();

                    //Finding devices
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        // Get the BluetoothDevice object from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device.getName().equals(Device)) {
                            mmDevice = device;
                            Toast.makeText(getApplicationContext(), "Bluetooth device trying to connect", Toast.LENGTH_SHORT).show();
                            pairDevice(device);
                            counter=1;
                        }

                    }
                }
            };

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);

        }

        private void pairDevice(BluetoothDevice device) {
            try {
                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                method.invoke(device, (Object[]) null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            IntentFilter intent = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            registerReceiver(mPairReceiver, intent);
        }

        private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    final int state        = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                    final int prevState    = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                    if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
                        try {
                            findBT();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){

                    }

                }
            }
        };
    }

    private class ConnectedThread extends Thread {
        public void run() {
            try {
                mmOutStream.write(control.getBytes());
            } catch (IOException e) {
                runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), "Sending data failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }


        }

    }
}
