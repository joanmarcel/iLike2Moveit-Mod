package mac.ilike2moveit.rabbit;

import net.minecraft.ChatFormatting;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;

/** Client-only model layer and texture routing for the optional Tiny Takeover baby rabbit. */
public final class RabbitVariantCompat {
    public static final ModelLayerLocation TINY_TAKEOVER_RABBIT_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("minecraft", "tiny_takeover_rabbit"), "main"
    );

    private RabbitVariantCompat() {
    }

    public static ResourceLocation tinyTexture(Rabbit rabbit) {
        String name = ChatFormatting.stripFormatting(rabbit.getName().getString());
        int index = "Toast".equals(name) ? 6 : switch (rabbit.getVariant()) {
            case BROWN -> 0;
            case WHITE -> 1;
            case BLACK -> 2;
            case WHITE_SPLOTCHED -> 3;
            case GOLD -> 4;
            case SALT -> 5;
            case EVIL -> 7;
        };
        return ResourceLocation.fromNamespaceAndPath(
                "minecraft", String.format(
                        "textures/entity/rabbit/tiny_takeover/tiny_variant_%02d.png", index)
        );
    }

    /** Exact vanilla RabbitModel anchors, with empty boxes because the matching JEM owns the mesh. */
    public static LayerDefinition createTinyTakeoverRabbitLayer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        CubeListBuilder empty = CubeListBuilder.create();
        root.addOrReplaceChild("left_hind_foot", empty, PartPose.offset(3.0F, 17.5F, 3.7F));
        root.addOrReplaceChild("right_hind_foot", empty, PartPose.offset(-3.0F, 17.5F, 3.7F));
        root.addOrReplaceChild("left_haunch", empty,
                PartPose.offsetAndRotation(3.0F, 17.5F, 3.7F,
                        (float) (-Math.PI / 9.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_haunch", empty,
                PartPose.offsetAndRotation(-3.0F, 17.5F, 3.7F,
                        (float) (-Math.PI / 9.0), 0.0F, 0.0F));
        root.addOrReplaceChild("body", empty,
                PartPose.offsetAndRotation(0.0F, 19.0F, 8.0F,
                        (float) (-Math.PI / 9.0), 0.0F, 0.0F));
        root.addOrReplaceChild("left_front_leg", empty,
                PartPose.offsetAndRotation(3.0F, 17.0F, -1.0F,
                        (float) (-Math.PI / 18.0), 0.0F, 0.0F));
        root.addOrReplaceChild("right_front_leg", empty,
                PartPose.offsetAndRotation(-3.0F, 17.0F, -1.0F,
                        (float) (-Math.PI / 18.0), 0.0F, 0.0F));
        root.addOrReplaceChild("head", empty, PartPose.offset(0.0F, 16.0F, -1.0F));
        root.addOrReplaceChild("right_ear", empty,
                PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F,
                        0.0F, (float) (-Math.PI / 12.0), 0.0F));
        root.addOrReplaceChild("left_ear", empty,
                PartPose.offsetAndRotation(0.0F, 16.0F, -1.0F,
                        0.0F, (float) (Math.PI / 12.0), 0.0F));
        root.addOrReplaceChild("tail", empty,
                PartPose.offsetAndRotation(0.0F, 20.0F, 7.0F,
                        (float) Math.toRadians(-20.0), 0.0F, 0.0F));
        root.addOrReplaceChild("nose", empty, PartPose.offset(0.0F, 16.0F, -1.0F));
        return LayerDefinition.create(mesh, 32, 32);
    }
}
