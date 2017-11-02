package com.caguiclajmg.pibot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by caguicla.jmg on 02/11/2017.
 */

public class TCPSocket {
    private final InetSocketAddress mHost;

    private Socket mSocket;
    private BufferedReader mReader;
    private BufferedWriter mWriter;

    TCPSocket(InetSocketAddress host) {
        mHost = host;
    }

    boolean connect() {
        try {
            mSocket = new Socket(mHost.getAddress(), mHost.getPort());
            mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream()));

            return true;
        } catch(IOException e) {
            disconnect();
        }

        return false;
    }

    void disconnect() {
        try {
            if(mWriter != null) mWriter.close();
            if(mReader != null) mReader.close();
            if(mSocket != null) mSocket.close();

            mWriter = null;
            mReader = null;
            mSocket = null;
        } catch(IOException ignored) {
        }
    }

    void write(char[] message) {
        try {
            if(mWriter != null) mWriter.write(message);
        } catch(IOException ignored) {
        }
    }

    int read(char[] buffer) {
        try {
            return mReader.read(buffer);
        } catch(IOException ignored) {
        }

        return 0;
    }
}
