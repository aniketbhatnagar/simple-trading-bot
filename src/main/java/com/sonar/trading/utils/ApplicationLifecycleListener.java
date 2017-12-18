package com.sonar.trading.utils;

public interface ApplicationLifecycleListener {
    void onStart();

    void onStop();
}
