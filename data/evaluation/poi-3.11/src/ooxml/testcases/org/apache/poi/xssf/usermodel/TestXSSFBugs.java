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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.POIDataSamples;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLException;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.Function;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.XLSBUnsupportedException;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.junit.Ignore;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCols;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedNames;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.impl.CTFontImpl;

public final class TestXSSFBugs extends BaseTestBugzillaIssues {

    public TestXSSFBugs() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * test writing a file with large number of unique strings,
     * open resulting file in Excel to check results!
     */
    @Test
    public void bug15375_2() {
        bug15375(1000);
    }

    /**
     * Named ranges had the right reference, but
     *  the wrong sheet name
     */
    @Test
    public void bug45430() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("45430.xlsx");
        assertFalse(wb.isMacroEnabled());
        assertEquals(3, wb.getNumberOfNames());

        assertEquals(0, wb.getNameAt(0).getCTName().getLocalSheetId());
        assertFalse(wb.getNameAt(0).getCTName().isSetLocalSheetId());
        assertEquals("SheetA!$A$1", wb.getNameAt(0).getRefersToFormula());
        assertEquals("SheetA", wb.getNameAt(0).getSheetName());

        assertEquals(0, wb.getNameAt(1).getCTName().getLocalSheetId());
        assertFalse(wb.getNameAt(1).getCTName().isSetLocalSheetId());
        assertEquals("SheetB!$A$1", wb.getNameAt(1).getRefersToFormula());
        assertEquals("SheetB", wb.getNameAt(1).getSheetName());

        assertEquals(0, wb.getNameAt(2).getCTName().getLocalSheetId());
        assertFalse(wb.getNameAt(2).getCTName().isSetLocalSheetId());
        assertEquals("SheetC!$A$1", wb.getNameAt(2).getRefersToFormula());
        assertEquals("SheetC", wb.getNameAt(2).getSheetName());

