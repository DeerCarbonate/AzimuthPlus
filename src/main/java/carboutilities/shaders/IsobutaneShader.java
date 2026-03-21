package carboutilities.shaders;

import arc.graphics.gl.Shader;
import arc.util.Log;
import mindustry.graphics.CacheLayer;

import java.io.InputStream;
import java.util.Scanner;

public class IsobutaneShader {

    public static void replaceMud() {
        try {
            String vertSrc = readResource("shaders/isobutane.vert");
            String fragSrc = readResource("shaders/isobutane.frag");

            if (vertSrc == null || fragSrc == null) {
                Log.err("[IsobutaneShader] Shader resources not found in jar!");
                return;
            }

            Log.info("[IsobutaneShader] Vert loaded, len=" + vertSrc.length());
            Log.info("[IsobutaneShader] Frag loaded, len=" + fragSrc.length());

            Shader newShader = new Shader(vertSrc, fragSrc);

            if (!newShader.isCompiled()) {
                Log.err("[IsobutaneShader] Compile error: " + newShader.getLog());
                return;
            }
            Log.info("[IsobutaneShader] Compiled OK");

            if (CacheLayer.mud instanceof CacheLayer.ShaderLayer) {
                ((CacheLayer.ShaderLayer) CacheLayer.mud).shader = newShader;
                Log.info("[IsobutaneShader] CacheLayer.mud replaced!");
            } else {
                Log.warn("[IsobutaneShader] mud is not ShaderLayer: " + CacheLayer.mud.getClass().getName());
            }

        } catch (Throwable e) {
            Log.err("[IsobutaneShader] Failed", e);
        }
    }

    private static String readResource(String path) {
        InputStream is = IsobutaneShader.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            Log.err("[IsobutaneShader] Resource not found: " + path);
            return null;
        }
        try {
            Scanner sc = new Scanner(is, "UTF-8").useDelimiter("\\A");
            return sc.hasNext() ? sc.next() : "";
        } finally {
            try { is.close(); } catch (Exception ignored) {}
        }
    }
}