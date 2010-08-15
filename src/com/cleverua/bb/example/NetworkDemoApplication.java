package com.cleverua.bb.example;

import net.rim.device.api.ui.UiApplication;

public class NetworkDemoApplication extends UiApplication {
	private static NetworkDemoApplication application;

	public static void main(String[] args) {
	    application = new NetworkDemoApplication();
	    UiApplication.getUiApplication().pushScreen(new NetworkDemoScreen());
	    application.enterEventDispatcher();
	}
	
	private NetworkDemoApplication() {}
}