        // Save and re-load, still there
        XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(3, nwb.getNumberOfNames());
        assertEquals("SheetA!$A$1", nwb.getNameAt(0).getRefersToFormula());
    }

    /**
     * We should carry vba macros over after save
     */
    @Test
    public void bug45431() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("45431.xlsm");
        OPCPackage pkg = wb.getPackage();
        assertTrue(wb.isMacroEnabled());

        // Check the various macro related bits can be found
        PackagePart vba = pkg.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
        );
        assertNotNull(vba);
        // And the drawing bit
        PackagePart drw = pkg.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
        );
        assertNotNull(drw);


        // Save and re-open, both still there
        XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        OPCPackage nPkg = nwb.getPackage();
        assertTrue(nwb.isMacroEnabled());

        vba = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
        );
        assertNotNull(vba);
        drw = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
        );
        assertNotNull(drw);

        // And again, just to be sure
        nwb = XSSFTestDataSamples.writeOutAndReadBack(nwb);
        nPkg = nwb.getPackage();
        assertTrue(nwb.isMacroEnabled());

        vba = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
        );
        assertNotNull(vba);
        drw = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
        );
        assertNotNull(drw);
    }

    @Test
    public void bug47504() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("47504.xlsx");
        assertEquals(1, wb.getNumberOfSheets());
        XSSFSheet sh = wb.getSheetAt(0);
        XSSFDrawing drawing = sh.createDrawingPatriarch();
        List<POIXMLDocumentPart> rels = drawing.getRelations();
        assertEquals(1, rels.size());
        assertEquals("Sheet1!A1", rels.get(0).getPackageRelationship().getTargetURI().getFragment());

        // And again, just to be sure
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(1, wb.getNumberOfSheets());
        sh = wb.getSheetAt(0);
        drawing = sh.createDrawingPatriarch();
        rels = drawing.getRelations();
        assertEquals(1, rels.size());
        assertEquals("Sheet1!A1", rels.get(0).getPackageRelationship().getTargetURI().getFragment());
    }
    
    /**
     * Excel will sometimes write a button with a textbox
     *  containing &gt;br&lt; (not closed!).
     * Clearly Excel shouldn't do this, but test that we can
     *  read the file despite the naughtyness
     */
    @Test
    public void bug49020() throws Exception {
       /*XSSFWorkbook wb =*/ XSSFTestDataSamples.openSampleWorkbook("BrNotClosed.xlsx");
    }

    /**
     * ensure that CTPhoneticPr is loaded by the ooxml test suite so that it is included in poi-ooxml-schemas
     */
    @Test
    public void bug49325() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("49325.xlsx");
        CTWorksheet sh = wb.getSheetAt(0).getCTWorksheet();
        assertNotNull(sh.getPhoneticPr());
    }
    
    /**
     * Names which are defined with a Sheet
     *  should return that sheet index properly 
     */
    @Test
    public void bug48923() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48923.xlsx");
       assertEquals(4, wb.getNumberOfNames());
       
       Name b1 = wb.getName("NameB1");
       Name b2 = wb.getName("NameB2");
       Name sheet2 = wb.getName("NameSheet2");
       Name test = wb.getName("Test");
       
       assertNotNull(b1);
       assertEquals("NameB1", b1.getNameName());
       assertEquals("Sheet1", b1.getSheetName());
       assertEquals(-1, b1.getSheetIndex());
       
       assertNotNull(b2);
       assertEquals("NameB2", b2.getNameName());
       assertEquals("Sheet1", b2.getSheetName());
       assertEquals(-1, b2.getSheetIndex());
       
       assertNotNull(sheet2);
       assertEquals("NameSheet2", sheet2.getNameName());
       assertEquals("Sheet2", sheet2.getSheetName());
       assertEquals(-1, sheet2.getSheetIndex());
       
       assertNotNull(test);
       assertEquals("Test", test.getNameName());
       assertEquals("Sheet1", test.getSheetName());
       assertEquals(-1, test.getSheetIndex());
    }
    
    /**
     * Problem with evaluation formulas due to
     *  NameXPtgs.
     * Blows up on:
     *   IF(B6= (ROUNDUP(B6,0) + ROUNDDOWN(B6,0))/2, MROUND(B6,2),ROUND(B6,0))
     * 
     * TODO: delete this test case when MROUND and VAR are implemented
     */
    @Test
    public void bug48539() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48539.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       assertEquals(0, wb.getNumberOfNames());
       
       // Try each cell individually
       XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
       for(int i=0; i<wb.getNumberOfSheets(); i++) {
          Sheet s = wb.getSheetAt(i);
          for(Row r : s) {
             for(Cell c : r) {
                if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    String formula = c.getCellFormula();
                    CellValue cv;
                    try {
                        cv = eval.evaluate(c);
                    } catch (Exception e) {
                        throw new RuntimeException("Can't evaluate formula: " + formula, e);
                    }
                    
                    if(cv.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        // assert that the calculated value agrees with
                        // the cached formula result calculated by Excel
                        double cachedFormulaResult = c.getNumericCellValue();
                        double evaluatedFormulaResult = cv.getNumberValue();
                        assertEquals(c.getCellFormula(), cachedFormulaResult, evaluatedFormulaResult, 1E-7);
                    }
                }
             }
          }
       }
       
       // Now all of them
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
    }
    
    /**
     * Foreground colours should be found even if
     *  a theme is used 
     */
    @Test
    public void bug48779() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48779.xlsx");
       XSSFCell cell = wb.getSheetAt(0).getRow(0).getCell(0);
       XSSFCellStyle cs = cell.getCellStyle();
       
       assertNotNull(cs);
       assertEquals(1, cs.getIndex());

       // Look at the low level xml elements
       assertEquals(2, cs.getCoreXf().getFillId());
       assertEquals(0, cs.getCoreXf().getXfId());
       assertEquals(true, cs.getCoreXf().getApplyFill());
       
       XSSFCellFill fg = wb.getStylesSource().getFillAt(2);
       assertEquals(0, fg.getFillForegroundColor().getIndexed());
       assertEquals(0.0, fg.getFillForegroundColor().getTint(), 0);
       assertEquals("FFFF0000", fg.getFillForegroundColor().getARGBHex());
       assertEquals(64, fg.getFillBackgroundColor().getIndexed());
       
       // Now look higher up
       assertNotNull(cs.getFillForegroundXSSFColor());
       assertEquals(0, cs.getFillForegroundColor());
       assertEquals("FFFF0000", cs.getFillForegroundXSSFColor().getARGBHex());
       assertEquals("FFFF0000", cs.getFillForegroundColorColor().getARGBHex());
       
       assertNotNull(cs.getFillBackgroundColor());
       assertEquals(64, cs.getFillBackgroundColor());
       assertEquals(null, cs.getFillBackgroundXSSFColor().getARGBHex());
       assertEquals(null, cs.getFillBackgroundColorColor().getARGBHex());
    }
    
    /**
     * With HSSF, if you create a font, don't change it, and
     *  create a 2nd, you really do get two fonts that you 
     *  can alter as and when you want.
     * With XSSF, that wasn't the case, but this verfies
     *  that it now is again
     */
    @Test
    public void bug48718() throws Exception {
       // Verify the HSSF behaviour
       // Then ensure the same for XSSF
       Workbook[] wbs = new Workbook[] {
             new HSSFWorkbook(),
             new XSSFWorkbook()
       };
       int[] initialFonts = new int[] { 4, 1 };
       for(int i=0; i<wbs.length; i++) {
          Workbook wb = wbs[i];
          int startingFonts = initialFonts[i];
          
          assertEquals(startingFonts, wb.getNumberOfFonts());
          
          // Get a font, and slightly change it
          Font a = wb.createFont();
          assertEquals(startingFonts+1, wb.getNumberOfFonts());
          a.setFontHeightInPoints((short)23);
          assertEquals(startingFonts+1, wb.getNumberOfFonts());
          
          // Get two more, unchanged
          /*Font b =*/ wb.createFont();
          assertEquals(startingFonts+2, wb.getNumberOfFonts());
          /*Font c =*/ wb.createFont();
          assertEquals(startingFonts+3, wb.getNumberOfFonts());
       }
    }
    
    /**
     * Ensure General and @ format are working properly
     *  for integers 
     */
    @Test
    public void bug47490() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("GeneralFormatTests.xlsx");
       Sheet s = wb.getSheetAt(1);
       Row r;
       DataFormatter df = new DataFormatter();
       
       r = s.getRow(1);
       assertEquals(1.0, r.getCell(2).getNumericCellValue(), 0);
       assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
       assertEquals("1", df.formatCellValue(r.getCell(2)));
       assertEquals("1", df.formatRawCellContents(1.0, -1, "@"));
       assertEquals("1", df.formatRawCellContents(1.0, -1, "General"));
              
       r = s.getRow(2);
       assertEquals(12.0, r.getCell(2).getNumericCellValue(), 0);
       assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
       assertEquals("12", df.formatCellValue(r.getCell(2)));
       assertEquals("12", df.formatRawCellContents(12.0, -1, "@"));
       assertEquals("12", df.formatRawCellContents(12.0, -1, "General"));
       
       r = s.getRow(3);
       assertEquals(123.0, r.getCell(2).getNumericCellValue(), 0);
       assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
       assertEquals("123", df.formatCellValue(r.getCell(2)));
       assertEquals("123", df.formatRawCellContents(123.0, -1, "@"));
       assertEquals("123", df.formatRawCellContents(123.0, -1, "General"));
    }
    
    /**
     * Ensures that XSSF and HSSF agree with each other,
     *  and with the docs on when fetching the wrong
     *  kind of value from a Formula cell
     */
    @Test
    public void bug47815() {
       Workbook[] wbs = new Workbook[] {
             new HSSFWorkbook(),
             new XSSFWorkbook()
       };
       for(Workbook wb : wbs) {
          Sheet s = wb.createSheet();
          Row r = s.createRow(0);
          
          // Setup
          Cell cn = r.createCell(0, Cell.CELL_TYPE_NUMERIC);
          cn.setCellValue(1.2);
          Cell cs = r.createCell(1, Cell.CELL_TYPE_STRING);
          cs.setCellValue("Testing");
          
          Cell cfn = r.createCell(2, Cell.CELL_TYPE_FORMULA);
          cfn.setCellFormula("A1");  
          Cell cfs = r.createCell(3, Cell.CELL_TYPE_FORMULA);
          cfs.setCellFormula("B1");
          
          FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
          assertEquals(Cell.CELL_TYPE_NUMERIC, fe.evaluate(cfn).getCellType());
          assertEquals(Cell.CELL_TYPE_STRING, fe.evaluate(cfs).getCellType());
          fe.evaluateFormulaCell(cfn);
          fe.evaluateFormulaCell(cfs);
          
          // Now test
          assertEquals(Cell.CELL_TYPE_NUMERIC, cn.getCellType());
          assertEquals(Cell.CELL_TYPE_STRING, cs.getCellType());
          assertEquals(Cell.CELL_TYPE_FORMULA, cfn.getCellType());
          assertEquals(Cell.CELL_TYPE_NUMERIC, cfn.getCachedFormulaResultType());
          assertEquals(Cell.CELL_TYPE_FORMULA, cfs.getCellType());
          assertEquals(Cell.CELL_TYPE_STRING, cfs.getCachedFormulaResultType());
          
          // Different ways of retrieving
          assertEquals(1.2, cn.getNumericCellValue(), 0);
          try {
             cn.getRichStringCellValue();
             fail();
          } catch(IllegalStateException e) {}
          
          assertEquals("Testing", cs.getStringCellValue());
          try {
             cs.getNumericCellValue();
             fail();
          } catch(IllegalStateException e) {}
          
          assertEquals(1.2, cfn.getNumericCellValue(), 0);
          try {
             cfn.getRichStringCellValue();
             fail();
          } catch(IllegalStateException e) {}
          
          assertEquals("Testing", cfs.getStringCellValue());
          try {
             cfs.getNumericCellValue();
             fail();
          } catch(IllegalStateException e) {}
       }
    }

    /**
     * A problem file from a non-standard source (a scientific instrument that saves its
     * output as an .xlsx file) that have two issues:
     * 1. The Content Type part name is lower-case:  [content_types].xml
     * 2. The file appears to use backslashes as path separators
     *
     * The OPC spec tolerates both of these peculiarities, so does POI
     */
    @Test
    public void bug49609() throws Exception {
        XSSFWorkbook wb =  XSSFTestDataSamples.openSampleWorkbook("49609.xlsx");
        assertEquals("FAM", wb.getSheetName(0));
        assertEquals("Cycle", wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());

    }

    @Test
    public void bug49783() throws Exception {
        Workbook wb =  XSSFTestDataSamples.openSampleWorkbook("49783.xlsx");
        Sheet sheet = wb.getSheetAt(0);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        Cell cell;

        cell = sheet.getRow(0).getCell(0);
        assertEquals("#REF!*#REF!", cell.getCellFormula());
        assertEquals(Cell.CELL_TYPE_ERROR, evaluator.evaluateInCell(cell).getCellType());
        assertEquals("#REF!", FormulaError.forInt(cell.getErrorCellValue()).getString());

        Name nm1 = wb.getName("sale_1");
        assertNotNull("name sale_1 should be present", nm1);
        assertEquals("Sheet1!#REF!", nm1.getRefersToFormula());
        Name nm2 = wb.getName("sale_2");
        assertNotNull("name sale_2 should be present", nm2);
        assertEquals("Sheet1!#REF!", nm2.getRefersToFormula());

        cell = sheet.getRow(1).getCell(0);
        assertEquals("sale_1*sale_2", cell.getCellFormula());
        assertEquals(Cell.CELL_TYPE_ERROR, evaluator.evaluateInCell(cell).getCellType());
        assertEquals("#REF!", FormulaError.forInt(cell.getErrorCellValue()).getString());
    }
    
    /**
     * Creating a rich string of "hello world" and applying
     *  a font to characters 1-5 means we have two strings,
     *  "hello" and " world". As such, we need to apply
     *  preserve spaces to the 2nd bit, lest we end up
     *  with something like "helloworld" !
     */
    @Test
    public void bug49941() throws Exception {
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFSheet s = wb.createSheet();
       XSSFRow r = s.createRow(0);
       XSSFCell c = r.createCell(0);
       
       // First without fonts
       c.setCellValue(
             new XSSFRichTextString(" with spaces ")
       );
       assertEquals(" with spaces ", c.getRichStringCellValue().toString());
       assertEquals(0, c.getRichStringCellValue().getCTRst().sizeOfRArray());
       assertEquals(true, c.getRichStringCellValue().getCTRst().isSetT());
       // Should have the preserve set
       assertEquals(
             1,
             c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().getLength()
       );
       assertEquals(
             "preserve",
             c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().item(0).getNodeValue()
       );
       
       // Save and check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s = wb.getSheetAt(0);
       r = s.getRow(0);
       c = r.getCell(0);
       assertEquals(" with spaces ", c.getRichStringCellValue().toString());
       assertEquals(0, c.getRichStringCellValue().getCTRst().sizeOfRArray());
       assertEquals(true, c.getRichStringCellValue().getCTRst().isSetT());
       
       // Change the string
       c.setCellValue(
             new XSSFRichTextString("hello world")
       );
       assertEquals("hello world", c.getRichStringCellValue().toString());
       // Won't have preserve
       assertEquals(
             0,
             c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().getLength()
       );
       
       // Apply a font
       XSSFFont f = wb.createFont();
       f.setBold(true);
       c.getRichStringCellValue().applyFont(0, 5, f);
       assertEquals("hello world", c.getRichStringCellValue().toString());
       // Does need preserving on the 2nd part
       assertEquals(2, c.getRichStringCellValue().getCTRst().sizeOfRArray());
       assertEquals(
             0,
             c.getRichStringCellValue().getCTRst().getRArray(0).xgetT().getDomNode().getAttributes().getLength()
       );
       assertEquals(
             1,
             c.getRichStringCellValue().getCTRst().getRArray(1).xgetT().getDomNode().getAttributes().getLength()
       );
       assertEquals(
             "preserve",
             c.getRichStringCellValue().getCTRst().getRArray(1).xgetT().getDomNode().getAttributes().item(0).getNodeValue()
       );
       
       // Save and check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s = wb.getSheetAt(0);
       r = s.getRow(0);
       c = r.getCell(0);
       assertEquals("hello world", c.getRichStringCellValue().toString());
       wb.close();
    }
    
    /**
     * Repeatedly writing the same file which has styles
     */
    @Test
    public void bug49940() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("styles.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       assertEquals(10, wb.getStylesSource().getNumCellStyles());
       
       ByteArrayOutputStream b1 = new ByteArrayOutputStream();
       ByteArrayOutputStream b2 = new ByteArrayOutputStream();
       ByteArrayOutputStream b3 = new ByteArrayOutputStream();
       wb.write(b1);
       wb.write(b2);
       wb.write(b3);
       
       for(byte[] data : new byte[][] {
             b1.toByteArray(), b2.toByteArray(), b3.toByteArray()
       }) {
          ByteArrayInputStream bais = new ByteArrayInputStream(data);
          wb = new XSSFWorkbook(bais);
          assertEquals(3, wb.getNumberOfSheets());
          assertEquals(10, wb.getStylesSource().getNumCellStyles());
       }
    }

    /**
     * Various ways of removing a cell formula should all zap the calcChain
     * entry.
     */
    @Test
    public void bug49966() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("shared_formulas.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);

        Workbook wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);

        // CalcChain has lots of entries
        CalculationChain cc = wb.getCalculationChain();
        assertEquals("A2", cc.getCTCalcChain().getCArray(0).getR());
        assertEquals("A3", cc.getCTCalcChain().getCArray(1).getR());
        assertEquals("A4", cc.getCTCalcChain().getCArray(2).getR());
        assertEquals("A5", cc.getCTCalcChain().getCArray(3).getR());
        assertEquals("A6", cc.getCTCalcChain().getCArray(4).getR());
        assertEquals("A7", cc.getCTCalcChain().getCArray(5).getR());
        assertEquals("A8", cc.getCTCalcChain().getCArray(6).getR());
        assertEquals(40, cc.getCTCalcChain().sizeOfCArray());
        wbRead.close();
        
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);

        // Try various ways of changing the formulas
        // If it stays a formula, chain entry should remain
        // Otherwise should go
        sheet.getRow(1).getCell(0).setCellFormula("A1"); // stay
        sheet.getRow(2).getCell(0).setCellFormula(null); // go
        sheet.getRow(3).getCell(0).setCellType(Cell.CELL_TYPE_FORMULA); // stay
        wbRead.close();
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet.getRow(4).getCell(0).setCellType(Cell.CELL_TYPE_STRING); // go
        wbRead.close();
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);

        validateCells(sheet);
        sheet.getRow(5).removeCell(sheet.getRow(5).getCell(0)); // go
        validateCells(sheet);
        wbRead.close();
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        
        sheet.getRow(6).getCell(0).setCellType(Cell.CELL_TYPE_BLANK); // go
        wbRead.close();
        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);
        sheet.getRow(7).getCell(0).setCellValue((String) null); // go
        wbRead.close();

        wbRead = XSSFTestDataSamples.writeOutAndReadBack(wb);

        // Save and check
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(35, cc.getCTCalcChain().sizeOfCArray());

        cc = wb.getCalculationChain();
        assertEquals("A2", cc.getCTCalcChain().getCArray(0).getR());
        assertEquals("A4", cc.getCTCalcChain().getCArray(1).getR());
        assertEquals("A9", cc.getCTCalcChain().getCArray(2).getR());
        wbRead.close();
    }

    @Test
    public void bug49966Row() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples
                .openSampleWorkbook("shared_formulas.xlsx");
        XSSFSheet sheet = wb.getSheetAt(0);

        validateCells(sheet);
        sheet.getRow(5).removeCell(sheet.getRow(5).getCell(0)); // go
        validateCells(sheet);
    }

    private void validateCells(XSSFSheet sheet) {
        for(Row row : sheet) {
            // trigger handling
            ((XSSFRow)row).onDocumentWrite();
        }
    }

    @Test
    public void bug49156() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("49156.xlsx");
        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheetAt(0);
        for(Row row : sheet){
            for(Cell cell : row){
                if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
                    formulaEvaluator.evaluateInCell(cell); // caused NPE on some cells
                }
            }
        }
    }
    
    /**
     * Newlines are valid characters in a formula
     */
    @Test
    public void bug50440And51875() throws Exception {
       Workbook wb = XSSFTestDataSamples.openSampleWorkbook("NewlineInFormulas.xlsx");
       Sheet s = wb.getSheetAt(0);
       Cell c = s.getRow(0).getCell(0);
       
       assertEquals("SUM(\n1,2\n)", c.getCellFormula());
       assertEquals(3.0, c.getNumericCellValue(), 0);
       
       FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
       formulaEvaluator.evaluateFormulaCell(c);
       
       assertEquals("SUM(\n1,2\n)", c.getCellFormula());
       assertEquals(3.0, c.getNumericCellValue(), 0);

       // For 51875
       Cell b3 = s.getRow(2).getCell(1);
       formulaEvaluator.evaluateFormulaCell(b3);
       assertEquals("B1+B2", b3.getCellFormula()); // The newline is lost for shared formulas
       assertEquals(3.0, b3.getNumericCellValue(), 0);
    }
    
    /**
     * Moving a cell comment from one cell to another
     */
    @Test
    public void bug50795() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50795.xlsx");
       XSSFSheet sheet = wb.getSheetAt(0);
       XSSFRow row = sheet.getRow(0);

       XSSFCell cellWith = row.getCell(0);
       XSSFCell cellWithoutComment = row.getCell(1);
       
       assertNotNull(cellWith.getCellComment());
       assertNull(cellWithoutComment.getCellComment());
       
       String exp = "\u0410\u0432\u0442\u043e\u0440:\ncomment";
       XSSFComment comment = cellWith.getCellComment();
       assertEquals(exp, comment.getString().getString());
       
       
       // Check we can write it out and read it back as-is
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(0);
       cellWith = row.getCell(0);
       cellWithoutComment = row.getCell(1);
       
       // Double check things are as expected
       assertNotNull(cellWith.getCellComment());
       assertNull(cellWithoutComment.getCellComment());
       comment = cellWith.getCellComment();
       assertEquals(exp, comment.getString().getString());

       
       // Move the comment
       cellWithoutComment.setCellComment(comment);
       
       
       // Write out and re-check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(0);
       
       // Ensure it swapped over
       cellWith = row.getCell(0);
       cellWithoutComment = row.getCell(1);
       assertNull(cellWith.getCellComment());
       assertNotNull(cellWithoutComment.getCellComment());
       
       comment = cellWithoutComment.getCellComment();
       assertEquals(exp, comment.getString().getString());
    }
    
    /**
     * When the cell background colour is set with one of the first
     *  two columns of the theme colour palette, the colours are 
     *  shades of white or black.
     * For those cases, ensure we don't break on reading the colour
     */
    @Test
    public void bug50299() throws Exception {
       Workbook wb = XSSFTestDataSamples.openSampleWorkbook("50299.xlsx");
       
       // Check all the colours
       for(int sn=0; sn<wb.getNumberOfSheets(); sn++) {
          Sheet s = wb.getSheetAt(sn);
          for(Row r : s) {
             for(Cell c : r) {
                CellStyle cs = c.getCellStyle();
                if(cs != null) {
                   cs.getFillForegroundColor();
                }
             }
          }
       }
       
       // Check one bit in detail
       // Check that we get back foreground=0 for the theme colours,
       //  and background=64 for the auto colouring
       Sheet s = wb.getSheetAt(0);
       assertEquals(0,  s.getRow(0).getCell(8).getCellStyle().getFillForegroundColor());
       assertEquals(64, s.getRow(0).getCell(8).getCellStyle().getFillBackgroundColor());
       assertEquals(0,  s.getRow(1).getCell(8).getCellStyle().getFillForegroundColor());
       assertEquals(64, s.getRow(1).getCell(8).getCellStyle().getFillBackgroundColor());
    }
    
    /**
     * Excel .xls style indexed colours in a .xlsx file
     */
    @Test
    public void bug50786() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50786-indexed_colours.xlsx");
       XSSFSheet s = wb.getSheetAt(0);
       XSSFRow r = s.getRow(2);
       
       // Check we have the right cell
       XSSFCell c = r.getCell(1);
       assertEquals("test\u00a0", c.getRichStringCellValue().getString());
       
       // It should be light green
       XSSFCellStyle cs = c.getCellStyle();
       assertEquals(42, cs.getFillForegroundColor());
       assertEquals(42, cs.getFillForegroundColorColor().getIndexed());
       assertNotNull(cs.getFillForegroundColorColor().getRgb());
       assertEquals("FFCCFFCC", cs.getFillForegroundColorColor().getARGBHex());
    }
    
    /**
     * If the border colours are set with themes, then we 
     *  should still be able to get colours
     */
    @Test
    public void bug50846() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50846-border_colours.xlsx");
       
       XSSFSheet sheet = wb.getSheetAt(0);
       XSSFRow row = sheet.getRow(0);
       
       // Border from a theme, brown
       XSSFCell cellT = row.getCell(0);
       XSSFCellStyle styleT = cellT.getCellStyle();
       XSSFColor colorT = styleT.getBottomBorderXSSFColor();
       
       assertEquals(5, colorT.getTheme());
       assertEquals("FFC0504D", colorT.getARGBHex());
       
       // Border from a style direct, red
       XSSFCell cellS = row.getCell(1);
       XSSFCellStyle styleS = cellS.getCellStyle();
       XSSFColor colorS = styleS.getBottomBorderXSSFColor();
       
       assertEquals(0, colorS.getTheme());
       assertEquals("FFFF0000", colorS.getARGBHex());
    }
    
    /**
     * Fonts where their colours come from the theme rather
     *  then being set explicitly still should allow the
     *  fetching of the RGB.
     */
    @Test
    public void bug50784() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50784-font_theme_colours.xlsx");
       XSSFSheet s = wb.getSheetAt(0);
       XSSFRow r = s.getRow(0);
       
       // Column 1 has a font with regular colours
       XSSFCell cr = r.getCell(1);
       XSSFFont fr = wb.getFontAt( cr.getCellStyle().getFontIndex() );
       XSSFColor colr =  fr.getXSSFColor();
       // No theme, has colours
       assertEquals(0, colr.getTheme());
       assertNotNull( colr.getRgb() );
       
       // Column 0 has a font with colours from a theme
       XSSFCell ct = r.getCell(0);
       XSSFFont ft = wb.getFontAt( ct.getCellStyle().getFontIndex() );
       XSSFColor colt =  ft.getXSSFColor();
       // Has a theme, which has the colours on it
       assertEquals(9, colt.getTheme());
       XSSFColor themeC = wb.getTheme().getThemeColor(colt.getTheme());
       assertNotNull( themeC.getRgb() );
       assertNotNull( colt.getRgb() );
       assertEquals( themeC.getARGBHex(), colt.getARGBHex() ); // The same colour
    }

    /**
     * New lines were being eaten when setting a font on
     *  a rich text string
     */
    @Test
    public void bug48877() throws Exception {
       String text = "Use \n with word wrap on to create a new line.\n" +
          "This line finishes with two trailing spaces.  ";
       
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFSheet sheet = wb.createSheet();

       Font font1 = wb.createFont();
       font1.setColor((short) 20);
       Font font2 = wb.createFont();
       font2.setColor(Font.COLOR_RED);
       Font font3 = wb.getFontAt((short)0);

       XSSFRow row = sheet.createRow(2);
       XSSFCell cell = row.createCell(2);

       XSSFRichTextString richTextString =
          wb.getCreationHelper().createRichTextString(text);
       
       // Check the text has the newline
       assertEquals(text, richTextString.getString());
       
       // Apply the font
       richTextString.applyFont(font3);
       richTextString.applyFont(0, 3, font1);
       cell.setCellValue(richTextString);

       // To enable newlines you need set a cell styles with wrap=true
       CellStyle cs = wb.createCellStyle();
       cs.setWrapText(true);
       cell.setCellStyle(cs);

       // Check the text has the
       assertEquals(text, cell.getStringCellValue());
       
       // Save the file and re-read it
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(2);
       cell = row.getCell(2);
       assertEquals(text, cell.getStringCellValue());
       
       // Now add a 2nd, and check again
       int fontAt = text.indexOf("\n", 6);
       cell.getRichStringCellValue().applyFont(10, fontAt+1, font2);
       assertEquals(text, cell.getStringCellValue());
       
       assertEquals(4, cell.getRichStringCellValue().numFormattingRuns());
       assertEquals("Use", cell.getRichStringCellValue().getCTRst().getRArray(0).getT());
       
       String r3 = cell.getRichStringCellValue().getCTRst().getRArray(2).getT();
       assertEquals("line.\n", r3.substring(r3.length()-6));

       // Save and re-check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(2);
       cell = row.getCell(2);
       assertEquals(text, cell.getStringCellValue());
       wb.close();

//       FileOutputStream out = new FileOutputStream("/tmp/test48877.xlsx");
//       wb.write(out);
//       out.close();
    }
    
    /**
     * Adding sheets when one has a table, then re-ordering
     */
    @Test
    public void bug50867() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50867_with_table.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       
       XSSFSheet s1 = wb.getSheetAt(0);
       XSSFSheet s2 = wb.getSheetAt(1);
       XSSFSheet s3 = wb.getSheetAt(2);
       assertEquals(1, s1.getTables().size());
       assertEquals(0, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       
       XSSFTable t = s1.getTables().get(0);
       assertEquals("Tabella1", t.getName());
       assertEquals("Tabella1", t.getDisplayName());
       assertEquals("A1:C3", t.getCTTable().getRef());
       
       // Add a sheet and re-order
       XSSFSheet s4 = wb.createSheet("NewSheet");
       wb.setSheetOrder(s4.getSheetName(), 0);
       
       // Check on tables
       assertEquals(1, s1.getTables().size());
       assertEquals(0, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       // Refetch to get the new order
       s1 = wb.getSheetAt(0);
       s2 = wb.getSheetAt(1);
       s3 = wb.getSheetAt(2);
       s4 = wb.getSheetAt(3);
       assertEquals(0, s1.getTables().size());
       assertEquals(1, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       // Save and re-load
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s1 = wb.getSheetAt(0);
       s2 = wb.getSheetAt(1);
       s3 = wb.getSheetAt(2);
       s4 = wb.getSheetAt(3);
       assertEquals(0, s1.getTables().size());
       assertEquals(1, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       t = s2.getTables().get(0);
       assertEquals("Tabella1", t.getName());
       assertEquals("Tabella1", t.getDisplayName());
       assertEquals("A1:C3", t.getCTTable().getRef());

       
       // Add some more tables, and check
       t = s2.createTable();
       t.setName("New 2");
       t.setDisplayName("New 2");
       t = s3.createTable();
       t.setName("New 3");
       t.setDisplayName("New 3");
       
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s1 = wb.getSheetAt(0);
       s2 = wb.getSheetAt(1);
       s3 = wb.getSheetAt(2);
       s4 = wb.getSheetAt(3);
       assertEquals(0, s1.getTables().size());
       assertEquals(2, s2.getTables().size());
       assertEquals(1, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       t = s2.getTables().get(0);
       assertEquals("Tabella1", t.getName());
       assertEquals("Tabella1", t.getDisplayName());
       assertEquals("A1:C3", t.getCTTable().getRef());
       
       t = s2.getTables().get(1);
       assertEquals("New 2", t.getName());
       assertEquals("New 2", t.getDisplayName());
       
       t = s3.getTables().get(0);
       assertEquals("New 3", t.getName());
       assertEquals("New 3", t.getDisplayName());
       
       // Check the relationships
       assertEquals(0, s1.getRelations().size());
       assertEquals(3, s2.getRelations().size());
       assertEquals(1, s3.getRelations().size());
       assertEquals(0, s4.getRelations().size());
       
       assertEquals(
             XSSFRelation.PRINTER_SETTINGS.getContentType(), 
             s2.getRelations().get(0).getPackagePart().getContentType()
       );
       assertEquals(
             XSSFRelation.TABLE.getContentType(), 
             s2.getRelations().get(1).getPackagePart().getContentType()
       );
       assertEquals(
             XSSFRelation.TABLE.getContentType(), 
             s2.getRelations().get(2).getPackagePart().getContentType()
       );
       assertEquals(
             XSSFRelation.TABLE.getContentType(), 
             s3.getRelations().get(0).getPackagePart().getContentType()
       );
       assertEquals(
             "/xl/tables/table3.xml",
             s3.getRelations().get(0).getPackagePart().getPartName().toString()
       );
    }
    
    /**
     * Setting repeating rows and columns shouldn't break
     *  any print settings that were there before
     */
    @SuppressWarnings("deprecation")
    @Test
	public void bug49253() throws Exception {
       XSSFWorkbook wb1 = new XSSFWorkbook();
       XSSFWorkbook wb2 = new XSSFWorkbook();
       
       // No print settings before repeating
       XSSFSheet s1 = wb1.createSheet(); 
       assertEquals(false, s1.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s1.getCTWorksheet().isSetPageMargins());
       
       wb1.setRepeatingRowsAndColumns(0, 2, 3, 1, 2);
       
       assertEquals(true, s1.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s1.getCTWorksheet().isSetPageMargins());
       
       XSSFPrintSetup ps1 = s1.getPrintSetup();
       assertEquals(false, ps1.getValidSettings());
       assertEquals(false, ps1.getLandscape());
       
       
       // Had valid print settings before repeating
       XSSFSheet s2 = wb2.createSheet();
       XSSFPrintSetup ps2 = s2.getPrintSetup();
       assertEquals(true, s2.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s2.getCTWorksheet().isSetPageMargins());
       
       ps2.setLandscape(false);
       assertEquals(true, ps2.getValidSettings());
       assertEquals(false, ps2.getLandscape());
       
       wb2.setRepeatingRowsAndColumns(0, 2, 3, 1, 2);
       
       ps2 = s2.getPrintSetup();
       assertEquals(true, s2.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s2.getCTWorksheet().isSetPageMargins());
       assertEquals(true, ps2.getValidSettings());
       assertEquals(false, ps2.getLandscape());
       
       wb1.close();
       wb2.close();
    }

    /**
     * Default Column style
     */
    @Test
    public void bug51037() throws Exception {
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFSheet s = wb.createSheet();
       
       CellStyle defaultStyle = wb.getCellStyleAt((short)0);
       assertEquals(0, defaultStyle.getIndex());
       
       CellStyle blueStyle = wb.createCellStyle();
       blueStyle.setFillForegroundColor(IndexedColors.AQUA.getIndex());
       blueStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
       assertEquals(1, blueStyle.getIndex());

       CellStyle pinkStyle = wb.createCellStyle();
       pinkStyle.setFillForegroundColor(IndexedColors.PINK.getIndex());
       pinkStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
       assertEquals(2, pinkStyle.getIndex());

       // Starts empty
       assertEquals(1, s.getCTWorksheet().sizeOfColsArray());
       CTCols cols = s.getCTWorksheet().getColsArray(0);
       assertEquals(0, cols.sizeOfColArray());
       
       // Add some rows and columns
       XSSFRow r1 = s.createRow(0);
       XSSFRow r2 = s.createRow(1);
       r1.createCell(0);
       r1.createCell(2);
       r2.createCell(0);
       r2.createCell(3);
       
       // Check no style is there
       assertEquals(1, s.getCTWorksheet().sizeOfColsArray());
       assertEquals(0, cols.sizeOfColArray());
       
       assertEquals(defaultStyle, s.getColumnStyle(0));
       assertEquals(defaultStyle, s.getColumnStyle(2));
       assertEquals(defaultStyle, s.getColumnStyle(3));
       
       
       // Apply the styles
       s.setDefaultColumnStyle(0, pinkStyle);
       s.setDefaultColumnStyle(3, blueStyle);
       
       // Check
       assertEquals(pinkStyle, s.getColumnStyle(0));
       assertEquals(defaultStyle, s.getColumnStyle(2));
       assertEquals(blueStyle, s.getColumnStyle(3));
       
       assertEquals(1, s.getCTWorksheet().sizeOfColsArray());
       assertEquals(2, cols.sizeOfColArray());
       
       assertEquals(1, cols.getColArray(0).getMin());
       assertEquals(1, cols.getColArray(0).getMax());
       assertEquals(pinkStyle.getIndex(), cols.getColArray(0).getStyle());
       
       assertEquals(4, cols.getColArray(1).getMin());
       assertEquals(4, cols.getColArray(1).getMax());
       assertEquals(blueStyle.getIndex(), cols.getColArray(1).getStyle());
       
       
       // Save, re-load and re-check 
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s = wb.getSheetAt(0);
       defaultStyle = wb.getCellStyleAt(defaultStyle.getIndex());
       blueStyle = wb.getCellStyleAt(blueStyle.getIndex());
       pinkStyle = wb.getCellStyleAt(pinkStyle.getIndex());
       
       assertEquals(pinkStyle, s.getColumnStyle(0));
       assertEquals(defaultStyle, s.getColumnStyle(2));
       assertEquals(blueStyle, s.getColumnStyle(3));
       wb.close();
    }
    
    /**
     * Repeatedly writing a file.
     * Something with the SharedStringsTable currently breaks...
     */
    @Test
    public void bug46662() throws Exception {
       // New file
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       
       // Simple file
       wb = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       
       // Complex file
       // TODO
    }
    
    /**
     * Colours and styles when the list has gaps in it 
     */
    @Test
    public void bug51222() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("51222.xlsx");
       XSSFSheet s = wb.getSheetAt(0);
       
       XSSFCell cA4_EEECE1 = s.getRow(3).getCell(0);
       XSSFCell cA5_1F497D = s.getRow(4).getCell(0);
       
       // Check the text
       assertEquals("A4", cA4_EEECE1.getRichStringCellValue().getString());
       assertEquals("A5", cA5_1F497D.getRichStringCellValue().getString());
       
       // Check the styles assigned to them
       assertEquals(4, cA4_EEECE1.getCTCell().getS());
       assertEquals(5, cA5_1F497D.getCTCell().getS());
       
       // Check we look up the correct style
       assertEquals(4, cA4_EEECE1.getCellStyle().getIndex());
       assertEquals(5, cA5_1F497D.getCellStyle().getIndex());
       
       // Check the fills on them at the low level
       assertEquals(5, cA4_EEECE1.getCellStyle().getCoreXf().getFillId());
       assertEquals(6, cA5_1F497D.getCellStyle().getCoreXf().getFillId());

       // These should reference themes 2 and 3
       assertEquals(2, wb.getStylesSource().getFillAt(5).getCTFill().getPatternFill().getFgColor().getTheme());
       assertEquals(3, wb.getStylesSource().getFillAt(6).getCTFill().getPatternFill().getFgColor().getTheme());
       
       // Ensure we get the right colours for these themes
       // TODO fix
//       assertEquals("FFEEECE1", wb.getTheme().getThemeColor(2).getARGBHex());
//       assertEquals("FF1F497D", wb.getTheme().getThemeColor(3).getARGBHex());
       
       // Finally check the colours on the styles
       // TODO fix
//       assertEquals("FFEEECE1", cA4_EEECE1.getCellStyle().getFillForegroundXSSFColor().getARGBHex());
//       assertEquals("FF1F497D", cA5_1F497D.getCellStyle().getFillForegroundXSSFColor().getARGBHex());
    }

    @Test
    public void bug51470() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("51470.xlsx");
        XSSFSheet sh0 = wb.getSheetAt(0);
        XSSFSheet sh1 = wb.cloneSheet(0);
        List<POIXMLDocumentPart> rels0 = sh0.getRelations();
        List<POIXMLDocumentPart> rels1 = sh1.getRelations();
        assertEquals(1, rels0.size());
        assertEquals(1, rels1.size());

        assertEquals(rels0.get(0).getPackageRelationship(), rels1.get(0).getPackageRelationship());
    }
    
    /**
     * Add comments to Sheet 1, when Sheet 2 already has
     *  comments (so /xl/comments1.xml is taken)
     */
    @Test
    public void bug51850() {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("51850.xlsx");
       XSSFSheet sh1 = wb.getSheetAt(0);
       XSSFSheet sh2 = wb.getSheetAt(1);
 
       // Sheet 2 has comments
       assertNotNull(sh2.getCommentsTable(false));
       assertEquals(1, sh2.getCommentsTable(false).getNumberOfComments());
       
       // Sheet 1 doesn't (yet)
       assertNull(sh1.getCommentsTable(false));
       
       // Try to add comments to Sheet 1
       CreationHelper factory = wb.getCreationHelper();
       Drawing drawing = sh1.createDrawingPatriarch();

       ClientAnchor anchor = factory.createClientAnchor();
       anchor.setCol1(0);
       anchor.setCol2(4);
       anchor.setRow1(0);
       anchor.setRow2(1);

       Comment comment1 = drawing.createCellComment(anchor);
       comment1.setString(
             factory.createRichTextString("I like this cell. It's my favourite."));
       comment1.setAuthor("Bob T. Fish");
       
       anchor = factory.createClientAnchor();
       anchor.setCol1(0);
       anchor.setCol2(4);
       anchor.setRow1(1);
       anchor.setRow2(1);
       Comment comment2 = drawing.createCellComment(anchor);
       comment2.setString(
             factory.createRichTextString("This is much less fun..."));
       comment2.setAuthor("Bob T. Fish");

       Cell c1 = sh1.getRow(0).createCell(4);
       c1.setCellValue(2.3);
       c1.setCellComment(comment1);
       
       Cell c2 = sh1.getRow(0).createCell(5);
       c2.setCellValue(2.1);
       c2.setCellComment(comment2);
       
       
       // Save and re-load
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sh1 = wb.getSheetAt(0);
       sh2 = wb.getSheetAt(1);
       
       // Check the comments
       assertNotNull(sh2.getCommentsTable(false));
       assertEquals(1, sh2.getCommentsTable(false).getNumberOfComments());
       
       assertNotNull(sh1.getCommentsTable(false));
       assertEquals(2, sh1.getCommentsTable(false).getNumberOfComments());
    }
    
    /**
     * Sheet names with a , in them
     */
    @Test
    public void bug51963() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("51963.xlsx");
       XSSFSheet sheet = wb.getSheetAt(0);
       assertEquals("Abc,1", sheet.getSheetName());
       
       Name name = wb.getName("Intekon.ProdCodes");
       assertEquals("'Abc,1'!$A$1:$A$2", name.getRefersToFormula());
       
       AreaReference ref = new AreaReference(name.getRefersToFormula());
       assertEquals(0, ref.getFirstCell().getRow());
       assertEquals(0, ref.getFirstCell().getCol());
       assertEquals(1, ref.getLastCell().getRow());
       assertEquals(0, ref.getLastCell().getCol());
    }
    
    /**
     * Sum across multiple workbooks
     *  eg =SUM($Sheet1.C1:$Sheet4.C1)
     * DISABLED As we can't currently evaluate these
     */
    @Ignore
    public void bug48703() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48703.xlsx");
       XSSFSheet sheet = wb.getSheetAt(0);
       
       // Contains two forms, one with a range and one a list
       XSSFRow r1 = sheet.getRow(0);
       XSSFRow r2 = sheet.getRow(1);
       XSSFCell c1 = r1.getCell(1);
       XSSFCell c2 = r2.getCell(1);
       
       assertEquals(20.0, c1.getNumericCellValue(), 0);
       assertEquals("SUM(Sheet1!C1,Sheet2!C1,Sheet3!C1,Sheet4!C1)", c1.getCellFormula());
       
       assertEquals(20.0, c2.getNumericCellValue(), 0);
       assertEquals("SUM(Sheet1:Sheet4!C1)", c2.getCellFormula());
       
       // Try evaluating both
       XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
       eval.evaluateFormulaCell(c1);
       eval.evaluateFormulaCell(c2);
       
       assertEquals(20.0, c1.getNumericCellValue(), 0);
       assertEquals(20.0, c2.getNumericCellValue(), 0);
    }

    /**
     * Bugzilla 51710: problems reading shared formuals from .xlsx
     */
    @Test
    public void bug51710() {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("51710.xlsx");

        final String[] columns = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N"};
        final int rowMax = 500; // bug triggers on row index 59

        Sheet sheet = wb.getSheetAt(0);


        // go through all formula cells
        for (int rInd = 2; rInd <= rowMax; rInd++) {
            Row row = sheet.getRow(rInd);

            for (int cInd = 1; cInd <= 12; cInd++) {
                Cell cell = row.getCell(cInd);
                String formula = cell.getCellFormula();
                CellReference ref = new CellReference(cell);

                //simulate correct answer
                String correct = "$A" + (rInd + 1) + "*" + columns[cInd] + "$2";

                assertEquals("Incorrect formula in " + ref.formatAsString(), correct, formula);
            }

        }
    }

    /**
     * Bug 53101:
     */
    @Test
    public void bug5301(){
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("53101.xlsx");
        FormulaEvaluator evaluator =
                workbook.getCreationHelper().createFormulaEvaluator();
        // A1: SUM(B1: IZ1)
        double a1Value =
                evaluator.evaluate(workbook.getSheetAt(0).getRow(0).getCell(0)).getNumberValue();

        // Assert
        assertEquals(259.0, a1Value, 0.0);

        // KY: SUM(B1: IZ1)
        /*double ky1Value =*/
                evaluator.evaluate(workbook.getSheetAt(0).getRow(0).getCell(310)).getNumberValue();

        // Assert
        assertEquals(259.0, a1Value, 0.0);
    }

    @Test
    public void bug54436(){
        Workbook workbook = XSSFTestDataSamples.openSampleWorkbook("54436.xlsx");
        if(!WorkbookEvaluator.getSupportedFunctionNames().contains("GETPIVOTDATA")){
            Function func = new Function() {
                public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
                    return ErrorEval.NA;
                }
            };

            WorkbookEvaluator.registerFunction("GETPIVOTDATA", func);
        }
        workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
    }
    
    /**
     * Password Protected .xlsx files should give a helpful
     *  error message when called via WorkbookFactory.
     * (You need to supply a password explicitly for them)
     */
    @Test(expected=EncryptedDocumentException.class)
    public void bug55692_stream() throws Exception {
        // Directly on a Stream
        WorkbookFactory.create(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));
    }
    
    @Test(expected=EncryptedDocumentException.class)
    public void bug55692_poifs() throws Exception {
        // Via a POIFSFileSystem
        POIFSFileSystem fsP = new POIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));
        WorkbookFactory.create(fsP);
    }
    
    @Test(expected=EncryptedDocumentException.class)
    public void bug55692_npoifs() throws Exception {
        // Via a NPOIFSFileSystem
        NPOIFSFileSystem fsNP = new NPOIFSFileSystem(POIDataSamples.getPOIFSInstance().openResourceAsStream("protect.xlsx"));
        WorkbookFactory.create(fsNP);
    }

    @Test
    public void bug53282() {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("53282b.xlsx");
        Cell c = wb.getSheetAt(0).getRow(1).getCell(0);
        assertEquals("#@_#", c.getStringCellValue()); 
        assertEquals("http://invalid.uri", c.getHyperlink().getAddress()); 
    }
    
    /**
     * Was giving NullPointerException
     * at org.apache.poi.xssf.usermodel.XSSFWorkbook.onDocumentRead
     * due to a lack of Styles Table
     */
    @Test
    public void bug56278() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56278.xlsx");
        assertEquals(0, wb.getSheetIndex("Market Rates"));
        
        // Save and re-check
        Workbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(0, nwb.getSheetIndex("Market Rates"));
    }

    @Test
    public void bug56315() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56315.xlsx");
        Cell c = wb.getSheetAt(0).getRow(1).getCell(0);
        CellValue cv = wb.getCreationHelper().createFormulaEvaluator().evaluate(c);
        double rounded = cv.getNumberValue();
        assertEquals(0.1, rounded, 0.0);
    }

    @Test
    public void bug56468() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellValue("Hi");
        sheet.setRepeatingRows(new CellRangeAddress(0, 0, 0, 0));
        
        ByteArrayOutputStream bos = new ByteArrayOutputStream(8096);
        wb.write(bos);
        byte firstSave[] = bos.toByteArray();
        bos.reset();
        wb.write(bos);
        byte secondSave[] = bos.toByteArray();
        
        assertArrayEquals(firstSave, secondSave);
        
        wb.close();
    }
    
    /**
     * ISO-8601 style cell formats with a T in them, eg
     * cell format of "yyyy-MM-ddTHH:mm:ss"
     */
    @Test
    public void bug54034() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("54034.xlsx");
        Sheet sheet = wb.getSheet("Sheet1");
        Row row = sheet.getRow(1);
        Cell cell = row.getCell(2);
        assertTrue(DateUtil.isCellDateFormatted(cell));
        
        DataFormatter fmt = new DataFormatter();
        assertEquals("yyyy\\-mm\\-dd\\Thh:mm", cell.getCellStyle().getDataFormatString());
        assertEquals("2012-08-08T22:59", fmt.formatCellValue(cell));
    }


    @Test
    public void testBug53798XLSX() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53798_shiftNegative_TMPL.xlsx");
        File xlsOutput = TempFile.createTempFile("testBug53798", ".xlsx");
        bug53798Work(wb, xlsOutput);
    }

    @Ignore("Shifting rows is not yet implemented in SXSSFSheet")
    @Test
    public void testBug53798XLSXStream() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("53798_shiftNegative_TMPL.xlsx");
        File xlsOutput = TempFile.createTempFile("testBug53798", ".xlsx");
        bug53798Work(new SXSSFWorkbook(wb), xlsOutput);
    }

    @Test
    public void testBug53798XLS() throws IOException {
        Workbook wb = HSSFTestDataSamples.openSampleWorkbook("53798_shiftNegative_TMPL.xls");
        File xlsOutput = TempFile.createTempFile("testBug53798", ".xls");
        bug53798Work(wb, xlsOutput);
    }
    
    /**
     * SUMIF was throwing a NPE on some formulas
     */
    @Test
    @Ignore("This bug is still to be fixed")
    public void testBug56420SumIfNPE() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56420.xlsx");
        
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheetAt(0);
        Row r = sheet.getRow(2);
        Cell c = r.getCell(2);
        assertEquals("SUMIF($A$1:$A$4,A3,$B$1:$B$4)", c.getCellFormula());
        evaluator.evaluateInCell(c);
    }

    private void bug53798Work(Workbook wb, File xlsOutput) throws IOException {
        Sheet testSheet = wb.getSheetAt(0);

        testSheet.shiftRows(2, 2, 1);

        saveAndReloadReport(wb, xlsOutput);

        // 1) corrupted xlsx (unreadable data in the first row of a shifted group) already comes about
        // when shifted by less than -1 negative amount (try -2)
        testSheet.shiftRows(3, 3, -1);

        saveAndReloadReport(wb, xlsOutput);

        testSheet.shiftRows(2, 2, 1);

        saveAndReloadReport(wb, xlsOutput);

        Row newRow = null;
        Cell newCell = null;
        // 2) attempt to create a new row IN PLACE of a removed row by a negative shift causes corrupted
        // xlsx file with  unreadable data in the negative shifted row.
        // NOTE it's ok to create any other row.
        newRow = testSheet.createRow(3);

        saveAndReloadReport(wb, xlsOutput);

        newCell = newRow.createCell(0);

        saveAndReloadReport(wb, xlsOutput);

        newCell.setCellValue("new Cell in row "+newRow.getRowNum());

        saveAndReloadReport(wb, xlsOutput);

        // 3) once a negative shift has been made any attempt to shift another group of rows
        // (note: outside of previously negative shifted rows) by a POSITIVE amount causes POI exception:
        // org.apache.xmlbeans.impl.values.XmlValueDisconnectedException.
        // NOTE: another negative shift on another group of rows is successful, provided no new rows in
        // place of previously shifted rows were attempted to be created as explained above.
        testSheet.shiftRows(6, 7, 1);   // -- CHANGE the shift to positive once the behaviour of
                                        // the above has been tested

        saveAndReloadReport(wb, xlsOutput);
    }

    /**
     * XSSFCell.typeMismatch on certain blank cells when formatting
     *  with DataFormatter
     */
    @Test
    public void bug56702() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("56702.xlsx");
        
        Sheet sheet = wb.getSheetAt(0);

        // Get wrong cell by row 8 & column 7
        Cell cell = sheet.getRow(8).getCell(7);
        assertEquals(Cell.CELL_TYPE_NUMERIC, cell.getCellType());
        
        // Check the value - will be zero as it is <c><v/></c>
        assertEquals(0.0, cell.getNumericCellValue(), 0.001);
        
        // Try to format
        DataFormatter formatter = new DataFormatter();
        formatter.formatCellValue(cell);
        
        // Check the formatting
        assertEquals("0", formatter.formatCellValue(cell));
    }
    
    /**
     * Formulas which reference named ranges, either in other
     *  sheets, or workbook scoped but in other workbooks.
     * Used to fail with with errors like
     * org.apache.poi.ss.formula.FormulaParseException: Cell reference expected after sheet name at index 9
     * org.apache.poi.ss.formula.FormulaParseException: Parse error near char 0 '[' in specified formula '[0]!NR_Global_B2'. Expected number, string, or defined name 
     */
    @Test
    public void bug56737() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56737.xlsx");
        
        // Check the named range definitions
        Name nSheetScope = wb.getName("NR_To_A1");
        Name nWBScope = wb.getName("NR_Global_B2");

        assertNotNull(nSheetScope);
        assertNotNull(nWBScope);
        
        assertEquals("Defines!$A$1", nSheetScope.getRefersToFormula());
        assertEquals("Defines!$B$2", nWBScope.getRefersToFormula());
        
        // Check the different kinds of formulas
        Sheet s = wb.getSheetAt(0);
        Cell cRefSName = s.getRow(1).getCell(3);
        Cell cRefWName = s.getRow(2).getCell(3);
        
        assertEquals("Defines!NR_To_A1", cRefSName.getCellFormula());
        // Note the formula, as stored in the file, has the external name index not filename
        // TODO Provide a way to get the one with the filename
        assertEquals("[0]!NR_Global_B2", cRefWName.getCellFormula());
        
        // Try to evaluate them
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals("Test A1", eval.evaluate(cRefSName).getStringValue());
        assertEquals(142, (int)eval.evaluate(cRefWName).getNumberValue());
        
        // Try to evaluate everything
        eval.evaluateAll();
    }

    private void saveAndReloadReport(Workbook wb, File outFile) throws IOException {
        // run some method on the font to verify if it is "disconnected" already
        //for(short i = 0;i < 256;i++)
        {
            Font font = wb.getFontAt((short)0);
            if(font instanceof XSSFFont) {
                XSSFFont xfont = (XSSFFont) wb.getFontAt((short)0);
                CTFontImpl ctFont = (CTFontImpl) xfont.getCTFont();
                assertEquals(0, ctFont.sizeOfBArray());
            }
        }

        FileOutputStream fileOutStream = new FileOutputStream(outFile);
        wb.write(fileOutStream);
        fileOutStream.close();
        //System.out.println("File \""+outFile.getName()+"\" has been saved successfully");

        FileInputStream is = new FileInputStream(outFile);
        try {
            Workbook newWB = null;
            try {
                if(wb instanceof XSSFWorkbook) {
                    newWB = new XSSFWorkbook(is);
                } else if(wb instanceof HSSFWorkbook) {
                    newWB = new HSSFWorkbook(is);
                } else if(wb instanceof SXSSFWorkbook) {
                    newWB = new SXSSFWorkbook(new XSSFWorkbook(is));
                } else {
                    throw new IllegalStateException("Unknown workbook: " + wb);
                }
                assertNotNull(newWB.getSheet("test"));
            } finally {
                newWB.close();
            }
        } finally {
            is.close();
        }
    }
    
    @Test
    public void testBug56688_1() {
        XSSFWorkbook excel = XSSFTestDataSamples.openSampleWorkbook("56688_1.xlsx");
        checkValue(excel, "-1.0");  /* Not 0.0 because POI sees date "0" minus one month as invalid date, which is -1! */
    }
    
    @Test
    public void testBug56688_2() {
        XSSFWorkbook excel = XSSFTestDataSamples.openSampleWorkbook("56688_2.xlsx");
        checkValue(excel, "#VALUE!");
    }
    
    @Test
    public void testBug56688_3() {
        XSSFWorkbook excel = XSSFTestDataSamples.openSampleWorkbook("56688_3.xlsx");
        checkValue(excel, "#VALUE!");
    }
    
    @Test
    public void testBug56688_4() {
        XSSFWorkbook excel = XSSFTestDataSamples.openSampleWorkbook("56688_4.xlsx");
        
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 2);
        double excelDate = DateUtil.getExcelDate(calendar.getTime());
        NumberEval eval = new NumberEval(Math.floor(excelDate));
        checkValue(excel, eval.getStringValue() + ".0");
    }
    
    /**
     * New hyperlink with no initial cell reference, still need
     *  to be able to change it
     * @throws IOException 
     */
    @Test
    public void testBug56527() throws IOException {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        XSSFCreationHelper creationHelper = wb.getCreationHelper();
        XSSFHyperlink hyperlink;
        
        // Try with a cell reference
        hyperlink = creationHelper.createHyperlink(Hyperlink.LINK_URL);
        sheet.addHyperlink(hyperlink);
        hyperlink.setAddress("http://myurl");
        hyperlink.setCellReference("B4");
        assertEquals(3, hyperlink.getFirstRow());
        assertEquals(1, hyperlink.getFirstColumn());
        assertEquals(3, hyperlink.getLastRow());
        assertEquals(1, hyperlink.getLastColumn());
        
        // Try with explicit rows / columns
        hyperlink = creationHelper.createHyperlink(Hyperlink.LINK_URL);
        sheet.addHyperlink(hyperlink);
        hyperlink.setAddress("http://myurl");
        hyperlink.setFirstRow(5);
        hyperlink.setFirstColumn(3);
        
        assertEquals(5, hyperlink.getFirstRow());
        assertEquals(3, hyperlink.getFirstColumn());
        assertEquals(5, hyperlink.getLastRow());
        assertEquals(3, hyperlink.getLastColumn());
        wb.close();
    }
    
    /**
     * Shifting rows with a formula that references a 
     * function in another file
     */
    @Test
    public void bug56502() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56502.xlsx");
        Sheet sheet = wb.getSheetAt(0);
       
        Cell cFunc = sheet.getRow(3).getCell(0);
        assertEquals("[1]!LUCANET(\"Ist\")", cFunc.getCellFormula());
        Cell cRef = sheet.getRow(3).createCell(1);
        cRef.setCellFormula("A3");
        
        // Shift it down one row
        sheet.shiftRows(1, sheet.getLastRowNum(), 1);
        
        // Check the new formulas: Function won't change, Reference will
        cFunc = sheet.getRow(4).getCell(0);
        assertEquals("[1]!LUCANET(\"Ist\")", cFunc.getCellFormula());
        cRef = sheet.getRow(4).getCell(1);
        assertEquals("A4", cRef.getCellFormula());
    }
    
    @Test
    public void bug54764() throws Exception {
        OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("54764.xlsx");
        
        // Check the core properties - will be found but empty, due
        //  to the expansion being too much to be considered valid
        POIXMLProperties props = new POIXMLProperties(pkg);
        assertEquals(null, props.getCoreProperties().getTitle());
        assertEquals(null, props.getCoreProperties().getSubject());
        assertEquals(null, props.getCoreProperties().getDescription());
        
        // Now check the spreadsheet itself
        try {
            new XSSFWorkbook(pkg);
            fail("Should fail as too much expansion occurs");
        } catch(POIXMLException e) {
            // Expected
        }
        
        // Try with one with the entities in the Content Types
        try {
            XSSFTestDataSamples.openSamplePackage("54764-2.xlsx");
            fail("Should fail as too much expansion occurs");
        } catch(Exception e) {
            // Expected
        }
        
        // Check we can still parse valid files after all that
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
        assertEquals(3, wb.getNumberOfSheets());
    }
    
    /**
     * CTDefinedNamesImpl should be included in the smaller
     *  poi-ooxml-schemas jar
     */
    @Test
    public void bug57176() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57176.xlsx");
        CTDefinedNames definedNames = wb.getCTWorkbook().getDefinedNames();
        List<CTDefinedName> definedNameList = definedNames.getDefinedNameList();
        for (CTDefinedName defName : definedNameList) {
            assertNotNull(defName.getName());
            assertNotNull(defName.getStringValue());
        }
        assertEquals("TestDefinedName", definedNameList.get(0).getName());
    }
    
    /**
     * .xlsb files are not supported, but we should generate a helpful
     *  error message if given one
     */
    @Test
    public void bug56800_xlsb() throws Exception {
        // Can be opened at the OPC level
        OPCPackage pkg = XSSFTestDataSamples.openSamplePackage("Simple.xlsb");
        
        // XSSF Workbook gives helpful error
        try {
            new XSSFWorkbook(pkg);
            fail(".xlsb files not supported");
        } catch (XLSBUnsupportedException e) {
            // Good, detected and warned
        }
        
        // Workbook Factory gives helpful error on package
        try {
            WorkbookFactory.create(pkg);
            fail(".xlsb files not supported");
        } catch (XLSBUnsupportedException e) {
            // Good, detected and warned
        }
        
        // Workbook Factory gives helpful error on file
        File xlsbFile = HSSFTestDataSamples.getSampleFile("Simple.xlsb");
        try {
            WorkbookFactory.create(xlsbFile);
            fail(".xlsb files not supported");
        } catch (XLSBUnsupportedException e) {
            // Good, detected and warned
        }
    }

    private void checkValue(XSSFWorkbook excel, String expect) {
        XSSFFormulaEvaluator evaluator = new XSSFFormulaEvaluator(excel);
        evaluator.evaluateAll();
        
        XSSFCell cell = excel.getSheetAt(0).getRow(1).getCell(1);
        CellValue value = evaluator.evaluate(cell);
        
        assertEquals(expect, value.formatAsString());
    }
    
    @Test
    public void testBug57196() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57196.xlsx");
        Sheet sheet = wb.getSheet("Feuil1");
        Row mod=sheet.getRow(1);
        mod.getCell(1).setCellValue(3);
        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
