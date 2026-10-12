package villagerdecorum.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villagerdecorum.Cushions;

@Mixin(WalkNodeEvaluator.class)
public abstract class WalkNodeEvaluatorMixin extends NodeEvaluator {
	@Unique
	private static final double SEATED_SEARCH = 32.0;

	@Unique
	private final LongSet villagerdecorum$seated = new LongOpenHashSet();

	@Inject(method = "prepare(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;)V", at = @At("TAIL"))
	private void villagerdecorum$findSeated(PathNavigationRegion region, Mob mob, CallbackInfo ci) {
		this.villagerdecorum$seated.clear();
		if (!(mob instanceof Villager)) {
			return;
		}
		for (Villager other : mob.level().getEntitiesOfClass(Villager.class, mob.getBoundingBox().inflate(SEATED_SEARCH), seated -> seated != mob && Cushions.seated(seated))) {
			Entity cushion = other.getVehicle();
			if (cushion == null) {
				continue;
			}
			BlockPos at = cushion.blockPosition();
			this.villagerdecorum$seated.add(at.asLong());
			this.villagerdecorum$seated.add(BlockPos.asLong(other.getBlockX(), at.getY(), other.getBlockZ()));
		}
	}

	@Inject(method = "done()V", at = @At("TAIL"))
	private void villagerdecorum$forgetSeated(CallbackInfo ci) {
		this.villagerdecorum$seated.clear();
	}

	@ModifyReturnValue(method = "getPathTypeOfMob(Lnet/minecraft/world/level/pathfinder/PathfindingContext;IIILnet/minecraft/world/entity/Mob;)Lnet/minecraft/world/level/pathfinder/PathType;", at = @At("RETURN"))
	private PathType villagerdecorum$avoidSeated(PathType type, PathfindingContext context, int x, int y, int z, Mob mob) {
		if (type == PathType.WALKABLE && !this.villagerdecorum$seated.isEmpty() && this.villagerdecorum$seated.contains(BlockPos.asLong(x, y, z))) {
			return PathType.DAMAGING_IN_NEIGHBOR;
		}
		return type;
	}
}
