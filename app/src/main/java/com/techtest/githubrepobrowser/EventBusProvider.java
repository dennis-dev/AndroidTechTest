package com.techtest.githubrepobrowser;

import com.squareup.otto.Bus;

public class EventBusProvider {

    private static Bus bus = null;

    public static Bus getEventBus() {
        if (bus == null) {
            bus = new Bus();
        }

        return bus;
    }
}
