package com.cleverua.bb;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class NetworkUtils {
    public static final String[] CONSUMER_TRANSPORT_PRIORITIES = new String[]{
        Transports.WIFI, Transports.WAP2, Transports.MDS, Transports.DIRECT_TCP};
    public static final String[] ENTERPRISE_TRANSPORT_PRIORITIES = new String[]{
        Transports.MDS, Transports.WIFI, Transports.WAP2, Transports.DIRECT_TCP};

    public static String getConnectionUrl(String baseUrl, String transportType) {
        Transports transports = Transports.getInstance();
        if (transports.isAcceptable(transportType)) {
            String url = transports.getUrlForTransport(baseUrl, transportType);
            if (testHTTP(url)) {
                return url;
            }
        }
        return null;
    }
    
    public static String getConnectionUrl(String baseUrl, String[] transportPriorities) {
        for (int i = 0; i < transportPriorities.length; i++) {
            String transportType = transportPriorities[i];
            String url  = getConnectionUrl(baseUrl, transportType);
            if (url != null) {
                return url;
            }
        }
        return null;
    }

    public static boolean testConnection(String baseUrl, String transportType) {
        if (Transports.getInstance().isAcceptable(transportType)) {
            String url = Transports.getInstance().getUrlForTransport(baseUrl, transportType);
            return testHTTP(url);
        }
        return false;
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
