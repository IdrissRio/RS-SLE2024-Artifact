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

package org.apache.poi.hslf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.apache.poi.POIDataSamples;
import org.apache.poi.ddf.EscherArrayProperty;
import org.apache.poi.ddf.EscherColorRef;
import org.apache.poi.ddf.EscherOptRecord;
import org.apache.poi.ddf.EscherProperties;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.HSLFTestDataSamples;
import org.apache.poi.hslf.exceptions.OldPowerPointFormatException;
import org.apache.poi.hslf.model.AutoShape;
import org.apache.poi.hslf.model.Background;
import org.apache.poi.hslf.model.Fill;
import org.apache.poi.hslf.model.HeadersFooters;
import org.apache.poi.hslf.model.MasterSheet;
import org.apache.poi.hslf.model.Notes;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Shape;
import org.apache.poi.hslf.model.ShapeGroup;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.SlideMaster;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.model.TextShape;
import org.apache.poi.hslf.model.TitleMaster;
import org.apache.poi.hslf.record.Document;
import org.apache.poi.hslf.record.Record;
import org.apache.poi.hslf.record.SlideListWithText;
import org.apache.poi.hslf.record.SlideListWithText.SlideAtomsSet;
import org.apache.poi.hslf.record.TextHeaderAtom;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.Units;
import org.junit.Test;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 *
 * @author Yegor Kozlov
 */
public final class TestBugs {
    private static POIDataSamples _slTests = POIDataSamples.getSlideShowInstance();

    /**
     * Bug 41384: Array index wrong in record creation
     */
    @Test
    public void bug41384() throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("41384.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlides().length);

        PictureData[] pict = ppt.getPictureData();
        assertEquals(2, pict.length);
        assertEquals(Picture.JPEG, pict[0].getType());
        assertEquals(Picture.JPEG, pict[1].getType());
    }

