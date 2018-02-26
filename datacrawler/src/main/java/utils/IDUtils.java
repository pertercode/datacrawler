package utils;

import java.util.UUID;

/**
 * 构建ID的工具
 */
public class IDUtils {

    // 产生UUID
    public static String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 构建ID
     *
     * @param platform
     * @param uuid
     * @return
     */
    public static String genId(String platform, String uuid) {
        return platform + "_" + uuid;
    }
}
