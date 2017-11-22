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

    public SomfyCULHandler(Thing thing) {
        super(thing);
        propertyFile = new File(ConfigConstants.getUserDataFolder() + File.separator + "somfycul" + File.separator
                + thing.getUID().getAsString().replace(':', '_') + ".properties");
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
                // Get config for this roller shutter - save rollingCode and address
                Properties p = loadProperties();

                // We delegate the execution to the bridge handler
                ThingHandler bridgeHandler = getBridge().getHandler();
                if (bridgeHandler instanceof CulHandler) {
                    String rollingCode = p.getProperty("rollingCode", "0000");
                    String address = p.getProperty("address", "000000");
                    logger.debug("rolling code before command {}", rollingCode);

                    boolean executedSuccessfully = ((CulHandler) bridgeHandler).executeCULCommand(getThing(),
                            somfyCommand, rollingCode, address);
                    if (executedSuccessfully && command instanceof State) {
                        updateState(channelUID, (State) command);

                        long rollCode = Long.decode("0x" + rollingCode);
                        rollCode++;
                        rollingCode = String.format("%04X", rollCode);
                        logger.debug("Updated rolling code to {}", rollingCode);
                        p.setProperty("rollingCode", rollingCode);
                        p.setProperty("address", address);

                        storeProperties(p);
                    }
                }
            }
        }
    }

    private void storeProperties(Properties p) {
        try {
            FileWriter fileWriter = new FileWriter(propertyFile);
            p.store(fileWriter, "no comment");
            fileWriter.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Properties loadProperties() {
        Properties p = new Properties();

        if (!propertyFile.exists()) {
            // TODO Create file during programming the shutter
            try {
                logger.debug("Trying to create file {}.", propertyFile);
                Files.createParentDirs(propertyFile);
                Files.touch(propertyFile);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        try {
            FileReader fileReader = new FileReader(propertyFile);
            p.load(fileReader);
            fileReader.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return p;
    }

    @Override
    public void initialize() {
        // TODO: Thing should only be initialized after proper programming and rolling code be set.
        logger.info("Added roler shutter");
        updateStatus(ThingStatus.ONLINE);

    }
}
