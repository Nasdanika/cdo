/***************************************************************************
 * Copyright (c) 2004 - 2007 Eike Stepper, Germany.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 **************************************************************************/
package org.eclipse.internal.net4j;

import org.eclipse.net4j.ConnectorException;
import org.eclipse.net4j.ConnectorLocation;
import org.eclipse.net4j.ConnectorState;
import org.eclipse.net4j.IBuffer;
import org.eclipse.net4j.IBufferProvider;
import org.eclipse.net4j.IChannel;
import org.eclipse.net4j.IConnector;
import org.eclipse.net4j.IConnectorCredentials;
import org.eclipse.net4j.IConnectorStateEvent;
import org.eclipse.net4j.IProtocol;
import org.eclipse.net4j.internal.util.container.Container;
import org.eclipse.net4j.internal.util.container.LifecycleEventConverter;
import org.eclipse.net4j.internal.util.event.Event;
import org.eclipse.net4j.internal.util.factory.FactoryKey;
import org.eclipse.net4j.internal.util.om.trace.ContextTracer;
import org.eclipse.net4j.util.StringUtil;
import org.eclipse.net4j.util.WrappedException;
import org.eclipse.net4j.util.concurrent.RWLock;
import org.eclipse.net4j.util.container.IContainer;
import org.eclipse.net4j.util.container.IContainerEvent;
import org.eclipse.net4j.util.container.IElementProcessor;
import org.eclipse.net4j.util.container.IContainerDelta.Kind;
import org.eclipse.net4j.util.event.IListener;
import org.eclipse.net4j.util.event.INotifier;
import org.eclipse.net4j.util.factory.IFactory;
import org.eclipse.net4j.util.factory.IFactoryKey;
import org.eclipse.net4j.util.lifecycle.LifecycleUtil;
import org.eclipse.net4j.util.registry.IRegistry;

import org.eclipse.internal.net4j.bundle.OM;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Eike Stepper
 */
public abstract class Connector extends Container<IChannel> implements IConnector
{
  private static final ContextTracer TRACER = new ContextTracer(OM.DEBUG_CONNECTOR, Connector.class);

  private String userID;

  private IConnectorCredentials credentials;

  private IRegistry<IFactoryKey, IFactory> protocolFactoryRegistry;

  private List<IElementProcessor> protocolPostProcessors;

  private IBufferProvider bufferProvider;

  /**
   * An optional executor to be used by the {@link IChannel}s to process their {@link Channel#receiveQueue} instead of
   * the current thread. If not <code>null</code> the sender and the receiver peers become decoupled.
   * <p>
   */
  private ExecutorService receiveExecutor;

  private List<Channel> channels = new ArrayList<Channel>(0);

  private RWLock channelsLock = new RWLock(2500);

  private ConnectorState connectorState = ConnectorState.DISCONNECTED;

  /**
   * Is registered with each {@link IChannel} of this {@link IConnector}.
   */
  private transient IListener channelListener = new LifecycleEventConverter<IChannel>(this)
  {
    @Override
    protected IContainerEvent<IChannel> createContainerEvent(IContainer<IChannel> container, IChannel element, Kind kind)
    {
      return newContainerEvent(element, kind);
    }
  };

  private transient CountDownLatch finishedConnecting;

  private transient CountDownLatch finishedNegotiating;

  public Connector()
  {
  }

  public abstract void multiplexBuffer(IChannel channel);

  public ExecutorService getReceiveExecutor()
  {
    return receiveExecutor;
  }

  public void setReceiveExecutor(ExecutorService receiveExecutor)
  {
    this.receiveExecutor = receiveExecutor;
  }

  public IRegistry<IFactoryKey, IFactory> getProtocolFactoryRegistry()
  {
    return protocolFactoryRegistry;
  }

  public void setProtocolFactoryRegistry(IRegistry<IFactoryKey, IFactory> protocolFactoryRegistry)
  {
    this.protocolFactoryRegistry = protocolFactoryRegistry;
  }

  public List<IElementProcessor> getProtocolPostProcessors()
  {
    return protocolPostProcessors;
  }

