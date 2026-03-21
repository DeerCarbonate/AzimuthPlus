varying vec2 v_texCoords;
varying vec4 v_color;

uniform sampler2D u_texture;
uniform float u_time;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float vnoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(hash(i),                  hash(i + vec2(1.0, 0.0)), f.x),
        mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), f.x),
        f.y
    );
}

float fbm(vec2 p) {
    return vnoise(p)       * 0.50
         + vnoise(p * 2.0) * 0.25
         + vnoise(p * 4.0) * 0.125;
}

void main() {
    float t = u_time * 0.00045;

    // Лёгкое искажение UV
    float fx = fbm(v_texCoords * 3.0 + vec2( t * 0.5,  t * 0.3)) * 0.015;
    float fy = fbm(v_texCoords * 3.0 + vec2(-t * 0.3,  t * 0.6)) * 0.015;
    vec2  uv = v_texCoords + vec2(fx, fy);

    vec4 tex = texture2D(u_texture, uv);

    // Цвета изобутана
    vec3 colorBase  = vec3(0.604, 0.416, 0.345); // #9a6a58
    vec3 colorDark  = vec3(0.400, 0.247, 0.247); // #663f3f
    vec3 colorLight = vec3(0.720, 0.510, 0.430);

    float big   = fbm(uv * 2.5 + vec2( t * 0.3, -t * 0.2));
    float small = fbm(uv * 6.0 + vec2(-t * 0.5,  t * 0.4)) * 0.4;
    float mask  = clamp(big + small, 0.0, 1.0);

    vec3 procColor = mix(colorDark, colorBase, mask);

    // Блики
    float gloss = fbm(uv * 10.0 + vec2(t * 0.9, -t * 0.7));
    procColor += colorLight * smoothstep(0.62, 0.78, gloss) * 0.15;

    // Тёмные пятна
    float bubble = fbm(uv * 8.0 + vec2(-t * 0.25, t * 0.5));
    procColor -= colorDark * smoothstep(0.70, 0.78, bubble) * 0.2;

    // 70% цват + 30% оригинальная текстура
    vec3 finalColor = mix(tex.rgb, procColor, 0.7);

    gl_FragColor = vec4(finalColor * v_color.rgb, tex.a * v_color.a);
}
