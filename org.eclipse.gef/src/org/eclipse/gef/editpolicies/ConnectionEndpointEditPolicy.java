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
package org.eclipse.gef.editpolicies;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import org.eclipse.draw2d.AncestorListener;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Polygon;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.handles.ConnectionEndHandle;
import org.eclipse.gef.handles.ConnectionStartHandle;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * A selection handle policy for placing handles at the two ends of a ConnectionEditPart.
 * All ConnectionEditParts should have one of these, even if the ends of the connection
 * aren't draggable, because this is the primary SelectionEditPolicy for showing focus.
 * <P>
 * A connection can receive focus but not selection by pressing <code>Control+/</code> on
 * the keyboard.
 * @since 2.0 */
public class ConnectionEndpointEditPolicy
	extends SelectionHandlesEditPolicy
{

private ConnectionAnchor originalAnchor;
private FeedbackHelper feedbackHelper;
private ConnectionFocus focus;

static final Color XORFocusColor = new Color(null, 255, 255, 255);

class ConnectionFocus
	extends Polygon
	implements PropertyChangeListener
{
	PointList second;
	AncestorListener ancestorListener = new AncestorListener.Stub() {
		public void ancestorMoved(IFigure ancestor) {
			revalidate();
		}
	};
	
	ConnectionFocus() {
		setForegroundColor(XORFocusColor);
		setBackgroundColor(ColorConstants.black);
		setXOR(true);
		setLineStyle(Graphics.LINE_DOT);
		setFill(false);
		setOutline(true);
	}
	
	public void addNotify() {
		super.addNotify();
		getConnection().addPropertyChangeListener(Connection.PROPERTY_POINTS, this);
		getConnection().addAncestorListener(ancestorListener);
	}
	protected void outlineShape(Graphics g) {
		super.outlineShape(g);
		g.drawPolygon(second);
	}
	public void propertyChange(PropertyChangeEvent evt) {
		revalidate();
	}

	public void removeNotify() {
		getConnection().removePropertyChangeListener(Connection.PROPERTY_POINTS, this);
		getConnection().removeAncestorListener(ancestorListener);
		super.removeNotify();
	}
	
	public void validate() {
		if (isValid())
			return;
		PointList points = getConnection().getPoints().getCopy();
		getConnection().translateToAbsolute(points);
		points = StrokePointList.strokeList(points, 5);
		translateToRelative(points);
		setPoints(points);
		second = new PointList(points.size() + 2);
		int minY = Integer.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.getPoint(i);
			if (p.y < minY) {
				minY = p.y;
				index = i;
			}
			second.addPoint(p);
		}
		if (index != -1) {
			second.insertPoint(points.getPoint(index), index);
			second.insertPoint(points.getPoint(index).translate(0, -1), index + 1);
			second.insertPoint(points.getPoint(index).translate(-1, -1), index + 1);
			second.insertPoint(points.getPoint(index).translate(0, -2), index + 1);
		}
	}
}

/** * @see org.eclipse.gef.editpolicies.SelectionHandlesEditPolicy#createSelectionHandles() */
protected List createSelectionHandles() {
	List list = new ArrayList();
	list.add(new ConnectionEndHandle((ConnectionEditPart)getHost()));
	list.add(new ConnectionStartHandle((ConnectionEditPart)getHost()));
 	return list;
}

/**
 * Erases connection move feedback. This method is called when a ReconnectRequest is
 * received.
 * @param request the reconnect request.
 */
protected void eraseConnectionMoveFeedback(ReconnectRequest request) {
	if (originalAnchor == null)
		return;
	if (request.isMovingStartAnchor())
		getConnection().setSourceAnchor(originalAnchor);
	else
		getConnection().setTargetAnchor(originalAnchor);
	originalAnchor = null;
	feedbackHelper = null;
}

/**
 * @see org.eclipse.gef.EditPolicy#eraseSourceFeedback(org.eclipse.gef.Request)
 */
public void eraseSourceFeedback(Request request) {
	if (REQ_RECONNECT_TARGET.equals(request.getType())
	  || REQ_RECONNECT_SOURCE.equals(request.getType()))
		eraseConnectionMoveFeedback((ReconnectRequest)request);
}

/**
 * @see org.eclipse.gef.EditPolicy#getCommand(org.eclipse.gef.Request)
 */
public Command getCommand(Request request) {
	return null;
}

/**
 * Convenience method for obtaining the host's <code>Connection</code> figure.
 * @return the Connection figure
 */
protected Connection getConnection() {
	return (Connection)((GraphicalEditPart)getHost()).getFigure();
}

/**
 * Lazily creates and returns the feedback helper for the given request. The helper will
 * be configured as either moving the source or target end of the connection.
 * @param request the reconnect request
 * @return the feedback helper
 */
protected FeedbackHelper getFeedbackHelper(ReconnectRequest request) {
	if (feedbackHelper == null) {
		feedbackHelper = new FeedbackHelper();
		feedbackHelper.setConnection(getConnection());
		feedbackHelper.setMovingStartAnchor(request.isMovingStartAnchor());
	}
	return feedbackHelper;
}

/**
 * Hides the focus indicator. The focus indicator is a dotted outline around the
 * connection.
 * @see #showFocus()
 * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#hideFocus()
 */
protected void hideFocus() {
	if (focus != null) {
		removeFeedback(focus);
		focus = null;
	}
}

/**
 * Shows or updates connection move feedback.  Called whenever a show feedback request is
 * received for reconnection.
 * @param request the reconnect request
 */
protected void showConnectionMoveFeedback(ReconnectRequest request) {
	NodeEditPart node = null;
	if (request.getTarget() instanceof NodeEditPart)
		node = (NodeEditPart)request.getTarget();
	if (originalAnchor == null) {
		if (request.isMovingStartAnchor())
			originalAnchor = getConnection().getSourceAnchor();
		else
			originalAnchor = getConnection().getTargetAnchor();
	}
	ConnectionAnchor anchor = null;
	if (node != null) {
		if (request.isMovingStartAnchor())
			anchor = node.getSourceConnectionAnchor(request);
		else
			anchor = node.getTargetConnectionAnchor(request);
	}
	FeedbackHelper helper = getFeedbackHelper(request);
	helper.update(anchor, request.getLocation());
}

/**
 * Shows focus around the connection.
 * @see org.eclipse.gef.editpolicies.SelectionEditPolicy#showFocus()
 */
protected void showFocus() {
	if (focus == null) {
		focus = new ConnectionFocus();
		addFeedback(focus);
	}
}

/**
 * @see org.eclipse.gef.EditPolicy#showSourceFeedback(org.eclipse.gef.Request)
 */
public void showSourceFeedback(Request request) {
	if (REQ_RECONNECT_SOURCE.equals(request.getType())
	  || REQ_RECONNECT_TARGET.equals(request.getType()))
		showConnectionMoveFeedback((ReconnectRequest)request);
}

}