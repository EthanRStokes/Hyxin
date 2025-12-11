package com.build_9.hyxin.mixin;

import com.build_9.hyxin.Constants;
import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class Bootstrap implements IMixinServiceBootstrap {

    @Override
    public String getName() {
        return Constants.NAME;
    }

    @Override
    public String getServiceClassName() {
        return "com.build_9.hyxin.mixin.MixinService";
    }

    @Override
    public void bootstrap() {
        // No-op
    }
}