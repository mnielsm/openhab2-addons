/**
 * Copyright (c) 2014-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.freeathome.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.freeathome.handler.FreeAtHomeBaseHandler;
import org.openhab.binding.freeathome.internal.stateconvert.StateConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeAtHomeHandlerUpdateHandler} is responsible for managing the registered update events
 *
 *
 * @author ruebox - Initial contribution
 */

public class FreeAtHomeUpdateHandler {

    private Logger logger = LoggerFactory.getLogger(FreeAtHomeUpdateHandler.class);

    public class FreeAtHomeThingChannel {
        public FreeAtHomeBaseHandler m_BaseHandler = null;;
        public ChannelUID m_Channel = null;
        public StateConverter m_StateConverter = null;

        public FreeAtHomeThingChannel(FreeAtHomeBaseHandler h, ChannelUID c, StateConverter s) {
            m_BaseHandler = h;
            m_Channel = c;
            m_StateConverter = s;
        }
    }

    Map<String, FreeAtHomeThingChannel> m_RegisteredThings;

    public FreeAtHomeUpdateHandler() {
        m_RegisteredThings = new HashMap<String, FreeAtHomeThingChannel>();
    }

    private String GenerateId(String serial, String channel, String dataPoint) {
        return serial + "_" + channel + "_" + dataPoint;
    }

    public void Register(FreeAtHomeUpdateChannel channel) {
        this.RegisterThing(channel.m_Thing, channel.m_OhThingChanneId, channel.m_OhThingStateConverter,
                channel.m_FhSerial, channel.m_FhChannel, channel.m_FhDataPoint);
    }

    private void RegisterThing(FreeAtHomeBaseHandler thing, String channelId, StateConverter state, String serial,
            String channel, String dataPoint) {

        ChannelUID cUid = thing.getThing().getChannel(channelId).getUID();
        FreeAtHomeThingChannel c = new FreeAtHomeThingChannel(thing, cUid, state);

        m_RegisteredThings.put(this.GenerateId(serial, channel, dataPoint), c);
    }

    public void Unregister(FreeAtHomeUpdateChannel channel) {
        UnregisterThing(channel.m_FhSerial, channel.m_FhChannel, channel.m_FhDataPoint);
    }

    private void UnregisterThing(String serial, String channel, String dataPoint) {
        m_RegisteredThings.remove(this.GenerateId(serial, channel, dataPoint));
    }

    public void NotifyThing(String serial, String channel, String dataPoint, String value) {
        // Find entry and call notify method at thing
        FreeAtHomeThingChannel thing = m_RegisteredThings.get(this.GenerateId(serial, channel, dataPoint));

        if (thing != null) {
            /*
             * For each channel a specific converter can be registered that
             * generates a State from string value
             */
            StateConverter sConvert = thing.m_StateConverter;
            State sState = sConvert.convert(value);

            if (sState != null) {
                thing.m_BaseHandler.notifyUpdate(thing.m_Channel, sState);
            }
        }
    }
}
