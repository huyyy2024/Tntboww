package com.firebow.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Tính toán vị trí đặt rail — dùng client world để check block state.
 * Giống BOOST-FPS dùng client world để predict target position.
 */
public final class PlacementCalc {

    private PlacementCalc() {}

    public static BlockPos fromBlockHit(World world, BlockPos hitPos, Direction side) {
        if (side == Direction.UP) {
            BlockPos railPos = hitPos.up();
            if (!world.getBlockState(railPos).isAir()) return null;
            if (!isSolidGround(world, hitPos)) return null;
            return railPos;
        }
        return findSurface(world, hitPos.offset(side));
    }

    public static BlockPos fromEntityHit(World world, Entity target, Vec3d velocity) {
        Vec3d dir  = velocity.normalize();
        BlockPos base = target.getBlockPos().add(
            (int) Math.round(dir.x),
            0,
            (int) Math.round(dir.z)
        );
        return findSurface(world, base);
    }

    /** Tìm vị trí AIR ngay trên block SOLID đầu tiên, xuống tối đa 8 block. */
    public static BlockPos findSurface(World world, BlockPos start) {
        for (int dy = 0; dy <= 8; dy++) {
            BlockPos candidate = start.down(dy);
            BlockPos below     = candidate.down();
            if (!world.getBlockState(candidate).isAir()) return null;
            if (isSolidGround(world, below)) return candidate;
        }
        return null;
    }

    /** Nền hợp lệ: solid, không phải air / water / lava. */
    public static boolean isSolidGround(World world, BlockPos pos) {
        BlockState s = world.getBlockState(pos);
        if (s.isAir())            return false;
        if (s.isOf(Blocks.WATER)) return false;
        if (s.isOf(Blocks.LAVA))  return false;
        return s.isSolidBlock(world, pos);
    }
}
