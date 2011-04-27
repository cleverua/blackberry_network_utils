package com.cleverua.bb;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class NetworkUtils {

    public static String getConnectionUrl(String baseUrl, String transportType) {
        Logger.debug("Obtaining connection url for transport type: " + transportType);
        Transports transports = Transports.getInstance();
        if (transports.isAcceptable(transportType)) {
            Logger.debug(transportType + " is acceptable!");
            String url = transports.getUrlForTransport(baseUrl, transportType);
            if (testHTTP(url)) {
                Logger.debug("Url test successful!");
                transports.setLastSuccessfulTransport(transportType);
                return url;
            } else {
                Logger.debug("Url test failed!");
            }
        } else {
            Logger.debug(transportType + " is not acceptable!");
        }
        return null;
    }
    
    public static String getConnectionUrl(String baseUrl, String[] priorities) {
        Transports transports = Transports.getInstance();
        
        String lastSuccessfulTransport = transports.getLastSuccessfulTransport();
        Logger.debug("Last successful transport is " + lastSuccessfulTransport);
        if (StringUtils.isNotBlank(lastSuccessfulTransport)) {
            String url  = getConnectionUrl(baseUrl, lastSuccessfulTransport);
            if (url != null) {
                return url;
            }
        }
        Logger.debug("Last successful transport unknown or unavailable! Going to check priorities...");
        for (int i = 0; i < priorities.length; i++) {
            String transportType = priorities[i];
            String url  = getConnectionUrl(baseUrl, transportType);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public static boolean testConnection(String baseUrl, String transportType) {
        Logger.debug("Performing test of the " + transportType + " for connection " + baseUrl);
        if (Transports.getInstance().isAcceptable(transportType)) {
            Logger.debug(transportType + " is acceptable!");
            String url = Transports.getInstance().getUrlForTransport(baseUrl, transportType);
            return testHTTP(url);
        } else {
            Logger.debug(transportType + " is not acceptable!");
            return false;
        }
    }
    
    private static boolean testHTTP(String url) {
        Logger.debug("Testing url: " + url);
        HttpConnection hconn = null;
        try {           
            Logger.debug("Openning connection");
            hconn = (HttpConnection) Connector.open(url);
            Logger.debug("Connection opened");
            
            Logger.debug("Getting response code");
            int responseCode = hconn.getResponseCode();
            Logger.debug("Got response code: "+responseCode);
            return true;
        } catch (Exception e) {
            Logger.debug("Got exception: " + e);
            return false;
        } finally {
            if (hconn != null) {
                try {
                    Logger.debug("Closing connection");
                    hconn.close();
                    Logger.debug("Connection closed");
                } catch (IOException e) {
                    Logger.debug("Exception while closing HTTP connection: " + e);
                }
            }
        }
    }
}
