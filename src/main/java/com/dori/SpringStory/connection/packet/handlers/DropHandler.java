package com.dori.SpringStory.connection.packet.handlers;

import com.dori.SpringStory.client.MapleClient;
import com.dori.SpringStory.client.character.MapleChar;
import com.dori.SpringStory.connection.packet.Handler;
import com.dori.SpringStory.connection.packet.InPacket;
import com.dori.SpringStory.connection.packet.packets.CWvsContext;
import com.dori.SpringStory.enums.PickupMessageType;
import com.dori.SpringStory.inventory.Item;
import com.dori.SpringStory.logger.Logger;
import com.dori.SpringStory.world.fieldEntities.Drop;

import static com.dori.SpringStory.connection.packet.headers.InHeader.DropPickUpRequest;

public class DropHandler {
    // Logger -
    private static final Logger logger = new Logger(DropHandler.class);

    @Handler(op = DropPickUpRequest)
    public static void handleDropPickUpRequest(MapleClient c, InPacket inPacket) {
        MapleChar chr = c.getChr();
        inPacket.decodeByte(); // fieldKey
        inPacket.decodeInt(); // tick
        inPacket.decodePosition(); // drop position
        int dropID = inPacket.decodeInt();
        inPacket.decodeInt(); // CliCrc

        Drop drop = chr.getField().getDrops().get(dropID);
        if (drop != null) {
            if (drop.getItem() != null) {
                Item droppedItem = drop.getItem();
                boolean canPickup = switch (droppedItem.getType()) {
                    case EQUIP -> !chr.haveEquip(droppedItem.getItemId());
                    case BUNDLE -> !chr.haveItem(droppedItem.getItemId());
                    case null, default -> false;
                };
                if (canPickup) {
                    chr.pickupItem(drop);
                } else {
                    chr.write(CWvsContext.dropPickupMessage(0, PickupMessageType.ITEM_UNAVAILABLE_TO_PICKUP, (short) 0, 0));
                    chr.enableAction();
                }
            }

        }
    }
}
