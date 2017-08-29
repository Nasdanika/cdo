# cdo
CDO Model Repository project repository (cdo)

Forked the CDO repository and created this branch in order to try to fix 

```
java.util.ConcurrentModificationException
        at java.util.HashMap$HashIterator.nextNode(Unknown Source)
        at java.util.HashMap$KeyIterator.next(Unknown Source)
        at java.util.Collections$UnmodifiableCollection$1.next(Unknown Source)
        at org.eclipse.emf.cdo.common.lock.CDOLockUtil.createLockState(CDOLockUtil.java:67)
        at org.eclipse.emf.cdo.internal.server.Repository.toCDOLockStates(Repository.java:1983)
        at org.eclipse.emf.cdo.internal.server.Repository.doUnlock(Repository.java:2020)
        at org.eclipse.emf.cdo.internal.server.Repository.unlock(Repository.java:2004)
        at org.eclipse.emf.cdo.server.internal.net4j.protocol.UnlockObjectsIndication.indicating(UnlockObjectsIndication.java:71)
        at org.eclipse.emf.cdo.server.internal.net4j.protocol.CDOServerIndication.indicating(CDOServerIndication.java:108)
        at org.eclipse.net4j.signal.IndicationWithResponse.doExtendedInput(IndicationWithResponse.java:100)
        at org.eclipse.net4j.signal.Signal.doInput(Signal.java:377)
        at org.eclipse.net4j.signal.IndicationWithResponse.execute(IndicationWithResponse.java:73)
        at org.eclipse.emf.cdo.server.internal.net4j.protocol.CDOServerWriteIndication.execute(CDOServerWriteIndication.java:39)
```

[Additional info](https://www.eclipse.org/forums/index.php/m/1771560/#msg_1771560)
