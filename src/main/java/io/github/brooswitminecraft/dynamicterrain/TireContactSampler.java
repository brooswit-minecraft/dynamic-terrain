package io.github.brooswitminecraft.dynamicterrain;

/**
 * Samples a {@link TexelField} across the tire contact patch rather than at
 * a single rendering-frame point: it walks the texels between where the
 * patch was and where it is now this physics step, on the face orientation
 * transforms the contact into.
 */
public final class TireContactSampler {
    private TireContactSampler() {}

    /** Average and peak evidence over the texels crossed this step. */
    public record ContactPatch(TexelField.TexelSample average, TexelField.TexelSample peak) {}

    /**
     * @param face the contacted face; orients the patch into texture space
     * @param from, to block-local contact point (each coordinate 0..1) at the start and end of this step
     */
    public static ContactPatch integrate(TexelField field, Face face,
            double fromX, double fromY, double fromZ, double toX, double toY, double toZ) {
        FaceUv.Uv from = FaceUv.of(face, fromX, fromY, fromZ);
        FaceUv.Uv to = FaceUv.of(face, toX, toY, toZ);
        double du = to.u() - from.u();
        double dv = to.v() - from.v();
        int texelSpan = Math.max(field.width(), field.height());
        int steps = Math.max(1, (int) Math.ceil(Math.hypot(du, dv) * texelSpan));

        float chroma = 0;
        float contrast = 0;
        float xVariation = 0;
        float yVariation = 0;
        float peakContrast = 0;
        int n = steps + 1;
        for (int i = 0; i < n; i++) {
            double t = steps == 0 ? 0 : (double) i / steps;
            TexelField.TexelSample s = field.sample(from.u() + du * t, from.v() + dv * t);
            chroma += s.chroma();
            contrast += s.luminanceContrast();
            xVariation += s.xVariation();
            yVariation += s.yVariation();
            peakContrast = Math.max(peakContrast, s.luminanceContrast());
        }
        TexelField.TexelSample average = new TexelField.TexelSample(
                chroma / n, contrast / n, xVariation / n, yVariation / n);
        TexelField.TexelSample peak = new TexelField.TexelSample(
                average.chroma(), peakContrast, average.xVariation(), average.yVariation());
        return new ContactPatch(average, peak);
    }
}
