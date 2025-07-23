package com.anpilogoff;

import com.anpilogoff.controller.PlayServlet;
import com.anpilogoff.controller.SearchServlet;
import com.anpilogoff.service.DaoService;
import com.anpilogoff.service.FFMpegService;
import com.anpilogoff.service.QobuzFetcher;
import com.anpilogoff.service.S3Service;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.Attributes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {
    public static void main(String[] args) throws Exception {
          ExecutorService s3uploadExecutor = Executors.newFixedThreadPool(10);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(10);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("audioPU");

        QobuzFetcher qobuzFetcher = new QobuzFetcher();
        FFMpegService ffMpegService = new FFMpegService();
        S3Service s3Service = S3Service.create(s3uploadExecutor);
        DaoService daoService = new DaoService(emf);

        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);

        context.setContextPath("/");
        context.setAttribute("ioExecutor", ioExecutor);

        context.setAttribute("qobuzFetcher", qobuzFetcher);
        context.setAttribute("s3Service", s3Service);
        context.setAttribute("ffmpegService", ffMpegService);
        context.setAttribute("daoService", daoService);


        SearchServlet searchServlet = new SearchServlet();
        PlayServlet playServlet = new PlayServlet();

        String resourceBaseDir = Main.class.getClassLoader().getResource("static").toExternalForm();
        ServletHolder servletHolder = new ServletHolder("default", DefaultServlet.class);
        servletHolder.setInitParameter("resourceBase", resourceBaseDir);

        context.addServlet(new ServletHolder(searchServlet),"/search");
        context.addServlet(new ServletHolder(playServlet),"/play");
        context.addServlet(servletHolder,"/");

        server.setHandler(context);
        server.start();
        server.join();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered, cleaning up resources...");
            try {
                if (emf != null && emf.isOpen()) {
                    emf.close();
                    log.info("EntityManagerFactory closed");
                }
                s3uploadExecutor.shutdown();
                ioExecutor.shutdown();
                ffMpegService.shutdown();

                if (!s3uploadExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    s3uploadExecutor.shutdownNow();
                }
                if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    ioExecutor.shutdownNow();
                }

                server.stop();
                log.info("Jetty server stopped");
            } catch (Exception e) {
                log.error("Error during shutdown", e);
            }
        }));

}
    }
