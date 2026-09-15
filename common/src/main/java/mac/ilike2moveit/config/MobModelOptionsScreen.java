package mac.ilike2moveit.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Dedicated resource-pack model settings, deliberately independent from EMF's global config. */
public final class MobModelOptionsScreen extends Screen {
    private final Screen parent;

    public MobModelOptionsScreen(Screen parent) {
        super(Component.translatable("config.ilike2moveit.resource_pack_options.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int center = width / 2;
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.ChickenBabyModel::label)
                .withValues(MobModelConfig.ChickenBabyModel.values())
                .withInitialValue(MobModelConfig.chickenBabyModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.chicken.tooltip")))
                .create(
                        center - 100,
                        92,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.chicken"),
                        (button, value) -> MobModelConfig.setChickenBabyModel(value)
                ));
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.CatBabyModel::label)
                .withValues(MobModelConfig.CatBabyModel.values())
                .withInitialValue(MobModelConfig.catBabyModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.cat.tooltip")))
                .create(
                        center - 100,
                        116,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.cat"),
                        (button, value) -> MobModelConfig.setCatBabyModel(value)
                ));
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.WolfBabyModel::label)
                .withValues(MobModelConfig.WolfBabyModel.values())
                .withInitialValue(MobModelConfig.wolfBabyModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.wolf.tooltip")))
                .create(
                        center - 100,
                        140,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.wolf"),
                        (button, value) -> MobModelConfig.setWolfBabyModel(value)
                ));
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.OcelotBabyModel::label)
                .withValues(MobModelConfig.OcelotBabyModel.values())
                .withInitialValue(MobModelConfig.ocelotBabyModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.ocelot.tooltip")))
                .create(
                        center - 100,
                        164,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.ocelot"),
                        (button, value) -> MobModelConfig.setOcelotBabyModel(value)
                ));
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.RabbitBabyModel::label)
                .withValues(MobModelConfig.RabbitBabyModel.values())
                .withInitialValue(MobModelConfig.rabbitBabyModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.rabbit.tooltip")))
                .create(
                        center - 100,
                        188,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.rabbit"),
                        (button, value) -> MobModelConfig.setRabbitBabyModel(value)
                ));
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.PigBabyModel::label)
                .withValues(MobModelConfig.PigBabyModel.values())
                .withInitialValue(MobModelConfig.pigBabyModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.pig.tooltip")))
                .create(
                        center - 100,
                        212,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.pig"),
                        (button, value) -> MobModelConfig.setPigBabyModel(value)
                ));
        addRenderableWidget(CycleButton
                .builder(MobModelConfig.SheepAdultModel::label)
                .withValues(MobModelConfig.SheepAdultModel.values())
                .withInitialValue(MobModelConfig.sheepAdultModel())
                .withTooltip(value -> Tooltip.create(Component.translatable(
                        "config.ilike2moveit.mob_models.sheep.tooltip")))
                .create(
                        center - 100,
                        236,
                        200,
                        20,
                        Component.translatable("config.ilike2moveit.mob_models.sheep"),
                        (button, value) -> MobModelConfig.setSheepAdultModel(value)
                ));
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .bounds(center - 100, height - 28, 200, 20)
                .build());
    }

    @Override
    public void onClose() {
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 28, 0xFFFFFF);
        graphics.drawCenteredString(
                font,
                Component.translatable("config.ilike2moveit.resource_pack_options.subtitle"),
                width / 2,
                48,
                0xA0A0A0
        );
        graphics.drawString(
                font,
                Component.translatable("config.ilike2moveit.mob_models"),
                width / 2 - 100,
                66,
                0xFFFFFF
        );
        graphics.drawString(
                font,
                Component.literal("  ").append(Component.translatable(
                        "config.ilike2moveit.mob_models.passive")),
                width / 2 - 100,
                78,
                0xA0A0A0
        );
    }
}
