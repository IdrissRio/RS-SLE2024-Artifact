/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xssf.usermodel;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestSheetShiftRows;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;

/**
 * @author Yegor Kozlov
 */
public final class TestXSSFSheetShiftRows extends BaseTestSheetShiftRows {

    public TestXSSFSheetShiftRows(){
        super(XSSFITestDataProvider.instance);
    }

    @Override
	public void testShiftRowBreaks() { // disabled test from superclass
        // TODO - support shifting of page breaks
    }

	public void testBug54524() throws IOException {
        XSSFWorkbook workbook = XSSFTestDataSamples.openSampleWorkbook("54524.xlsx");
        XSSFSheet sheet = workbook.getSheetAt(0);
		sheet.shiftRows(3, 5, -1);

        Cell cell = CellUtil.getCell(sheet.getRow(1), 0);
		assertEquals(1.0, cell.getNumericCellValue());
		cell = CellUtil.getCell(sheet.getRow(2), 0);
		assertEquals("SUM(A2:A2)", cell.getCellFormula());
		cell = CellUtil.getCell(sheet.getRow(3), 0);
		assertEquals("X", cell.getStringCellValue());
	}

	public void testBug53798() throws IOException {
		// NOTE that for HSSF (.xls) negative shifts combined with positive ones do work as expected  
		Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53798.xlsx");

		Sheet testSheet	= wb.getSheetAt(0);
		// 1) corrupted xlsx (unreadable data in the first row of a shifted group) already comes about  
		// when shifted by less than -1 negative amount (try -2)
		testSheet.shiftRows(3, 3, -2);
		
		Row newRow = null; Cell newCell = null;
		// 2) attempt to create a new row IN PLACE of a removed row by a negative shift causes corrupted 
		// xlsx file with  unreadable data in the negative shifted row. 
		// NOTE it's ok to create any other row.
		newRow = testSheet.createRow(3);
		newCell = newRow.createCell(0);
		newCell.setCellValue("new Cell in row "+newRow.getRowNum());
		
		// 3) once a negative shift has been made any attempt to shift another group of rows 
		// (note: outside of previously negative shifted rows) by a POSITIVE amount causes POI exception: 
		// org.apache.xmlbeans.impl.values.XmlValueDisconnectedException.
		// NOTE: another negative shift on another group of rows is successful, provided no new rows in  
		// place of previously shifted rows were attempted to be created as explained above.
		testSheet.shiftRows(6, 7, 1);	// -- CHANGE the shift to positive once the behaviour of  
										// the above has been tested
		
		//saveReport(wb, new File("/tmp/53798.xlsx"));
		Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
		assertNotNull(read);
		
		Sheet readSheet = read.getSheetAt(0);
		verifyCellContent(readSheet, 0, "0.0");
		verifyCellContent(readSheet, 1, "3.0");
		verifyCellContent(readSheet, 2, "2.0");
		verifyCellContent(readSheet, 3, "new Cell in row 3");
		verifyCellContent(readSheet, 4, "4.0");
		verifyCellContent(readSheet, 5, "5.0");
		verifyCellContent(readSheet, 6, null);
		verifyCellContent(readSheet, 7, "6.0");
		verifyCellContent(readSheet, 8, "7.0");
	}

	private void verifyCellContent(Sheet readSheet, int row, String expect) {
		Row readRow = readSheet.getRow(row);
		if(expect == null) {
			assertNull(readRow);
			return;
		}
		Cell readCell = readRow.getCell(0);
		if(readCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			assertEquals(expect, Double.toString(readCell.getNumericCellValue()));
		} else {
			assertEquals(expect, readCell.getStringCellValue());
		}
	}
	
	public void testBug53798a() throws IOException {
		Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53798.xlsx");

		Sheet testSheet	= wb.getSheetAt(0);
		testSheet.shiftRows(3, 3, -1);
        for (Row r : testSheet) {
        	r.getRowNum();
        }
		testSheet.shiftRows(6, 6, 1);
		
		//saveReport(wb, new File("/tmp/53798.xlsx"));
		Workbook read = XSSFTestDataSamples.writeOutAndReadBack(wb);
		assertNotNull(read);
		
		Sheet readSheet = read.getSheetAt(0);
		verifyCellContent(readSheet, 0, "0.0");
		verifyCellContent(readSheet, 1, "1.0");
		verifyCellContent(readSheet, 2, "3.0");
		verifyCellContent(readSheet, 3, null);
		verifyCellContent(readSheet, 4, "4.0");
		verifyCellContent(readSheet, 5, "5.0");
		verifyCellContent(readSheet, 6, null);
		verifyCellContent(readSheet, 7, "6.0");
		verifyCellContent(readSheet, 8, "8.0");
	}
	
