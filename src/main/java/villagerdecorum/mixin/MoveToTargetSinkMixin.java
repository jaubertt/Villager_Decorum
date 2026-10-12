package villagerdecorum.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import villagerdecorum.Cushions;

@Mixin(MoveToTargetSink.class)
public abstract class MoveToTargetSinkMixin {
	@Inject(method = "checkExtraStartConditions(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Mob;)Z",
		at = @At("HEAD"), cancellable = true)
	private void villagerdecorum$staySeated(ServerLevel level, Mob body, CallbackInfoReturnable<Boolean> cir) {
		if (body instanceof Villager villager && Cushions.seated(villager)) {
			cir.setReturnValue(false);
		}
	}
}
