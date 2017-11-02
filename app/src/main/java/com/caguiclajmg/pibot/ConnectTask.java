package com.caguiclajmg.pibot;

import android.os.AsyncTask;

import java.net.InetSocketAddress;

/**
 * Created by caguicla.jmg on 02/11/2017.
 */

public class ConnectTask extends AsyncTask<Void, Void, TCPSocket> {
    public interface OnConnectedListener { void onConnected(TCPSocket socket); }

    private TCPSocket mSocket;

    private OnConnectedListener mConnectedListener = null;

    ConnectTask(InetSocketAddress host) {
        mSocket = new TCPSocket(host);
    }

    public void setOnConnectedListener(OnConnectedListener l) {
        mConnectedListener = l;
    }

    @Override
    protected TCPSocket doInBackground(Void... voids) {
        return mSocket.connect() ? mSocket : null;
    }

    @Override
    protected void onPostExecute(TCPSocket result) {
        if(mConnectedListener != null) mConnectedListener.onConnected(result);
    }
}
