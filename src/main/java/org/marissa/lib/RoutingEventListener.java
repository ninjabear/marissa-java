package org.marissa.lib;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;

public interface RoutingEventListener {
    @Suspendable
    void routingEvent(String trigger, Response response) throws InterruptedException, SuspendExecution;
}
