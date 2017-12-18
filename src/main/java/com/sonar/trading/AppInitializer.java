package com.sonar.trading;

import com.sonar.trading.bitso.BitsoModule;
import com.sonar.trading.config.AppConfig;
import com.sonar.trading.messages.ExchangeMessage;
import com.sonar.trading.queue.NonPersistentQueueFactory;
import com.sonar.trading.queue.Queue;
import com.sonar.trading.queue.QueueFactory;
import com.sonar.trading.trading.TradingModule;
import com.sonar.trading.ui.UIModule;
import com.sonar.trading.utils.ApplicationLifecycleListener;
import com.sonar.trading.utils.ApplicationLifecycleManager;
import com.sonar.trading.utils.Resources;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class AppInitializer {

    public void init(Stage stage) {
        AppConfig appConfig = loadAppConfig();
        ApplicationLifecycleManager applicationLifecycleManager = new ApplicationLifecycleManager();
        QueueFactory<ExchangeMessage> queueFactory = new NonPersistentQueueFactory<>();

        ScheduledExecutorService exchangePollerThreadPool = Executors.newScheduledThreadPool(appConfig.getExchangePollerThreadPoolSize());
        BitsoModule bitsoModule = new BitsoModule();
        bitsoModule.init(appConfig, exchangePollerThreadPool, applicationLifecycleManager, queueFactory);

        TradingModule tradingModule = new TradingModule();
        List<Queue<? extends ExchangeMessage>> exchangeFeedQueues = new ArrayList<>(bitsoModule.getTradeQueuesPerBook().values());
        tradingModule.init(appConfig, exchangeFeedQueues, queueFactory);

        UIModule uiModule = new UIModule();
        uiModule.init(stage, appConfig, bitsoModule.getTradeQueuesPerBook().values(), tradingModule.getSignalMessageQueue());

        applicationLifecycleManager.addListener(new ApplicationLifecycleListener() {
            @Override
            public void onStart() {}

            @Override
            public void onStop() {
                exchangePollerThreadPool.shutdown();
            }
        });

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                applicationLifecycleManager.stopApp();
            }
        });

        applicationLifecycleManager.startApp();
    }

    private AppConfig loadAppConfig() {
        InputStream configFileStream = Resources.getResourceStream("appConfig.yml");
        Yaml yaml = new Yaml();
        return yaml.loadAs(configFileStream, AppConfig.class);
    }

}
