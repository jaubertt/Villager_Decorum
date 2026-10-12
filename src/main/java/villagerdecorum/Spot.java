package villagerdecorum;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public enum Spot {
	WORKSTATION(Activity.WORK, MemoryModuleType.JOB_SITE, 1),
	BED(Activity.IDLE, MemoryModuleType.HOME, 5),
	BELL(Activity.MEET, MemoryModuleType.MEETING_POINT, 10);

	private final Activity activity;
	private final MemoryModuleType<GlobalPos> place;
	private final int reach;

	Spot(Activity activity, MemoryModuleType<GlobalPos> place, int reach) {
		this.activity = activity;
		this.place = place;
		this.reach = reach;
	}

	public Activity activity() {
		return this.activity;
	}

	public MemoryModuleType<GlobalPos> place() {
		return this.place;
	}

	public boolean enabled() {
		DecorumConfig config = DecorumConfig.get();
		return switch (this) {
			case WORKSTATION -> config.workstations;
			case BED -> config.beds;
			case BELL -> config.bells;
		};
	}

	public @Nullable BlockPos placeOf(ServerLevel level, Villager villager) {
		return villager.getBrain().getMemory(this.place)
			.filter(pos -> pos.dimension() == level.dimension())
			.map(GlobalPos::pos)
			.orElse(null);
	}

	public AABB searchArea(BlockPos place) {
		return new AABB(place).inflate(this.reach);
	}

	public boolean fits(ServerLevel level, Villager villager, BlockPos place, Cushion cushion) {
		BlockPos at = cushion.blockPosition();
		int dx = at.getX() - place.getX();
		int dy = at.getY() - place.getY();
		int dz = at.getZ() - place.getZ();
		if (this != WORKSTATION) {
			return Math.abs(dx) <= this.reach && Math.abs(dy) <= this.reach && Math.abs(dz) <= this.reach;
		}
		if (dy != 0 || Math.abs(dx) + Math.abs(dz) != 1) {
			return false;
		}
		Direction back = back(level.getBlockState(place));
		if (back != null && dx == back.getStepX() && dz == back.getStepZ()) {
			return false;
		}
		float yaw = Cushions.yawToward(cushion, place);
		return place.closerToCenterThan(Cushions.seat(cushion, yaw, villager.getScale()), Cushions.WORK_RANGE);
	}

	public float facing(ServerLevel level, BlockPos place, Cushion cushion) {
		if (this == WORKSTATION) {
			return Cushions.yawToward(cushion, place);
		}
		Direction stairFront = Cushions.stairFront(level, cushion);
		if (stairFront != null) {
			return stairFront.toYRot();
		}
		return this == BED ? Cushions.openYaw(level, cushion) : Cushions.yawToward(cushion, place);
	}

	public boolean looksAtPlace() {
		return this != BED;
	}

	private static @Nullable Direction back(BlockState state) {
		if (state.getOptionalValue(BlockStateProperties.ATTACH_FACE).filter(face -> face != AttachFace.WALL).isPresent()) {
			return null;
		}
		Optional<Direction> facing = state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING);
		if (facing.isEmpty()) {
			facing = state.getOptionalValue(BlockStateProperties.FACING).filter(side -> side.getAxis().isHorizontal());
		}
		return facing.map(Direction::getOpposite).orElse(null);
	}
}
