package com.anpilogoff.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ConfigUtil {
    public static Properties loadConfig(String filename) {
        Properties prop = new Properties();
        try(InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream(filename)){
            if(is == null){ throw new IOException("Resource file not founded"); }
            prop.load(is);

        } catch (IOException e) {
            log.warn("Error while loading config file: {}", filename, e);
            throw new RuntimeException(e);
        }
        return prop;
    }
}
