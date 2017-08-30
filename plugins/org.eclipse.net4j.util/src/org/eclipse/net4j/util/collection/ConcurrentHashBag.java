package org.eclipse.net4j.util.collection;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Protects target {@link HashBag} using {@link ReadWriteLock}.
 * This class does not implement {@link Set} interface because iterator is not supported - forEach() method replaces iteration.
 * Also some default methods are not implemented for brevity.
 * @author Pavel Vlasov
 */
public class ConcurrentHashBag<T>
{

  private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

  private HashBag<T> target;

  public ConcurrentHashBag(HashBag<T> target)
  {
    this.target = target;
  }

  public int getCounterFor(T o)
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.getCounterFor(o);
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  /**
   * TODO Replace with java.util.function.Consumer when upgrading bundle compatibility level to 1.8
   * @author Pavel Vlasov
   */
  public interface Consumer<T>
  {

    void accept(T obj);

  }

  public void forEach(Consumer<? super T> action)
  {
    readWriteLock.readLock().lock();
    try
    {
      for (T e : target)
      {
        action.accept(e);
      }
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  public int size()
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.size();
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  public boolean isEmpty()
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.isEmpty();
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  public boolean contains(Object o)
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.contains(o);
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  public Object[] toArray()
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.toArray();
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  @SuppressWarnings("hiding")
  public <T> T[] toArray(T[] a)
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.toArray(a);
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  public boolean add(T e)
  {
    readWriteLock.writeLock().lock();
    try
    {
      return target.add(e);
    }
    finally
    {
      readWriteLock.writeLock().unlock();
    }
  }

  public boolean remove(Object o)
  {
    readWriteLock.writeLock().lock();
    try
    {
      return target.remove(o);
    }
    finally
    {
      readWriteLock.writeLock().unlock();
    }
  }

  public boolean containsAll(Collection<?> c)
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.containsAll(c);
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  public boolean addAll(Collection<? extends T> c)
  {
    readWriteLock.writeLock().lock();
    try
    {
      return target.addAll(c);
    }
    finally
    {
      readWriteLock.writeLock().unlock();
    }
  }

  public boolean retainAll(Collection<?> c)
  {
    readWriteLock.writeLock().lock();
    try
    {
      return target.retainAll(c);
    }
    finally
    {
      readWriteLock.writeLock().unlock();
    }
  }

  public boolean removeAll(Collection<?> c)
  {
    readWriteLock.writeLock().lock();
    try
    {
      return target.removeAll(c);
    }
    finally
    {
      readWriteLock.writeLock().unlock();
    }
  }

  public void clear()
  {
    readWriteLock.writeLock().lock();
    try
    {
      target.clear();
    }
    finally
    {
      readWriteLock.writeLock().unlock();
    }
  }

  @Override
  public boolean equals(Object o)
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.equals(o);
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

  @Override
  public int hashCode()
  {
    readWriteLock.readLock().lock();
    try
    {
      return target.hashCode();
    }
    finally
    {
      readWriteLock.readLock().unlock();
    }
  }

}
