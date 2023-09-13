package com.simplicite.objects.Demo;

import java.util.*;

import com.simplicite.util.*;
import com.simplicite.util.exceptions.*;
import com.simplicite.util.tools.*;

/**
 * Business object DemoSupplier
 */
public class DemoSupplier extends ObjectDB {
	private static final long serialVersionUID = 1L;
	
	public void actionMethod(){
		postValidate();
	}
	
	@Override
	public void preExport() {
		AppLog.info("hi", null);
		getField("demoSupName").setExportable(false);
		getField("demoSupCode").setLabel("My custom label");
	}
	
	@Override
	public List<String> postValidate() {
		List<String> msgs = new ArrayList<>();
		
		//msgs.add(Message.formatInfo("INFO_CODE", "Message", "fieldName"));
		//msgs.add(Message.formatWarning("WARNING_CODE", "Message", "fieldName"));
		//msgs.add(Message.formatError("ERROR_CODE", "Message", "fieldName"));
		AppLog.info("preValidate", getGrant());
		getField("demoSupWebsite").setRequired(true);
		
		return msgs;
	}
}
