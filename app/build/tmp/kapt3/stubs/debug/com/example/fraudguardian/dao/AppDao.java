package com.example.fraudguardian.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0007\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\bH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00050\bH\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\tH\u00a7@\u00a2\u0006\u0002\u0010\u0011J\u0016\u0010\u0012\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u0016\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0016"}, d2 = {"Lcom/example/fraudguardian/dao/AppDao;", "", "deleteTrusted", "", "contact", "Lcom/example/fraudguardian/entities/TrustedContact;", "(Lcom/example/fraudguardian/entities/TrustedContact;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCalls", "", "Lcom/example/fraudguardian/entities/CallLog;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getOtps", "Lcom/example/fraudguardian/entities/OtpLog;", "getTrusted", "insertCall", "", "call", "(Lcom/example/fraudguardian/entities/CallLog;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertOtp", "otp", "(Lcom/example/fraudguardian/entities/OtpLog;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertTrusted", "app_debug"})
@androidx.room.Dao()
public abstract interface AppDao {
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertOtp(@org.jetbrains.annotations.NotNull()
    com.example.fraudguardian.entities.OtpLog otp, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM otp_logs ORDER BY receivedAt DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getOtps(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.fraudguardian.entities.OtpLog>> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertCall(@org.jetbrains.annotations.NotNull()
    com.example.fraudguardian.entities.CallLog call, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM call_logs ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCalls(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.fraudguardian.entities.CallLog>> $completion);
    
    @androidx.room.Insert()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insertTrusted(@org.jetbrains.annotations.NotNull()
    com.example.fraudguardian.entities.TrustedContact contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM trusted_contacts ORDER BY name ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTrusted(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.example.fraudguardian.entities.TrustedContact>> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteTrusted(@org.jetbrains.annotations.NotNull()
    com.example.fraudguardian.entities.TrustedContact contact, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}