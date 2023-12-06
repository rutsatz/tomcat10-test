package com.test.tomcat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Component
public class AppStartupRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(AppStartupRunner.class);
    private static final ForkJoinPool THREAD_POOL = new ForkJoinPool(1);
    private final ResourceLoader resourceLoader;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public AppStartupRunner(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(ApplicationArguments args) {
        Runnable runnable = () -> {
            try {
                String fontPath = resourceLoader.getResource("classpath:fonts/font.ttf").getFile().getPath();
                logger.info("fontPath: {}", fontPath);
            } catch (IOException e) {
                logger.error("error load font thread", e);
                throw new RuntimeException(e);
            }
        };

        try {
            logger.info("---> Load font using thread");
            new Thread(runnable).start();
            Thread.sleep(2_000);

            logger.info("---> Load font using executor service");
            executorService.submit(runnable).get();

            /*
            It throws an exception:
            java.io.FileNotFoundException: class path resource [fonts/font.ttf] cannot be resolved to URL because it does not exist
             */
            logger.info("---> Load font using fork join pool");
            THREAD_POOL.submit(runnable).get();
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }
}
