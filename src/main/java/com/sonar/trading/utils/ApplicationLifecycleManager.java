package com.sonar.trading.utils;

import java.util.ArrayList;
import java.util.List;

public class ApplicationLifecycleManager {

    private List<ApplicationLifecycleListener> listeners = new ArrayList<>();

    public ApplicationLifecycleManager() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                stopApp();
            }
        });
    }

    public void addListener(ApplicationLifecycleListener listener) {
        listeners.add(listener);
    }

    public void startApp() {
        for (ApplicationLifecycleListener listener: listeners) {
            listener.onStart();
        }
    }

    public void stopApp() {
        for (ApplicationLifecycleListener listener: listeners) {
            listener.onStop();
        }
    }
}
