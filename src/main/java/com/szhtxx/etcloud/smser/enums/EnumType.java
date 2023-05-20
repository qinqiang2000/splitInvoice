package com.szhtxx.etcloud.smser.enums;

public interface EnumType
{
    public enum InvKindEnum
    {
        SPECIAL("SPECIAL", 0, Integer.valueOf(0), "专票"), 
        NORMAL("NORMAL", 1, Integer.valueOf(2), "普票"), 
        ROLL("ROLL", 2, Integer.valueOf(41), "卷票"), 
        EINV("EINV", 3, Integer.valueOf(51), "电子发票"), 
        ESINV("ESINV", 4, Integer.valueOf(52), "电子专票");
        
        private Integer value;
        private String desc;
        
        private InvKindEnum(final String s, final int n, final Integer value, final String desc) {
            this.value = value;
            this.desc = desc;
        }
        
        public Integer getValue() {
            return this.value;
        }
        
        public void setValue(final Integer value) {
            this.value = value;
        }
        
        public String getDesc() {
            return this.desc;
        }
        
        public void setDesc(final String desc) {
            this.desc = desc;
        }
        
        public static Boolean notExisted(final int invKind) {
            InvKindEnum[] values;
            for (int length = (values = values()).length, i = 0; i < length; ++i) {
                final InvKindEnum invKindEnum = values[i];
                if (invKindEnum.getValue() == invKind) {
                    return false;
                }
            }
            return true;
        }
    }
    
    public enum LinePropertyEnum
    {
        ZERO("ZERO", 0, Integer.valueOf(0), "商品行"), 
        THREE("THREE", 1, Integer.valueOf(3), "被折扣行"), 
        FOUR("FOUR", 2, Integer.valueOf(4), "折扣行");
        
        private Integer value;
        private String desc;
        
        private LinePropertyEnum(final String s, final int n, final Integer value, final String desc) {
            this.value = value;
            this.desc = desc;
        }
        
        public Integer getValue() {
            return this.value;
        }
        
        public void setValue(final Integer value) {
            this.value = value;
        }
        
        public String getDesc() {
            return this.desc;
        }
        
        public void setDesc(final String desc) {
            this.desc = desc;
        }
    }
    
    public enum NumberTypeEnum
    {
        ZERO("ZERO", 0, Integer.valueOf(0)), 
        ONE("ONE", 1, Integer.valueOf(1)), 
        TWO("TWO", 2, Integer.valueOf(2));
        
        private Integer value;
        
        private NumberTypeEnum(final String s, final int n, final Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return this.value;
        }
        
        public void setValue(final Integer value) {
            this.value = value;
        }
    }
    
    public enum SplitGoodsTypeEnum
    {
        ONE("ONE", 0, Integer.valueOf(1), "调整金额"), 
        TWO("TWO", 1, Integer.valueOf(2), "单价不变"), 
        THREE("THREE", 2, Integer.valueOf(3), "数量不变");
        
        private Integer value;
        private String desc;
        
        private SplitGoodsTypeEnum(final String s, final int n, final Integer value, final String desc) {
            this.value = value;
            this.desc = desc;
        }
        
        public Integer getValue() {
            return this.value;
        }
        
        public void setValue(final Integer value) {
            this.value = value;
        }
        
        public String getDesc() {
            return this.desc;
        }
        
        public void setDesc(final String desc) {
            this.desc = desc;
        }
    }
    
    public enum YOrNEnum
    {
        YES("YES", 0, Integer.valueOf(1)), 
        NO("NO", 1, Integer.valueOf(0));
        
        private Integer value;
        
        private YOrNEnum(final String s, final int n, final Integer value) {
            this.value = value;
        }
        
        public Integer getValue() {
            return this.value;
        }
        
        public void setValue(final Integer value) {
            this.value = value;
        }
    }
}
