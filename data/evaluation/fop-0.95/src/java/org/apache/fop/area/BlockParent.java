/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: BlockParent.java 557337 2007-07-18 17:37:14Z adelmelle $ */
 
package org.apache.fop.area;

import java.util.ArrayList;
import java.util.List;

/**
 * A BlockParent holds block-level areas.
 */
public class BlockParent extends Area {

    // this position is used for absolute position
    // or as an indent
    // this has the size in the block progression dimension

    /**
     * The x offset position of this block parent.
     * Used for relative (serves as left-offset trait) and absolute positioning
     * (serves as left-position trait).
     */
    protected int xOffset = 0;

    /**
     * The y offset position of this block parent.
     * Used for relative (serves as top-offset trait) and absolute positioning
     * (serves as top-position trait).
     */
    protected int yOffset = 0;

    /**
     * The children of this block parent area.
     */
    protected List children = null;

    // orientation if reference area
    private int orientation = ORIENT_0;

    /** {@inheritDoc} */
    public void addChildArea(Area childArea) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(childArea);
    }
    
    /**
     * Add the block area to this block parent.
     *
     * @param block the child block area to add
     */
    public void addBlock(Block block) {
        addChildArea(block);
    }

    /**
     * Get the list of child areas for this block area.
     *
     * @return the list of child areas
     */
    public List getChildAreas() {
        return children;
    }

    /**
     * Check whether there are child areas.
     *
     * @return the result.
     */
    public boolean isEmpty() {
        return children == null || children.size() == 0;
    }

    /**
     * Set the X offset of this block parent area.
     *
     * @param off the x offset of the block parent area
     */
    public void setXOffset(int off) {
        xOffset = off;
    }

    /**
     * Set the Y offset of this block parent area.
     *
     * @param off the y offset of the block parent area
     */
    public void setYOffset(int off) {
        yOffset = off;
    }

    /**
     * Get the X offset of this block parent area.
     *
     * @return the x offset of the block parent area
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * Get the Y offset of this block parent area.
     *
     * @return the y offset of the block parent area
     */
    public int getYOffset() {
        return yOffset;
    }
}
