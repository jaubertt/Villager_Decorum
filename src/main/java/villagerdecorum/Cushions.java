package villagerdecorum;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class Cushions {
	public static final double SIT_DEPTH = 0.6;
	public static final double SIT_BACK = 0.15;
	public static final double WORK_RANGE = 1.73;
	private static final double BACKREST_HEIGHT = 0.35;
	private static final int ROOM_AHEAD = 3;

	private Cushions() {
	}

	public static boolean seated(Villager villager) {
		return villager.getVehicle() instanceof Cushion && villager.getPose() == Pose.SITTING;
	}

	public static Vec3 seatOffset(float yRot, float scale) {
		return new Vec3(0.0, SIT_DEPTH, SIT_BACK).yRot(-yRot * Mth.DEG_TO_RAD).scale(scale);
	}

	public static Vec3 seat(Cushion cushion, float yRot, float scale) {
		return new Vec3(cushion.getX(), cushion.getY() + cushion.getBbHeight(), cushion.getZ()).subtract(seatOffset(yRot, scale));
	}

	public static float yawToward(Cushion cushion, BlockPos place) {
		double dx = place.getX() + 0.5 - cushion.getX();
		double dz = place.getZ() + 0.5 - cushion.getZ();
		return (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
	}

	public static @Nullable Direction stairFront(Level level, Cushion cushion) {
		BlockState state = level.getBlockState(cushion.blockPosition());
		if (!state.is(BlockTags.STAIRS) || state.getOptionalValue(BlockStateProperties.HALF).orElse(Half.TOP) != Half.BOTTOM) {
			return null;
		}
		return state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).map(Direction::getOpposite).orElse(null);
	}

	public static float openYaw(Level level, Cushion cushion) {
		BlockPos from = BlockPos.containing(cushion.getX(), cushion.getY() + cushion.getBbHeight() + 0.5, cushion.getZ());
		Direction best = Direction.fromYRot(cushion.getYRot()).getOpposite();
		int most = room(level, from, best);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			int room = room(level, from, side);
			if (room > most) {
				best = side;
				most = room;
			}
		}
		return best.toYRot();
	}

	private static int room(Level level, BlockPos from, Direction side) {
		BlockPos.MutableBlockPos pos = from.mutable();
		for (int i = 0; i < ROOM_AHEAD; i++) {
			pos.move(side);
			if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
				return i;
			}
		}
		return ROOM_AHEAD;
	}

	public static boolean roomToSit(Level level, Villager villager, Cushion cushion, float yRot) {
		Vec3 seat = seat(cushion, yRot, villager.getScale());
		double half = villager.getBbWidth() / 2.0;
		double bottom = cushion.getY() + cushion.getBbHeight() + BACKREST_HEIGHT;
		AABB body = new AABB(seat.x - half, bottom, seat.z - half, seat.x + half, seat.y + villager.getBbHeight(), seat.z + half);
		return level.noBlockCollision(villager, body);
	}

	public static void checkSeat(Villager villager) {
		boolean onCushion = villager.getVehicle() instanceof Cushion;
		boolean sitting = villager.getPose() == Pose.SITTING;
		if (onCushion && !sitting) {
			villager.stopRiding();
		} else if (sitting && !onCushion) {
			villager.setPose(Pose.STANDING);
		}
	}

	public static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> withSitting(
		ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> behaviors, Spot spot) {
		return ImmutableList.<Pair<Integer, ? extends BehaviorControl<? super Villager>>>builder()
			.addAll(behaviors)
			.add(Pair.of(0, new SitOnCushion(spot)))
			.build();
	}
}
