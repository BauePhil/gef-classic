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
package org.eclipse.gef.commands;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Stack;

/**
 * An implementation of a command stack.  A command stack manages the executing, undoing,
 * and redoing of {@link Command Commands}.  Executed commands are pushed onto an undo
 * stack. Commands that are undone are pushed onto a Redo stack. Whenever a new command is
 * executed, the Redo stack is flushed.
 * <P>
 * A CommandStack contains a dirty property. This property can be used to determine
 * when persisting the Commands' changes is required. The stack is dirty whenever the last
 * executed or redone command is different than the command that was at the top of the
 * undo stack when {@link #markSaveLocation()} was last called. Upon construction, the
 * undo stack is empty, and is not dirty.
 * 
 * @author hudsonr
 */
public class CommandStack {

private int undoLimit = 0;
private int saveLocation = 0;
private Stack undo = new Stack();
private Stack redo = new Stack();

/**
 * the list of {@link CommandStackListener}s
 */
protected List listeners = new ArrayList();

/**
 * Constructs a command stack. By default, there is no undo limit.
 */
public CommandStack() { }

/**
 * Adds the specified listener. Multiple adds will result in multiple notifications.
 * @param listener the listener
 */
public void addCommandStackListener(CommandStackListener listener) {
	listeners.add(listener);
}

/**
 * @return <code>true</code> if it is appropriate to call {@link #redo()}.
 */
public boolean canRedo() {
	return !redo.isEmpty();
}

/**
 * @return <code>true</code> if {@link #undo()} can be called
 */
public boolean canUndo() {
	if (undo.size() == 0)
		return false;
	return ((Command)undo.lastElement()).canUndo();
}

/**
 * Executes the specified Command if it is executable. Flushes the redo stack.
 * @param command the Command to execute
 */
public void execute(Command command) {
	if (command == null || !command.canExecute())
		return;
	flushRedo();
	command.execute();
	if (getUndoLimit() > 0) {
		while (undo.size() >= getUndoLimit()) {
			((Command)undo.remove(0)).dispose();
			if (saveLocation > -1)
				saveLocation--;
		}
	}
	if (saveLocation > undo.size())
		saveLocation = -1; //The save point was somewhere in the redo stack
	undo.push(command);
	notifyListeners();
}

/**
 * This will <code>dispose()</code> all the commands in both the undo and redo stack. Both
 * stacks will be empty afterwards.
 */
public void dispose() {
	flushUndo();
	flushRedo();
}

/**
 * Flushes the entire stack and resets the save location to zero. This method might be
 * called when performing "revert to saved".
 */
public void flush() {
	flushRedo();
	flushUndo();
	saveLocation = 0;
	notifyListeners();
}

private void flushRedo() {
	while (!redo.isEmpty())
		((Command)redo.pop()).dispose();
}

private void flushUndo() {
	while (!undo.isEmpty())
		((Command)undo.pop()).dispose();
}

/**
 * @return an array containing all commands in the order they were executed
 */
public Object[] getCommands() { 
	List commands = new ArrayList(undo);
	for (int i = redo.size() - 1; i >= 0; i--) {
		commands.add(redo.get(i));
	}
	return commands.toArray();
}

/**
 * Peeks at the top of the <i>redo</i> stack. This is useful for describing to the User
 * what will be redone. The returned <code>Command</code> has a label describing it.
 * @return the top of the <i>redo</i> stack, which may be <code>null</code>
 */
public Command getRedoCommand() {
	return redo.isEmpty() ? null : (Command)redo.peek();
}

/**
 * Peeks at the top of the <i>undo</i> stack. This is useful for describing to the User
 * what will be undone. The returned <code>Command</code> has a label describing it.
 * @return the top of the <i>undo</i> stack, which may be <code>null</code>
 */
public Command getUndoCommand() {
	return undo.isEmpty() ? null : (Command)undo.peek();
}

/**
 * Returns the undo limit. The undo limit is the maximum number of atomic operations that
 * the User can undo. <code>-1</code> is used to indicate no limit.
 * @return the undo limit
 */
public int getUndoLimit() {
	return undoLimit;
}

/**
 * Returns true if the stack is dirty. The stack is dirty whenever the last executed or
 * redone command is different than the command that was at the top of the undo stack when
 * {@link #markSaveLocation()} was last called. 
 * @return <code>true</code> if the stack is dirty
 */
public boolean isDirty() {
	return undo.size() != saveLocation;
}

/**
 * Marks the last executed or redone Command as the point at which the changes were saved.
 * Calculation of {@link #isDirty()} will be based on this checkpoint.
 */
public void markSaveLocation() {
	saveLocation = undo.size();
	notifyListeners();
}

/**
 * Sends notification to all {@link CommandStackListener}s.
 */
protected void notifyListeners() {
	EventObject event = new EventObject(this);
	for (int i = 0; i < listeners.size(); i++)
		((CommandStackListener)listeners.get(i))
			.commandStackChanged(event);
}

/**
 * Calls redo on the Command at the top of the <i>redo</i> stack, and pushes that Command
 * onto the <i>undo</i> stack. This method should only be called when {@link #canUndo()}
 * returns <code>true</code>.
 */
public void redo() {
	//Assert.isTrue(canRedo())
	if (!canRedo())
		return;
	Command command = (Command)redo.pop();
	command.redo();
	undo.push(command);
	notifyListeners();
}

/**
 * Removes the first occurance of the specified listener.
 * @param listener the listener
 */
public void removeCommandStackListener(CommandStackListener listener) {
	listeners.remove(listener);
}

/**
 * Sets the undo limit. The undo limit is the maximum number of atomic operations that the
 * User can undo. <code>-1</code> is used to indicate no limit.
 * @param undoLimit the undo limit
 */
public void setUndoLimit(int undoLimit) {
	this.undoLimit = undoLimit;
}

/**
 * Undoes the most recently executed (or redone) Command. The Command is popped from the
 * undo stack to and pushed onto the redo stack. This method should only be called when
 * {@link #canUndo()} returns <code>true</code>.
 */
public void undo() {
	//Assert.isTrue(canUndo());
	Command command = (Command)undo.pop();
	command.undo();
	redo.push(command);
	notifyListeners();
}

}
