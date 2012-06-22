package com.cleverua.bb;

import java.io.EOFException;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.util.DataBuffer;
import net.rim.device.api.util.TLEUtilities;

public class Transports {
    private static final int BYTE_CONFIG_TYPE   = 12;
    private static final int BYTE_PROXY_ADDRESS = 28;
    
    private static final String INTERFACE_WIFI = ";interface=wifi";
    private static final String CONNECTION_UID = ";ConnectionUID=";
    // only for RIM partners : 
    // http://www.blackberryforums.com/developer-forum/104107-anyone-heard-deiceside-false-connectiontype-mds-public.html
    // http://www.blackberryforums.com/developer-forum/204371-confused-between-bis-bes-network-connetion-strings.html
    private static final String CONNECTION_TYPE_BIS = "";
    
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
//    private ServiceRecord srWiFi;
    private ServiceRecord srUnite;
    private ServiceRecord srTCP;

    private boolean coverageTCP   = false;
    private boolean coverageMDS   = false;
    private boolean coverageBIS   = false;
    private boolean coverageWAP   = false;
    private boolean coverageWAP2  = false;
    private boolean coverageWiFi  = false;
    private boolean coverageUnite = false;
    
    private String lastSuccessfulTransport;

    public static Transports getInstance() {
        if (instance == null) {
            instance = new Transports();
        }
        return instance;
    }

    public boolean isMDSAcceptable() {
        return coverageMDS && (srMDS != null);
    }
    
    public boolean isUniteAcceptable() {
        return coverageUnite && (srUnite != null);
    }

    public boolean isTCPAcceptable() {
        return coverageTCP;
    }

    public boolean isWiFiAcceptable() {
        return coverageWiFi;
    }

    public boolean isWAP2Acceptable() {
        return coverageWAP2 && (srWAP2 != null);
    }

    public boolean isWAPAcceptable() {
        return coverageWAP && (srWAP != null);
    }

    public boolean isBISAcceptable() {
        return coverageBIS && (srBIS != null);
    }
    
    public boolean isAcceptable(String transportType) {
        if (NetworkTypes.MDS.equals(transportType)) {
            return isMDSAcceptable();
        } else if (NetworkTypes.BIS.equals(transportType)) {
            return isBISAcceptable();
//        } else if (NetworkTypes.WAP.equals(transportType)) {
//            return isWAPAcceptable();
        } else if (NetworkTypes.WAP2.equals(transportType)) {
            return isWAP2Acceptable();
        } else if (NetworkTypes.WIFI.equals(transportType)) {
            return isWiFiAcceptable();
        } else if (NetworkTypes.DIRECT_TCP.equals(transportType)) {
            return isTCPAcceptable();   
        } else if (NetworkTypes.UNITE.equals(transportType)) {
            return isUniteAcceptable();
        } else {
            return false;
        }
    }

    public String getUrlForTransport(String baseUrl, String transportType) {
        if (NetworkTypes.MDS.equals(transportType)) {
            Logger.debug("Preparing url for MDS...");
            return getMDSUrl(baseUrl);
            
        } else if (NetworkTypes.BIS.equals(transportType)) {            
            Logger.debug("Preparing url for BIS...");
            return getBISUrl(baseUrl);
            
//        } else if (NetworkTypes.WAP.equals(transportType)) {
//            Logger.debug("Preparing url for WAP 1.x...");
//            return getWAPUrl(baseUrl);
            
        } else if (NetworkTypes.WAP2.equals(transportType)) {
            Logger.debug("Preparing url for WAP2...");
            return getWAP2Url(baseUrl);
            
        } else if (NetworkTypes.WIFI.equals(transportType)) {
            Logger.debug("Preparing url for WiFi...");
            return getWiFiUrl(baseUrl);
            
        } else if (NetworkTypes.DIRECT_TCP.equals(transportType)) {
            Logger.debug("Preparing url for Direct TCP...");
            return getTCPUrl(baseUrl);
            
        } else if (NetworkTypes.UNITE.equals(transportType)) {
            Logger.debug("Preparing url for Unite...");
            return getUniteUrl(baseUrl);
        } else {
            return null;
        }
    }
    
