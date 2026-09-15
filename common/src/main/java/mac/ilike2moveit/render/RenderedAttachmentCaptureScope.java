package mac.ilike2moveit.render;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Entity-render scope for attachment poses captured during the real model traversal.
 *
 * <p>The model renderer may recompute an attachment later from a bare root pose. Such a recompute
 * is not equivalent to rendering the model: age transforms such as {@code AgeableListModel}'s baby
 * head/body scale are missing. A capture is therefore accepted only while a rendered model part is
 * on the stack and before feature-layer rendering begins.
 */
public final class RenderedAttachmentCaptureScope<K, R, V, P> {

    public record Capture<R, V, P>(R root, V variant, P pose) {
    }

    private final ThreadLocal<Deque<EntityScope<K, R, V, P>>> entityScopes =
            ThreadLocal.withInitial(ArrayDeque::new);

    public void beginEntity(K entity) {
        entityScopes.get().push(new EntityScope<>(entity));
    }

    public void endEntity(K entity) {
        Deque<EntityScope<K, R, V, P>> scopes = entityScopes.get();
        if (scopes.isEmpty()) {
            return;
        }
        EntityScope<K, R, V, P> scope = scopes.pop();
        if (!Objects.equals(scope.entity, entity)) {
            scopes.clear();
        }
        if (scopes.isEmpty()) {
            entityScopes.remove();
        }
    }

    public void enterRenderedPart(R root, V variant) {
        EntityScope<K, R, V, P> scope = currentScope();
        if (scope != null) {
            scope.renderedParts.push(new RenderedPart<>(root, variant));
        }
    }

    public void exitRenderedPart() {
        EntityScope<K, R, V, P> scope = currentScope();
        if (scope != null && !scope.renderedParts.isEmpty()) {
            scope.renderedParts.pop();
        }
    }

    /**
     * Captures only a main-model render pass. Direct post-render recomputes have no rendered part
     * marker; feature-model passes are rejected through {@code layerPhase}.
     */
    public boolean capture(K stateEntity, P pose, boolean layerPhase) {
        EntityScope<K, R, V, P> scope = currentScope();
        if (scope == null || layerPhase || pose == null
                || !Objects.equals(scope.entity, stateEntity) || scope.renderedParts.isEmpty()) {
            return false;
        }
        RenderedPart<R, V> renderedPart = scope.renderedParts.peek();
        scope.capture = new Capture<>(renderedPart.root, renderedPart.variant, pose);
        return true;
    }

    public Capture<R, V, P> current(K entity) {
        EntityScope<K, R, V, P> scope = currentScope();
        return scope != null && Objects.equals(scope.entity, entity) ? scope.capture : null;
    }

    private EntityScope<K, R, V, P> currentScope() {
        Deque<EntityScope<K, R, V, P>> scopes = entityScopes.get();
        return scopes.isEmpty() ? null : scopes.peek();
    }

    private static final class EntityScope<K, R, V, P> {
        private final K entity;
        private final Deque<RenderedPart<R, V>> renderedParts = new ArrayDeque<>();
        private Capture<R, V, P> capture;

        private EntityScope(K entity) {
            this.entity = entity;
        }
    }

    private record RenderedPart<R, V>(R root, V variant) {
    }
}
