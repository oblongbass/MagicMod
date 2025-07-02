package com.magic.scale;

/**
 * 缩放数据类
 * 存储实体的缩放信息
 */
public class ScaleData {
    private float scale = 1.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    
    public ScaleData() {
        this(1.0f);
    }
    
    public ScaleData(float scale) {
        this.scale = scale;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 获取缩放值
     * @return 缩放值
     */
    public float getScale() {
        return scale;
    }
    
    /**
     * 设置缩放值
     * @param scale 缩放值
     */
    public void setScale(float scale) {
        this.scale = scale;
        this.lastUpdateTime = System.currentTimeMillis();
    }
    
    /**
     * 获取最后更新时间
     * @return 时间戳
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    /**
     * 检查是否有效（非默认值）
     * @return 如果缩放值不是1.0f则返回true
     */
    public boolean isValid() {
        return scale != 1.0f;
    }
    
    @Override
    public String toString() {
        return "ScaleData{scale=" + scale + ", lastUpdate=" + lastUpdateTime + "}";
    }
} 