  public void setProtocolPostProcessors(List<IElementProcessor> protocolPostProcessors)
  {
    this.protocolPostProcessors = protocolPostProcessors;
  }

  public IBufferProvider getBufferProvider()
  {
    return bufferProvider;
  }

  public void setBufferProvider(IBufferProvider bufferProvider)
  {
    this.bufferProvider = bufferProvider;
  }

  public boolean isClient()
  {
    return getLocation() == ConnectorLocation.CLIENT;
  }

  public boolean isServer()
  {
    return getLocation() == ConnectorLocation.SERVER;
  }

  public String getUserID()
  {
    return userID;
  }

  public void setUserID(String userID)
  {
    this.userID = userID;
  }

  public IConnectorCredentials getCredentials()
  {
    return credentials;
  }

  public void setCredentials(IConnectorCredentials credentials)
  {
    this.credentials = credentials;
  }

  public ConnectorState getState()
  {
    return connectorState;
  }

  public void setState(ConnectorState newState) throws ConnectorException
  {
    ConnectorState oldState = getState();
    if (newState != oldState)
    {
      if (TRACER.isEnabled())
      {
        TRACER.trace("Setting state " + newState + " (was " + oldState.toString().toLowerCase() //$NON-NLS-1$ //$NON-NLS-2$
            + ")"); //$NON-NLS-1$
      }

      connectorState = newState;
      fireEvent(new ConnectorStateEvent(this, oldState, newState));
      switch (newState)
      {
      case DISCONNECTED:
        if (finishedConnecting != null)
        {
          finishedConnecting.countDown();
          finishedConnecting = null;
        }

        if (finishedNegotiating != null)
        {
          finishedNegotiating.countDown();
          finishedNegotiating = null;
        }
        break;

      case CONNECTING:
        finishedConnecting = new CountDownLatch(1);
        finishedNegotiating = new CountDownLatch(1);
        if (isServer())
        {
          setState(ConnectorState.NEGOTIATING);
        }
        break;

      case NEGOTIATING:
        finishedConnecting.countDown();
        setState(ConnectorState.CONNECTED); // TODO Implement negotiation
        break;

      case CONNECTED:
        finishedConnecting.countDown(); // Just in case of suspicion
        finishedNegotiating.countDown();
        break;

      }
    }
  }

  public boolean isConnected()
  {
    return getState() == ConnectorState.CONNECTED;
  }

  public void connectAsync() throws ConnectorException
  {
    try
    {
      activate();
    }
    catch (ConnectorException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new ConnectorException(ex);
    }
  }

