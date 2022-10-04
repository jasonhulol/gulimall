package com.atguigu.common.constant;

import lombok.Data;

/**
 * @Author: jiegege
 * @Description:
 * @Date: 2022/9/29 5:54 下午
 * @Version 1.0
 */
public class ProductConstant {
    public enum SpuStatusEnum{
        SPU_DOWN(0, "新建"),
        SPU_UP(1,"商品上架"),
        NEW_SPU(2,"商品下架");

        private int code;
        private String msg;
        SpuStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

    public enum AttrEnum{
        ATTR_TYPE_BASE(1,"基本属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private int code;
        private String msg;
        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
