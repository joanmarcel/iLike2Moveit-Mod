package mac.ilike2moveit.render;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Per-mixin render-call stack for a temporary renderer model replacement.
 *
 * <p>The marker stack records every applicable render invocation, including no-op adult renders,
 * while the model stack records only real replacements. This keeps HEAD/RETURN symmetric without
 * allowing an unrelated model selector to save or restore the shared LivingEntityRenderer field.
 */
public final class RendererModelSwapStack<T> {
    private final ThreadLocal<Deque<T>> savedModels = ThreadLocal.withInitial(ArrayDeque::new);
    private final ThreadLocal<Deque<Boolean>> swapMarkers = ThreadLocal.withInitial(ArrayDeque::new);

    public T begin(T currentModel, T replacementModel) {
        boolean swapped = replacementModel != null && replacementModel != currentModel;
        swapMarkers.get().push(swapped);
        if (swapped) {
            savedModels.get().push(currentModel);
            return replacementModel;
        }
        return currentModel;
    }

    public T end(T currentModel) {
        Deque<Boolean> markers = swapMarkers.get();
        if (markers.isEmpty()) {
            throw new IllegalStateException("Renderer model swap RETURN without matching HEAD");
        }
        boolean swapped = markers.pop();
        if (markers.isEmpty()) {
            swapMarkers.remove();
        }
        if (!swapped) {
            return currentModel;
        }

        Deque<T> models = savedModels.get();
        T restoredModel = models.pop();
        if (models.isEmpty()) {
            savedModels.remove();
        }
        return restoredModel;
    }
}
