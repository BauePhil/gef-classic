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
package org.eclipse.gef.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;

/**
 * Utilities for {@link org.eclipse.gef.Tool Tools}.
 */
public class ToolUtilities {

/**
 * Returns a list containing the top level selected edit parts based on the viewer's
 * selection.
 * @param viewer the viewer
 * @return the selection excluding dependants
 */
public static List getSelectionWithoutDependants(EditPartViewer viewer) {
	List selectedParts = viewer.getSelectedEditParts();
	List result = new ArrayList();
	for (int i = 0; i < selectedParts.size(); i++) {
		GraphicalEditPart editpart = (GraphicalEditPart)selectedParts.get(i);
		if (!isAncestorContainedIn(selectedParts, editpart))
			result.add(editpart);
	}
	return result;
}

/**
 * Returns a list containing the top level selected edit parts based on the passed in
 * list of selection.
 * @param selectedParts the complete selection
 * @return the selection excluding dependants
 */
public static List getSelectionWithoutDependants(List selectedParts) {
	List result = new ArrayList();
	for (int i = 0; i < selectedParts.size(); i++) {
		GraphicalEditPart editpart = (GraphicalEditPart)selectedParts.get(i);
		if (!isAncestorContainedIn(selectedParts, editpart))
			result.add(editpart);
	}
	return result;
}

/**
 * Filters the given list of EditParts so that the list only contains the EditParts that 
 * understand the given request (i.e. return <code>true</code> from 
 * {@link EditPart#understandsRequest(Request)} when passed the given request).
 * @param list the list of edit parts to filter
 * @param request the request 
 */
public static void filterEditPartsUnderstanding(List list, Request request) {
	Iterator iter = list.iterator();
	while (iter.hasNext()) {
		EditPart ep = (EditPart)iter.next();
		if (!ep.understandsRequest(request))
			iter.remove();
	}
}

private static boolean isAncestorContainedIn(Collection c, EditPart ep) {
	ep = ep.getParent();
	while (ep != null) {
		if (c.contains(ep))
			return true;
		ep = ep.getParent();
	}
	return false;
}

}