    /**
     * First fix from Bug 42474: NPE in RichTextRun.isBold()
     * when the RichTextRun comes from a Notes model object
     */
    @Test
    public void bug42474_1() throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42474-1.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);
        assertEquals(2, ppt.getSlides().length);

        TextRun txrun;
        Notes notes;

        notes = ppt.getSlides()[0].getNotesSheet();
        assertNotNull(notes);
        txrun = notes.getTextRuns()[0];
        assertEquals("Notes-1", txrun.getRawText());
        assertEquals(false, txrun.getRichTextRuns()[0].isBold());

        //notes for the second slide are in bold
        notes = ppt.getSlides()[1].getNotesSheet();
        assertNotNull(notes);
        txrun = notes.getTextRuns()[0];
        assertEquals("Notes-2", txrun.getRawText());
        assertEquals(true, txrun.getRichTextRuns()[0].isBold());

    }

    /**
     * Second fix from Bug 42474: Incorrect matching of notes to slides
     */
    @Test
    public void bug42474_2() throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42474-2.ppt"));

        SlideShow ppt = new SlideShow(hslf);

        //map slide number and starting phrase of its notes
        Map<Integer, String> notesMap = new HashMap<Integer, String>();
        notesMap.put(Integer.valueOf(4), "For  decades before calculators");
        notesMap.put(Integer.valueOf(5), "Several commercial applications");
        notesMap.put(Integer.valueOf(6), "There are three variations of LNS that are discussed here");
        notesMap.put(Integer.valueOf(7), "Although multiply and square root are easier");
        notesMap.put(Integer.valueOf(8), "The bus Z is split into Z_H and Z_L");

        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Integer slideNumber = Integer.valueOf(slide[i].getSlideNumber());
            Notes notes = slide[i].getNotesSheet();
            if (notesMap.containsKey(slideNumber)){
                assertNotNull(notes);
                String text = notes.getTextRuns()[0].getRawText();
                String startingPhrase = notesMap.get(slideNumber);
                assertTrue("Notes for slide " + slideNumber + " must start with " +
                        startingPhrase , text.startsWith(startingPhrase));
            }
        }
    }

    /**
     * Bug 42485: All TextBoxes inside ShapeGroups have null TextRuns
     */
    @Test
    public void bug42485 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42485.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        Shape[] shape = ppt.getSlides()[0].getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof ShapeGroup){
                ShapeGroup  group = (ShapeGroup)shape[i];
                Shape[] sh = group.getShapes();
                for (int j = 0; j < sh.length; j++) {
                    if( sh[j] instanceof TextBox){
                        TextBox txt = (TextBox)sh[j];
                        assertNotNull(txt.getTextRun());
                    }
                }
            }
        }
    }

    /**
     * Bug 42484: NullPointerException from ShapeGroup.getAnchor()
     */
    @Test
    public void bug42484 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42485.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        Shape[] shape = ppt.getSlides()[0].getShapes();
        for (int i = 0; i < shape.length; i++) {
            if(shape[i] instanceof ShapeGroup){
                ShapeGroup  group = (ShapeGroup)shape[i];
                assertNotNull(group.getAnchor());
                Shape[] sh = group.getShapes();
                for (int j = 0; j < sh.length; j++) {
                    assertNotNull(sh[j].getAnchor());
                }
            }
        }
        assertTrue("No Exceptions while reading file", true);
    }

    /**
     * Bug 41381: Exception from Slide.getMasterSheet() on a seemingly valid PPT file
     */
    @Test
    public void bug41381() throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("alterman_security.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlidesMasters().length);
        assertEquals(1, ppt.getTitleMasters().length);
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            MasterSheet master = slide[i].getMasterSheet();
            if (i == 0) assertTrue(master instanceof TitleMaster); //the first slide follows TitleMaster
            else assertTrue(master instanceof SlideMaster);
        }
    }

    /**
     * Bug 42486:  Failure parsing a seemingly valid PPT
     */
    @Test
    public void bug42486 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42486.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            @SuppressWarnings("unused")
            Shape[] shape = slide[i].getShapes();
        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 42524:  NPE in Shape.getShapeType()
     */
    @Test
    public void bug42524 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42486.ppt"));

        SlideShow ppt = new SlideShow(hslf);
        //walk down the tree and see if there were no errors while reading
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
                assertNotNull(shape[j].getShapeName());
                if (shape[j] instanceof ShapeGroup){
                    ShapeGroup group = (ShapeGroup)shape[j];
                    Shape[] comps = group.getShapes();
                    for (int k = 0; k < comps.length; k++) {
                        assertNotNull(comps[k].getShapeName());
                   }
                }
            }

        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 42520:  NPE in Picture.getPictureData()
     */
    @Test
    public void bug42520 () throws Exception {
        HSLFSlideShow hslf = new HSLFSlideShow(_slTests.openResourceAsStream("42520.ppt"));

        SlideShow ppt = new SlideShow(hslf);

        //test case from the bug report
        ShapeGroup shapeGroup = (ShapeGroup)ppt.getSlides()[11].getShapes()[10];
        Picture picture = (Picture)shapeGroup.getShapes()[0];
        picture.getPictureData();

        //walk down the tree and see if there were no errors while reading
        Slide[] slide = ppt.getSlides();
        for (int i = 0; i < slide.length; i++) {
            Shape[] shape = slide[i].getShapes();
            for (int j = 0; j < shape.length; j++) {
              if (shape[j] instanceof ShapeGroup){
                    ShapeGroup group = (ShapeGroup)shape[j];
                    Shape[] comps = group.getShapes();
                    for (int k = 0; k < comps.length; k++) {
                        Shape comp = comps[k];
                        if (comp instanceof Picture){
                            @SuppressWarnings("unused")
                            PictureData pict = ((Picture)comp).getPictureData();
                        }
                    }
                }
            }

        }
        assertTrue("No Exceptions while reading file", true);

    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    @Test
    public void bug38256 () throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("38256.ppt"));

        assertTrue("No Exceptions while reading file", true);

        Slide[] slide = ppt.getSlides();
        assertEquals(1, slide.length);
        TextRun[] runs = slide[0].getTextRuns();
        assertEquals(4, runs.length);

        Set<String> txt = new HashSet<String>();
        txt.add("\u201CHAPPY BIRTHDAY SCOTT\u201D");
        txt.add("Have a HAPPY DAY");
        txt.add("PS Nobody is allowed to hassle Scott TODAY\u2026");
        txt.add("Drinks will be in the Boardroom at 5pm today to celebrate Scott\u2019s B\u2019Day\u2026  See you all there!");

        for (int i = 0; i < runs.length; i++) {
            String text = runs[i].getRawText();
            assertTrue(text, txt.contains(text));
        }

    }

    /**
     * Bug 38256:  RuntimeException: Couldn't instantiate the class for type with id 0.
     * ( also fixed followup: getTextRuns() returns no text )
     */
    @Test
    public void bug43781 () throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("43781.ppt"));

        assertTrue("No Exceptions while reading file", true);

        // Check the first slide
        Slide slide = ppt.getSlides()[0];
        TextRun[] slTr = slide.getTextRuns();
        
        // Has two text runs, one from slide text, one from drawing
        assertEquals(2, slTr.length);
        assertEquals(false, slTr[0].isDrawingBased());
        assertEquals(true, slTr[1].isDrawingBased());
        assertEquals("First run", slTr[0].getText());
        assertEquals("Second run", slTr[1].getText());

        // Check the shape based text runs
        List<TextRun> lst = new ArrayList<TextRun>();
        Shape[] shape = slide.getShapes();
        for (int i = 0; i < shape.length; i++) {
            if( shape[i] instanceof TextShape){
                TextRun textRun = ((TextShape)shape[i]).getTextRun();
                if(textRun != null) {
                    lst.add(textRun);
                }
            }

        }
        // There should be only one shape based one found
        assertEquals(1, lst.size());
        
        // And it should be the second one
        assertEquals("Second run", lst.get(0).getText());
    }

    /**
     * Bug 44296: HSLF Not Extracting Slide Background Image
     */
    @Test
    public void bug44296  () throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("44296.ppt"));

        Slide slide = ppt.getSlides()[0];

        Background b = slide.getBackground();
        Fill f = b.getFill();
        assertEquals(Fill.FILL_PICTURE, f.getFillType());

        PictureData pict = f.getPictureData();
        assertNotNull(pict);
        assertEquals(Picture.JPEG, pict.getType());
    }

    /**
     * Bug 44770: java.lang.RuntimeException: Couldn't instantiate the class for type with id 1036 on class class org.apache.poi.hslf.record.PPDrawing
     */
    @Test
    public void bug44770() throws Exception {
        try {
             new SlideShow(_slTests.openResourceAsStream("44770.ppt"));
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Couldn't instantiate the class for type with id 1036 on class class org.apache.poi.hslf.record.PPDrawing")) {
                throw new AssertionFailedError("Identified bug 44770");
            }
            throw e;
        }
    }

    /**
     * Bug 41071: Will not extract text from Powerpoint TextBoxes
     */
    @Test
    public void bug41071() throws Exception {
        SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("41071.ppt"));

        Slide slide = ppt.getSlides()[0];
        Shape[] sh = slide.getShapes();
        assertEquals(1, sh.length);
        assertTrue(sh[0] instanceof TextShape);
        TextShape tx = (TextShape)sh[0];
        assertEquals("Fundera, planera och involvera.", tx.getTextRun().getText());

        TextRun[] run = slide.getTextRuns();
        assertEquals(1, run.length);
        assertEquals("Fundera, planera och involvera.", run[0].getText());
    }

    /**
     * PowerPoint 95 files should throw a more helpful exception
     * @throws Exception
     */
    @Test(expected=OldPowerPointFormatException.class)
    public void bug41711() throws Exception {
    	// New file is fine
        new SlideShow(_slTests.openResourceAsStream("SampleShow.ppt"));

        // PowerPoint 95 gives an old format exception
    	new SlideShow(_slTests.openResourceAsStream("PPT95.ppt"));
    }
    
    /**
     * Changing text from Ascii to Unicode
     */
    @Test
    public void bug49648() throws Exception {
       SlideShow ppt = new SlideShow(_slTests.openResourceAsStream("49648.ppt"));
       for(Slide slide : ppt.getSlides()) {
          for(TextRun run : slide.getTextRuns()) {
             String text = run.getRawText();
             text.replace("{txtTot}", "With \u0123\u1234\u5678 unicode");
             run.setRawText(text);
          }
       }
    }

    /**
     * Bug 41246: AIOOB with illegal note references
     */
    @Test
    public void bug41246a() throws Exception {
        InputStream fis = _slTests.openResourceAsStream("41246-1.ppt");
        HSLFSlideShow hslf = new HSLFSlideShow(fis);
        fis.close();

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        ppt = HSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertTrue("No Exceptions while rewriting file", true);
    }

    @Test
    public void bug41246b() throws Exception {
        InputStream fis = _slTests.openResourceAsStream("41246-2.ppt");
        HSLFSlideShow hslf = new HSLFSlideShow(fis);
        fis.close();

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        ppt = HSLFTestDataSamples.writeOutAndReadBack(ppt);
        assertTrue("No Exceptions while rewriting file", true);
    }

    /**
     * Bug 45776: Fix corrupt file problem using TextRun.setText
     */
    @Test
    public void bug45776() throws Exception {
        InputStream is = _slTests.openResourceAsStream("45776.ppt");
        SlideShow ppt = new SlideShow(new HSLFSlideShow(is));
        is.close();

        // get slides
        for (Slide slide : ppt.getSlides()) {
            for (Shape shape : slide.getShapes()) {
                if (!(shape instanceof TextBox)) continue;
                TextBox tb = (TextBox) shape;
                // work with TextBox
                String str = tb.getText();

                if (!str.contains("$$DATE$$")) continue;
                str = str.replace("$$DATE$$", new Date().toString());
                tb.setText(str);
                
                TextRun tr = tb.getTextRun();
                assertEquals(str.length()+1,tr.getStyleTextPropAtom().getParagraphStyles().getFirst().getCharactersCovered());
                assertEquals(str.length()+1,tr.getStyleTextPropAtom().getCharacterStyles().getFirst().getCharactersCovered());
            }
        }
    }

    @Test
    public void bug55732() throws Exception {
        File file = _slTests.getFile("bug55732.ppt");
        
        HSLFSlideShow ss = new HSLFSlideShow(file.getAbsolutePath());
        SlideShow _show = new SlideShow(ss);
        Slide[] _slides = _show.getSlides();

        /* Iterate over slides and extract text */
        for( Slide slide : _slides ) {
            HeadersFooters hf = slide.getHeadersFooters();
            /*boolean visible =*/ hf.isHeaderVisible(); // exception happens here
        }
        assertTrue("No Exceptions while reading headers", true);
    }
    
    @Test
    public void bug56260() throws Exception {
        File file = _slTests.getFile("56260.ppt");
        
        HSLFSlideShow ss = new HSLFSlideShow(file.getAbsolutePath());
        SlideShow _show = new SlideShow(ss);
        Slide[] _slides = _show.getSlides();
        assertEquals(13, _slides.length);
        
        // Check the number of TextHeaderAtoms on Slide 1
        Document dr = _show.getDocumentRecord();
        SlideListWithText slidesSLWT = dr.getSlideSlideListWithText();
        SlideAtomsSet s1 = slidesSLWT.getSlideAtomsSets()[0];

        int tha = 0;
        for (Record r : s1.getSlideRecords()) {
            if (r instanceof TextHeaderAtom) tha++;
        }
        assertEquals(2, tha);
        
        // Check to see that we have a pair next to each other
        assertEquals(TextHeaderAtom.class, s1.getSlideRecords()[0].getClass());
        assertEquals(TextHeaderAtom.class, s1.getSlideRecords()[1].getClass());
        
        
        // Check the number of text runs based on the slide (not textbox)
        // Will have skipped the empty one
        int str = 0;
        for (TextRun tr : _slides[0].getTextRuns()) {
            if (! tr.isDrawingBased()) str++;
        }
        assertEquals(1, str);
    }
    
    @Test
    public void bug37625() throws IOException {
        InputStream inputStream = new FileInputStream(_slTests.getFile("37625.ppt"));
        try {
            SlideShow slideShow = new SlideShow(inputStream);
            assertEquals(29, slideShow.getSlides().length);
            
            SlideShow slideBack = HSLFTestDataSamples.writeOutAndReadBack(slideShow);
            assertNotNull(slideBack);
            assertEquals(29, slideBack.getSlides().length);
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug57272() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("57272_corrupted_usereditatom.ppt"));
        try {
            SlideShow slideShow = new SlideShow(inputStream);
            assertEquals(6, slideShow.getSlides().length);

            SlideShow slideBack = HSLFTestDataSamples.writeOutAndReadBack(slideShow);
            assertNotNull(slideBack);
            assertEquals(6, slideBack.getSlides().length);
        } finally {
            inputStream.close();
        }
    }

    @Test
    public void bug49541() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("49541_symbol_map.ppt"));
        try {
            SlideShow slideShow = new SlideShow(inputStream);
            Slide slide = slideShow.getSlides()[0];
            ShapeGroup sg = (ShapeGroup)slide.getShapes()[0];
            TextBox tb = (TextBox)sg.getShapes()[0];
            String text = StringUtil.mapMsCodepointString(tb.getText());
            assertEquals("\u226575 years", text);
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug47261() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("bug47261.ppt"));
        try {
            SlideShow slideShow = new SlideShow(inputStream);
            slideShow.removeSlide(0);
            slideShow.createSlide();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            slideShow.write(bos);
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug56240() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("bug56240.ppt"));
        try {
            SlideShow slideShow = new SlideShow(inputStream);
            int slideCnt = slideShow.getSlides().length;
            assertEquals(105, slideCnt);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            slideShow.write(bos);
            bos.close();
        } finally {
            inputStream.close();
        }
    }
    
    @Test
    public void bug46441() throws Exception {
        InputStream inputStream = new FileInputStream(_slTests.getFile("bug46441.ppt"));
        try {
            SlideShow slideShow = new SlideShow(inputStream);
            AutoShape as = (AutoShape)slideShow.getSlides()[0].getShapes()[0];
            EscherOptRecord opt = as.getEscherOptRecord();
            EscherArrayProperty ep = Shape.getEscherProperty(opt, EscherProperties.FILL__SHADECOLORS);
            double exp[][] = {
                // r, g, b, position
                { 94, 158, 255, 0 },
                { 133, 194, 255, 0.399994 },
                { 196, 214, 235, 0.699997 },
                { 255, 235, 250, 1 }                    
            };
            
            int i = 0;
            for (byte[] data : ep) {
                EscherColorRef ecr = new EscherColorRef(data, 0, 4);
                int rgb[] = ecr.getRGB();
                double pos = Units.fixedPointToDecimal(LittleEndian.getInt(data, 4));
                assertEquals((int)exp[i][0], rgb[0]);
                assertEquals((int)exp[i][1], rgb[1]);
                assertEquals((int)exp[i][2], rgb[2]);
                assertEquals(exp[i][3], pos, 0.01);
                i++;
            }
        } finally {
            inputStream.close();
        }
    }
}
