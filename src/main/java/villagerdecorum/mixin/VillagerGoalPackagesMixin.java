package villagerdecorum.mixin;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.VillagerGoalPackages;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import villagerdecorum.Cushions;
import villagerdecorum.Spot;

@Mixin(VillagerGoalPackages.class)
public abstract class VillagerGoalPackagesMixin {
	@ModifyReturnValue(method = "getWorkPackage", at = @At("RETURN"))
	private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerdecorum$sitAtWorkstation(
		ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> behaviors) {
		return Cushions.withSitting(behaviors, Spot.WORKSTATION);
	}

	@ModifyReturnValue(method = "getIdlePackage", at = @At("RETURN"))
	private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerdecorum$sitByBed(
		ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> behaviors) {
		return Cushions.withSitting(behaviors, Spot.BED);
	}

	@ModifyReturnValue(method = "getMeetPackage", at = @At("RETURN"))
	private static ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> villagerdecorum$sitByBell(
		ImmutableList<Pair<Integer, ? extends BehaviorControl<? super Villager>>> behaviors) {
		return Cushions.withSitting(behaviors, Spot.BELL);
	}
}