	public void testBug56017() throws IOException {
	    Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56017.xlsx");

        Sheet sheet = wb.getSheetAt(0);

        Comment comment = sheet.getCellComment(0, 0);
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        
        sheet.shiftRows(0, 1, 1);

        // comment in row 0 is gone
        comment = sheet.getCellComment(0, 0);
        assertNull(comment);
        
        // comment is now in row 1
        comment = sheet.getCellComment(1, 0);
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        
//        FileOutputStream outputStream = new FileOutputStream("/tmp/56017.xlsx");
//        try {
//            wb.write(outputStream);
//        } finally {
//            outputStream.close();
//        }
        
        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertNotNull(wbBack);

        Sheet sheetBack = wbBack.getSheetAt(0);

        // comment in row 0 is gone
        comment = sheetBack.getCellComment(0, 0);
        assertNull(comment);

        // comment is now in row 1
        comment = sheetBack.getCellComment(1, 0);
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
	}

    public void test57171() throws Exception {
	    Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        removeAllSheetsBut(5, wb); // 5 is the active / selected sheet
        assertEquals(0, wb.getActiveSheetIndex());

        Workbook wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(0, wbRead.getActiveSheetIndex());

        wbRead.removeSheetAt(0);
        assertEquals(0, wbRead.getActiveSheetIndex());

        //wb.write(new FileOutputStream("/tmp/57171.xls"));
    }

    public void test57163() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        wb.removeSheetAt(0);
        assertEquals(4, wb.getActiveSheetIndex());

        //wb.write(new FileOutputStream("/tmp/57163.xls"));
    }

    public void testSetSheetOrderAndAdjustActiveSheet() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        
        assertEquals(5, wb.getActiveSheetIndex());

        // move the sheets around in all possible combinations to check that the active sheet
        // is set correctly in all cases
        wb.setSheetOrder(wb.getSheetName(5), 4);
        assertEquals(4, wb.getActiveSheetIndex());
        
        wb.setSheetOrder(wb.getSheetName(5), 5);
        assertEquals(4, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(3), 5);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(4), 5);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(2), 2);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(2), 1);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(3), 5);
        assertEquals(5, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(4, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(2, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(1, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(0, wb.getActiveSheetIndex());

        wb.setSheetOrder(wb.getSheetName(0), 5);
        assertEquals(5, wb.getActiveSheetIndex());
    }   

    public void testRemoveSheetAndAdjustActiveSheet() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        
        assertEquals(5, wb.getActiveSheetIndex());
        
        wb.removeSheetAt(0);
        assertEquals(4, wb.getActiveSheetIndex());
        
        wb.setActiveSheet(3);
        assertEquals(3, wb.getActiveSheetIndex());
        
        wb.removeSheetAt(4);
        assertEquals(3, wb.getActiveSheetIndex());

        wb.removeSheetAt(3);
        assertEquals(2, wb.getActiveSheetIndex());

        wb.removeSheetAt(0);
        assertEquals(1, wb.getActiveSheetIndex());

        wb.removeSheetAt(1);
        assertEquals(0, wb.getActiveSheetIndex());

        wb.removeSheetAt(0);
        assertEquals(0, wb.getActiveSheetIndex());

        try {
            wb.removeSheetAt(0);
            fail("Should catch exception as no more sheets are there");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(0, wb.getActiveSheetIndex());
        
        wb.createSheet();
        assertEquals(0, wb.getActiveSheetIndex());
        
        wb.removeSheetAt(0);
        assertEquals(0, wb.getActiveSheetIndex());
    }

    // TODO: enable when bug 57165 is fixed
    public void disabled_test57165() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        assertEquals(5, wb.getActiveSheetIndex());
        removeAllSheetsBut(3, wb);
        assertEquals(0, wb.getActiveSheetIndex());
        wb.createSheet("New Sheet1");
        assertEquals(0, wb.getActiveSheetIndex());
        wb.cloneSheet(0); // Throws exception here
        wb.setSheetName(1, "New Sheet");
        assertEquals(0, wb.getActiveSheetIndex());

        //wb.write(new FileOutputStream("/tmp/57165.xls"));
    }

//    public void test57165b() throws IOException {
//        Workbook wb = new XSSFWorkbook();
//        try {
//            wb.createSheet("New Sheet 1");
//            wb.createSheet("New Sheet 2");
//        } finally {
//            wb.close();
//        }
//    }

    private static void removeAllSheetsBut(int sheetIndex, Workbook wb)
    {
        int sheetNb = wb.getNumberOfSheets();
        // Move this sheet at the first position
        wb.setSheetOrder(wb.getSheetName(sheetIndex), 0);
        // Must make this sheet active (otherwise, for XLSX, Excel might protest that active sheet no longer exists)
        // I think POI should automatically handle this case when deleting sheets...
//      wb.setActiveSheet(0);
        for (int sn = sheetNb - 1; sn > 0; sn--)
        {
            wb.removeSheetAt(sn);
        }
    }
}