//        FileOutputStream fileOutput = new FileOutputStream("/tmp/57196.xlsx");
//        wb.write(fileOutput);
//        fileOutput.close();
        wb.close();
    }
    
    @Test
    public void test57196_Detail() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Sheet1");
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellFormula("DEC2HEX(HEX2DEC(O8)-O2+D2)");
        XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(wb);
        CellValue cv = fe.evaluate(cell);

        assertNotNull(cv);
    }    
    
    @Test
    public void test57196_Detail2() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Sheet1");
        XSSFRow row = sheet.createRow(0);
        XSSFCell cell = row.createCell(0);
        cell.setCellFormula("DEC2HEX(O2+D2)");
        XSSFFormulaEvaluator fe = new XSSFFormulaEvaluator(wb);
        CellValue cv = fe.evaluate(cell);

        assertNotNull(cv);
    }    

    @Test
    public void test57196_WorkbookEvaluator() {
        //System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.SystemOutLogger");
        //System.setProperty("poi.log.level", "3");
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("Sheet1");
            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue("0");
            cell = row.createCell(1);
            cell.setCellValue(0);
            cell = row.createCell(2);
            cell.setCellValue(0);

            // simple formula worked
            cell.setCellFormula("DEC2HEX(O2+D2)");
    
            WorkbookEvaluator workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));
            
            // this already failed! Hex2Dec did not correctly handle RefEval
            cell.setCellFormula("HEX2DEC(O8)");
            workbookEvaluator.clearAllCachedResultValues();
    
            workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));

            // slightly more complex one failed
            cell.setCellFormula("HEX2DEC(O8)-O2+D2");
            workbookEvaluator.clearAllCachedResultValues();
    
            workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));

            // more complicated failed
            cell.setCellFormula("DEC2HEX(HEX2DEC(O8)-O2+D2)");
            workbookEvaluator.clearAllCachedResultValues();

            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));

            // what other similar functions
            cell.setCellFormula("DEC2BIN(O8)-O2+D2");
            workbookEvaluator.clearAllCachedResultValues();
    
            workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));

            // what other similar functions
            cell.setCellFormula("DEC2BIN(A1)");
            workbookEvaluator.clearAllCachedResultValues();
    
            workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));

            // what other similar functions
            cell.setCellFormula("BIN2DEC(B1)");
            workbookEvaluator.clearAllCachedResultValues();
    
            workbookEvaluator = new WorkbookEvaluator(XSSFEvaluationWorkbook.create(wb), null, null);
            workbookEvaluator.setDebugEvaluationOutputForNextEval(true);
            workbookEvaluator.evaluate(new XSSFEvaluationCell(cell));
        } finally {
            System.clearProperty("org.apache.poi.util.POILogger");
            System.clearProperty("poi.log.level");
        }
    }
    
    @Test
    public void bug57430() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        try {
            wb.createSheet("Sheet1");

            XSSFName name1 = wb.createName();
            name1.setNameName("FMLA");
            name1.setRefersToFormula("Sheet1!$B$3");
        } finally {
            wb.close();
        }
    }
    
    /**
     * A .xlsx file with no Shared Strings table should open fine
     *  in read-only mode
     */
    @Test
    public void bug57482() throws Exception {
        for (PackageAccess access : new PackageAccess[] {
                PackageAccess.READ_WRITE, PackageAccess.READ
        }) {
            File file = HSSFTestDataSamples.getSampleFile("57482-OnlyNumeric.xlsx");
            OPCPackage pkg = OPCPackage.open(file, access);
            try {
                // Try to open it and read the contents
                XSSFWorkbook wb = new XSSFWorkbook(pkg);
                assertNotNull(wb.getSharedStringSource());
                assertEquals(0, wb.getSharedStringSource().getCount());
                
                DataFormatter fmt = new DataFormatter();
                XSSFSheet s = wb.getSheetAt(0);
                assertEquals("1",  fmt.formatCellValue(s.getRow(0).getCell(0)));
                assertEquals("11", fmt.formatCellValue(s.getRow(0).getCell(1)));
                assertEquals("5",  fmt.formatCellValue(s.getRow(4).getCell(0)));
                
                // Add a text cell
                s.getRow(0).createCell(3).setCellValue("Testing");
                assertEquals("Testing",  fmt.formatCellValue(s.getRow(0).getCell(3)));
                
                // Try to write-out and read again, should only work
                //  in read-write mode, not read-only mode
                try {
                    wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
                    if (access == PackageAccess.READ)
                        fail("Shouln't be able to write from read-only mode");
                } catch (InvalidOperationException e) {
                    if (access == PackageAccess.READ) {
                        // Expected
                    } else {
                        // Shouldn't occur in write-mode
                        throw e;
                    }
                }
                
                // Check again
                s = wb.getSheetAt(0);
                assertEquals("1",  fmt.formatCellValue(s.getRow(0).getCell(0)));
                assertEquals("11", fmt.formatCellValue(s.getRow(0).getCell(1)));
                assertEquals("5",  fmt.formatCellValue(s.getRow(4).getCell(0)));
                assertEquals("Testing",  fmt.formatCellValue(s.getRow(0).getCell(3)));
            } finally {
                pkg.revert();
            }
        }
    }
    
    /**
     * "Unknown error type: -60" fetching formula error value
     */
    @Test
    public void bug57535() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("57535.xlsx");
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
        
        Sheet sheet = wb.getSheet("Sheet1");
        Cell cell = sheet.getRow(5).getCell(4);
        assertEquals(Cell.CELL_TYPE_FORMULA, cell.getCellType());
        assertEquals("E4+E5", cell.getCellFormula());
        
        CellValue value = evaluator.evaluate(cell);
        assertEquals(Cell.CELL_TYPE_ERROR, value.getCellType());
        assertEquals(-60, value.getErrorValue());
        assertEquals("~CIRCULAR~REF~", FormulaError.forInt(value.getErrorValue()).getString());
        assertEquals("CIRCULAR_REF", FormulaError.forInt(value.getErrorValue()).toString());
    }

    
    @Test
    public void test57165() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        try {
            removeAllSheetsBut(3, wb);
            wb.cloneSheet(0); // Throws exception here
            wb.setSheetName(1, "New Sheet");
            //saveWorkbook(wb, fileName);
            
            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
            try {
                
            } finally {
                wbBack.close();
            }
        } finally {
            wb.close();
        }
    }

    @Test
    public void test57165_create() throws IOException {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("57171_57163_57165.xlsx");
        try {
            removeAllSheetsBut(3, wb);
            wb.createSheet("newsheet"); // Throws exception here
            wb.setSheetName(1, "New Sheet");
            //saveWorkbook(wb, fileName);
            
            XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
            try {
                
            } finally {
                wbBack.close();
            }
        } finally {
            wb.close();
        }
    }

    private static void removeAllSheetsBut(int sheetIndex, Workbook wb)
    {
        int sheetNb = wb.getNumberOfSheets();
        // Move this sheet at the first position
        wb.setSheetOrder(wb.getSheetName(sheetIndex), 0);
        for (int sn = sheetNb - 1; sn > 0; sn--)
        {
            wb.removeSheetAt(sn);
        }
    }

    /**
     * Sums 2 plus the cell at the left, indirectly to avoid reference
     * problems when deleting columns, conditionally to stop recursion
     */
    private static final String FORMULA1 =
            "IF( INDIRECT( ADDRESS( ROW(), COLUMN()-1 ) ) = 0, 0,"
                    + "INDIRECT( ADDRESS( ROW(), COLUMN()-1 ) ) ) + 2";

    /**
     * Sums 2 plus the upper cell, indirectly to avoid reference
     * problems when deleting rows, conditionally to stop recursion
     */
    private static final String FORMULA2 =
            "IF( INDIRECT( ADDRESS( ROW()-1, COLUMN() ) ) = 0, 0,"
                    + "INDIRECT( ADDRESS( ROW()-1, COLUMN() ) ) ) + 2";

    /**
     * Expected:

     * [  0][  2][  4]
     * @throws IOException
     */
    @Test
    public void testBug56820_Formula1() throws IOException {
        Workbook wb = new XSSFWorkbook();
        try {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sh = wb.createSheet();

            sh.createRow(0).createCell(0).setCellValue(0.0d);
            Cell formulaCell1 = sh.getRow(0).createCell(1);
            Cell formulaCell2 = sh.getRow(0).createCell(2);
            formulaCell1.setCellFormula(FORMULA1);
            formulaCell2.setCellFormula(FORMULA1);

            double A1 = evaluator.evaluate(formulaCell1).getNumberValue();
            double A2 = evaluator.evaluate(formulaCell2).getNumberValue();

            assertEquals(2, A1, 0);
            assertEquals(4, A2, 0);  //<-- FAILS EXPECTATIONS
        } finally {
            wb.close();
        }
    }

    /**
     * Expected:

     * [  0] <- number
     * [  2] <- formula
     * [  4] <- formula
     * @throws IOException
     */
    @Test
    public void testBug56820_Formula2() throws IOException {
        Workbook wb = new XSSFWorkbook();
        try {
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sh = wb.createSheet();

            sh.createRow(0).createCell(0).setCellValue(0.0d);
            Cell formulaCell1 = sh.createRow(1).createCell(0);
            Cell formulaCell2 = sh.createRow(2).createCell(0);
            formulaCell1.setCellFormula(FORMULA2);
            formulaCell2.setCellFormula(FORMULA2);

            double A1 = evaluator.evaluate(formulaCell1).getNumberValue();
            double A2 = evaluator.evaluate(formulaCell2).getNumberValue(); //<-- FAILS EVALUATION

            assertEquals(2, A1, 0);
            assertEquals(4, A2, 0);
        } finally {
            wb.close();
        }
    }
    
    @Test
    public void test56467() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("picture.xlsx");
        try {
            Sheet orig = wb.getSheetAt(0);
            assertNotNull(orig);
            
            Sheet sheet = wb.cloneSheet(0);
            Drawing drawing = sheet.createDrawingPatriarch();
            for (XSSFShape shape : ((XSSFDrawing) drawing).getShapes()) {
                if (shape instanceof XSSFPicture) {
                    XSSFPictureData pictureData = ((XSSFPicture) shape).getPictureData();
                    assertNotNull(pictureData);
                }
            }
            
//            OutputStream out = new FileOutputStream("/tmp/56467.xls");
//            try {
//            	wb.write(out);
//            } finally {
//            	out.close();
//            }
        } finally {
        	wb.close();
        }
    }
    
    /**
     * OOXML-Strict files
     * Not currently working - namespace mis-match from XMLBeans
     */
    @Test
    @Ignore("XMLBeans namespace mis-match on ooxml-strict files")
    public void test57699() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("sample.strict.xlsx");
        assertEquals(3, wb.getNumberOfSheets());
        // TODO Check sheet contents
        // TODO Check formula evaluation
        
        XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(3, wbBack.getNumberOfSheets());
        // TODO Re-check sheet contents
        // TODO Re-check formula evaluation
    }

    @Test
    public void testBug56295_MergeXlslsWithStyles() throws IOException {
        XSSFWorkbook xlsToAppendWorkbook = XSSFTestDataSamples.openSampleWorkbook("56295.xlsx");
        XSSFSheet sheet = xlsToAppendWorkbook.getSheetAt(0);
        XSSFRow srcRow = sheet.getRow(0);
        XSSFCell oldCell = srcRow.getCell(0);
        XSSFCellStyle cellStyle = oldCell.getCellStyle();
        
        checkStyle(cellStyle);
        
//        StylesTable table = xlsToAppendWorkbook.getStylesSource();
//        List<XSSFCellFill> fills = table.getFills();
//        System.out.println("Having " + fills.size() + " fills");
//        for(XSSFCellFill fill : fills) {
//        	System.out.println("Fill: " + fill.getFillBackgroundColor() + "/" + fill.getFillForegroundColor());
//        }        
        
        XSSFWorkbook targetWorkbook = new XSSFWorkbook();
        XSSFSheet newSheet = targetWorkbook.createSheet(sheet.getSheetName());
        XSSFRow destRow = newSheet.createRow(0);
        XSSFCell newCell = destRow.createCell(0);

		//newCell.getCellStyle().cloneStyleFrom(cellStyle);
        CellStyle newCellStyle = targetWorkbook.createCellStyle();
        newCellStyle.cloneStyleFrom(cellStyle);
        newCell.setCellStyle(newCellStyle);
		checkStyle(newCell.getCellStyle());
        newCell.setCellValue(oldCell.getStringCellValue());

//        OutputStream os = new FileOutputStream("output.xlsm");
//        try {
//        	targetWorkbook.write(os);
//        } finally {
//        	os.close();
//        }
        
        XSSFWorkbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(targetWorkbook);
        XSSFCellStyle styleBack = wbBack.getSheetAt(0).getRow(0).getCell(0).getCellStyle();
        checkStyle(styleBack);
    }

	private void checkStyle(XSSFCellStyle cellStyle) {
		assertNotNull(cellStyle);
        assertEquals(0, cellStyle.getFillForegroundColor());
        assertNotNull(cellStyle.getFillForegroundXSSFColor());
        XSSFColor fgColor = cellStyle.getFillForegroundColorColor();
		assertNotNull(fgColor);
		assertEquals("FF00FFFF", fgColor.getARGBHex());

        assertEquals(0, cellStyle.getFillBackgroundColor());
        assertNotNull(cellStyle.getFillBackgroundXSSFColor());
        XSSFColor bgColor = cellStyle.getFillBackgroundColorColor();
		assertNotNull(bgColor);
		assertEquals("FF00FFFF", fgColor.getARGBHex());
	}
}
