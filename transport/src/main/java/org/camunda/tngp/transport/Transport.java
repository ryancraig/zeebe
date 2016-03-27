package org.camunda.tngp.transport;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.camunda.tngp.dispatcher.Dispatcher;
import org.camunda.tngp.transport.impl.TransportContext;
import org.camunda.tngp.transport.protocol.client.TransportConnectionManager;
import org.camunda.tngp.transport.protocol.client.TransportConnection;

public class Transport implements AutoCloseable
{
    public final static int STATE_OPEN = 0;
    public final static int STATE_CLOSING = 1;
    public final static int STATE_CLOSED = 2;

    protected final static AtomicIntegerFieldUpdater<Transport> STATE_FIELD
        = AtomicIntegerFieldUpdater.newUpdater(Transport.class, "state");

    protected final TransportContext transportContext;
    protected final Dispatcher sendBuffer;

    protected volatile int state;

    public Transport(TransportContext transportContext)
    {
        this.transportContext = transportContext;
        this.sendBuffer = transportContext.getSendBuffer();

        STATE_FIELD.set(this, STATE_OPEN);
    }

    public ClientChannelBuilder createClientChannel(InetSocketAddress remoteAddress)
    {
        if(STATE_OPEN != STATE_FIELD.get(this))
        {
            throw new IllegalStateException("Cannot create client channel on "+this+", transport is not open.");
        }

        return new ClientChannelBuilder(transportContext, remoteAddress);
    }

    public ServerSocketBindingBuilder createServerSocketBinding(InetSocketAddress addr)
    {
        if(STATE_OPEN != STATE_FIELD.get(this))
        {
            throw new IllegalStateException("Cannot create server socket on "+this+", transport is not open.");
        }

        return new ServerSocketBindingBuilder(transportContext, addr);
    }

    public Dispatcher getSendBuffer()
    {
        return sendBuffer;
    }

    public CompletableFuture<Transport> closeAsync()
    {
        if(STATE_FIELD.compareAndSet(this, STATE_OPEN, STATE_CLOSING))
        {
            final CompletableFuture<Transport> closeFuture = new CompletableFuture<>();

            transportContext.getConductorCmdQueue().add((c) ->
            {
                c.close(this, closeFuture);
            });

            return closeFuture;
        }
        else
        {
            return CompletableFuture.completedFuture(this);
        }
    }

    public void close()
    {
        closeAsync().join();
    }

}
