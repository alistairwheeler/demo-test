package com.simplicite.commons.Demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.simplicite.objects.Demo.DemoProduct;
import com.simplicite.util.Grant;
import com.simplicite.util.ObjectField;

/**
 * Product business object server unit tests
 */
public class DemoProductTest {
	/** Increment test */
	@Test
	public void testIncrement() {
		try {
			DemoProduct prd = (DemoProduct)Grant.getSystemAdmin().getTmpObject("DemoProduct");
			prd.setValues(prd.search().get(0));
			ObjectField s = prd.getField("demoPrdStock");
			int n = s.getInt(0);
			prd.increaseStock();
			assertEquals(n + DemoProduct.INCREMENT, s.getInt(0));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
