package carboutilities;

import arc.math.Mathf;
import arc.util.Log;
import arc.util.Reflect;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.type.UnitType;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import java.lang.reflect.Method;

public class wmb {


    private static float hash(int x, int y, int seed) {
        int n = x * 1619 + y * 31337 + seed * 1000003;
        n = (n << 13) ^ n;
        return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / (float)0x7fffffff;
    }

    private static float vnoise(float x, float y, int seed) {
        int ix = (int)Math.floor(x), iy = (int)Math.floor(y);
        float fx = x - ix, fy = y - iy;
        float ux = fx * fx * (3 - 2 * fx), uy = fy * fy * (3 - 2 * fy);
        return arc.math.Mathf.lerp(
                arc.math.Mathf.lerp(hash(ix,   iy,   seed), hash(ix+1, iy,   seed), ux),
                arc.math.Mathf.lerp(hash(ix,   iy+1, seed), hash(ix+1, iy+1, seed), ux),
                uy
        );
    }

    private static float fbm(float x, float y, int seed, int oct) {
        float val = 0, amp = 1f, total = 0, freq = 1f;
        for (int i = 0; i < oct; i++) {
            val   += vnoise(x * freq, y * freq, seed + i * 7919) * amp;
            total += amp;
            amp   *= 0.5f;
            freq  *= 2f;
        }
        return val / total;
    }

    // ── Инъекция ───────────────────────────────────────────────────────────────

    public static void injectPlanetBackground() {
        try {
            Object renderer = Reflect.get(Vars.ui.menufrag, "renderer");
            if (renderer == null) {
                Log.err("[WMB] renderer is null");
                return;
            }

            int width  = Reflect.get(renderer, "width");
            int height = Reflect.get(renderer, "height");

            Floor floorAsh   = floor("azimut-ashium-floor",       "shale");
            Floor floorSnow  = floor("azimut-snowy-ashium-floor", "snow");
            Floor floorRed   = floor("azimut-red-snow-floor",     "ice");
            Block wallAsh    = block("azimut-ashium-wall",         "shale-wall");
            Block wallRed    = block("azimut-red-snow-wall",       "snow-wall");
            Block boulderAsh = block("azimut-ashium-boulder",      "boulder");
            Block boulderRed = block("azimut-red-snow-boulder",    "boulder");
            Block tree       = block("azimut-ashium-smalltree",    "pine");

            Vars.world.setGenerating(true);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    Tile tile = Vars.world.tile(x, y);
                    if (tile == null) continue;

                    float biome = fbm(x / 24f, y / 28f, 1237, 2);
                    float snow  = fbm(x / 4f, y / 4f, 4244, 2);
                    float wall  = fbm(x / 17f, y / 14f, 9992, 3);
                    float decor = fbm(x / 12f, y /  13f, 7724, 7);

                    boolean isRed  = biome > 0.70f;
                    boolean isSnow = !isRed && snow > 0.65f;

                    tile.setFloor(isRed ? floorRed : isSnow ? floorSnow : floorAsh);
                    tile.setBlock(Blocks.air);
                    tile.setOverlay(Blocks.air);

                    if (wall > 0.53f) {
                        tile.setBlock(isRed ? wallRed : wallAsh);
                    } else if (!isRed) {
                        if      (decor > 0.79f) tile.setBlock(tree);
                        else if (decor > 0.78f) tile.setBlock(boulderAsh);
                    } else {
                        if (decor > 0.73f) tile.setBlock(boulderRed);
                    }
                }
            }

            Vars.world.setGenerating(false);

            Method cache = renderer.getClass().getDeclaredMethod("cache");
            cache.setAccessible(true);
            cache.invoke(renderer);

            Log.info("[WMB] Azimut background injected!");
        } catch (Throwable e) {
            Log.err("[WMB] inject failed", e);
        }
    }

    public static void injectFlyerUnit() {
        try {
            Object renderer = Reflect.get(Vars.ui.menufrag, "renderer");
            if (renderer == null) return;

            UnitType adder = null;
            for (UnitType u : Vars.content.units()) {
                if ("azimut-adder".equals(u.name)) { adder = u; break; }
            }
            if (adder == null) { Log.warn("[WMB] azimut-adder not found"); return; }

            try {
                Reflect.set(renderer, "flyerType", adder);
            } catch (Throwable e1) {
                try {
                    // В некоторых билдах поле может называться иначе
                    java.lang.reflect.Field[] fields = renderer.getClass().getDeclaredFields();
                    for (java.lang.reflect.Field f : fields) {
                        if (f.getType() == UnitType.class) {
                            f.setAccessible(true);
                            f.set(renderer, adder);
                            Log.info("[WMB] flyerType set via field scan: " + f.getName());
                            break;
                        }
                    }
                } catch (Throwable e2) {
                    Log.warn("[WMB] Could not set flyerType: " + e2.getMessage());
                    return;
                }
            }

            Log.info("[WMB] Flyer replaced with azimut-adder");
        } catch (Throwable e) {
            Log.err("[WMB] injectFlyerUnit failed", e);
        }
    }

    private static Floor floor(String primary, String fallback) {
        Block b = Vars.content.block(primary);
        if (b instanceof Floor) return (Floor)b;
        b = Vars.content.block(fallback);
        if (b instanceof Floor) return (Floor)b;
        return (Floor)Blocks.shale;
    }

    private static Block block(String primary, String fallback) {
        Block b = Vars.content.block(primary);
        if (b != null && b != Blocks.air) return b;
        b = Vars.content.block(fallback);
        if (b != null && b != Blocks.air) return b;
        return Blocks.boulder;
    }
}