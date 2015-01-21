package com.outpost;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Adapted BlueToothChat Example.
 */
public class Transfer {

    private static final String TAG = "BluetoothChatService";
    Scan_Service scan = new Scan_Service();

    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";


    private static final UUID MY_UUID_SECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public BluetoothAdapter mAdapter;

    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;




    public Transfer(Context context) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;

    }
/*
    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

    }



    public synchronized int getState() {
        return mState;
    }


    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectedSendThread != null) {
            mConnectedSendThread.cancel();
            mConnectedSendThread = null;
        }

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }


    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);



        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectedSendThread != null) {
            mConnectedSendThread.cancel();
            mConnectedSendThread = null;
        }



        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }


    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        if (mConnectedSendThread != null) {
            mConnectedSendThread.cancel();
            mConnectedSendThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }



        //mConnectedSendThread = new ConnectedSendThread(socket, socketType);
       // mConnectedSendThread.start();
        sc = new socketcheck(socket, socketType);
        sc.start();



        setState(STATE_CONNECTED);
    }

    public synchronized void connectedsend(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        if (mConnectedSendThread != null) {
            mConnectedSendThread.cancel();
            mConnectedSendThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }



        //mConnectedSendThread = new ConnectedSendThread(socket, socketType);
        //mConnectedSendThread.start();
        sc = new socketcheck(socket, socketType);
        sc.start();



        setState(STATE_CONNECTED);
    }


    public synchronized void stop() {
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mConnectedSendThread != null) {
            mConnectedSendThread.cancel();
            mConnectedSendThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }


    public void write() throws IOException {
        // Create temporary object
        ConnectedSendThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedSendThread;
        }

        r.start();
    }


    private void connectionFailed() {
        setState(STATE_NONE);

    }


    private void connectionLost() {

        setState(STATE_NONE);
    }

    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_SECURE,
                            MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                            NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            Log.w(TAG, "Socket Type: " + mSocketType +
                    "BEGIN mAcceptThread" + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (mState != STATE_CONNECTED) {
                try {
                    Log.w(TAG,"I'm trying to connect");
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (Transfer.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_NONE:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;

                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.w(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.w(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            //mAdapter.cancelDiscovery();
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception

                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    Method m = null;
                    try {
                        m = mmDevice.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                        Log.w("","trying reflection");
                    } catch (NoSuchMethodException e1) {
                        e1.printStackTrace();
                    }
                    mmSocket=(BluetoothSocket)m.invoke(mmDevice,Integer.valueOf(1));
                    mmSocket.connect();
                    try{

                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    connectionFailed();
                    return;
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType
                            + " socket during connection failure", e2);
                } catch (InvocationTargetException e1) {
                    e1.printStackTrace();
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                }

            }

            // Reset the ConnectThread because we're done
            synchronized (Transfer.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connectedsend(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;


        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.w(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;

            try {
                tmpIn = socket.getInputStream();

            } catch (IOException e) {
                Log.w(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;

        }

        public void run() {
            Log.w(TAG, "I FUCKING READ SHIT");

            FileOutputStream fos = null;
            File f = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/in");
            int leng = (int) f.length() + 1;
            try {
                fos = new FileOutputStream(
                        Environment.getExternalStorageDirectory()
                                + "/OutpostShare/in/copy"+ leng + ".txt");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            byte[] buffer = new byte[1024];
            int bytes,current;
            try {
                bytes = mmInStream.read(buffer, 0, buffer.length);
                current = bytes;



                do {
                    Log.d(TAG, "do-while -- current: " + current);
                    bytes = mmInStream.read(buffer, current,
                            buffer.length - bytes);


                    if (bytes >= 0)
                        current += bytes;
                } while (bytes > -1);

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    fos.write(buffer);
                    fos.flush();
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                    Log.w("","something didnt work writing file to disk");
                }

            }


        }



        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.w(TAG, "close() of connect socket failed", e);
            }
        }
    }




    private class ConnectedSendThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        public ConnectedSendThread(BluetoothSocket socket, String socketType) {
            Log.w(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;

            OutputStream tmpOut = null;

            try {

                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }


            mmOutStream = tmpOut;
        }


        public void run() {
            Log.w(TAG, "I FUCKING WRITE SHIT");

            File f = new File(Environment.getExternalStorageDirectory() + "/OutpostShare/out");
            File fileList[] = f.listFiles();
            for(int i = 0; i<fileList.length;i++) {

                byte[] buffer = new byte[1024];


                    try {

                        FileInputStream fis = new FileInputStream(fileList[i]);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        bis.read(buffer);
                        mmOutStream.write(buffer);


                    } catch (IOException e) {
                        Log.e(TAG, "Exception during write", e);
                    }

            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class socketcheck extends Thread {
        private final BluetoothSocket mmSocket;
        private final OutputStream mmOutStream;

        public socketcheck(BluetoothSocket socket, String socketType) {
            Log.w(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;

            OutputStream tmpOut = null;

            try {

                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }


            mmOutStream = tmpOut;
        }


        public void run() {

            Log.w("", String.valueOf(mmSocket.isConnected()));
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}

*/

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

        private synchronized void setState(int state) {
            Log.d(TAG, "setState() " + mState + " -> " + state);
            mState = state;

            // Give the new state to the Handler so the UI Activity can update

        }

        /**
         * Return the current connection state.
         */
        public synchronized int getState() {
            return mState;
        }

        /**
         * Start the chat service. Specifically start AcceptThread to begin a
         * session in listening (server) mode. Called by the Activity onResume()
         */
        public synchronized void start() {
            Log.d(TAG, "start");

            // Cancel any thread attempting to make a connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            setState(STATE_LISTEN);

            // Start the thread to listen on a BluetoothServerSocket

            if (mInsecureAcceptThread == null) {
                mInsecureAcceptThread = new AcceptThread(false);
                mInsecureAcceptThread.start();
            }
        }

        /**
         * Start the ConnectThread to initiate a connection to a remote device.
         *
         * @param device The BluetoothDevice to connect
         * @param secure Socket Security type - Secure (true) , Insecure (false)
         */
        public synchronized void connect(BluetoothDevice device, boolean secure) {
            Log.d(TAG, "connect to: " + device);

            // Cancel any thread attempting to make a connection
            if (mState == STATE_CONNECTING) {
                if (mConnectThread != null) {
                    mConnectThread.cancel();
                    mConnectThread = null;
                }
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Start the thread to connect with the given device
            mConnectThread = new ConnectThread(device, secure);
            mConnectThread.start();
            setState(STATE_CONNECTING);
        }

        /**
         * Start the ConnectedThread to begin managing a Bluetooth connection
         *
         * @param socket The BluetoothSocket on which the connection was made
         * @param device The BluetoothDevice that has been connected
         */
        public synchronized void connected(BluetoothSocket socket, BluetoothDevice
                device, final String socketType) {
            Log.w(TAG, "connected, Socket Type:" + socketType);

            // Cancel the thread that completed the connection
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            // Cancel any thread currently running a connection
            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            // Cancel the accept thread because we only want to connect to one device
            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }
            if (mInsecureAcceptThread != null) {
                mInsecureAcceptThread.cancel();
                mInsecureAcceptThread = null;
            }

            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = new ConnectedThread(socket, socketType);
            mConnectedThread.start();

            // Send the name of the connected device back to the UI Activity


            setState(STATE_CONNECTED);
        }

        /**
         * Stop all threads
         */
        public synchronized void stop() {
            Log.w(TAG, "stop");

            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }

            if (mConnectedThread != null) {
                mConnectedThread.cancel();
                mConnectedThread = null;
            }

            if (mSecureAcceptThread != null) {
                mSecureAcceptThread.cancel();
                mSecureAcceptThread = null;
            }

            if (mInsecureAcceptThread != null) {
                mInsecureAcceptThread.cancel();
                mInsecureAcceptThread = null;
            }
            setState(STATE_NONE);
        }

        /**
         * Write to the ConnectedThread in an unsynchronized manner
         *
         * @param out The bytes to write
         * @see ConnectedThread#write(byte[])
         */
        public void write(byte[] out) {
            // Create temporary object
            ConnectedThread r;
            // Synchronize a copy of the ConnectedThread
            synchronized (this) {
                if (mState != STATE_CONNECTED) return;
                r = mConnectedThread;
            }
            // Perform the write unsynchronized
            r.write(out);
        }

        /**
         * Indicate that the connection attempt failed and notify the UI Activity.
         */
        private void connectionFailed() {
            // Send a failure message back to the Activity


            // Start the service over to restart listening mode
            Transfer.this.start();
        }

        /**
         * Indicate that the connection was lost and notify the UI Activity.
         */
        private void connectionLost() {
            // Send a failure message back to the Activity


            // Start the service over to restart listening mode
            Transfer.this.start();
        }

        /**
         * This thread runs while listening for incoming connections. It behaves
         * like a server-side client. It runs until a connection is accepted
         * (or until cancelled).
         */
        private class AcceptThread extends Thread {
            // The local server socket
            private final BluetoothServerSocket mmServerSocket;
            private String mSocketType;

            public AcceptThread(boolean secure) {
                BluetoothServerSocket tmp = null;
                mSocketType = secure ? "Secure" : "Insecure";

                // Create a new listening server socket
                try {
                    if (secure) {
                        tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE,
                                MY_UUID_SECURE);
                    } else {
                        tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(
                                NAME_INSECURE, MY_UUID_INSECURE);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
                }
                mmServerSocket = tmp;
            }

            public void run() {
                Log.w(TAG, "Socket Type: " + mSocketType +
                        "BEGIN mAcceptThread" + this);
                setName("AcceptThread" + mSocketType);

                BluetoothSocket socket = null;

                // Listen to the server socket if we're not connected
                while (mState != STATE_CONNECTED) {
                    try {
                        // This is a blocking call and will only return on a
                        // successful connection or an exception
                        socket = mmServerSocket.accept();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                        break;
                    }

                    // If a connection was accepted
                    if (socket != null) {
                        synchronized (Transfer.this) {
                            switch (mState) {
                                case STATE_LISTEN:
                                case STATE_CONNECTING:
                                    // Situation normal. Start the connected thread.
                                    connected(socket, socket.getRemoteDevice(),
                                            mSocketType);
                                    break;
                                case STATE_NONE:
                                case STATE_CONNECTED:
                                    // Either not ready or already connected. Terminate new socket.
                                    try {
                                        socket.close();
                                    } catch (IOException e) {
                                        Log.e(TAG, "Could not close unwanted socket", e);
                                    }
                                    break;
                            }
                        }
                    }
                }
                Log.w(TAG, "END mAcceptThread, socket Type: " + mSocketType);

            }

            public void cancel() {
                Log.w(TAG, "Socket Type" + mSocketType + "cancel " + this);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
                }
            }
        }


        /**
         * This thread runs while attempting to make an outgoing connection
         * with a device. It runs straight through; the connection either
         * succeeds or fails.
         */
        private class ConnectThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final BluetoothDevice mmDevice;
            private String mSocketType;

            public ConnectThread(BluetoothDevice device, boolean secure) {
                mmDevice = device;
                BluetoothSocket tmp = null;
                mSocketType = secure ? "Secure" : "Insecure";

                // Get a BluetoothSocket for a connection with the
                // given BluetoothDevice
                try {
                    if (secure) {
                        tmp = device.createRfcommSocketToServiceRecord(
                                MY_UUID_SECURE);
                    } else {
                        tmp = device.createInsecureRfcommSocketToServiceRecord(
                                MY_UUID_INSECURE);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                }
                mmSocket = tmp;
            }

            public void run() {
                Log.w(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
                setName("ConnectThread" + mSocketType);

                // Always cancel discovery because it will slow down a connection
                //mAdapter.cancelDiscovery();

                // Make a connection to the BluetoothSocket
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                } catch (IOException e) {
                    // Close the socket
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "unable to close() " + mSocketType +
                                " socket during connection failure", e2);
                    }
                    connectionFailed();
                    return;
                }

                // Reset the ConnectThread because we're done
                synchronized (Transfer.this) {
                    mConnectThread = null;
                }

                // Start the connected thread
                connected(mmSocket, mmDevice, mSocketType);
            }

            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
                }
            }
        }

        /**
         * This thread runs during a connection with a remote device.
         * It handles all incoming and outgoing transmissions.
         */
        private class ConnectedThread extends Thread {
            private BluetoothSocket mmSocket;
            private InputStream mmInStream;
            private OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket, String socketType) {
                Log.d(TAG, "create ConnectedThread: " + socketType);
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the BluetoothSocket input and output streams
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "temp sockets not created", e);
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {

                byte[] buffer = new byte[1024];
                int bytes;

                // Keep listening to the InputStream while connected
                while (true) {
                    try {
                        Log.w("sadiofusoidufoisuoidfopsopdf", String.valueOf(mmSocket.isConnected()));
                        // Read from the InputStream
                        bytes = mmInStream.read(buffer);

                        // Send the obtained bytes to the UI Activity
                        Log.w("", String.valueOf(buffer));
                    } catch (IOException e) {
                        Log.e(TAG, "disconnected", e);
                        connectionLost();

                        // Start the service over to restart listening mode
                        Transfer.this.start();
                        break;
                    }
                }
            }

            /**
             * Write to the connected OutStream.
             *
             * @param buffer The bytes to write
             */
            public void write(byte[] buffer) {
                try {
                    mmOutStream.write(12322);

                    // Share the sent message back to the UI Activity

                } catch (IOException e) {
                    Log.e(TAG, "Exception during write", e);
                }
            }

            public void cancel() {


                if (mmOutStream != null) {
                    try {mmOutStream.close();} catch (Exception e) {}
                    mmOutStream = null;
                }

                if (mmInStream != null) {
                    try {mmInStream.close();} catch (Exception e) {}
                    mmInStream = null;
                }

                if (mmSocket != null) {
                    try {mmSocket.close();} catch (Exception e) {}
                    mmSocket = null;
                }



            }
        }
    }
