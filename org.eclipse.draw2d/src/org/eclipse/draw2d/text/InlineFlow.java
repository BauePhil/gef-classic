/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d.text;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A <code>FlowFigure</code> represented by multiple <code>LineBox</code> fragments. An
 * <code>InlineFlow</code>'s parent must be either a {@link BlockFlow} or another
 * InlineFlow.
 * 
 * <P>An InlineFlow may contain other InlineFlow figures.
 * 
 * <P>WARNING: This class is not intended to be subclassed by clients.
 * @author hudsonr
 * @since 2.0
 */
public class InlineFlow extends FlowFigure {

List fragments = new ArrayList(1);

/** * @see org.eclipse.draw2d.IFigure#containsPoint(int, int) */
public boolean containsPoint(int x, int y) {
	if (!super.containsPoint(x, y))
		return false;
	List frags = getFragments();
	for (int i = 0; i < frags.size(); i++)
		if (((FlowBox)frags.get(i)).containsPoint(x, y))
			return true;

	return false;
}

/**
 * @see FlowFigure#createDefaultFlowLayout()
 */
protected FlowFigureLayout createDefaultFlowLayout() {
	return new InlineFlowLayout(this);
}

/**
 * Returns the <code>LineBox</code> fragments contained in this InlineFlow
 * @return The fragments
 */
public List getFragments() {
	return fragments;
}

/**
 * @see FlowFigure#postValidate()
 */
public void postValidate() {
	List list = getFragments();
	FlowBox box;
	int left = Integer.MAX_VALUE, top = left;
	int right = Integer.MIN_VALUE, bottom = right;
	for (int i = 0; i < list.size(); i++) {
		box = (FlowBox)list.get(i);
		left = Math.min(left, box.x);
		right = Math.max(right, box.x + box.width);
		top = Math.min(top, box.y);
		bottom = Math.max(bottom, box.y + box.height);
	}
	setBounds(new Rectangle(left, top, right - left, bottom - top));
	list = getChildren();
	for (int i = 0; i < list.size(); i++)
		((FlowFigure)list.get(i)).postValidate();
}

}
