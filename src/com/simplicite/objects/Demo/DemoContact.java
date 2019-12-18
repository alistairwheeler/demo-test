package com.simplicite.objects.Demo;

import java.util.ArrayList;
import java.util.List;

import com.simplicite.util.AppLog;
import com.simplicite.util.ObjectDB;
import com.simplicite.util.PrintTemplate;
import com.simplicite.util.Tool;
import com.simplicite.util.tools.ExcelPOITool;
import com.simplicite.util.tools.ExcelPOITool.ExcelCell;
import com.simplicite.util.tools.ExcelPOITool.ExcelRow;
import com.simplicite.util.tools.ExcelPOITool.ExcelSheet;

/**
 * Contact business object
 */
@SuppressWarnings("deprecation")
public class DemoContact extends ObjectDB {
	private static final long serialVersionUID = 1L;

	/** Publication: Microsoft Excel(R) contact sheet */
	public Object printExcel(PrintTemplate pt) {
		try {
			// Build rows from selected IDs or from current filters
			List<String[]>rows = new ArrayList<>();
			List<String> ids = getSelectedIds();
			if (!Tool.isEmpty(ids)) {
				for (int k = 0; k < ids.size(); k++)
					if (select(ids.get(k)))
						rows.add(getValues());
			} else {
				rows = search(false);
			}

			/* Old deprecated version (kept for backward compatibility */
			ExcelPOITool xls = new ExcelPOITool(true); // true = XLSX format
			ExcelSheet sh = xls.newSheet("Contacts");
			for (int i = 0; i < rows.size(); i++) {
				ExcelRow r = xls.newRow(i);
				String[] row = rows.get(i);
				for (int j = 0; j < row.length; j++) {
					ExcelCell c = xls.newCell(j, row[j]);
					r.add(c);
				}
				sh.add(r);
			}
			xls.add(sh);

			/* TODO: new version
			ExcelTool xls = new ExcelTool(true); // true = XLSX format
			Sheet sheet = xls.addSheet("Contacts");
			for (int i = 0; i < rows.size(); i++) {
				ExcelRow r = new ExcelRow(i);
				String[] row = rows.get(i);
				for (int j = 0; j < row.length; j++) {
					r.add(xls.newCell(j, row[j]));
				}
				xls.addRow(sheet, r);
			}*/

			return xls.generateToByteArray();
		} catch (Exception e) {
			AppLog.error(getClass(), "printExcel", "Unable to publish", e, getGrant());
			return null;
		}
	}

	/** Hook override: hide history records on tree view */
	@Override
	public boolean canReference(String objName, String fkFieldName) {
		return !isTreeviewInstance() || "DemoContactHistoric".equals(objName);
	}
}
