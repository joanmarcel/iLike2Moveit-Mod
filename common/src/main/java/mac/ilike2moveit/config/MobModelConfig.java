package mac.ilike2moveit.config;

import mac.ilike2moveit.MoveItCore;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Persistent client choices for resource-pack mob model families. */
public final class MobModelConfig {
    private static final String CHICK_KEY = "chicken_baby";
    private static final String CAT_KEY = "cat_baby";
    private static final String WOLF_KEY = "wolf_baby";
    private static final String OCELOT_KEY = "ocelot_baby";
    private static final String RABBIT_KEY = "rabbit_baby";
    private static final String PIG_KEY = "pig_baby";
    private static final String SHEEP_KEY = "sheep_adult";
    private static final Path CONFIG_PATH = Minecraft.getInstance().gameDirectory.toPath()
            .resolve("config")
            .resolve("ilike2moveit-mob-models.properties");

    private static ChickenBabyModel chickenBabyModel = loadChickenBabyModel();
    private static CatBabyModel catBabyModel = loadCatBabyModel();
    private static WolfBabyModel wolfBabyModel = loadWolfBabyModel();
    private static OcelotBabyModel ocelotBabyModel = loadOcelotBabyModel();
    private static RabbitBabyModel rabbitBabyModel = loadRabbitBabyModel();
    private static PigBabyModel pigBabyModel = loadPigBabyModel();
    private static SheepAdultModel sheepAdultModel = loadSheepAdultModel();

    private MobModelConfig() {
    }

    public static ChickenBabyModel chickenBabyModel() {
        return chickenBabyModel;
    }

    public static void setChickenBabyModel(ChickenBabyModel model) {
        chickenBabyModel = model;
        save();
    }

    public static CatBabyModel catBabyModel() {
        return catBabyModel;
    }

    public static void setCatBabyModel(CatBabyModel model) {
        catBabyModel = model;
        save();
    }

    public static WolfBabyModel wolfBabyModel() {
        return wolfBabyModel;
    }

    public static void setWolfBabyModel(WolfBabyModel model) {
        wolfBabyModel = model;
        save();
    }

    public static OcelotBabyModel ocelotBabyModel() {
        return ocelotBabyModel;
    }

    public static void setOcelotBabyModel(OcelotBabyModel model) {
        ocelotBabyModel = model;
        save();
    }

    public static RabbitBabyModel rabbitBabyModel() {
        return rabbitBabyModel;
    }

    public static void setRabbitBabyModel(RabbitBabyModel model) {
        rabbitBabyModel = model;
        save();
    }

    public static PigBabyModel pigBabyModel() {
        return pigBabyModel;
    }

    public static void setPigBabyModel(PigBabyModel model) {
        pigBabyModel = model;
        save();
    }

    public static SheepAdultModel sheepAdultModel() {
        return sheepAdultModel;
    }

    public static void setSheepAdultModel(SheepAdultModel model) {
        sheepAdultModel = model;
        save();
    }

