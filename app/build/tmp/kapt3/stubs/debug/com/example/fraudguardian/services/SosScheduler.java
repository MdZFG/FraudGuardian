package com.example.fraudguardian.services;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ \u0010\t\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/example/fraudguardian/services/SosScheduler;", "", "()V", "SOS_WORK_TAG", "", "cancelAllSos", "", "context", "Landroid/content/Context;", "scheduleSos", "message", "delaySeconds", "", "app_debug"})
public final class SosScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SOS_WORK_TAG = "fraudguardian_sos";
    @org.jetbrains.annotations.NotNull()
    public static final com.example.fraudguardian.services.SosScheduler INSTANCE = null;
    
    private SosScheduler() {
        super();
    }
    
    public final void scheduleSos(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String message, long delaySeconds) {
    }
    
    public final void cancelAllSos(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}