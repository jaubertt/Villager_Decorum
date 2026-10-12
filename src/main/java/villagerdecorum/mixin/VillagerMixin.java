package villagerdecorum.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villagerdecorum.Cushions;

@Mixin(Villager.class)
public abstract class VillagerMixin {
	@Inject(method = "customServerAiStep", at = @At("TAIL"))
	private void villagerdecorum$standUpUnlessSitting(ServerLevel level, CallbackInfo ci) {
		Cushions.checkSeat((Villager) (Object) this);
	}
}
