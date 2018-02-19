/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfycul;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SomfyCULBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Daniel Weisser - Initial contribution
 */
@NonNullByDefault
public class SomfyCULBindingConstants {

    private static final String BINDING_ID = "somfycul";

    // List of all Thing Type UIDs
    /**
     * CUL stick
     */
    public static final ThingTypeUID CUL_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "culdevice");

    /**
     * Somfy Device (e.g. rollershutter)
     */
    public static final ThingTypeUID SOMFY_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "somfydevice");

    /**
     * Rollershutter's position
     */
    public static final String POSITION = "position";

    /**
     * Rollershutter's program
     */
    public static final String PROGRAM = "program";

}
