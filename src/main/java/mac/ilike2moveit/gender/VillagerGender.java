package mac.ilike2moveit.gender;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;
import traben.entity_texture_features.ETFApi;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WOMAN VARIANT of the villager (gender layer, drawn by VillagerProfessionLayerMixin between the
 * biome pass and the profession one -> hierarchy skin &lt; biome &lt; gender &lt; job).
 *
 * Supports N woman textures, each with its own set of compatible base skins. One skin may be
 * compatible with SEVERAL textures -> one is picked stably by UUID.
 *
 * Per villager:
 *   1) WOMAN = ~33% stable by UUID (MSB, so as NOT to correlate with the skin ETF picks by LSB).
 *   2) ADULTS only.
 *   3) EXACT skin&lt;-&gt;woman association (Option B): the base skin is READ through ETFApi (robust, without
 *      replicating ETF's algorithm); only the textures whose set contains that skin apply.
 *   4) Among the compatible textures, one is picked stably by UUID.
 *
 * Each "womanX" key maps to the texture textures/entity/villager/gender/womanX.png.
 * Client-side. percent and the per-texture sets are hot-reloadable from /tmp/ilike2moveit_gender.txt:
 *     percent=33
 *     woman=5 7 13 17 20 21
 *     woman2=7 9 11 13 17 20 21
 */
public final class VillagerGender {

    private static final ResourceLocation VILLAGER_BASE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/villager.png");
    private static final Pattern SKIN_NUM = Pattern.compile("villager(\\d*)\\.png$");

    private static volatile int womanPercent = 33;
    // key (womanX) -> compatible base skins. One skin may appear under several keys.
    private static volatile Map<String, Set<Integer>> assets = defaultAssets();

    private static final File CFG = new File("/tmp/ilike2moveit_gender.txt");
    private static volatile long lastLoad = 0L;

    private VillagerGender() {
    }

    private static Map<String, Set<Integer>> defaultAssets() {
        Map<String, Set<Integer>> map = new LinkedHashMap<>();
        map.put("woman", new HashSet<>(Set.of(5, 7, 13, 17, 20, 21)));
        map.put("woman2", new HashSet<>(Set.of(3, 7, 9, 11, 13, 17, 20, 21)));
        map.put("woman3", new HashSet<>(Set.of(11, 12, 17, 18, 20)));
        map.put("woman4", new HashSet<>(Set.of(3, 5)));
        return map;
    }

    /** Woman layer texture for this villager, or null if it does not apply (man, baby, or unmapped skin). */
    public static ResourceLocation womanTextureFor(Villager villager) {
        if (villager.isBaby()) {
            return null;
        }
        reloadIfNeeded();
        UUID id = villager.getUUID();
        if (((int) (id.getMostSignificantBits() & 0x7fffffffL)) % 100 >= womanPercent) {
            return null;   // hombre
        }
        ResourceLocation etfTex = currentSkinTexture(villager);
        int skin = skinNumberFromTex(etfTex);
        Map<String, Set<Integer>> current = assets;
        List<String> compatible = new ArrayList<>();
        for (Map.Entry<String, Set<Integer>> entry : current.entrySet()) {
            if (entry.getValue().contains(skin)) {
                compatible.add(entry.getKey());
            }
        }
        Collections.sort(compatible);   // deterministic order, independent of the map's own order
        String chosen = null;
        if (!compatible.isEmpty()) {
            // Pick that is stable and independent of the woman roll (which uses MSB): high MSB ^ LSB.
            int pick = (int) (((id.getMostSignificantBits() >>> 33) ^ id.getLeastSignificantBits()) & 0x7fffffffL)
                    % compatible.size();
            chosen = compatible.get(pick);
        }
        return chosen == null ? null : texFor(chosen);
    }

    private static ResourceLocation texFor(String key) {
        return ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/villager/gender/" + key + ".png");
    }

    /** Skin texture ETF assigned to the entity (or null). */
    private static ResourceLocation currentSkinTexture(Villager villager) {
        try {
            return ETFApi.getCurrentETFVariantTextureOfEntity(villager, VILLAGER_BASE);
        } catch (Throwable ignored) {
            return null;   // ETF absent or API change.
        }
    }

    /** Variant number from the ETF texture (villager20.png -> 20 ; villager.png or null -> 1). */
    private static int skinNumberFromTex(ResourceLocation tex) {
        if (tex != null) {
            Matcher matcher = SKIN_NUM.matcher(tex.getPath());
            if (matcher.find()) {
                String number = matcher.group(1);
                return number.isEmpty() ? 1 : Integer.parseInt(number);
            }
        }
        return 1;
    }

    private static void reloadIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastLoad < 400L) {
            return;
        }
        lastLoad = now;
        try {
            if (!CFG.exists()) {
                return;
            }
            int percent = womanPercent;
            Map<String, Set<Integer>> rebuilt = defaultAssets();   // base = defaults; el archivo sobrescribe/anade
            for (String line : Files.readAllLines(CFG.toPath())) {
                String[] kv = line.split("=");
                if (kv.length != 2) {
                    continue;
                }
                String key = kv[0].trim();
                String value = kv[1].trim();
                if (key.equals("percent")) {
                    percent = Math.max(0, Math.min(100, Integer.parseInt(value)));
                } else {
                    Set<Integer> parsed = new HashSet<>();
                    for (String token : value.split("[ ,]+")) {
                        if (!token.isEmpty()) {
                            parsed.add(Integer.parseInt(token));
                        }
                    }
                    if (parsed.isEmpty()) {
                        rebuilt.remove(key);       // key= (vacio) -> desactiva esa textura
                    } else {
                        rebuilt.put(key, parsed);  // override o textura nueva
                    }
                }
            }
            womanPercent = percent;
            assets = rebuilt;
        } catch (Exception ignored) {
            // linea malformada -> conservar valores actuales.
        }
    }
}
