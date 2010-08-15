package com.cleverua.bb.example;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.MainScreen;

import com.cleverua.bb.NetworkUtils;
import com.cleverua.bb.Transports;

public class NetworkDemoScreen extends MainScreen {
	private static final String TEST_FAILED_MSG 	= "Test failed!";
	private static final String TEST_SUCCESSFUL_MSG = "Test successful! Last successful network type: ";
	private static final String BUTTON_LABEL 		= "Perform network test...";
	private static final String NETWORK_TYPE_LABEL 	= "Network type";
	private static final String TITLE 				= "Network Utils Demo";

	private static final String BASE_URL = "http://www.google.com";

	private static final String[] NETWORK_TYPES = new String[] { Transports.AUTOMATIC,
			Transports.MDS, Transports.WAP2, Transports.WIFI, Transports.DIRECT_TCP, Transports.UNITE };
	
	private ObjectChoiceField networkTypes;
	
	public NetworkDemoScreen() {
		super();
		setTitle(TITLE);
		LabelField selectNetworktype = new LabelField("Please, select network type:");
		add(selectNetworktype);
		networkTypes = new ObjectChoiceField(NETWORK_TYPE_LABEL, NETWORK_TYPES);
		add(networkTypes);
		ButtonField networkTest = new ButtonField(BUTTON_LABEL);
		networkTest.setChangeListener(new FieldChangeListener() {
			public void fieldChanged(Field field, int context) {
				performTest();
			}
		});
		add(networkTest);
	}
	
	protected boolean onSavePrompt() {
		return true;
	}

	private String getSelectedNetworkType() {
		return (String) networkTypes.getChoice(networkTypes.getSelectedIndex()); 
	}
	
	private void performTest() {
		String networkType = getSelectedNetworkType();
		String url = null;
		if (Transports.AUTOMATIC.equals(networkType)) {
			url = NetworkUtils.getConnectionUrl(BASE_URL, NetworkUtils.CONSUMER_TRANSPORT_PRIORITIES); 
		} else {
			url = NetworkUtils.getConnectionUrl(BASE_URL, networkType);
		}
		
		if (url != null) {
			Dialog.alert(TEST_SUCCESSFUL_MSG + Transports.getInstance().getLastSuccessfulTransport());
		} else {
			Dialog.alert(TEST_FAILED_MSG);
		}
	}
}
