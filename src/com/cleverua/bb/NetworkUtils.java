package com.cleverua.bb;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

public class NetworkUtils {
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
        HttpConnection hconn = null;
        try {           
            Logger.debug("Openning connection");
            hconn = (HttpConnection) Connector.open(url);
            Logger.debug("Connection opened");
            
            Logger.debug("Getting response code");
            int responseCode = hconn.getResponseCode();
            Logger.debug("Got response code: "+responseCode);
            return true;
//            if (responseCode != HttpConnection.HTTP_OK) {
//                Logger.debug("Response code is not OK!");
//                return false;
////                throw new IOException("HTTP response code: " + responseCode);
//            }
//            Logger.debug("Got response: "+responseCode);
            
//            Logger.debug("Reading content");
//            InputStream is = hconn.openInputStream();
//            String result = read(is);
//            Logger.debug("Received content. Length: "+result.length());
        } catch (Exception e) {
            Logger.debug("Got exception: " + e);
            return false;
        } finally {
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
