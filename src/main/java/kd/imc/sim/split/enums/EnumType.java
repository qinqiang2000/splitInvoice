package kd.imc.sim.split.enums;

public interface EnumType {
    enum InvKindEnum {
        SPECIAL(0, "专票"),
        NORMAL(2, "普票"),
        ROLL(41, "卷票"),
        EINV(51, "电子发票"),
        ESINV(52, "电子专票");

        private Integer value;
        private String desc;

        InvKindEnum(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return this.value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getDesc() {
            return this.desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public static boolean notExisted(int invKind) {
            InvKindEnum[] values = values();

            for (InvKindEnum invKindEnum : values) {
                if(invKindEnum.getValue() == invKind){
                    return false;
                }
            }
            return true;
        }
    }

    enum LinePropertyEnum {
        ZERO(0, "商品行"),
        THREE(3, "被折扣行"),
        FOUR(4, "折扣行");

        private Integer value;
        private String desc;

        LinePropertyEnum(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return this.value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getDesc() {
            return this.desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum NumberTypeEnum {
        ZERO(0),
        ONE(1),
        TWO(2);

        private Integer value;

        NumberTypeEnum(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return this.value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    enum SplitGoodsTypeEnum {
        ONE(1, "调整金额"),
        TWO(2, "单价不变"),
        THREE(3, "数量不变");

        private Integer value;
        private String desc;

        SplitGoodsTypeEnum(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return this.value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        public String getDesc() {
            return this.desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    enum YOrNEnum {
        YES(1),
        NO(0);

        private Integer value;

        YOrNEnum(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return this.value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }
}