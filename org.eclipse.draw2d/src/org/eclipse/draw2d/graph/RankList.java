/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
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

/**
 * For internal use only.  A list of lists.
 * @author hudsonr
 * @since 2.1
 */
public class RankList {

ArrayList ranks = new ArrayList();

/**
 * Returns the specified rank.
 * @param rank the row
 * @return the rank
 */
public Rank getRank(int rank) {
	while (ranks.size() <= rank)
		ranks.add(new Rank());
	return (Rank)ranks.get(rank);
}

/**
 * Returns the total number or ranks.
 * @return the total number of ranks
 */
public int size() {
	return ranks.size();
}

}
