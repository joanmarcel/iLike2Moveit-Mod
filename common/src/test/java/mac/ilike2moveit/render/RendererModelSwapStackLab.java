package mac.ilike2moveit.render;

/** Runnable without Minecraft: guards against cross-species renderer model restoration. */
public final class RendererModelSwapStackLab {
    public static void main(String[] args) {
        Object adultCat = new Object();
        Object babyCat = new Object();
        Object adultOcelot = new Object();
        Object babyOcelot = new Object();

        RendererModelSwapStack<Object> cat = new RendererModelSwapStack<>();
        RendererModelSwapStack<Object> ocelot = new RendererModelSwapStack<>();

        Object current = cat.begin(adultCat, babyCat);
        expectSame(babyCat, current, "cat baby selected");
        current = cat.end(current);
        expectSame(adultCat, current, "cat adult restored");

        current = cat.begin(adultCat, null);
        expectSame(adultCat, current, "adult no-op remains adult");
        current = cat.end(current);
        expectSame(adultCat, current, "adult no-op RETURN remains adult");

        Object catCurrent = cat.begin(adultCat, babyCat);
        Object ocelotCurrent = ocelot.begin(adultOcelot, babyOcelot);
        catCurrent = cat.end(catCurrent);
        ocelotCurrent = ocelot.end(ocelotCurrent);
        expectSame(adultCat, catCurrent, "cat restore isolated from ocelot stack");
        expectSame(adultOcelot, ocelotCurrent, "ocelot restore isolated from cat stack");

        current = cat.begin(adultCat, babyCat);
        current = cat.begin(current, adultCat);
        current = cat.end(current);
        expectSame(babyCat, current, "nested inner restore");
        current = cat.end(current);
        expectSame(adultCat, current, "nested outer restore");

        System.out.println("RendererModelSwapStackLab OK: no-op, nested and cross-species restores isolated");
    }

    private static void expectSame(Object expected, Object actual, String context) {
        if (actual != expected) {
            throw new AssertionError("Incorrect renderer model: " + context);
        }
    }
}
