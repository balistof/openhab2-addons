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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.config.core.ConfigConstants;
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

import com.google.common.io.Files;

/**
 * The {@link SomfyCULHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Daniel Weisser - Initial contribution
 */
@NonNullByDefault
public class SomfyCULHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyCULHandler.class);
    private File propertyFile;
    private Properties p;

    /**
     * Initializes the thing. As persistent state is necessary the properties are stored in the user data directory and
     * fetched within the constructor.
     *
     * @param thing
     */
    public SomfyCULHandler(Thing thing) {
        super(thing);
        propertyFile = new File(ConfigConstants.getUserDataFolder() + File.separator + "somfycul" + File.separator
                + thing.getUID().getAsString().replace(':', '_') + ".properties");
        p = initProperties();
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
                // We delegate the execution to the bridge handler
                ThingHandler bridgeHandler = getBridge().getHandler();
                if (bridgeHandler instanceof CulHandler) {
                    logger.debug("rolling code before command {}", p.getProperty("rollingCode"));

                    boolean executedSuccessfully = ((CulHandler) bridgeHandler).executeCULCommand(getThing(),
                            somfyCommand, p.getProperty("rollingCode"), p.getProperty("address"));
                    if (executedSuccessfully && command instanceof State) {
                        updateState(channelUID, (State) command);

                        long newRollingCode = Long.decode("0x" + p.getProperty("rollingCode")) + 1;
                        p.setProperty("rollingCode", String.format("%04X", newRollingCode));
                        logger.debug("Updated rolling code to {}", p.getProperty("rollingCode"));
                        p.setProperty("address", p.getProperty("address"));

                        try {
                            p.store(new FileWriter(propertyFile), "no comment");
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Initializes the properties for the thing (shutter).
     *
     * @return Valid properties (address and rollingCode)
     */
    private Properties initProperties() {
        p = new Properties();

        try {
            if (!propertyFile.exists()) {
                logger.debug("Trying to create file {}.", propertyFile);
                Files.createParentDirs(propertyFile);
                FileWriter fileWriter = new FileWriter(propertyFile);

                // TODO Calculate new address based on other fields
                p.setProperty("rollingCode", "0000");
                p.setProperty("address", "000000");
                p.store(fileWriter, "Initialized fields");
                fileWriter.close();
            } else {
                FileReader fileReader = new FileReader(propertyFile);
                p.load(fileReader);
                fileReader.close();
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return p;
    }

    /**
     * The roller shutter is by default initialized and set to online, as there is no feedback that can check if the
     * shutter is available.
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {
        logger.info("Initialized roller shutter");
        updateStatus(ThingStatus.ONLINE);
    }
}
