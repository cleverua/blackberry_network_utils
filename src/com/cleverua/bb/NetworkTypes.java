package com.cleverua.bb;


public class NetworkTypes {
    public static final String MDS         = "MDS";
    public static final String BIS         = "BIS";
    private static final String WAP        = "WAP 1.x";    // WAP 1.x is not supported yet
    public static final String WAP2        = "WAP2";
    public static final String WIFI        = "WiFi";
    public static final String DIRECT_TCP  = "Direct TCP";
    public static final String UNITE       = "Unite";
    public static final String AUTOMATIC   = "Automatic";
    
    public static final String[] CONSUMER_NETWORK_PRIORITIES = new String[]{WIFI, WAP2, MDS, DIRECT_TCP};
    public static final String[] ENTERPRISE_NETWORK_PRIORITIES = new String[]{MDS, WIFI, WAP2, DIRECT_TCP};
}
