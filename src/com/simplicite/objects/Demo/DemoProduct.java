package com.simplicite.objects.Demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import com.simplicite.util.Action;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.Message;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.ObjectField;
import com.simplicite.util.PrintTemplate;
import com.simplicite.util.Tool;
import com.simplicite.util.annotations.BusinessObjectAction;
import com.simplicite.util.annotations.BusinessObjectPublication;
import com.simplicite.util.tools.DocxTool;
import com.simplicite.util.tools.MailTool;
import com.simplicite.util.tools.JUnitTool;

/**
 * Product business object
 */
public class DemoProduct extends ObjectDB {
	private static final long serialVersionUID = 1L;

	/** Default increment */
	public static final int DEFAULT_INCREMENT = 10;

	public static final String REFERENCE_FIELDNAME = "demoPrdReference";
	public static final String STOCK_FIELDNAME = "demoPrdStock";
	public static final String INCREMENT_FIELDNAME = "demoPrdIncrement";

@Override
public void initCreate() {
	if(isProcessInstance()){ //only apply if object is created via process
		String bimId = getGrant().simpleQuery("select row_id from demo_supplier where sup_code = 'BIM'");
		setFieldValue("demoPrdSupId", bimId);
		populate(true);
	}
}


	/** Init default increment */
	@Override
	public void initAction(Action action) {
		if ("DEMO_INCSTOCK".equals(action.getName())) {
			ObjectField f = action.getConfirmField(getGrant().getLang(), INCREMENT_FIELDNAME);
			if (f!=null) f.setDefaultValue(String.valueOf(DEFAULT_INCREMENT));
		}
	}

	/** Action: increase stock */
	@BusinessObjectAction
	public String increaseStock(Map<String, String> params) {
		return Message.formatError(null, "Erreur", "trnPrdName");
		/*int q = Tool.parseInt(params.get(INCREMENT_FIELDNAME), DEFAULT_INCREMENT);
		if (q > 0) {
			ObjectField s = getField(STOCK_FIELDNAME);
			s.setValue(s.getInt(0) + q);
			save();
			// Log
			AppLog.log("DEMO_INFO", getClass(), "increaseStock", "Stock for " + getFieldValue(REFERENCE_FIELDNAME) + " is now " + s.getValue(), getGrant());
			// User message
			return Message.formatSimpleInfo("DEMO_PRD_STOCK_INCREASED:" + s.getValue());
		} else {
			return Message.formatSimpleError("DEMO_PRD_ERR_INCREMENT:" + q);
		}*/
	}

	/** Action: decrease stock */
	@BusinessObjectAction
	public String decreaseStock() {
		// Decrease stock
		int q = getIntParameter("QUANTITY", 0);
		ObjectField s = getField(STOCK_FIELDNAME);
		s.setValue(s.getInt() - q);
		save();
		// Log
		AppLog.log("DEMO_INFO", getClass(), "decreaseStock", "Stock for " + getFieldValue(REFERENCE_FIELDNAME) + " is now " + s.getValue(), getGrant());
		// User message
		return Message.formatSimpleInfo("DEMO_PRD_STOCK_DECREASED:" + q);
	}

	/** Action method: send product data in an email */
	@BusinessObjectAction
	public String sendEmail(Action a) {
		try {
			MailTool mt = new MailTool(getGrant());
			mt.addRcpt(getGrant().getEmail());
			String ref = getFieldValue(REFERENCE_FIELDNAME);
			mt.setSubject(getName() + " " + ref);
			mt.addAttach(this, getField("demoPrdBrochure"));
			String picCid = mt.addImage(this, getField("demoPrdPicture"));
			mt.setBody(
				"<h1>" + Tool.toHTML(ref) + "</h1>" +
				"<img src=\"cid:" + picCid +  "\"/>" +
				"<h3>" + Tool.toHTML(getFieldValue("demoPrdName")) + "</h3>" +
				"<h5>" + Tool.toHTML(getFieldValue("demoPrdDescription")) + "</h5>" +
				"<div>" + getFieldValue("demoPrdDocumentation") + "</div>"
			);
			mt.send();
			return Message.formatSimpleInfo("OK");
		} catch (Exception e) {
			AppLog.error(null, e, getGrant());
			return Message.formatSimpleError(e.getMessage());
		}
	}

	/** Publication: Microsoft Word(R) catalog */
	@BusinessObjectPublication
	public Object printCatalog(PrintTemplate pt) {
		try {
			DocxTool d = new DocxTool();
			d.newDocument();
			d.addStyledParagraph(DocxTool.STYLE_TITLE, getFieldValue("demoPrdName") + " (" + getFieldValue(REFERENCE_FIELDNAME) + ")");
			d.addParagraph(getFieldValue("demoPrdDescription"));
			d.addHTML(getFieldValue("demoPrdDocumentation"));
			return d.toByteArray();
		} catch (Exception e) {
			AppLog.error("Unable to publish " + pt.getName(), e, getGrant());
			return e.getMessage();
		}
	}

	/** Hook override: custom short label */
	@Override
	public String getUserKeyLabel(String[] row) {
		return getFieldValue(REFERENCE_FIELDNAME, row);
	}

	/** Hook override: hide history records on tree view */
	@Override
	public boolean canReference(String objName, String fkFieldName) {
		return !isTreeviewInstance() || "DemoProductHistoric".equals(objName);
	}

	/** JUnit test class */
	public static class DemoProductTest {
		/** Decrement test */
		@Test
		public void testDecrement() {
			try {
				ObjectDB prd = Grant.getSystemAdmin().getTmpObject("DemoProduct");
				prd.setValues(prd.search().get(0), true);
				ObjectField s = prd.getField(STOCK_FIELDNAME);
				int n = s.getInt(0);
				prd.setParameter("QUANTITY", 10);
				prd.invokeAction("DEMO_DECSTOCK");
				assertEquals((long)n - 10, s.getInt(0));
			} catch (Exception e) {
				fail(e.getMessage());
			}
		}
	}

	/** Hook override: launch JUnit tests classes */
	@Override
	public String unitTests() {
		JUnitTool t = new JUnitTool(getGrant());
		return
			t.run("com.simplicite.tests.Demo.DemoTests") + // Shared code unit tests class
			t.run(DemoProductTest.class); // Nested test class
	}
}
