package villagerdecorum.client.mixin;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.npc.VillagerModel;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import villagerdecorum.client.SittingPose;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin {
	@Shadow
	@Final
	private ModelPart rightLeg;
	@Shadow
	@Final
	private ModelPart leftLeg;

	@Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;)V", at = @At("TAIL"))
	private void villagerdecorum$sitting(VillagerRenderState state, CallbackInfo ci) {
		if (state.hasPose(Pose.SITTING)) {
			SittingPose.legs(this.rightLeg, this.leftLeg);
		}
	}
}
