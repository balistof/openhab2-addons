/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfycul.handler;

import static org.openhab.binding.somfycul.SomfyCULBindingConstants.POSITION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyCULHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Weisser - Initial contribution
 */
@NonNullByDefault
public class SomfyCULHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyCULHandler.class);

    public SomfyCULHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("channelUID: " + channelUID + ", command: " + command);
        if (channelUID.getId().equals(POSITION)) {
            SomfyCommand somfyCommand = null;
            if (command instanceof UpDownType) {
                switch ((UpDownType) command) {
                    case UP:
                        somfyCommand = SomfyCommand.UP;
                        break;
                    case DOWN:
                        somfyCommand = SomfyCommand.DOWN;
                        break;
                }
            } else if (command instanceof StopMoveType) {
                switch ((StopMoveType) command) {
                    case STOP:
                        somfyCommand = SomfyCommand.MY;
                        break;
                    default:
                        break;
                }
            }
            if (somfyCommand != null) {

                // thing.setProperty("RollingCode", "0000");
                // TODO Build the complete command with rolling code
                // thing.getConfiguration().put("RollingCode", "0000");

                // We delegate the execution to the bridge handler
                ThingHandler bridgeHandler = getBridge().getHandler();
                if (bridgeHandler instanceof CulHandler) {
                    boolean executedSuccessfully = ((CulHandler) bridgeHandler).executeCULCommand(getThing(),
                            somfyCommand);
                    if (executedSuccessfully && command instanceof State) {
                        updateState(channelUID, (State) command);
                        // Update rolling code
                        int rollCode = Integer.parseInt(thing.getConfiguration().get("RollingCode").toString());
                        rollCode++;
                        thing.getConfiguration().put("RollingCode", rollCode + "");
                        logger.debug("Updated rolling code to {}", rollCode);
                    }
                }
            }
        }

    }

    @Override
    public void initialize() {
        // TODO: Thing should only be initialized after proper programming and rolling code be set.
        logger.info("Added roler shutter");
        updateStatus(ThingStatus.ONLINE);

    }
}
