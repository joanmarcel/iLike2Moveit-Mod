package mac.ilike2moveit;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

/** Side-neutral identity and logger. Safe to load on a dedicated server. */
public final class ILike2MoveIt {
    public static final String MODID = "ilike2moveit";
    public static final Logger LOGGER = LogUtils.getLogger();

    private ILike2MoveIt() {
    }
}
