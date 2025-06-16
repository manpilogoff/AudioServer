package com.anpilogoff.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    public static Properties loadConfig(String filename) throws IOException {
        Properties prop = new Properties();
        try(InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream(filename)){
            if(is == null){ throw new IOException("Resource file not founded"); }
            prop.load(is);
        }
        return prop;
    }
}
