package com.cleverua.bb;

import java.io.EOFException;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.util.DataBuffer;

public class Transports {
    private static final String INTERFACE_WIFI = ";interface=wifi";
    private static final String CONNECTION_UID = ";ConnectionUID=";
    private static final String CONNECTION_TYPE_BIS = ";ConnectionType=mds-public";
    public static final int MDS         = 0;
    public static final int BIS         = 1;
    public static final int WAP         = 2;
    public static final int WAP2        = 3;
    public static final int WIFI        = 4;
    public static final int DIRECT_TCP  = 5;
    public static final int UNITE       = 6;

    /** 
     * CONFIG_TYPE_ constants which are used to find appropriate service books.
     * TODO Currently only Unite is detected this way. 
     */ 
    private static final int CONFIG_TYPE_WAP  = 0;
    private static final int CONFIG_TYPE_BES  = 1;
    
    private static final String UNITE_STR   = "unite";
    private static final String MMS_STR     = "mms";
    private static final String GPMDS_STR   = "gpmds";
    private static final String IPPP_STR    = "ippp";
    private static final String WAP_STR     = "wap";
    private static final String WPTCP_STR   = "wptcp";
    private static final String WIFI_STR    = "wifi";
    
    private static final String DEVICESIDE_TRUE  = ";deviceside=true";
    private static final String DEVICESIDE_FALSE = ";deviceside=false";

    private static Transports instance;

    private ServiceRecord srMDS;
    private ServiceRecord srBIS;
    private ServiceRecord srWAP;
    private ServiceRecord srWAP2;
    private ServiceRecord srWiFi;
    private ServiceRecord srUnite;

    private boolean coverageTCP   = false;
    private boolean coverageMDS   = false;
    private boolean coverageBIS   = false;
    private boolean coverageWAP   = false;
    private boolean coverageWAP2  = false;
    private boolean coverageWiFi  = false;
    private boolean coverageUnite = false;

    public static Transports getInstance() {
        if (instance == null) {
            instance = new Transports();
        }
        return instance;
    }

    public boolean isAcceptable(int transportType) {
        switch (transportType) {
            case Transports.MDS: 
                return coverageMDS;
            case Transports.BIS: 
                return coverageBIS;
            case Transports.WAP: 
                return coverageWAP;
            case Transports.WAP2: 
                return coverageWAP2;
            case Transports.WIFI: 
                return coverageWiFi;
            case Transports.DIRECT_TCP: 
                return coverageTCP;
            case Transports.UNITE: 
                return coverageUnite;
            default: 
                return false;
        }
    }
    
    public String getUrlForTransport(String baseUrl, int transportType) {
        switch (transportType) {
            case Transports.MDS: 
                return getMDSUrl(baseUrl);
            case Transports.BIS: 
                return getBISUrl(baseUrl);
            case Transports.WAP: 
                return getWAPUrl(baseUrl);
            case Transports.WAP2: 
                return getWAP2Url(baseUrl);
            case Transports.WIFI: 
                return getWiFiUrl(baseUrl);
            case Transports.DIRECT_TCP: 
                return getTCPUrl(baseUrl);
            case Transports.UNITE: 
                return getUniteUrl(baseUrl);
            default: 
                return null;
        }
    }
    
    private String getMDSUrl(String url) {
        return url + DEVICESIDE_FALSE;
    }

    private String getBISUrl(String baseUrl) {
        return baseUrl + CONNECTION_TYPE_BIS;
    }
    
    // TODO: Manage WAP's attributes
    private String getWAPUrl(String baseUrl) {
        return baseUrl + DEVICESIDE_TRUE;
    }
    
    private String getWAP2Url(String baseUrl) {
        return baseUrl + DEVICESIDE_TRUE + CONNECTION_UID + srWAP2.getUid();
    }
    
    private String getWiFiUrl(String baseUrl) {
        return baseUrl + INTERFACE_WIFI;
    }
    
    // TODO: manage APN's
    private String getTCPUrl(String baseUrl) {
        return baseUrl + DEVICESIDE_TRUE;
    }
    
    private String getUniteUrl(String baseUrl) {
        return baseUrl + DEVICESIDE_FALSE + CONNECTION_UID + srUnite.getUid();
    }
    
    private Transports() {
        init();
    }

    private void init() {
        ServiceBook sb = ServiceBook.getSB();
        ServiceRecord[] records = sb.getRecords();

        for (int i = 0; i < records.length; i++) {
            ServiceRecord myRecord = records[i];
            String cid, uid;

            if (myRecord.isValid() && !myRecord.isDisabled()) {
                cid = myRecord.getCid().toLowerCase();
                uid = myRecord.getUid().toLowerCase();
                // BIS
                if (cid.indexOf(IPPP_STR) != -1 && uid.indexOf(GPMDS_STR) != -1) {
                    srBIS = myRecord;
                }           

                // BES
                if (cid.indexOf(IPPP_STR) != -1 && uid.indexOf(GPMDS_STR) == -1) {
                    srMDS = myRecord;
                }
                // WiFi
                if (cid.indexOf(WPTCP_STR) != -1 && uid.indexOf(WIFI_STR) != -1) {
                    srWiFi = myRecord;
                }       
                // Wap1.0
                if (getConfigType(myRecord)==CONFIG_TYPE_WAP && cid.equalsIgnoreCase(WAP_STR)) {
                    srWAP = myRecord;
                }
                // Wap2.0
                if (cid.indexOf(WPTCP_STR) != -1 && uid.indexOf(WIFI_STR) == -1 && uid.indexOf(MMS_STR) == -1) {
                    srWAP2 = myRecord;
                }
                // Unite
                if(getConfigType(myRecord) == CONFIG_TYPE_BES && myRecord.getName().equalsIgnoreCase(UNITE_STR)) {
                    srUnite = myRecord;
                }
            }   
        }
        
        if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B)){
            coverageBIS=true;   
        }       
        if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT)){
            coverageTCP=true;
            coverageWAP=true;
            coverageWAP2=true;
        }
        if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS)){           
            coverageMDS=true;
            coverageUnite=true;
        }   
        
        if(WLANInfo.getWLANState()==WLANInfo.WLAN_STATE_CONNECTED){
            coverageWiFi = true;
        }
    }

    /**
     * Gets the config type of a ServiceRecord using getDataInt below
     * @param record    A ServiceRecord
     * @return  configType of the ServiceRecord
     */
    private int getConfigType(ServiceRecord record) {
        return getDataInt(record, 12);
    }

    /**
     * Gets the config type of a ServiceRecord. Passing 12 as type returns the configType.    
     * @param record    A ServiceRecord
     * @param type  dataType
     * @return  configType
     */
    private int getDataInt(ServiceRecord record, int type)
    {
        DataBuffer buffer = null;
        buffer = getDataBuffer(record, type);

        if (buffer != null){
            try {
                return ConverterUtilities.readInt(buffer);
            } catch (EOFException e) {
                return -1;
            }
        }
        return -1;
    }

    /** 
     * Utility Method for getDataInt()
     */
    private DataBuffer getDataBuffer(ServiceRecord record, int type) {
        byte[] data = record.getApplicationData();
        if (data != null) {
            DataBuffer buffer = new DataBuffer(data, 0, data.length, true);
            try {
                buffer.readByte();
            } catch (EOFException e1) {
                return null;
            }
            if (ConverterUtilities.findType(buffer, type)) {
                return buffer;
            }
        }
        return null;
    }
}
