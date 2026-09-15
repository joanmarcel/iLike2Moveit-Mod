package mac.ilike2moveit.render;

/** Runnable without Minecraft: distinguishes entity, root, model variant and render pass. */
public final class RenderedAttachmentCaptureScopeLab {
    public static void main(String[] args) {
        RenderedAttachmentCaptureScope<String, Object, Integer, String> scope =
                new RenderedAttachmentCaptureScope<>();
        Object adultRoot = new Object();
        Object babyRoot = new Object();

        scope.beginEntity("fox");
        scope.enterRenderedPart(adultRoot, 1);
        expect(true, scope.capture("fox", "adult-render-pose", false), "adult model traversal accepted");
        scope.exitRenderedPart();
        expect(false, scope.capture("fox", "adult-post-recompute", false),
                "bare post-render recompute rejected");
        expectCapture(scope.current("fox"), adultRoot, 1, "adult-render-pose",
                "post recompute cannot overwrite rendered pose");
        scope.endEntity("fox");

        // Same UUID in a later render must start empty. This is the adult -> baby variant case.
        scope.beginEntity("fox");
        expect(null, scope.current("fox"), "same UUID does not retain adult capture");
        scope.enterRenderedPart(babyRoot, 2);
        expect(false, scope.capture("fox", "baby-feature-layer", true),
                "feature-layer attachment rejected");
        expect(false, scope.capture("another-fox", "foreign-pose", false),
                "foreign entity rejected");
        expect(true, scope.capture("fox", "baby-model-pose", false), "baby model traversal accepted");
        scope.exitRenderedPart();
        expectCapture(scope.current("fox"), babyRoot, 2, "baby-model-pose",
                "baby root and variant retained");
        scope.endEntity("fox");

        System.out.println("RenderedAttachmentCaptureScopeLab OK: UUID, root, variant and pass isolated");
    }

    private static void expectCapture(
            RenderedAttachmentCaptureScope.Capture<Object, Integer, String> capture,
            Object root, int variant, String pose, String context) {
        if (capture == null || capture.root() != root || capture.variant() != variant
                || !pose.equals(capture.pose())) {
            throw new AssertionError(context + ": " + capture);
        }
    }

    private static void expect(Object expected, Object actual, String context) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(context + ": expected=" + expected + " actual=" + actual);
        }
    }
}
