/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.draw2d.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * A vertex representation for the ShortestPathRouting. Vertices are either one of 
 * four corners on an <code>Obstacle</code>(Rectangle), or one of the two end points of a <code>Path</code>.
 * 
 * This class is not intended to be subclassed.
 * @author Whitney Sorenson
 * @since 3.0
 */
class Vertex 
	extends Point 
{

/**
 * The default amount to offset multiple paths on a Vertex.
 */
public final static int BEND_OFFSET = 4;

// constants for the vertex type
final static int NOT_SET = 0;
final static int INNIE = 1;
final static int OUTIE = 2;

// for shortest path
List neighbors;
boolean isPermanent = false;
Vertex label;
double cost = 0;

// for routing
double shortestDistance = 0;
double offset = 0;
int type = NOT_SET;
int count = 0;
int totalCount = 0;
Obstacle obs;
List paths;
boolean shortestDistanceChecked = false;
Map pathsToSegmentsMap;
int positionOnObstacle = -1;

private int origX, origY;

/**
 * Creates a new Vertex with the given x, y position and on the given obstacle.
 * 
 * @param x x point
 * @param y y point
 * @param obs obstacle - can be null
 */
Vertex(int x, int y, Obstacle obs) {
	super(x, y);
	origX = x;
	origY = y;
	this.obs = obs;
}

/**
 * Creates a new Vertex with the given point position and on the given obstacle.
 * 
 * @param p the point
 * @param obs obstacle - can be null
 */
Vertex(Point p, Obstacle obs) {
	this(p.x, p.y, obs);
}

/**
 * Adds a path to this vertex, calculates angle between two segments and caches it.
 * 
 * @param path the path
 * @param start the segment to this vertex
 * @param end the segment away from this vertex
 */
void addPath(Path path, Segment start, Segment end) {
	if (paths == null) {
		paths = new ArrayList();
		pathsToSegmentsMap = new HashMap();
	}
	if (!paths.contains(path))
		paths.add(path);
	pathsToSegmentsMap.put(path, new Double(start.cosine(end)));
}

/**
 * Creates a point that represents this vertex offset by the given amount times
 * the offset.
 * 
 * @param modifier the offset
 * @return a Point that has been bent around this vertex
 */
Point bend(int modifier) {
	Point point = new Point(x, y);
	if ((positionOnObstacle & PositionConstants.NORTH) > 0)
		point.y -= modifier * offset;
	else
		point.y += modifier * offset;
	if ((positionOnObstacle & PositionConstants.EAST) > 0)
		point.x += modifier * offset;
	else
		point.x -= modifier * offset;
	return point;
}

/**
 * Resets all fields on this Vertex.
 */
void fullReset() {
	totalCount = 0;
	type = NOT_SET;
	count = 0;
	cost = 0;
	offset = BEND_OFFSET;
	shortestDistance = 0;
	label = null;
	shortestDistanceChecked = false;
	isPermanent = false;		
	if (neighbors != null)
		neighbors.clear();
	if (pathsToSegmentsMap != null)
		pathsToSegmentsMap.clear();
	if (paths != null) 
		paths.clear();
}

/**
 * Returns a Rectangle that represents the region around this vertex that
 * paths will be traveling in.
 * 
 * @param extraOffset a buffer to add to the region.
 * @return the rectangle
 */
Rectangle getDeformedRectangle(int extraOffset) {
	Rectangle rect = new Rectangle(0, 0, 0, 0);
	
	if ((positionOnObstacle & PositionConstants.NORTH) > 0) {
		rect.y = y - extraOffset;
		rect.height = origY - y + extraOffset;
	} else {
		rect.y = origY;
		rect.height = y - origY + extraOffset;
	}
	if ((positionOnObstacle & PositionConstants.EAST) > 0) {
		rect.x = origX;
		rect.width = x - origX + extraOffset;
	} else {
		rect.x = x - extraOffset;
		rect.width = origX - x + extraOffset;
	}
	
	return rect;
}

/**
 * Grows this vertex by its offset to its maximum size.
 */
void grow() {
	int modifier;
	
	if (shortestDistance == 0)
		modifier = totalCount * BEND_OFFSET;
	else
		modifier = (int)(shortestDistance / 2) - 1;
	
	if ((positionOnObstacle & PositionConstants.NORTH) > 0)
		y -= modifier;
	else
		y += modifier;
	if ((positionOnObstacle & PositionConstants.EAST) > 0)
		x += modifier;
	else
		x -= modifier;
}

/**
 * Shrinks this vertex to its original size.
 */
void shrink() {
	x = origX;
	y = origY;
}

/**
 * Updates the offset of this vertex based on its shortest distance.
 */
void updateOffset() {
	if (shortestDistance != 0)
		offset = ((shortestDistance / 2) - 1) / totalCount;
}

}