  public boolean waitForConnection(long timeout) throws ConnectorException
  {
    ConnectorState connectorState = getState();
    if (connectorState == ConnectorState.DISCONNECTED)
    {
      return false;
    }

    if (connectorState == ConnectorState.CONNECTED)
    {
      return true;
    }

    try
    {
      if (TRACER.isEnabled())
      {
        TRACER.trace("Waiting for connection..."); //$NON-NLS-1$
      }

      return finishedNegotiating.await(timeout, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException ex)
    {
      return false;
    }
  }

  public boolean connect(long timeout) throws ConnectorException
  {
    connectAsync();
    return waitForConnection(timeout);
  }

  public ConnectorException disconnect()
  {
    Exception ex = deactivate();
    if (ex == null)
    {
      return null;
    }

    if (ex instanceof ConnectorException)
    {
      return (ConnectorException)ex;
    }

    return new ConnectorException(ex);
  }

  public IChannel[] getChannels()
  {
    final List<IChannel> result = new ArrayList<IChannel>(0);
    channelsLock.read(new Runnable()
    {
      public void run()
      {
        for (Channel channel : channels)
        {
          if (channel != null)
          {
            result.add(channel);
          }
        }
      }
    });

    return result.toArray(new IChannel[result.size()]);
  }

  @Override
  public boolean isEmpty()
  {
    return getElements().length == 0;
  }

  public IChannel[] getElements()
  {
    return getChannels();
  }

  public IChannel openChannel() throws ConnectorException
  {
    return openChannel((IProtocol)null);
  }

  public IChannel openChannel(String protocolID, Object infraStructure) throws ConnectorException
  {
    IProtocol protocol = createProtocol(protocolID, infraStructure);
    if (protocol == null)
    {
      throw new IllegalArgumentException("Unknown protocolID: " + protocolID);
    }

    return openChannel(protocol);
  }

  public IChannel openChannel(IProtocol protocol) throws ConnectorException
  {
    waitForConnection(Long.MAX_VALUE);
    short channelIndex = findFreeChannelIndex();
    Channel channel = createChannel(channelIndex, protocol);
    registerChannelWithPeer(channelIndex, protocol);

    try
    {
      channel.activate();
    }
    catch (ConnectorException ex)
    {
      throw ex;
    }
    catch (Exception ex)
    {
      throw new ConnectorException(ex);
    }

    return channel;
  }

  public Channel createChannel(short channelIndex, String protocolID)
  {
    IProtocol protocol = createProtocol(protocolID, null);
    return createChannel(channelIndex, protocol);
  }

  public Channel createChannel(short channelIndex, IProtocol protocol)
  {
    Channel channel = new Channel(receiveExecutor);
    if (protocol != null)
    {
      protocol.setChannel(channel);
      LifecycleUtil.activate(protocol);
      if (TRACER.isEnabled())
      {
        TRACER.format(
            "Opening channel {0} with protocol {1}", channelIndex, protocol == null ? null : protocol.getType()); //$NON-NLS-1$
      }
    }
    else
    {
      if (TRACER.isEnabled())
      {
        TRACER.format("Opening channel {0} without protocol", channelIndex); //$NON-NLS-1$
      }
    }

    channel.setChannelIndex(channelIndex);
    channel.setConnector(this);
    channel.setReceiveHandler(protocol);
    channel.addListener(channelListener); // TODO remove?
    addChannel(channel);
    return channel;
  }

  public Channel getChannel(final short channelIndex)
  {
    return channelsLock.read(new Callable<Channel>()
    {
      public Channel call() throws Exception
      {
        return channels.get(channelIndex);
      }
    });
  }

  protected List<Queue<IBuffer>> getChannelBufferQueues()
  {
    final List<Queue<IBuffer>> result = new ArrayList<Queue<IBuffer>>(channels.size());
    channelsLock.read(new Runnable()
    {
      public void run()
      {
        for (final Channel channel : channels)
        {
          if (channel != null && channel.isActive())
          {
            Queue<IBuffer> bufferQueue = channel.getSendQueue();
            result.add(bufferQueue);
          }
        }
      }
    });

    return result;
  }

  private short findFreeChannelIndex()
  {
    return channelsLock.read(new Callable<Short>()
    {
      public Short call() throws Exception
      {
        int size = channels.size();
        for (short i = 0; i < size; i++)
        {
          if (channels.get(i) == null)
          {
            return i;
          }
        }

        return (short)size;
      }
    });
  }

  protected void addChannel(final Channel channel)
  {
    channelsLock.write(new Runnable()
    {
      public void run()
      {
        short channelIndex = channel.getChannelIndex();
        while (channelIndex >= channels.size())
        {
          channels.add(null);
        }

        channels.set(channelIndex, channel);
      }
    });
  }

  /**
   * @return <code>true</code> if the channel was removed, <code>false</code> otherwise.
   */
  protected boolean removeChannel(final Channel channel)
  {
    if (channel == null)
    {
      throw new IllegalArgumentException("channel == null");
    }

    if (!isConnected())
    {
      return false;
    }

    final int channelIndex = channel.getChannelIndex();
    boolean removed = false;
    try
    {
      removed = channelsLock.write(new Callable<Boolean>()
      {
        public Boolean call() throws Exception
        {
          if (channelIndex < channels.size() && channels.get(channelIndex) == channel)
          {
            if (TRACER.isEnabled())
            {
              TRACER.trace("Removing channel " + channelIndex); //$NON-NLS-1$
            }

            channels.set(channelIndex, null);
            return true;
          }

          return false;
        }
      });

      if (removed)
      {
        channel.close();
      }
    }
    catch (RuntimeException ex)
    {
      Exception unwrapped = WrappedException.unwrap(ex);
      if (unwrapped instanceof TimeoutException)
      {
        if (channelIndex < channels.size())
        {
          Channel c = channels.get(channelIndex);
          if (c != null && c.isActive())
          {
            throw ex;
          }
        }
      }
      else
      {
        throw ex;
      }
    }

    return removed;
  }

  public void inverseRemoveChannel(short channelIndex)
  {
    try
    {
      Channel channel = getChannel(channelIndex);
      if (channel != null)
      {
        removeChannel(channel);
      }
    }
    catch (RuntimeException ex)
    {
      OM.LOG.warn(ex);
    }
  }

  /**
   * TODO Use IProtocolProvider and make the protocols real container elements, so that the post processors can reach
   * them. The protocol description can be used to store unique protocol IDs so that always new protocols are created in
   * the container.
   */
  protected IProtocol createProtocol(String type, Object infraStructure)
  {
    IRegistry<IFactoryKey, IFactory> registry = getProtocolFactoryRegistry();
    if (StringUtil.isEmpty(type) || registry == null)
    {
      return null;
    }

    // Get protocol factory
    IFactoryKey key = createProtocolFactoryKey(type);
    IFactory factory = registry.get(key);
    if (factory == null)
    {
      if (TRACER.isEnabled())
      {
        TRACER.trace("Unknown protocol " + type); //$NON-NLS-1$
      }

      return null;
    }

    // Create protocol
    String description = null;
    IProtocol protocol = (IProtocol)factory.create(description);
    if (infraStructure != null)
    {
      protocol.setInfraStructure(infraStructure);
    }

    // Post process protocol
    List<IElementProcessor> processors = getProtocolPostProcessors();
    if (processors != null)
    {
      for (IElementProcessor processor : processors)
      {
        protocol = (IProtocol)processor.process(null, key.getProductGroup(), key.getType(), description, protocol);
      }
    }

    return protocol;
  }

  protected IFactoryKey createProtocolFactoryKey(String type)
  {
    switch (getLocation())
    {
    case SERVER:
      return new FactoryKey(ServerProtocolFactory.PRODUCT_GROUP, type);
    case CLIENT:
      return new FactoryKey(ClientProtocolFactory.PRODUCT_GROUP, type);
    default:
      throw new IllegalStateException();
    }
  }

  @Override
  protected void doBeforeActivate() throws Exception
  {
    super.doBeforeActivate();
    if (bufferProvider == null)
    {
      throw new IllegalStateException("bufferProvider == null"); //$NON-NLS-1$
    }

    if (protocolFactoryRegistry == null && TRACER.isEnabled())
    {
      // Just a reminder during development
      TRACER.trace("No factoryRegistry!"); //$NON-NLS-1$
    }

    if (receiveExecutor == null && TRACER.isEnabled())
    {
      // Just a reminder during development
      TRACER.trace("No receiveExecutor!"); //$NON-NLS-1$
    }
  }

  @Override
  protected void doActivate() throws Exception
  {
    super.doActivate();
    setState(ConnectorState.CONNECTING);
  }

  @Override
  protected void doDeactivate() throws Exception
  {
    setState(ConnectorState.DISCONNECTED);
    channelsLock.write(new Runnable()
    {
      public void run()
      {
        for (short i = 0; i < channels.size(); i++)
        {
          Channel channel = channels.get(i);
          if (channel != null)
          {
            LifecycleUtil.deactivate(channel);
          }
        }

        channels.clear();
      }
    });

    super.doDeactivate();
  }

  protected abstract void registerChannelWithPeer(short channelIndex, IProtocol protocol) throws ConnectorException;

  /**
   * @author Eike Stepper
   */
  private static class ConnectorStateEvent extends Event implements IConnectorStateEvent
  {
    private static final long serialVersionUID = 1L;

    private ConnectorState oldState;

    private ConnectorState newState;

    public ConnectorStateEvent(INotifier notifier, ConnectorState oldState, ConnectorState newState)
    {
      super(notifier);
      this.oldState = oldState;
      this.newState = newState;
    }

    public IConnector getConnector()
    {
      return (IConnector)getSource();
    }

    public ConnectorState getOldState()
    {
      return oldState;
    }

    public ConnectorState getNewState()
    {
      return newState;
    }
  }
}
