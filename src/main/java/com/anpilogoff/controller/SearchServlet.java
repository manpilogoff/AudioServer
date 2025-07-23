package com.anpilogoff.controller;

import com.anpilogoff.service.QobuzFetcher;
import com.anpilogoff.util.ConfigUtil;
import com.anpilogoff.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

@Slf4j
@WebServlet(urlPatterns = "/search")
public class SearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String param = req.getParameter("query");

        try(Writer writer = resp.getWriter()){
            if(param == null) {req.getRequestDispatcher("/search.html").forward(req, resp); }
            else {
                QobuzFetcher qobuzFetcher = (QobuzFetcher) req.getServletContext().getAttribute("qobuzFetcher");

                String token = ConfigUtil.getProperty("env.properties","AUTH_TOKEN");
                String root = qobuzFetcher.searchTracks(param, 20, token);
                String searchJson = JsonUtil.extractSearch(root);

                resp.setContentType("application/json");
                writer.write(searchJson);
                writer.flush();
            }
        } catch (IOException | ServletException e) {
            log.error("Error in doGet|SearchServlet{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

