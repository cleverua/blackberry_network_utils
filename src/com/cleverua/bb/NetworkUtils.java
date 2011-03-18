package com.cleverua.bb;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class NetworkUtils {
    private static final String[] CONSUMER_PRIORITIES = new String[]{
        Transports.WIFI, Transports.WAP2, Transports.MDS, Transports.DIRECT_TCP};
    private static final String[] ENTERPRISE_PRIORITIES = new String[]{
        Transports.MDS, Transports.WIFI, Transports.WAP2, Transports.DIRECT_TCP};
    
    public static final TransportPriorities CONSUMER_TRANSPORT_PRIORITIES = new TransportPriorities(CONSUMER_PRIORITIES);
    public static final TransportPriorities ENTERPRISE_TRANSPORT_PRIORITIES = new TransportPriorities(ENTERPRISE_PRIORITIES);
    

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
    
    public static String getConnectionUrl(String baseUrl, TransportPriorities transportPriorities) {
        Transports transports = Transports.getInstance();
        
        String lastSuccessfulTransport = transports.getLastSuccessfulTransport();
        Logger.debug("Last successful transport is " + lastSuccessfulTransport);
        if (StringUtils.isNotBlank(lastSuccessfulTransport) && 
                transportPriorities.containsTransport(lastSuccessfulTransport)) {
            Logger.debug("Transport priorities contains sucessful transport!");
            String url  = getConnectionUrl(baseUrl, lastSuccessfulTransport);
            if (url != null) {
                return url;
            }
        }
        Logger.debug("Last successful transport unknown or unavailable! Going to check priorities...");
        String[] priorities = transportPriorities.getPriorities();
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
    
    private static class TransportPriorities {
        private String[] priorities;
        
        public TransportPriorities(String[] priorities) {
            this.priorities = priorities;
        }
        
        public String[] getPriorities() {
            return priorities;
        }

        public boolean containsTransport(String transport) {
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equals(transport)) {
                    return true;
                }
            }
            return false;
        }
    }
}
