package villagerdecorum.client;

import net.minecraft.client.model.geom.ModelPart;

public final class SittingPose {
	private static final float LEG_PITCH = -1.4137167F;
	private static final float LEG_SPREAD = (float) (Math.PI / 10.0);
	private static final float LEG_ROLL = 0.07853982F;

	private SittingPose() {
	}

	public static void legs(ModelPart rightLeg, ModelPart leftLeg) {
		rightLeg.xRot = LEG_PITCH;
		rightLeg.yRot = LEG_SPREAD;
		rightLeg.zRot = LEG_ROLL;
		leftLeg.xRot = LEG_PITCH;
		leftLeg.yRot = -LEG_SPREAD;
		leftLeg.zRot = -LEG_ROLL;
	}
}