    public String getLastSuccessfulTransport() {
        return lastSuccessfulTransport;
    }

    public void setLastSuccessfulTransport(String lastSuccessfulTransport) {
        this.lastSuccessfulTransport = lastSuccessfulTransport;
    }

    public String getMDSUrl(String url) {
        return url + DEVICESIDE_FALSE;
    }

    public String getBISUrl(String baseUrl) {
        return baseUrl + DEVICESIDE_FALSE + CONNECTION_TYPE_BIS;
    }
    
    // TODO: Manage WAP's attributes (for WAP 1.x)
    public String getWAPUrl(String baseUrl) {
        return baseUrl + DEVICESIDE_TRUE;
    }
    
    public String getWAP2Url(String baseUrl) {
//        if (srWAP2 == null) {
//            throw new IllegalStateException("WAP2 is not supported!");
//        }
        return baseUrl + DEVICESIDE_TRUE + CONNECTION_UID + StringUtils.encodeUrl(srWAP2.getUid());
    }
    
    public String getWiFiUrl(String baseUrl) {
        return baseUrl + INTERFACE_WIFI;
    }
    
    /**
     * If there is no TCP service record on the device the current method intends 
     * that user has filled the APN settings in device options. 
     * So the result url WILL NOT include the 
     * <b>apn</b>, <b>tunnelauthusername</b> and <b>tunnelauthpassword</b> parameters. 
     */
    public String getTCPUrl(String baseUrl) {
        if (srTCP == null) {
            return baseUrl + DEVICESIDE_TRUE;
        } else {
            return baseUrl + DEVICESIDE_TRUE + CONNECTION_UID + StringUtils.encodeUrl(srTCP.getUid());
        }
    }
    
    public String getUniteUrl(String baseUrl) {
        if (srUnite == null) {
            throw new IllegalStateException("Unite is not supported!");
        }
        return baseUrl + DEVICESIDE_FALSE + CONNECTION_UID + StringUtils.encodeUrl(srUnite.getUid());
    }
    
    private Transports() {
        init();
    }

    private void init() {
        Logger.debug(" ====== Transports detection and initialization ===== ");
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
                    Logger.debug("BIS record detected!");
                    Logger.debug("cid = " + cid + ", uid = " + uid);
                    srBIS = myRecord;
                }           

