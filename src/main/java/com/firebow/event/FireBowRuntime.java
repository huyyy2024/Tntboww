package com.firebow.event;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Runtime singleton — quản lý queue placement, chạy client-side.
 *
 * Pattern tick:
 *   Tick T   : arrow hit → tính railPos → enqueue
 *   Tick T+1 : phase 1 → đặt Rail, lưu savedSlot, restore slot
 *   Tick T+2 : phase 2 → đặt TNT Cart, lưu savedSlot, restore slot
 *   Tick T+3 : phase 3 → restore slot về prevSlot (grimSync safe)
 *
 * FIX: slot restore dời sang tick tiếp theo (phase 3) thay vì
 * inline sau interactBlock() — tránh server nhận packet sai slot.
 */
public final class FireBowRuntime {

    private static final FireBowRuntime INSTANCE = new FireBowRuntime();
    public static FireBowRuntime get() { return INSTANCE; }

    private final Queue<PendingPlacement> queue = new ConcurrentLinkedQueue<>();
    private static final int MAX_QUEUE = 3;

    private PendingPlacement current   = null;
    private int              phase     = 0;
    // slot cần restore sau mỗi interact — -1 nếu không cần
    private int              savedSlot = -1;

    private FireBowRuntime() {}

    public void enqueue(BlockPos railPos) {
        if (queue.size() < MAX_QUEUE) {
            queue.add(new PendingPlacement(railPos));
        }
    }

    /**
     * Gọi từ ClientTickMixin mỗi client tick.
     *
     * phase 0 → idle, lấy pending tiếp theo
     * phase 1 → đặt Rail
     * phase 2 → đặt TNT Cart
     * phase 3 → restore slot (tick delay để server không flag)
     */
    public void onClientTick(MinecraftClient client) {
        if (client.player == null || client.world == null) return;

        switch (phase) {

            case 1: {
                savedSlot = placeRail(client, current.railPos);
                phase = 2;
                break;
            }

            case 2: {
                placeCart(client, current.railPos);
                phase = 3;
                break;
            }

            case 3: {
                // Restore slot ở tick này — sau khi server đã nhận cả 2 packet
                if (savedSlot != -1 && client.player != null) {
                    client.player.getInventory().selectedSlot = savedSlot;
                    savedSlot = -1;
                }
                current = null;
                phase   = 0;
                break;
            }

            default: {
                // phase 0: idle — lấy pending tiếp theo nếu có
                if (!queue.isEmpty()) {
                    current = queue.poll();
                    phase   = 1;
                }
                break;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Đặt Rail — trả về prevSlot để restore sau
    // ─────────────────────────────────────────────────────────────
    private int placeRail(MinecraftClient client, BlockPos railPos) {
        ClientPlayerEntity           player = client.player;
        ClientPlayerInteractionManager im   = client.interactionManager;
        if (im == null) return -1;

        int railSlot = findInHotbar(player, Items.RAIL);
        if (railSlot == -1) railSlot = findInHotbar(player, Items.POWERED_RAIL);
        if (railSlot == -1) return -1;

        int prevSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = railSlot;

        // Click phải vào mặt trên của block ngay dưới railPos
        BlockHitResult hit = new BlockHitResult(
            Vec3d.ofCenter(railPos),
            Direction.UP,
            railPos.down(),
            false
        );
        im.interactBlock(player, Hand.MAIN_HAND, hit);

        // KHÔNG restore ngay — trả về prevSlot để phase 3 xử lý
        return prevSlot;
    }

    // ─────────────────────────────────────────────────────────────
    //  Đặt TNT Cart — click phải vào rail vừa đặt
    // ─────────────────────────────────────────────────────────────
    private void placeCart(MinecraftClient client, BlockPos railPos) {
        ClientPlayerEntity           player = client.player;
        ClientPlayerInteractionManager im   = client.interactionManager;
        if (im == null) return;

        // Kiểm tra rail đã có trên client world chưa
        boolean hasRail = client.world.getBlockState(railPos).isOf(Blocks.RAIL)
                       || client.world.getBlockState(railPos).isOf(Blocks.POWERED_RAIL);
        if (!hasRail) return;

        int cartSlot = findInHotbar(player, Items.TNT_MINECART);
        if (cartSlot == -1) return;

        // Dùng savedSlot đang giữ từ placeRail để biết slot nào sẽ restore
        // (savedSlot sẽ bị ghi đè ở đây nếu cần — nhưng chỉ 1 placement tại 1 thời điểm)
        player.getInventory().selectedSlot = cartSlot;

        BlockHitResult hit = new BlockHitResult(
            Vec3d.ofCenter(railPos),
            Direction.UP,
            railPos,
            false
        );
        im.interactBlock(player, Hand.MAIN_HAND, hit);

        // KHÔNG restore ngay — phase 3 sẽ restore
    }

    // ─────────────────────────────────────────────────────────────
    //  Tìm item trong hotbar (slot 0–8)
    // ─────────────────────────────────────────────────────────────
    private int findInHotbar(ClientPlayerEntity player, net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    // ─────────────────────────────────────────────────────────────
    //  Data class
    // ─────────────────────────────────────────────────────────────
    private static class PendingPlacement {
        final BlockPos railPos;
        PendingPlacement(BlockPos p) { this.railPos = p; }
    }
}
