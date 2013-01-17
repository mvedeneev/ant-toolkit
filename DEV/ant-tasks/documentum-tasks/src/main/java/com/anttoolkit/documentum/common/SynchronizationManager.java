package com.anttoolkit.documentum.common;

import java.util.*;
import java.util.concurrent.locks.*;

public class SynchronizationManager
{
	private static final int LOCKS_POOL_SIZE = 100;

	private static volatile Stack<ReentrantLock> m_locksPool = new Stack<ReentrantLock>();
	private static volatile Map<Object, ReentrantLock> m_objectLocksMap = new HashMap<Object, ReentrantLock>();

	private static volatile List<Object> m_tryingToLockObjects = new Vector<Object>();

	public static void lock(Object obj)
	{
		if (obj == null)
		{
			return;
		}

		ReentrantLock lock = null;

		synchronized (m_objectLocksMap)
		{
			if (!m_objectLocksMap.containsKey(obj))
			{
				lock = getNewLock();
				m_objectLocksMap.put(obj, lock);
				lock.lock();

				return;
			}

			lock = m_objectLocksMap.get(obj);
			if (lock.isHeldByCurrentThread())
			{
				lock.lock();
				return;
			}

			if (lock.tryLock())
			{
				return;
			}

			m_tryingToLockObjects.add(obj);
		}

		try
		{
			lock.lock();
		}
		finally
		{
			m_tryingToLockObjects.remove(obj);
		}
	}

	public static void unlock(Object obj)
	{
		if (obj == null)
		{
			return;
		}

		synchronized (m_objectLocksMap)
		{
			ReentrantLock lock = m_objectLocksMap.get(obj);
			if (lock == null || !lock.isHeldByCurrentThread())
			{
				return;
			}

			if (!lock.hasQueuedThreads() &&
				lock.getHoldCount() == 1 &&
				!m_tryingToLockObjects.contains(obj))
			{
				m_objectLocksMap.remove(obj);
				returnToPool(lock);
			}

			lock.unlock();
		}
	}

	public static boolean isLocked(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		synchronized (m_objectLocksMap)
		{
			ReentrantLock lock = m_objectLocksMap.get(obj);
			return lock != null && lock.isLocked();
		}
	}

	public static boolean isLockedByCurrentThread(Object obj)
	{
		if (obj == null)
		{
			return false;
		}

		synchronized (m_objectLocksMap)
		{
			ReentrantLock lock = m_objectLocksMap.get(obj);
			return lock != null && lock.isHeldByCurrentThread();
		}
	}

	private static ReentrantLock getNewLock()
	{
		return m_locksPool.empty() ? new ReentrantLock() : m_locksPool.pop();
	}

	private static void returnToPool(ReentrantLock lock)
	{
		if (m_locksPool.size() <= LOCKS_POOL_SIZE)
		{
			m_locksPool.push(lock);
		}
	}
}
