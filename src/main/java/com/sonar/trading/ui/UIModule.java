package com.sonar.trading.ui;

import com.jfoenix.controls.JFXListView;
import com.sonar.trading.config.AppConfig;
import com.sonar.trading.config.UIConfig;
import com.sonar.trading.messages.SignalMessage;
import com.sonar.trading.messages.TradeMessage;
import com.sonar.trading.queue.Queue;
import com.sonar.trading.queue.Subscriber;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.stream.Collectors;

public class UIModule {

    private static final String SUBSCRIBER_ID = "ui";

    public void init(Stage stage, AppConfig appConfig,
                     Collection<Queue<TradeMessage>> exchangeFeedTradeQueues,
                     Queue<SignalMessage> signalMessageQueue) {
        stage.setMaximized(true);

        UIConfig uiConfig = appConfig.getUi();

        Scene scene = stage.getScene();
        initTradesView(scene, uiConfig, exchangeFeedTradeQueues);
        initSignalsView(scene, uiConfig, signalMessageQueue);

    }

    private void initSignalsView(Scene scene, UIConfig uiConfig, Queue<SignalMessage> signalMessageQueue) {
        JFXListView<HBox> signalsList = (JFXListView<HBox>) scene.lookup("#signals");
        signalMessageQueue.subscribe(SUBSCRIBER_ID, new Subscriber<SignalMessage>() {
            @Override
            public void onMessage(SignalMessage message) {
                Platform.runLater(() -> {
                    TradeRow newRow = new TradeRow(message);
                    int maxSignalsToDisplay = uiConfig.getMaxSignalsToDisplay();
                    addAndLimitRows(signalsList, maxSignalsToDisplay, newRow);
                });
            }
        });
    }

    private void initTradesView(Scene scene, UIConfig uiConfig, Collection<Queue<TradeMessage>> exchangeFeedQueues) {
        JFXListView<HBox> tradesList = (JFXListView<HBox>) scene.lookup("#trades");

        for (Queue<TradeMessage> tradeQueue: exchangeFeedQueues) {
            tradeQueue.subscribe(SUBSCRIBER_ID, new Subscriber<TradeMessage>() {
                @Override
                public void onMessage(TradeMessage message) {
                    Platform.runLater(() -> {
                        TradeRow newRow = new TradeRow(message);
                        int maxTradesToDisplay = uiConfig.getMaxTradesToDisplay();
                        addAndLimitRows(tradesList, maxTradesToDisplay, newRow);
                    });
                }
            });
        }
    }

    private <T> void addAndLimitRows(JFXListView<T> list, int maxRowsToDisplay, T newRow) {
        ObservableList<T> items = list.getItems();
        items.add(0, newRow);
        items = FXCollections.observableArrayList(items.stream().limit(maxRowsToDisplay).collect(Collectors.toList()));
        list.setItems(items);
    }
}
