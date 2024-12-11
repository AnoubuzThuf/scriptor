package com.ssblur.scriptor.events.network;

import com.ssblur.scriptor.ScriptorMod;
import com.ssblur.scriptor.events.network.client.*;
import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ScriptorNetwork {
  public static final ResourceLocation CLIENT_GET_TRACE_DATA = ScriptorMod.INSTANCE.location("client_get_touch_data");
  public static final ResourceLocation CLIENT_GET_HITSCAN_DATA = ScriptorMod.INSTANCE.location("client_get_hitscan_data");
  public static final ResourceLocation CLIENT_CURSOR_RETURN_BOOK_CREATIVE = ScriptorMod.INSTANCE.location("client_cursor_return_bookc");
  public static final ResourceLocation CLIENT_CURSOR_RETURN_SCROLL_CREATIVE = ScriptorMod.INSTANCE.location("client_cursor_return_scrollc");
  public static final ResourceLocation CLIENT_COLOR_RECEIVE = ScriptorMod.INSTANCE.location("client_color_receivec");
  public static final ResourceLocation CLIENT_PARTICLE = ScriptorMod.INSTANCE.location("client_particle");
  public static final ResourceLocation CLIENT_FLAG = ScriptorMod.INSTANCE.location("client_flag");

  public static void register() {
    register(new ClientTraceNetwork());
    register(new ClientExtendedTraceNetwork());
    register(new ClientCreativeBookNetwork());
    register(new ClientIdentifyNetwork());
    register(new ReceiveColorNetwork());
    register(new ParticleNetwork());
    register(new ReceiveConfigNetwork());
  }

  public static <T extends CustomPacketPayload> void register(ScriptorNetworkInterface<T> networkInterface) {
    if(networkInterface.side() == NetworkManager.Side.C2S || Platform.getEnv() == EnvType.CLIENT)
      NetworkManager.registerReceiver(networkInterface.side(), networkInterface.type(), networkInterface.streamCodec(), networkInterface);

    if(networkInterface.side() == NetworkManager.Side.S2C && Platform.getEnv() == EnvType.SERVER)
      NetworkManager.registerS2CPayloadType(networkInterface.type(), networkInterface.streamCodec());
  }

}