    private static ChickenBabyModel loadChickenBabyModel() {
        Properties properties = new Properties();
        if (Files.isRegularFile(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException exception) {
                MoveItCore.LOGGER.warn("Could not read mob model options from {}.", CONFIG_PATH, exception);
            }
        }

        try {
            return ChickenBabyModel.valueOf(properties.getProperty(CHICK_KEY, ChickenBabyModel.TINY_TAKEOVER.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown chick model '{}'; using Tiny Takeover.", properties.getProperty(CHICK_KEY));
            return ChickenBabyModel.TINY_TAKEOVER;
        }
    }

    private static CatBabyModel loadCatBabyModel() {
        Properties properties = loadProperties();
        try {
            return CatBabyModel.valueOf(properties.getProperty(CAT_KEY, CatBabyModel.TINY_TAKEOVER.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown kitten model '{}'; using Tiny Takeover.", properties.getProperty(CAT_KEY));
            return CatBabyModel.TINY_TAKEOVER;
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        if (Files.isRegularFile(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException exception) {
                MoveItCore.LOGGER.warn("Could not read mob model options from {}.", CONFIG_PATH, exception);
            }
        }
        return properties;
    }

    private static WolfBabyModel loadWolfBabyModel() {
        Properties properties = loadProperties();
        try {
            return WolfBabyModel.valueOf(properties.getProperty(WOLF_KEY, WolfBabyModel.TINY_TAKEOVER.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown puppy model '{}'; using Tiny Takeover.", properties.getProperty(WOLF_KEY));
            return WolfBabyModel.TINY_TAKEOVER;
        }
    }

    private static OcelotBabyModel loadOcelotBabyModel() {
        Properties properties = loadProperties();
        try {
            return OcelotBabyModel.valueOf(properties.getProperty(
                    OCELOT_KEY, OcelotBabyModel.TINY_TAKEOVER.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown baby ocelot model '{}'; using Tiny Takeover.",
                    properties.getProperty(OCELOT_KEY));
            return OcelotBabyModel.TINY_TAKEOVER;
        }
    }

    private static RabbitBabyModel loadRabbitBabyModel() {
        Properties properties = loadProperties();
        try {
            return RabbitBabyModel.valueOf(properties.getProperty(
                    RABBIT_KEY, RabbitBabyModel.TINY_TAKEOVER.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown baby rabbit model '{}'; using Tiny Takeover.",
                    properties.getProperty(RABBIT_KEY));
            return RabbitBabyModel.TINY_TAKEOVER;
        }
    }

    private static PigBabyModel loadPigBabyModel() {
        Properties properties = loadProperties();
        try {
            return PigBabyModel.valueOf(properties.getProperty(PIG_KEY, PigBabyModel.TINY_TAKEOVER.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown piglet model '{}'; using Tiny Takeover.", properties.getProperty(PIG_KEY));
            return PigBabyModel.TINY_TAKEOVER;
        }
    }

    private static SheepAdultModel loadSheepAdultModel() {
        Properties properties = loadProperties();
        try {
            return SheepAdultModel.valueOf(properties.getProperty(
                    SHEEP_KEY, SheepAdultModel.CLASSIC.name()));
        } catch (IllegalArgumentException exception) {
            MoveItCore.LOGGER.warn("Unknown adult sheep model '{}'; using Classic.",
                    properties.getProperty(SHEEP_KEY));
            return SheepAdultModel.CLASSIC;
        }
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty(CHICK_KEY, chickenBabyModel.name());
        properties.setProperty(CAT_KEY, catBabyModel.name());
        properties.setProperty(WOLF_KEY, wolfBabyModel.name());
        properties.setProperty(OCELOT_KEY, ocelotBabyModel.name());
        properties.setProperty(RABBIT_KEY, rabbitBabyModel.name());
        properties.setProperty(PIG_KEY, pigBabyModel.name());
        properties.setProperty(SHEEP_KEY, sheepAdultModel.name());
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                properties.store(writer, "iLike2MoveIt mob model choices");
            }
        } catch (IOException exception) {
            MoveItCore.LOGGER.error("Could not save mob model options to {}.", CONFIG_PATH, exception);
        }
    }

    public enum ChickenBabyModel {
        TINY_TAKEOVER("config.ilike2moveit.mob_models.chicken.tiny_takeover"),
        CLASSIC("config.ilike2moveit.mob_models.chicken.classic");

        private final String translationKey;

        ChickenBabyModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }

    public enum CatBabyModel {
        CLASSIC("config.ilike2moveit.mob_models.cat.classic"),
        TINY_TAKEOVER("config.ilike2moveit.mob_models.cat.tiny_takeover");

        private final String translationKey;

        CatBabyModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }

    public enum WolfBabyModel {
        CLASSIC("config.ilike2moveit.mob_models.wolf.classic"),
        TINY_TAKEOVER("config.ilike2moveit.mob_models.wolf.tiny_takeover");

        private final String translationKey;

        WolfBabyModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }

    public enum OcelotBabyModel {
        CLASSIC("config.ilike2moveit.mob_models.ocelot.classic"),
        TINY_TAKEOVER("config.ilike2moveit.mob_models.ocelot.tiny_takeover");

        private final String translationKey;

        OcelotBabyModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }

    public enum RabbitBabyModel {
        CLASSIC("config.ilike2moveit.mob_models.rabbit.classic"),
        TINY_TAKEOVER("config.ilike2moveit.mob_models.rabbit.tiny_takeover");

        private final String translationKey;

        RabbitBabyModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }

    public enum PigBabyModel {
        CLASSIC("config.ilike2moveit.mob_models.pig.classic"),
        TINY_TAKEOVER("config.ilike2moveit.mob_models.pig.tiny_takeover");

        private final String translationKey;

        PigBabyModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }

    public enum SheepAdultModel {
        CLASSIC("config.ilike2moveit.mob_models.sheep.classic"),
        ALTERNATE("config.ilike2moveit.mob_models.sheep.alternate");

        private final String translationKey;

        SheepAdultModel(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component label() {
            return Component.translatable(translationKey);
        }

        @Override
        public String toString() {
            return label().getString();
        }
    }
}
