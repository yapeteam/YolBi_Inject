package cn.yapeteam.yolbi.module.impl.misc;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.ModeValue;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.network.EventPacketSend;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.network.PacketUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C17PacketCustomPayload;

@ModuleInfo(name = "ClientSpoof", category = ModuleCategory.MISC)
public class ClientSpoof extends Module {
    private final ModeValue<String> mode = new ModeValue<String>("Spoof Client", "Vanilla",
            "Lunar",
            "Vanilla",
            "OptiFine",
            "Fabric",
            "LabyMod",
            "Feather",
            "CheatBreaker"
    );

    public ClientSpoof() {
        addValues(mode);
    }

    @Listener
    private void onPacketSend(EventPacketSend e) {
        if (e.getPacket() instanceof C17PacketCustomPayload) {
            e.setCancelled(true);

            if (mode.is("LabyMod")) {
                Packet<INetHandlerPlayServer> packet1 = new C17PacketCustomPayload("labymod3:main", getInfo());
                Packet<INetHandlerPlayServer> packet2 = new C17PacketCustomPayload("LMC", getInfo());
                PacketUtil.skip(packet1);
                PacketUtil.skip(packet2);
                mc.getNetHandler().addToSendQueue(packet1);
                mc.getNetHandler().addToSendQueue(packet2);
                return;
            }

            PacketBuffer buffer;
            switch (mode.getValue()) {
                case "Lunar":
                    buffer = generatePacketBuffer("lunarclient:v2.14.5-2411");
                    break;
                case "OptiFine":
                    buffer = generatePacketBuffer("optifine");
                    break;
                case "Fabric":
                    buffer = generatePacketBuffer("fabric");
                    break;
                case "Feather":
                    buffer = generatePacketBuffer("Feather Forge");
                    break;
                case "CheatBreaker":
                    buffer = generatePacketBuffer("CB");
                    break;
                case "Vanilla":
                default:
                    buffer = generatePacketBuffer("vanilla");
                    break;
            }
            Packet<INetHandlerPlayServer> packet = new C17PacketCustomPayload("MC|Brand", buffer);
            PacketUtil.skip(packet);
            mc.getNetHandler().addToSendQueue(packet);
        }
    }

    private PacketBuffer getInfo() {
        return new PacketBuffer(Unpooled.buffer())
                .writeString("INFO")
                .writeString(
                        "{  \n" +
                                "   \"version\": \"3.9.25\",\n" +
                                "   \"ccp\": {  \n" +
                                "      \"enabled\": true,\n" +
                                "      \"version\": 2\n" +
                                "   },\n" +
                                "   \"shadow\":{  \n" +
                                "      \"enabled\": true,\n" +
                                "      \"version\": 1\n" +
                                "   },\n" +
                                "   \"addons\": [  \n" +
                                "      {  \n" +
                                "         \"uuid\": \"null\",\n" +
                                "         \"name\": \"null\"\n" +
                                "      }\n" +
                                "   ],\n" +
                                "   \"mods\": [\n" +
                                "      {  \n" +
                                "         \"hash\":\"sha256:null\",\n" +
                                "         \"name\":\"null.jar\"\n" +
                                "      }\n" +
                                "   ]\n" +
                                "}"
                );
    }

    private PacketBuffer generatePacketBuffer(String string) {
        return new PacketBuffer(Unpooled.buffer()).writeString(string);
    }
}
