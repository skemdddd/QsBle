package com.zqs.ble;

import com.zqs.ble.core.utils.fun.VoidFunction;

import androidx.annotation.Nullable;

/*
 *   @author zhangqisheng
 *   @date 2022-08-01
 *   @description
 */
public abstract class BaseChain<T> {

    protected VoidFunction<T> successCallback;
    protected VoidFunction<Exception> failCallback;
    private ChainMessage parentMessage;
    private long delay = 0;
    private long timeout = 0;
    private boolean isCallback = false;
    private int retry = 0;
    //执行失败是否丢弃
    private boolean dump = true;
    //是否异步
    private boolean isAsync = false;

    private ChainHandleStatusCallback<T> chainHandleStatusCallback;

    public void setChainHandleStatusCallback(ChainHandleStatusCallback<T> chainHandleStatusCallback) {
        this.chainHandleStatusCallback = chainHandleStatusCallback;
    }

    public boolean isAsync() {
        return isAsync;
    }

    public void setAsync(boolean async) {
        isAsync = async;
    }

    public boolean isDump() {
        return dump;
    }

    public void setDump(boolean dump) {
        this.dump = dump;
    }

    public boolean isCallback() {
        return isCallback;
    }

    public void setCallback(boolean callback) {
        this.isCallback = callback;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public void letRetryLess(){
        --retry;
    }

    public long getDelay() {
        return delay;
    }

    public void setDelay(long delay) {
        this.delay = delay;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public void setParentMessage(ChainMessage parentMessage) {
        this.parentMessage = parentMessage;
    }

    public VoidFunction<T> getSuccessCallback() {
        return successCallback;
    }

    public void setSuccessCallback(VoidFunction<T> successCallback) {
        this.successCallback = successCallback;
    }

    public VoidFunction<Exception> getFailCallback() {
        return failCallback;
    }

    public void setFailCallback(VoidFunction<Exception> failCallback) {
        this.failCallback = failCallback;
    }

    protected QsBle getBle(){
        return QsBle.getInstance();
    }

    public void onSuccess(T data) {
        if (isCallback)return;
        isCallback = true;
        if (successCallback!=null){
            successCallback.apply(data);
        }
        onDestroy();
        parentMessage.onChainHandleSuccess(this,data);
        parentMessage = null;
    }

    public void onFail(Exception e) {
        if (isCallback)return;
        isCallback = true;
        if (failCallback!=null){
            failCallback.apply(e);
        }
        onDestroy();
        parentMessage.onChainHandleFail(this, e);
        parentMessage = null;
    }

    public void onCreate(){

    }

    public void onReport(Boolean isSuccess,Boolean isDump,@Nullable T data, @Nullable Exception e){
        if (chainHandleStatusCallback!=null){
            chainHandleStatusCallback.onReport(isSuccess,isDump,data,e);
        }
    }

    public final void onHandle(){
        if (isAsync){
            onSuccess(null);
        }
        handle();
    }

    public abstract void handle();

    public abstract void onDestroy();

    @FunctionalInterface
    public interface ChainHandleStatusCallback<T>{
        void onReport(Boolean isSuccess,Boolean isDump,@Nullable T data, @Nullable Exception e);
    }

    public boolean isAlreadHandleTimeoutOption(){
        return false;
    }

}
