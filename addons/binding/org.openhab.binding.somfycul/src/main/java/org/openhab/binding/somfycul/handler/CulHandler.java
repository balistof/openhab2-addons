package org.openhab.binding.somfycul.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CulHandler} is responsible for handling commands, which are
 * sent via the CUL stick.
 *
 * @author Daniel Weisser - Initial contribution
 */
public class CulHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(CulHandler.class);

    public CulHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // the bridge does not have any channels
    }

    @Override
    public void initialize() {
        logger.info("Set CUL stick online");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        logger.info("Set CUL stick offline");
        super.dispose();
    }
}
