package villagerdecorum;

import com.google.common.collect.ImmutableMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class SitOnCushion extends Behavior<Villager> {
	private static final int WALK_TICKS = 400;
	private static final int MIN_SIT_TICKS = 600;
	private static final int MAX_SIT_TICKS = 1200;
	private static final int MIN_BREAK_TICKS = 400;
	private static final int MAX_BREAK_TICKS = 800;
	private static final int MIN_SEARCH_TICKS = 100;
	private static final int MAX_SEARCH_TICKS = 200;
	private static final int PATHS_TRIED = 3;
	private static final int WALKS_RESUMED = 2;
	private static final double MOUNT_REACH = 1.8;
	private static final double MOUNT_HEIGHT = 1.25;
	private static final double SIGHT_ABOVE = 0.6;
	private static final double BARRIER_FROM = 1.1;
	private static final double BARRIER_TO = 1.5;
	private static final double PROBE_STEP = 0.25;
	private static final double PROBE_HALF_WIDTH = 0.1;
	private static final float WALK_SPEED = 0.5F;

	private final Spot spot;
	private final Map<UUID, Long> skipped = new HashMap<>();
	private @Nullable Cushion cushion;
	private @Nullable BlockPos place;
	private @Nullable BlockPos walkTo;
	private @Nullable Vec3 standUpAt;
	private boolean seated;
	private boolean finished;
	private boolean gaveUp;
	private int walksResumed;
	private float yaw;
	private long walkUntil;
	private long sitUntil;
	private long nextSearch;

	public SitOnCushion(Spot spot) {
		super(ImmutableMap.of(
			MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
			MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED,
			MemoryModuleType.BREED_TARGET, MemoryStatus.VALUE_ABSENT,
			MemoryModuleType.POTENTIAL_JOB_SITE, MemoryStatus.VALUE_ABSENT,
			spot.place(), MemoryStatus.VALUE_PRESENT
		));
		this.spot = spot;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, Villager villager) {
		if (villager.isBaby() || !this.spot.enabled()) {
			return false;
		}
		BlockPos place = this.spot.placeOf(level, villager);
		if (place == null) {
			return false;
		}
		if (villager.getVehicle() instanceof Cushion current) {
			if (villager.getPose() == Pose.SITTING || !this.spot.fits(level, villager, place, current)) {
				return false;
			}
			this.choose(current, place, null);
			return true;
		}
		long time = level.getGameTime();
		if (this.nextSearch == 0L) {
			this.nextSearch = time + between(level, 1, MAX_SEARCH_TICKS);
		}
		if (time < this.nextSearch || villager.isPassenger() || villager.isSleeping() || villager.isTrading()
			|| villager.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
			return false;
		}
		this.nextSearch = time + between(level, MIN_SEARCH_TICKS, MAX_SEARCH_TICKS);
		this.skipped.values().removeIf(until -> time >= until);
		List<Cushion> nearest = level.getEntitiesOfClass(Cushion.class, this.spot.searchArea(place),
				cushion -> !cushion.isVehicle() && !this.skipped.containsKey(cushion.getUUID())
					&& this.spot.fits(level, villager, place, cushion)
					&& Cushions.roomToSit(level, villager, cushion, this.spot.facing(level, place, cushion)))
			.stream()
			.sorted(Comparator.comparingDouble(cushion -> villager.distanceToSqr(cushion)))
			.limit(PATHS_TRIED)
			.toList();
		for (Cushion cushion : nearest) {
			BlockPos walkTo = BlockPos.containing(cushion.getX(), cushion.getY() + 0.5, cushion.getZ());
			Path path = villager.getNavigation().createPath(walkTo, 0);
			if (path != null && path.canReach()) {
				this.choose(cushion, place, walkTo);
				return true;
			}
		}
		return false;
	}

	private void choose(Cushion cushion, BlockPos place, @Nullable BlockPos walkTo) {
		this.cushion = cushion;
		this.place = place;
		this.walkTo = walkTo;
	}

	@Override
	protected void start(ServerLevel level, Villager villager, long time) {
		this.seated = false;
		this.finished = false;
		this.gaveUp = false;
		this.walksResumed = 0;
		this.standUpAt = null;
		if (this.walkTo == null) {
			this.settle(level, villager, time);
			return;
		}
		this.walkUntil = time + WALK_TICKS;
		villager.getBrain().setMemory(MemoryModuleType.WALK_TARGET, this.walk());
	}

	@Override
	protected boolean canStillUse(ServerLevel level, Villager villager, long time) {
		Cushion cushion = this.cushion;
		if (this.finished || cushion == null || cushion.isRemoved() || !this.spot.enabled() || villager.isSleeping()) {
			return false;
		}
		Brain<Villager> brain = villager.getBrain();
		if (!brain.isActive(this.spot.activity()) || brain.hasMemoryValue(MemoryModuleType.BREED_TARGET)
			|| brain.hasMemoryValue(MemoryModuleType.POTENTIAL_JOB_SITE) || !Objects.equals(this.spot.placeOf(level, villager), this.place)) {
			return false;
		}
		if (this.seated) {
			return villager.getVehicle() == cushion && (time < this.sitUntil || villager.isTrading());
		}
		return time < this.walkUntil && !villager.isPassenger();
	}

	@Override
	protected boolean timedOut(long time) {
		return false;
	}

	@Override
	protected void tick(ServerLevel level, Villager villager, long time) {
		Cushion cushion = this.cushion;
		if (cushion == null) {
			this.finished = true;
			return;
		}
		if (this.seated) {
			this.stay(villager);
			return;
		}
		if (cushion.isVehicle()) {
			this.finished = true;
			return;
		}
		Brain<Villager> brain = villager.getBrain();
		Optional<WalkTarget> walk = brain.getMemory(MemoryModuleType.WALK_TARGET);
		if (walk.filter(target -> !(target.getTarget() instanceof BlockPosTracker)).isPresent()) {
			this.finished = true;
			return;
		}
		boolean walking = walk.isPresent() && this.isOurs(walk.get());
		if ((!walking || villager.getNavigation().isDone()) && canMount(level, villager, cushion)) {
			this.mount(level, villager, cushion, time);
			return;
		}
		if (walking) {
			return;
		}
		if (brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE) || this.walksResumed >= WALKS_RESUMED) {
			this.gaveUp = true;
			this.finished = true;
			return;
		}
		this.walksResumed++;
		brain.setMemory(MemoryModuleType.WALK_TARGET, this.walk());
	}

	private static boolean canMount(ServerLevel level, Villager villager, Cushion cushion) {
		double dx = cushion.getX() - villager.getX();
		double dz = cushion.getZ() - villager.getZ();
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance > MOUNT_REACH || Math.abs(cushion.getY() - villager.getY()) > MOUNT_HEIGHT) {
			return false;
		}
		double sight = cushion.getY() + cushion.getBbHeight() + SIGHT_ABOVE;
		if (!villager.hasLineOfSight(cushion, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sight) && !villager.hasLineOfSight(cushion)) {
			return false;
		}
		double bottom = Math.max(Math.min(villager.getY(), cushion.getY()) + BARRIER_FROM, villager.getY() + villager.maxUpStep());
		double top = villager.getY() + BARRIER_TO;
		int steps = Math.max(1, Mth.ceil(distance / PROBE_STEP));
		for (int i = 0; i <= steps; i++) {
			double x = villager.getX() + dx * i / steps;
			double z = villager.getZ() + dz * i / steps;
			AABB probe = new AABB(x - PROBE_HALF_WIDTH, bottom, z - PROBE_HALF_WIDTH, x + PROBE_HALF_WIDTH, top, z + PROBE_HALF_WIDTH);
			if (!level.noBlockCollision(villager, probe)) {
				return false;
			}
		}
		return true;
	}

	private void mount(ServerLevel level, Villager villager, Cushion cushion, long time) {
		Vec3 from = villager.position();
		if (villager.startRiding(cushion)) {
			this.standUpAt = from;
			cushion.playSound(SoundEvents.CUSHION_SIT, 1.0F, 1.0F);
			this.settle(level, villager, time);
		}
	}

	private void settle(ServerLevel level, Villager villager, long time) {
		Cushion cushion = this.cushion;
		BlockPos place = this.place;
		if (cushion == null || place == null) {
			this.finished = true;
			return;
		}
		this.seated = true;
		this.sitUntil = time + between(level, MIN_SIT_TICKS, MAX_SIT_TICKS);
		this.yaw = this.spot.facing(level, place, cushion);
		villager.setPose(Pose.SITTING);
		villager.setYRot(this.yaw);
		villager.setYBodyRot(this.yaw);
		villager.setYHeadRot(this.yaw);
		villager.getNavigation().stop();
		villager.getMoveControl().setWantedPosition(villager.getX(), villager.getY(), villager.getZ(), 0.0);
		villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
		this.lookAtPlace(villager);
	}

	private void stay(Villager villager) {
		if (villager.getPose() != Pose.SITTING) {
			villager.setPose(Pose.SITTING);
		}
		if (villager.getYRot() != this.yaw) {
			villager.setYRot(this.yaw);
		}
		if (!villager.isTrading()) {
			this.lookAtPlace(villager);
		}
	}

	private void lookAtPlace(Villager villager) {
		Brain<Villager> brain = villager.getBrain();
		if (this.spot.looksAtPlace() && this.place != null && !brain.hasMemoryValue(MemoryModuleType.LOOK_TARGET)) {
			brain.setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(this.place));
		}
	}

	@Override
	protected void stop(ServerLevel level, Villager villager, long time) {
		Brain<Villager> brain = villager.getBrain();
		if (this.seated) {
			this.nextSearch = time + between(level, MIN_BREAK_TICKS, MAX_BREAK_TICKS);
		} else {
			if (this.cushion != null && (this.gaveUp || time >= this.walkUntil)) {
				this.skipped.put(this.cushion.getUUID(), time + between(level, MIN_BREAK_TICKS, MAX_BREAK_TICKS));
			}
			if (brain.getMemory(MemoryModuleType.WALK_TARGET).filter(this::isOurs).isPresent()) {
				brain.eraseMemory(MemoryModuleType.WALK_TARGET);
			}
		}
		if (villager.getPose() == Pose.SITTING) {
			villager.setPose(Pose.STANDING);
		}
		if (this.cushion != null && villager.getVehicle() == this.cushion) {
			villager.stopRiding();
			Vec3 to = this.standUpAt;
			if (to != null && !villager.isPassenger() && DismountHelper.canDismountTo(level, to, villager, Pose.STANDING)) {
				villager.dismountTo(to.x, to.y, to.z);
			}
		}
		this.cushion = null;
		this.place = null;
		this.walkTo = null;
		this.standUpAt = null;
		this.seated = false;
		this.finished = false;
		this.gaveUp = false;
	}

	private WalkTarget walk() {
		return new WalkTarget(this.walkTo, WALK_SPEED, 0);
	}

	private boolean isOurs(WalkTarget walk) {
		return walk.getCloseEnoughDist() == 0 && walk.getTarget() instanceof BlockPosTracker
			&& walk.getTarget().currentBlockPosition().equals(this.walkTo);
	}

	private static int between(ServerLevel level, int min, int max) {
		return min + level.getRandom().nextInt(max - min + 1);
	}
}
