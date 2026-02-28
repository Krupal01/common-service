package com.krunish.common.security;

public class OrgContext {

    private static final ThreadLocal<Ctx> holder = new ThreadLocal<>();

    public static void set(Long userId, Long orgId, String email, boolean isServiceCall) {
        holder.set(new Ctx(userId, orgId, email, isServiceCall));
    }

    public static Ctx get() {
        return holder.get();
    }

    public static Long getUserId() {
        return holder.get() != null ? holder.get().userId() : null;
    }

    public static Long getOrgId() {
        return holder.get() != null ? holder.get().orgId : null;
    }

    public static void clear() {
        holder.remove();
    }

    public record Ctx(Long userId, Long orgId, String email, boolean isServiceCall) {}
}