                // BES
                if (cid.indexOf(IPPP_STR) != -1 && uid.indexOf(GPMDS_STR) == -1) {
                    Logger.debug("BES record detected!");
                    Logger.debug("cid = " + cid + ", uid = " + uid);
                    srMDS = myRecord;
                }
                /* WiFi - we do not need the WiFi record. 
                 * If the mobile network is disabled the records in the Service Book is disabled too. 
                 * But in does not mean that WiFi is unavailable. 
                 * So we'll check the WiFi with the other methods later. */
//                if (cid.indexOf(WPTCP_STR) != -1 && uid.indexOf(WIFI_STR) != -1) {
//                    Logger.debug("WiFi record detected!");
//                    Logger.debug("cid = " + cid + ", uid = " + uid);
//                    srWiFi = myRecord;
//                }       
                // Wap1.0
                if (getConfigType(myRecord)==CONFIG_TYPE_WAP && cid.equalsIgnoreCase(WAP_STR)) {
                    Logger.debug("WAP 1.x record detected!");
                    Logger.debug("cid = " + cid + ", uid = " + uid);
                    srWAP = myRecord;
                }
                // Wap2.0
                if (cid.indexOf(WPTCP_STR) != -1 && uid.indexOf(WIFI_STR) == -1 && uid.indexOf(MMS_STR) == -1) {
                    String httpProxyAddress = getDataString(myRecord, BYTE_PROXY_ADDRESS);
                    if ((httpProxyAddress == null) || StringUtils.isBlank(httpProxyAddress.trim())) {
                        Logger.debug("TCP record detected!");
                        Logger.debug("cid = " + cid + ", uid = " + uid);
                        srTCP = myRecord;
                    } else {
                        Logger.debug("WAP 2 record detected!");
                        Logger.debug("cid = " + cid + ", uid = " + uid + ", proxy = " + httpProxyAddress);
                        srWAP2 = myRecord;
                    }
                }
                // Unite
                if(getConfigType(myRecord) == CONFIG_TYPE_BES && myRecord.getName().equalsIgnoreCase(UNITE_STR)) {
                    Logger.debug("Unite record detected!");
                    Logger.debug("cid = " + cid + ", uid = " + uid);
                    srUnite = myRecord;
                }
            }   
        }
        
        if (!CoverageInfo.isOutOfCoverage()) { /* data service turned ON in options */
            if (RadioInfo.isDataServiceOperational()) { /* radio enabled */
                if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B)){
                    Logger.debug("BIS coverage detected!");
                    coverageBIS=true;   
                }  
                
                if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT)){
                    Logger.debug("TCP, WAP 1.x, WAP 2 coverages detected!");
                    coverageTCP=true;
                    coverageWAP=true;
                    coverageWAP2=true;
                }
                
                // CoverageInfo.isCoverageSufficient always returns 'false' for simulator
                if(CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS) || 
                        DeviceInfo.isSimulator()){
                    Logger.debug("BES-MDS, Unite coverage detected!");
                    coverageMDS=true;
                    coverageUnite=true;
                }   
            }
            
            if(RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN) /* does the device support WiFi? */ && 
                    WLANInfo.getWLANState()==WLANInfo.WLAN_STATE_CONNECTED /* is the WiFi connected? */){
                Logger.debug("WiFi coverage detected!");
                coverageWiFi = true;
            }
        }
        Logger.debug(" ====== End of detection and initialization ===== ");
    }

    /**
     * Gets the config type of a ServiceRecord using getDataInt below
     * @param record a ServiceRecord
     * @return  configType of the ServiceRecord
     */
    private int getConfigType(ServiceRecord record) {
        return getDataInt(record, BYTE_CONFIG_TYPE);
    }

    /**
     * the int Value corresponding to the parameter tag from the IPPP Service Record's encoded Application Data.    
     * @param record a ServiceRecord
     * @param type  a byte representing the IPPP Application Data specific parameter tag.
     * @return  an int representing the value assigned to the particular parameter tag or -1 if the encoded value is not found
     */
    private int getDataInt(ServiceRecord record, int type)
    {
        int dataInt = -1;
        DataBuffer buffer = null;
        buffer = getDataBuffer(record, type);

        if ((buffer != null) && ConverterUtilities.findType(buffer, type)) {
            try {
                dataInt = ConverterUtilities.readInt(buffer);
            } catch (EOFException e) {
              // dataInt remains -1  
            }
        }
        return dataInt;
    }

    /**
     * Determines the String Value corresponding to the parameter tag from the WPTCP Service Record's encoded Application Data    
     * @param record a ServiceRecord
     * @param type  a byte representing the WPTCP Application Data specific parameter tag.
     * @return  a String representing the value assigned to the particular parameter tag or <b>null</b> if the encoded value does not exist.
     */
    private String getDataString(ServiceRecord record, int type) {
        String stringData = null;
        DataBuffer buffer = null;
        buffer = getDataBuffer(record, type);
        if ((buffer != null) && TLEUtilities.findType(buffer, type)) {
            try {
                stringData = TLEUtilities.readStringField(buffer, type);
            } catch (Throwable e) {
                // stringData remains null
            }
        }
        return stringData;
    }
    
    private DataBuffer getDataBuffer(ServiceRecord record, int type) {
        byte[] data = record.getApplicationData();
        if (data != null) {
            DataBuffer buffer = new DataBuffer(data, 0, data.length, true);
            try {
                buffer.readByte();
            } catch (EOFException e1) {
                return null;
            }
        }
        return null;
    }
}
