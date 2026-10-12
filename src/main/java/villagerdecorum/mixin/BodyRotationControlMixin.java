package villagerdecorum.mixin;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BodyRotationControl.class)
public abstract class BodyRotationControlMixin {
	@Shadow
	@Final
	private Mob mob;

	@Inject(method = "clientTick", at = @At("HEAD"), cancellable = true)
	private void villagerdecorum$faceForwardSeated(CallbackInfo ci) {
		if (this.mob instanceof Villager && this.mob.getVehicle() instanceof Cushion) {
			this.mob.yBodyRot = this.mob.getYRot();
			this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, this.mob.getMaxHeadYRot());
			ci.cancel();
		}
	}
}
