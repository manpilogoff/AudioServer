package com.anpilogoff.controller;

import com.anpilogoff.database.entity.Artist;
import com.anpilogoff.database.entity.Track;
import com.anpilogoff.service.DaoService;
import com.anpilogoff.service.FFMpegService;
import com.anpilogoff.service.QobuzFetcher;
import com.anpilogoff.service.S3Service;
import com.anpilogoff.util.HttpUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.http.HttpRequest;
import java.util.concurrent.*;

@Slf4j
@WebServlet( urlPatterns = "/play", asyncSupported = true)
public class PlayServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String trackId = req.getParameter("track_id");
        String artistId = req.getParameter("artist_id");
        PrintWriter writer = resp.getWriter();

        QobuzFetcher qobuzFetcher = (QobuzFetcher) getServletContext().getAttribute("qobuzFetcher");
        DaoService dao = (DaoService) getServletContext().getAttribute("daoService");
        S3Service s3 = (S3Service) getServletContext().getAttribute("s3Service");

        String url = qobuzFetcher.getFileUrl(Integer.parseInt(trackId), 27);
        HttpRequest reqst;
        Artist artist;

        reqst = HttpUtil.checkTokenValidForDownload(url);

        Track track = dao.getTrackEntity(trackId);

        log.info(" TRACK {} ", track);

        if (track == null) {
            writer.write(url);

            try { artist = qobuzFetcher.fetchArtistWithDetailsAsync(artistId).get(); }
            catch (InterruptedException | ExecutionException e) { throw new RuntimeException(e); }

            boolean isSaved = dao.saveArtist(artist);

            if(isSaved) { log.info(" ARTIST {} saved ", artist); }

            String name = HttpUtil.downloadFileAsync(url, trackId,".flac", reqst);
            String res = new FFMpegService().convertThenSegment(name,"320k");
            s3.uploadFolderToS3("track", trackId, new File(res));
            dao.updateTrackS3Status(trackId);

        }else if(!track.isS3Exists()) {
            writer.write(url);

            String fileName = HttpUtil.downloadFileAsync(url, trackId,".flac",reqst);
            String uploadFolderPath  = new FFMpegService().convertThenSegment(fileName,"320k");

            s3.uploadFolderToS3("track", trackId, new File(uploadFolderPath));

            dao.updateTrackS3Status(trackId);
        }else {
            writer.write("https://s3.regru.cloud/track/" + trackId + "/playlist.m3u8");
        }
        writer.flush();
        writer.close();
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException { }
}
