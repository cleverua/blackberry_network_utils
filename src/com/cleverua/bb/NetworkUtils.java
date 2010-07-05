package com.cleverua.bb;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class NetworkUtils {
    public static final int[] CONSUMER_TRANSPORT_PRIORITIES = new int[]{
        Transports.WIFI, Transports.WAP2, Transports.BIS, Transports.MDS, Transports.DIRECT_TCP};
    public static final int[] ENTERPRISE_TRANSPORT_PRIORITIES = new int[]{
        Transports.MDS, Transports.WIFI, Transports.WAP2, Transports.BIS, Transports.DIRECT_TCP};
    
    public static String getConnectionUrl(String baseUrl, int[] transportPriorities) {
        Transports transports = Transports.getInstance();
        for (int i = 0; i < transportPriorities.length; i++) {
            int transportType = transportPriorities[i];
            if (transports.isAcceptable(transportType)) {
                String url = Transports.getInstance().getUrlForTransport(baseUrl, transportType);
                if (testHTTP(url)) {
                    return url;
                }
            }
        }
        return null;
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
