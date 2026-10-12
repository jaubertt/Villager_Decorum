package villagerdecorum.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mob.class)
public abstract class MobMixin {
	@ModifyReturnValue(method = "convertTo(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/entity/ConversionParams;Lnet/minecraft/world/entity/EntitySpawnReason;Lnet/minecraft/world/entity/ConversionParams$AfterConversion;)Lnet/minecraft/world/entity/Mob;",
		at = @At("RETURN"))
	private Mob villagerdecorum$standUpConverted(Mob converted) {
		if ((Object) this instanceof Villager && converted != null && !(converted instanceof Villager) && converted.getVehicle() instanceof Cushion) {
			converted.stopRiding();
		}
		return converted;
	}
}
