package com.blueorbit.teamup.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class MembershipUtil {
    private MembershipUtil() {
    }

    public static Set<Long> parseIdSet(String raw) {
        Set<Long> ids = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return ids;
        }
        String[] parts = raw.split(";");
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            ids.add(Long.parseLong(part.trim()));
        }
        return ids;
    }

    public static String appendUnique(String raw, Long id) {
        Set<Long> ids = parseIdSet(raw);
        ids.add(id);
        return toStorageString(ids);
    }

    public static String toStorageString(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        return ids.stream().map(String::valueOf).collect(Collectors.joining(";", "", ";"));
    }
}
