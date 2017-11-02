package com.caguiclajmg.pibot;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by caguicla.jmg on 01/11/2017.
 */

final class DiscoverTask extends AsyncTask<Void, Void, InetSocketAddress> {
    interface OnTimeoutListener { void onTimeout(); }
    interface OnCompletionListener { void onCompletion(InetSocketAddress address); }

    private final InetSocketAddress mAddress;
    private final String mIdentifier;
    private final int mTimeout;

    private OnTimeoutListener mTimeoutListener = null;
    private OnCompletionListener mCompletionListener = null;

    DiscoverTask(InetSocketAddress address, String identifier, int timeout) {
        mAddress = address;
        mIdentifier = identifier;
        mTimeout = timeout;
    }

    void setOnTimeoutListener(OnTimeoutListener l) {
        mTimeoutListener = l;
    }

    void setOnCompletionListener(OnCompletionListener l) {
        mCompletionListener = l;
    }

    @Override
    protected InetSocketAddress doInBackground(Void... voids) {
        try {
            final DatagramSocket socket = new DatagramSocket(mAddress.getPort(), mAddress.getAddress());
            final byte[] buffer = new byte[mIdentifier.length() + 2];
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<InetSocketAddress> future = executor.submit(new Callable<InetSocketAddress>() {
                @Override
                public InetSocketAddress call() {
                    try {
                        final ByteBuffer message = ByteBuffer.wrap(buffer);
                        String identifier;

                        do {
                            identifier = "";
                            message.position(0);

                            socket.receive(packet);

                            for(int i = 0; i < mIdentifier.length(); ++i) identifier += String.valueOf((char) message.get());
                        } while(!identifier.equals(mIdentifier));

                        int port = message.getShort();

                        return new InetSocketAddress(packet.getAddress(), port);
                    } catch(IOException e) {
                        Log.e("DiscoverTask", "Failed to read from socket");
                    } finally {
                        socket.close();
                    }

                    return null;
                }
            });

            return future.get(mTimeout, TimeUnit.SECONDS);
        } catch(SocketException e) {
            Log.e("DiscoverTask", "Failed to create socket, socket is probably in use?");
        } catch(ExecutionException | TimeoutException | InterruptedException e) {
            Log.e("DiscoverTask", "Task timed out");
        }

        return null;
    }

    @Override
    protected void onPostExecute(InetSocketAddress result) {
        if(mCompletionListener != null) {
            if(result != null) {
                mCompletionListener.onCompletion(result);
            } else {
                mTimeoutListener.onTimeout();
            }
        }
    }
}
