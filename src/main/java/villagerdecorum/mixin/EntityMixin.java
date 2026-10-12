package villagerdecorum.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.Cushion;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import villagerdecorum.Cushions;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@ModifyReturnValue(method = "getVehicleAttachmentPoint", at = @At("RETURN"))
	private Vec3 villagerdecorum$sitDown(Vec3 point, @Local(argsOnly = true) Entity vehicle) {
		return (Object) this instanceof Villager villager && vehicle instanceof Cushion ? Cushions.seatOffset(villager.getYRot(), villager.getScale()) : point;
	}
}
