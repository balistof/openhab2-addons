/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfycul.handler;

/**
 * The {@code SomfyCommand} provides the available commands due to Somfy's RTS protocol.
 *
 * http://culfw.de/commandref.html#cmd_Y
 *
 * @author Daniel Weisser - Initial contribution
 *
 */
public enum SomfyCommand {
    MY("1"),
    UP("2"),
    DOWN("4"),
    PROG("8");

    private String actionKey;

    private SomfyCommand(String actionKey) {
        this.actionKey = actionKey;
    }

    /**
     * Returns the action key which is used for communicating with the CUL device.
     *
     * @return the action key
     */
    public String getActionKey() {
        return actionKey;
    }

}
