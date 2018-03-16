package org.genguava.fruitylib.integration;

import mcp.mobius.waila.api.IWailaRegistrar;

public class WailaRegistration {
    public static void registerCallbacks(IWailaRegistrar registrar) {
        WailaIntegration.Companion.callbackRegister(registrar);
    }
}
