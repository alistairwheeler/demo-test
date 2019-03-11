package com.simplicite.commons.Demo;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.simplicite.objects.Demo.DemoOrder;
import com.simplicite.util.AppLog;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectField;
import com.simplicite.util.Tool;
import com.simplicite.util.tools.PDFTool;

/**
 * Demo commons
 */
public class DemoCommon implements java.io.Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Check whether stock is low
	 * @param grant Grant
	 * @param prdId Product row ID
	 * @param stock Current stock for product
	 */
	public static boolean isLowStock(Grant grant, int prdId, int stock) {
		// Get work instance for DemoOrder object
		DemoOrder ord = (DemoOrder)grant.getTmpObject("DemoOrder");

		// Set search filters (on the last N days)
		ord.resetFilters();
		ord.getField("demoOrdPrdId").setFilter(prdId);
		int checkPeriod = grant.getIntParameter("DEMO_PRD_LOWSTOCK_PERIOD", 90);
		ord.getField("demoOrdDate").setFilterDateMin(Tool.shiftDays(Tool.getCurrentDate(), -checkPeriod));

		// Search
		List<String[]> rows = ord.search();
		AppLog.info(DemoCommon.class, "isLowStock", rows.size() + " orders found", grant);

		// Iterate over search result to calculate total ordered quantity
		int quantity = 0;
		int quantityIndex = ord.getFieldIndex("demoOrdQuantity");
		for (String[] row : rows)
			quantity += Tool.parseInt(row[quantityIndex], 10);
		AppLog.info(DemoCommon.class, "isLowStock", "Total ordered quantity = " + quantity, grant);

		// Stock is considered low if less than X% of total ordered quantity
		AppLog.info(DemoCommon.class, "isLowStock", "Current stock = " + stock, grant);
		int threshold = Math.round((grant.getIntParameter("DEMO_PRD_LOWSTOCK_THRESHOLD", 10) / 100) * quantity);
		AppLog.info(DemoCommon.class, "isLowStock", "Low stock threshold " + threshold, grant);
		boolean lowStock = stock < threshold;
		AppLog.info(DemoCommon.class, "isLowStock", "Low stock " + lowStock, grant);

		return lowStock;
	}

	/**
	 * Order receipt publication as PDF
	 * @param ord Order object
	 */
	public static byte[] orderReceipt(DemoOrder ord) {
		try (ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream()) {
			Document pdf = PDFTool.open(bos);

			// Logo
			pdf.add(PDFTool.getImageFromResource(ord.getGrant(), "DEMO_LOGO"));

			pdf.add(new Paragraph(ord.getGrant().T("DEMO_RECEIPT"), PDFTool.TITLE1));

			ObjectField f = ord.getField("demoOrdNumber");
			pdf.add(new Paragraph(f.getDisplay() + ": " + f.getValue(), PDFTool.TITLE2));
			f = ord.getField("demoOrdDate");
			pdf.add(new Paragraph(f.getDisplay() + ": " + ord.getGrant().toFormattedDate(f.getValue())));
			f = ord.getField("demoOrdDeliveryDate");
			pdf.add(new Paragraph(f.getDisplay() + ": " + ord.getGrant().toFormattedDatetime(f.getValue())));

			PdfPTable t = PDFTool.getTable(2, false);
			t.addCell(PDFTool.getHeaderCell(ord.getField("demoOrdCliId").getDisplay(), java.awt.Color.LIGHT_GRAY));
			t.addCell(PDFTool.getHeaderCell(ord.getField("demoOrdPrdId").getDisplay(), java.awt.Color.LIGHT_GRAY));

			PdfPCell c = PDFTool.getCell(null);
			f = ord.getField("demoOrdCliId.demoCliCode");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdCliId.demoCliFirstname");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdCliId.demoCliLastname");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));

			c.addElement(new Paragraph("\n"));

			f = ord.getField("demoOrdCliId.demoCliAddress1");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdCliId.demoCliAddress2");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdCliId.demoCliAddress3");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdCliId.demoCliZipCode");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdCliId.demoCliCountry");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			t.addCell(c);

			c = PDFTool.getCell(null);
			f = ord.getField("demoOrdPrdId.demoPrdSupId.demoSupName");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			String v = ord.getField("demoOrdPrdId.demoPrdSupId.demoSupLogo").getValue();
			if (!Tool.isEmpty(v)) {
				Image i = PDFTool.getImageFromDBDoc(ord.getGrant(), v);
				i.scaleAbsoluteWidth(100);
				i.setSpacingBefore(10);
				i.setSpacingAfter(10);
				c.addElement(i);
			}
			f = ord.getField("demoOrdPrdId.demoPrdReference");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			f = ord.getField("demoOrdPrdId.demoPrdName");
			c.addElement(new Paragraph(f.getDisplay() + ": " + f.getValue()));
			v = ord.getField("demoOrdPrdId.demoPrdPicture").getValue();
			if (!Tool.isEmpty(v)) {
				Image i = PDFTool.getImageFromDBDoc(ord.getGrant(), v);
				i.scaleAbsoluteWidth(150);
				i.setSpacingBefore(10);
				i.setSpacingAfter(10);
				c.addElement(i);
			}
			t.addCell(c);

			pdf.add(t);

			f = ord.getField("demoOrdQuantity");
			pdf.add(new Paragraph(f.getDisplay() + ": " + f.getValue(), PDFTool.TITLE1));
			f = ord.getField("demoOrdUnitPrice");
			pdf.add(new Paragraph(f.getDisplay() + ": " + ord.getGrant().toFormattedFloat(f.getValue(), 10, 2) + " Euros", PDFTool.TITLE2));
			f = ord.getField("demoOrdTotal");
			pdf.add(new Paragraph(f.getDisplay() + ": " + ord.getGrant().toFormattedFloat(f.getValue(), 10, 2) + " Euros", PDFTool.TITLE1));
			f = ord.getField("demoOrdVAT");
			pdf.add(new Paragraph(f.getDisplay() + " (" + ord.getGrant().toFormattedFloat(ord.getGrant().getParameter("DEMO_VAT"), 10, 2) + "%): " + ord.getGrant().toFormattedFloat(f.getValue(), 10, 2) + " Euros", PDFTool.TITLE2));

			PDFTool.close(pdf);
			return bos.toByteArray();
		} catch (Exception e) {
			AppLog.error(DemoCommon.class, "orderReceipt", "Unable to generate order receipt", e, ord.getGrant());
			return null;
		}
	}
}
